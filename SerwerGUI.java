import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;

public class SerwerGUI extends JFrame {

    private JButton btnRunSerwer, btnTasks;
    private JTextField port;
    private JTextArea logs;

    private int portNumber = 2345;
    private boolean isAllowAppendix = Config.IS_ALLOW_APPENDIX;
    private boolean isRunning = false;
    private boolean inProgress = false;

    private ArrayList<Connection> clients = new ArrayList<Connection>(Config.MAX_PLAYERS);

    private LinkedList<String> sendedTasks = new LinkedList<String>();
    private AtomicInteger tasksCount = new AtomicInteger(Config.MAX_TEXTS_QUEUE);

    private ArrayList<Player> leaderboard = new ArrayList<>(Config.MAX_PLAYERS);
    private int place = 0;
    private int playingPlayers = 0;

    public SerwerGUI() {

        super("Serwer " + Config.VERSION);
        setSize(450, 320);
        setMinimumSize(new Dimension(450, 320));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        Obsluga obsluga = new Obsluga();

        // -- panel górny
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logs = new JTextArea();
        logs.setForeground(Color.BLUE);
        logs.setLineWrap(true);
        logs.setEditable(false);

        port = new JTextField((new Integer(portNumber)).toString(), 8);
        btnRunSerwer = new JButton("Uruchom");
        btnRunSerwer.setPreferredSize(new Dimension(120, 30));
        btnRunSerwer.addActionListener(obsluga);

        panel.add(new JLabel("Port: "));
        panel.add(port);
        panel.add(btnRunSerwer);

        // -- panel dolny
        btnTasks = new JButton("Pokaz odebrane zadania (0)");
        btnTasks.setPreferredSize(new Dimension(450, 28));
        btnTasks.setEnabled(false);
        btnTasks.addActionListener(obsluga);

        // -- rozmieszczenie paneli
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(logs), BorderLayout.CENTER);
        add(btnTasks, BorderLayout.SOUTH);

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
                    inProgress = false;
                    btnRunSerwer.setText("Uruchom");
                    port.setEnabled(true);
                }
            } else if (e.getSource() == btnTasks) {

                if (sendedTasks.peek() != null) {
                    String text;
                    synchronized (sendedTasks) {
                        text = sendedTasks.poll();
                    }

                    if (tasksCount.incrementAndGet() >= Config.MAX_TEXTS_QUEUE)
                        btnTasks.setEnabled(false);

                    btnTasks.setText("Pokaz odebrane zadania (" + sendedTasks.size() + ")");
                    showPopupWithText(text);
                }
            }
        }
    }

    private void showPopupWithText(String text) {

        JTextArea ta = new JTextArea(text);
        ta.setLineWrap(true);
        ta.setPreferredSize(new Dimension(600, 300));
        JTextField tf = new JTextField();

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("Tresc zadania:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);
        panel2.add(new JLabel("Nazwa pliku [ ].txt"));
        panel2.add(tf);

        panel.add(panel2, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(null, panel, "Prosba o dodanie tekstu do aplikacji",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            ObslugaPlikow.stringToFile(tf.getText() + ".txt", ta.getText());
            // todo obsługa błędów i sprawdzić poprawność nazwy pliku
        }
    }

    private class Serwer extends Thread {

        private ServerSocket serwer;

        public void terminate() {
            try {
                for (Connection client : clients) {
                    if (client != null) {
                        try {
                            client.sendToClient.writeObject(
                                    new Packet(Command.LOGOUT, client.player.getId(), "Serwer zostal wylaczony."));
                            client.sendToClient.flush();
                            client.receiveFromClient.close();
                            client.sendToClient.close();
                            client.socket.close();
                        } catch (IOException e) {
                        }
                    }
                }
                serwer.close();//
                addLog("Wszystkie polaczenie zostaly zakonczone.");
            } catch (IOException e) {
            }
        }

        public void run() {
            try {
                serwer = new ServerSocket(new Integer(port.getText()));
                addLog("Serwer uruchomiony na porcie: " + port.getText());
                addLog("Maksymalna pojemnosc serwera to " + clients.size() + " miejsc.");

                while (isRunning) {
                    Socket socket = serwer.accept();
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
            addLog("Serwer zostal zatrzymany.");
        }
    }

    protected class Connection extends Thread {

        private Socket socket;
        private ObjectInputStream receiveFromClient;
        private ObjectOutputStream sendToClient;
        private Player player;
        private boolean isConnected = true;

        public Connection(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                sendToClient = new ObjectOutputStream(socket.getOutputStream()); // hint: http://stackoverflow.com/a/14111047
                receiveFromClient = new ObjectInputStream(socket.getInputStream());
                sendToClient.flush();

                Packet packet = null;
                while (isConnected && isRunning) {

                    try {
                        packet = (Packet) receiveFromClient.readObject();
                        if (packet != null) {

                            Command command = packet.getCommand();
                            switch (command) {

                            case LOGIN_REQUEST:
                                if (!inProgress) {
                                    synchronized (clients) {
                                        boolean isFreeSlots = false;

                                        for (int i = 0, k = clients.size(); i < k; i++) {
                                            if (clients.get(i) == null) {
                                                clients.set(i, this);
                                                player = new Player();
                                                player.setId(i);
                                                isFreeSlots = true;
                                                break;
                                            }
                                        }

                                        addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                                + " probuje sie polaczyc.");

                                        if (isFreeSlots) {
                                            sendToClient.writeObject(
                                                    new Packet(Command.LOGIN_RESPONSE, player.getId(), ""));
                                            sendToClient.flush();
                                            addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                                    + " zostal polaczony (SLOT " + player.getId() + ").");
                                        } else {
                                            sendToClient.writeObject(
                                                    new Packet(Command.LOGOUT, "Niestety nie ma wolnych miejsc :<"));
                                            sendToClient.flush();
                                            addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                                    + " zostal rozlaczony, z powodu braku wolnego miejsca.");
                                        }
                                    }
                                } else {
                                    sendToClient.writeObject(
                                            new Packet(Command.LOGOUT, "Niestety rozgrywka juz sie rozpoczela :<"));
                                    sendToClient.flush();
                                    addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                            + " zostal rozlaczony, poniewaz rozgrywka juz sie rozpoczela.");
                                }
                                break;

                            case LOGOUT:
                                for (Connection client : clients) {
                                    if (client != null && client != this)
                                        client.sendToClient
                                                .writeObject(new Packet(Command.LOGOUT_PLAYER_NOTIFY, player.getId()));
                                }

                                addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                        + " zostal rozlaczony (SLOT " + player.getId() + ").");

                                sendToClient.writeObject(new Packet(Command.LOGOUT, player.getId(), ""));
                                sendToClient.flush();

                                synchronized (clients) {
                                    clients.set(player.getId(), null);
                                }
                                this.isConnected = false;
                                break;

                            case NICK_SET:
                                player.setNick(packet.getParameter());

                                ArrayList<Player> players = new ArrayList<Player>(); // need improve
                                for (Connection client : clients) {
                                    if (client != null) {
                                        players.add(client.player);
                                    }
                                }
                                for (Connection client : clients) {
                                    if (client != null) {
                                        client.sendToClient.writeObject(new PacketWithPlayersList(players));
                                        client.sendToClient.flush();
                                    }
                                }
                                break;

                            case CHANGE_READY:
                                boolean isReady = player.toggleAndGetReady();
                                boolean isReadyAll = true;
                                for (Connection client : clients) {
                                    if (client != null) {
                                        if (isReadyAll)
                                            if (!client.player.isReady)
                                                isReadyAll = false;
                                        client.sendToClient
                                                .writeObject(new Packet(Command.CHANGE_READY, player.getId(), isReady));
                                        client.sendToClient.flush();
                                    }
                                }

                                if (isReadyAll) {
                                    inProgress = true;
                                    Zadanie zadanie = randomizeTask();
                                    if (zadanie != null) {
                                        synchronized (clients) {
                                            playingPlayers = 0;
                                            for (Connection client : clients) {
                                                if (client != null) {
                                                    playingPlayers++;
                                                    client.sendToClient.writeObject(new PacketWithTask(zadanie));
                                                    client.sendToClient.flush();
                                                    client.player.setUnready();
                                                }
                                            }
                                        }
                                    } else {
                                        //todo gdy nie ma żadnych zadań
                                    }
                                }
                                break;

                            case PROGRESS:
                                int senderId = packet.getPlayerId();
                                int progress = packet.getProgress();
                                for (Connection client : clients) {
                                    if (client != null) {
                                        client.sendToClient
                                                .writeObject(new Packet(Command.PROGRESS, senderId, progress));
                                        client.sendToClient.flush();
                                    }
                                }
                                break;

                            case WIN:
                                int winnerId = packet.getPlayerId();
                                synchronized (leaderboard) {
                                    leaderboard.add(player);
                                }
                                synchronized (clients) {
                                    place++;
                                    for (Connection client : clients) {
                                        if (client != null) {
                                            client.sendToClient.writeObject(new Packet(Command.WIN, winnerId, place));
                                            client.sendToClient.flush();
                                        }
                                    }
                                }

                                if (leaderboard.size() == playingPlayers) {
                                    place = 0;
                                    inProgress = false;
                                    for (Connection client : clients) {
                                        if (client != null) {
                                            client.sendToClient
                                                    .writeObject(new PacketWithPlayersList(Command.RESET, leaderboard));
                                            client.sendToClient.flush();
                                        }
                                    }
                                    leaderboard.clear();
                                }
                                break;

                            case SEND_TEXT_REQUEST:
                                if (isAllowAppendix) {
                                    synchronized (tasksCount) {
                                        if (tasksCount.get() > 0) {
                                            tasksCount.decrementAndGet();
                                            sendToClient.writeObject(new Packet(Command.SEND_TEXT_RESPONSE, -1, true));
                                        } else sendToClient.writeObject(new Packet(Command.SEND_TEXT_RESPONSE, -1, false));
                                    }
                                } else sendToClient.writeObject(new Packet(Command.SEND_TEXT_RESPONSE, -1, false));
                                sendToClient.flush();
                                break;

                            case SEND_TEXT:
                                String text = packet.getParameter().trim();
                                if (!text.isEmpty()) {
                                    synchronized (sendedTasks) {
                                        sendedTasks.offer(text);
                                    }
                                    addLog("Klient przeslal prosbe z tekstem.");
                                    btnTasks.setText("Pokaz odebrane zadania (" + sendedTasks.size() + ")");
                                    btnTasks.setEnabled(true);
                                } else
                                    tasksCount.incrementAndGet();
                                break;
                            }
                        }
                    } catch (ClassNotFoundException ex) {
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

    private Zadanie randomizeTask() {
        ArrayList<File> files = ObslugaPlikow.getFiles();
        if (!files.isEmpty()) {
            int filesCount = files.size();
            int randomIndex = new Random().nextInt(filesCount);
            File file = files.get(randomIndex);

            addLog("Serwer wylosowal: [" + randomIndex + "] " + file.getName() + "");

            try {
                String taskContent = new Scanner(file, "UTF-8").useDelimiter("\\A").next();
                return new Zadanie(taskContent);
            } catch (IOException e) {
                addLog("Blad odczytu pliku.");
                System.exit(2);
            }
        } else
            addLog("Nie ma plikow z tekstami");

        return null;
    }

    private void addLog(String content) {
        logs.append(content + "\n");
        logs.setCaretPosition(logs.getDocument().getLength());
    }

    public static void main(String[] args) {
        new SerwerGUI();
    }
}