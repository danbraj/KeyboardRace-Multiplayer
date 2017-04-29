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
                            client.executeCommand(Command.LOGOUT_COMMAND, "Serwer zostal wylaczony.");
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

    private class Connection extends Thread {

        private Socket socket;
        private BufferedReader receiveFromClient;
        private PrintWriter sendToClient;
        private String nick;
        private int connectionId = -1;
        private boolean isConnected = false;

        public Connection(Socket socket) {
            this.isConnected = true;
            this.socket = socket;
        }

        private void executeCommand(Command command) {
            executeCommand(command, "");
        }

        private void executeCommand(Command command, String parameter) {
            sendToClient.println(command + Config.DELIMITER + parameter);
        }

        public void run() {
            try {
                receiveFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                sendToClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                addLog("Na serwerze jest " + clients.size() + " wszystkich miejsc.\n");

                String command = null;
                while (isConnected && isRunning) {

                    command = receiveFromClient.readLine();
                    if (command != null) {

                        String[] commandParameters = command.split(Config.DELIMITER, 2);
                        //System.out.println("FROM CLIENT(" + this.connectionId + ")\n[1]: " + commandParameters[0] + "\n[2]: "+ commandParameters[1] + "\n");
                        Command protokol = Command.valueOf(commandParameters[0]);
                        switch (protokol) {

                        case LOGOUT_COMMAND:
                            executeCommand(Command.LOGOUT_COMMAND);
                            addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                    + " zostal rozlaczony (SLOT " + this.connectionId + ").\n");

                            synchronized (clients) {
                                //clients.remove(this);
                                clients.set(this.connectionId, null);
                            }
                            this.isConnected = false;
                            break;

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
                                    executeCommand(Command.LOGIN_COMMAND);
                                    addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                            + " zostal polaczony (SLOT " + this.connectionId + ").\n");

                                    // for (Connection client : clients) {
                                    //     client.executeCommand(Command.PLAYERS_LIST_COMMAND); //?
                                    // }
                                } else {
                                    executeCommand(Command.LOGOUT_COMMAND, "Niestey nie ma wolnych miejsc :<");
                                    addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                            + " zostal rozlaczony, z powodu braku wolnego miejsca.\n");
                                }
                            }
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