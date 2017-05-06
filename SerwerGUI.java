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
    private boolean isAllowAppendix = Consts.IS_ALLOW_APPENDIX;
    private int status = 0;

    private ArrayList<Connection> clients = new ArrayList<Connection>(Consts.MAX_PLAYERS);

    private LinkedList<String> sendedTasks = new LinkedList<String>();
    private AtomicInteger tasksCount = new AtomicInteger(Consts.MAX_TEXTS_QUEUE);

    private ArrayList<Player> leaderboard = new ArrayList<>(Consts.MAX_PLAYERS);
    private int place = 0;
    private int playersCount = 0;

    public SerwerGUI() {

        super("Serwer " + Consts.VERSION);
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

        for (int i = 0; i < Consts.MAX_PLAYERS; i++)
            clients.add(null);

        setVisible(true);
    }

    private class Obsluga implements ActionListener {

        private Serwer serwer;

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnRunSerwer) {
                status ^= Consts.RUNNING;
                if ((status & Consts.RUNNING) == Consts.RUNNING) {

                    for (int i = 0; i < Consts.MAX_PLAYERS; i++)
                        clients.set(i, null);

                    serwer = new Serwer();
                    serwer.start();
                    btnRunSerwer.setText("Zatrzymaj");
                    port.setEnabled(false);
                } else {
                    serwer.terminate();
                    status = status & ~Consts.STARTED;
                    btnRunSerwer.setText("Uruchom");
                    port.setEnabled(true);
                }
            } else if (e.getSource() == btnTasks) {

                if (sendedTasks.peek() != null) {
                    String text;
                    synchronized (sendedTasks) {
                        text = sendedTasks.poll();
                    }

                    if (tasksCount.incrementAndGet() >= Consts.MAX_TEXTS_QUEUE)
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
                            client.sendToClient.writeObject(new Packet(Command.LOGOUT, client.player.getPlayerId(),
                                    "Serwer zostal wylaczony."));
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
                addLog("Serwer uruchomiony na porcie: " + serwer.getLocalPort());
                addLog("Maksymalna pojemnosc serwera to " + clients.size() + " miejsc.");

                while ((status & Consts.RUNNING) == Consts.RUNNING) {
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
                while (((status & Consts.RUNNING) == Consts.RUNNING) && isConnected) {

                    try {
                        packet = (Packet) receiveFromClient.readObject();
                        if (packet != null) {

                            Command command = packet.getCommand();
                            if (command == Command.LOGIN_REQUEST) {

                                String connectionIp = socket.getInetAddress().getHostAddress();

                                if ((status & Consts.STARTED) != Consts.STARTED) {

                                    boolean isFreeSlots = false;

                                    addLog("Uzytkownik " + connectionIp + " probuje sie polaczyc.");

                                    // stwórz gracza i dodaj do listy, jeżeli jest wolne miejsce
                                    synchronized (clients) {
                                        for (int index = 0, k = clients.size(); index < k; index++) {
                                            if (clients.get(index) == null) {
                                                clients.set(index, this);
                                                player = new Player(index);
                                                isFreeSlots = true;

                                                sendToClient.writeObject(
                                                        new Packet(Command.LOGIN_RESPONSE, player.getPlayerId()));
                                                addLog("Uzytkownik " + connectionIp + " zostal polaczony (SLOT "
                                                        + player.getPlayerId() + ").");
                                                break;
                                            }
                                        }
                                    }

                                    if (!isFreeSlots) {
                                        sendToClient.writeObject(
                                                new Packet(Command.LOGOUT, "Niestety nie ma wolnych miejsc :<"));
                                        addLog("Uzytkownik " + connectionIp
                                                + " zostal rozlaczony, z powodu braku wolnego miejsca.");
                                    }
                                } else {
                                    sendToClient.writeObject(
                                            new Packet(Command.LOGOUT, "Niestety rozgrywka juz sie rozpoczela :<"));
                                    addLog("Uzytkownik " + connectionIp
                                            + " zostal rozlaczony, poniewaz rozgrywka juz sie rozpoczela.");
                                }

                            } else if (command == Command.LOGOUT) {

                                // poinformowanie pozostałych użytkowników o wylogowującym się użytkowników
                                for (Connection client : clients) {
                                    if (client != null && client != this)
                                        client.sendToClient.writeObject(new Packet(Command.LOGOUT_PLAYER_NOTIFY,
                                                player.getPlayerId(), (status & Consts.STARTED) == Consts.STARTED));
                                }
                                // usunięcie użytkownika z listy użytkowników
                                sendToClient.writeObject(new Packet(Command.LOGOUT, player.getPlayerId()));//
                                addLog("Uzytkownik " + socket.getInetAddress().getHostAddress()
                                        + " zostal rozlaczony (SLOT " + player.getPlayerId() + ").");

                                this.isConnected = false;
                                synchronized (clients) {
                                    clients.set(player.getPlayerId(), null);
                                }

                                // zatrzymanie rozgrywki, jeżeli ktoś wyszedł w trakcie gry
                                if ((status & Consts.STARTED) == Consts.STARTED)
                                    status = status & ~Consts.STARTED;

                            } else if (command == Command.NICK_SET) {

                                player.setNick(packet.getString());

                                synchronized (clients) {
                                    ArrayList<Player> players = new ArrayList<Player>();
                                    for (Connection client : clients) {
                                        if (client != null) {
                                            players.add(client.player);
                                        }
                                    }

                                    // aktualizacja użytkownków dla nowego użytkownika 
                                    // poinformowanie innych użytkowników o nowym użytkowniku
                                    for (Connection client : clients) {
                                        if (client != null)
                                            client.sendToClient.writeObject(
                                                    new ExtendedPacket(Command.UPDATE_PLAYERS_LIST, players));
                                    }
                                }

                            } else if (command == Command.CHANGE_READY) {

                                // zmiana gotowości użytkownika i sprawdzenie czy wszyscy pozostali są gotowi
                                boolean isReady = player.toggleAndGetReady();
                                boolean isReadyAll = true;
                                for (Connection client : clients) {
                                    if (client != null) {
                                        if (isReadyAll)
                                            if (!client.player.isReady)
                                                isReadyAll = false;
                                        client.sendToClient.writeObject(
                                                new Packet(Command.CHANGE_READY, player.getPlayerId(), isReady));
                                    }
                                }

                                // jeżeli wszyscy użytkownicy byli gotowi to startuje gra
                                if (isReadyAll) {
                                    status = status | Consts.STARTED;

                                    synchronized (leaderboard) {
                                        leaderboard.clear();
                                    }

                                    // wylosowanie zadania oraz jego przydzielenie do użytkowników i tym samym start rozgrywki 
                                    Zadanie zadanie = randomizeTask();
                                    if (zadanie != null) {
                                        synchronized (clients) {
                                            playersCount = 0;
                                            for (Connection client : clients) {
                                                if (client != null) {
                                                    playersCount++;
                                                    client.sendToClient.writeObject(
                                                            new ExtendedPacket(Command.START_GAME, zadanie));
                                                    client.player.setUnready();
                                                }
                                            }
                                        }
                                    } else {
                                        //todo gdy nie ma żadnych zadań
                                    }
                                }

                            } else if (command == Command.PROGRESS) {

                                // poinformowanie użytkowników o zmieniającym się progresie
                                int senderId = packet.getPlayerId();
                                int progress = packet.getInt();
                                for (Connection client : clients) {
                                    if (client != null)
                                        client.sendToClient
                                                .writeObject(new Packet(Command.PROGRESS, senderId, progress));
                                }

                            } else if (command == Command.WIN) {

                                int winnerId = packet.getPlayerId();

                                synchronized (clients) {
                                    place++;
                                    for (Connection client : clients) {
                                        if (client != null)
                                            client.sendToClient.writeObject(new Packet(Command.WIN, winnerId, place));
                                    }
                                }

                                // dodanie użytkownika, który skończył zadanie do listy
                                synchronized (leaderboard) {
                                    leaderboard.add(player);
                                }

                                // jeżeli wszyscy ukończyli zadanie, następuje ogłoszenie wyników
                                if (leaderboard.size() == playersCount) {
                                    place = 0;
                                    status = status & ~Consts.STARTED;
                                    synchronized (clients) {
                                        for (Connection client : clients) {
                                            if (client != null)
                                                client.sendToClient
                                                        .writeObject(new ExtendedPacket(Command.RESET, leaderboard));
                                        }
                                    }
                                    leaderboard.clear();
                                }

                            } else if (command == Command.SEND_TEXT_REQUEST) {

                                // jeżeli ustawiono w konfiguracji możliwość wysyłania tekstów to..
                                if (isAllowAppendix) {
                                    synchronized (tasksCount) {
                                        // jeżeli nie został osiągnięty limit tekstów w poczekalni to..
                                        if (tasksCount.get() > 0) {
                                            tasksCount.decrementAndGet();
                                            sendToClient.writeObject(new Packet(Command.SEND_TEXT_RESPONSE, true));
                                        } else
                                            sendToClient.writeObject(new Packet(Command.SEND_TEXT_RESPONSE, false));
                                    }
                                } else
                                    sendToClient.writeObject(new Packet(Command.SEND_TEXT_RESPONSE, false));

                            } else if (command == Command.SEND_TEXT) {

                                // dodanie wysłanego od klienta tekstu do listy
                                String text = packet.getString().trim();
                                if (!text.isEmpty()) {
                                    synchronized (sendedTasks) {
                                        sendedTasks.offer(text);
                                    }
                                    addLog("Klient przeslal prosbe z tekstem.");
                                    btnTasks.setText("Pokaz odebrane zadania (" + sendedTasks.size() + ")");
                                    btnTasks.setEnabled(true);
                                } else
                                    tasksCount.incrementAndGet();
                            }
                            sendToClient.flush();
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