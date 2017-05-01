import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;

public class SerwerGUI extends JFrame {

    private JButton btnRunSerwer;
    private JTextField port, cmdLine;
    private JTextArea logs;

    private int portNumber = 2345;
    private boolean isRunning = false;
    private boolean inProgress = false;

    private ArrayList<Connection> clients = new ArrayList<Connection>(Config.MAX_PLAYERS);

    public SerwerGUI() {

        super("Serwer " + Config.VERSION);
        setSize(450, 320);
        setMinimumSize(new Dimension(450, 320));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // -- panel górny
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logs = new JTextArea();
        logs.setForeground(Color.BLUE);
        logs.setLineWrap(true);
        logs.setEditable(false);

        port = new JTextField((new Integer(portNumber)).toString(), 8);
        btnRunSerwer = new JButton("Uruchom");
        btnRunSerwer.setPreferredSize(new Dimension(120, 30));
        btnRunSerwer.addActionListener(new Obsluga()); //

        panel.add(new JLabel("Port: "));
        panel.add(port);
        panel.add(btnRunSerwer);

        // -- panel dolny
        cmdLine = new JTextField();
        cmdLine.setFont(new Font("Verdana", Font.PLAIN, 16));
        cmdLine.setPreferredSize(new Dimension(450, 28));

        // -- rozmieszczenie paneli
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(logs), BorderLayout.CENTER);
        add(cmdLine, BorderLayout.SOUTH);

        for (int i = 0; i < Config.MAX_PLAYERS; i++)
            clients.add(null);

        setVisible(true);
    }

    private class Obsluga implements ActionListener {

        private Serwer serwer;

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnRunSerwer) {
                isRunning = !isRunning;
                if (isRunning) {

                    for (int i = 0; i < Config.MAX_PLAYERS; i++)
                        clients.set(i, null);

                    serwer = new Serwer();
                    serwer.start();
                    btnRunSerwer.setText("Zatrzymaj");
                    port.setEnabled(false);
                } else {
                    serwer.terminate();
                    btnRunSerwer.setText("Uruchom");
                    port.setEnabled(true);
                }
            }
        }
    }

    private class Serwer extends Thread {

        private ServerSocket serwer;

        public void terminate() {
            try {
                serwer.close();
                for (Connection client : clients) {
                    if (client != null) {
                        try {
                            client.sendToClient.writeObject(new Packet(Command.LOGOUT_COMMAND, "Serwer zostal wylaczony."));
                            client.sendToClient.flush();
                            client.socket.close();
                        } catch (IOException e) {
                        }
                    }
                }
                addLog("Wszystkie polaczenie zostaly zakonczone\n");
            } catch (IOException e) {
            }
        }

        public void run() {
            try {
                serwer = new ServerSocket(new Integer(port.getText()));
                addLog("Serwer uruchomiony na porcie: " + port.getText() + "\n");

                while (isRunning) {
                    Socket socket = serwer.accept();
                    addLog("Nowe polaczenie.\n");
                    new Connection(socket).start();
                }
            } catch (SocketException e) {
            } catch (Exception e) {
                addLog(e.toString());
            } finally {
                try {
                    if (serwer != null) {
                        serwer.close();
                    }
                } catch (IOException e) {
                    addLog(e.toString());
                }
            }
            addLog("Serwer zostal zatrzymany.\n");
        }
    }

    protected class Connection extends Thread {

        private Socket socket;
        private ObjectInputStream receiveFromClient;
        private ObjectOutputStream sendToClient;
        private String nick;
        private int connectionId = -1;
        private boolean isConnected = false;

        public Connection(Socket socket) {
            this.isConnected = true;
            this.socket = socket;
        }

        public void run() {
            try {
                sendToClient = new ObjectOutputStream(socket.getOutputStream()); // hint: http://stackoverflow.com/a/14111047
                receiveFromClient = new ObjectInputStream(socket.getInputStream());
                sendToClient.flush();

                addLog("Na serwerze jest " + clients.size() + " wszystkich miejsc.\n");

                Packet packet = null;
                while (isConnected && isRunning) {

                    try {
                        packet = (Packet)receiveFromClient.readObject();
                    } catch (ClassNotFoundException ex) {}

                    if (packet != null) {
                        Command command = packet.getCommand();
                        switch (command) {
                            case LOGIN_COMMAND:
                                synchronized (clients) {
                                    boolean isFreeSlots = false;

                                    for (int i = 0, k = clients.size(); i < k; i++) {
                                        if (clients.get(i) == null) {
                                            clients.set(i, this);
                                            this.connectionId = i;
                                            isFreeSlots = true;
                                            break;
                                        }
                                    }

                                    addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                            + " probuje sie polaczyc.\n");

                                    if (isFreeSlots) {
                                        sendToClient.writeObject(new Packet(Command.LOGIN_COMMAND));
                                        sendToClient.flush();
                                        addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                                + " zostal polaczony (SLOT " + this.connectionId + ").\n");

                                        // for (Connection client : clients) {
                                        //     client.executeCommand(Command.PLAYERS_LIST_COMMAND);
                                        // }
                                    } else {
                                        sendToClient.writeObject(new Packet(Command.LOGOUT_COMMAND, "Niestey nie ma wolnych miejsc :<"));
                                        sendToClient.flush();
                                        addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                                + " zostal rozlaczony, z powodu braku wolnego miejsca.\n");
                                    }
                                }
                                break;
                            case LOGOUT_COMMAND:
                                sendToClient.writeObject(new Packet(Command.LOGOUT_COMMAND));
                                addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                        + " zostal rozlaczony (SLOT " + this.connectionId + ").\n");

                                synchronized (clients) {
                                    clients.set(this.connectionId, null);
                                }
                                this.isConnected = false;
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                try {
                    receiveFromClient.close();
                    sendToClient.close();
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void addLog(String content) {
        logs.append(content);
        logs.setCaretPosition(logs.getDocument().getLength());
    }

    public static void main(String[] args) {
        new SerwerGUI();
    }
}