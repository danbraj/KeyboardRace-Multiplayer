import java.io.*;
import java.net.*;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class KlientGUI extends JFrame {

    private JTextArea text, logs;
    private JPanel panelGraczy, panelGry, panelBoczny, panelLogowania, panelDodatkowy;
    private JTextField host, input;
    private JButton btnHowToPlay, btnAddToSerwer, btnReady, btnLogon;
    private JLabel lbStatus;

    private String hostname = "localhost:2345";
    private boolean isConnected = false;

    private PanelPlayer[] panelGracza = new PanelPlayer[Config.MAX_PLAYERS];

    private Zadanie zadanie;

    private Klient watekKlienta;

    public KlientGUI() {
        super("Klient " + Config.VERSION);
        setSize(880, 600);
        setMinimumSize(new Dimension(640, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // -- panel z graczami
        panelGraczy = new JPanel(new GridLayout(Config.MAX_PLAYERS, 0));
        //panelGraczy.setLayout(new GridLayout(6, 0));
        for (int i = 0; i < Config.MAX_PLAYERS; i++) {
            panelGracza[i] = new PanelPlayer(i);
            panelGracza[i].setPreferredSize(new Dimension(40, 40));
            panelGraczy.add(panelGracza[i]);
        }

        // -- panel gry
        panelGry = new JPanel(new BorderLayout());
        panelGry.setBackground(Color.LIGHT_GRAY);

        text = new JTextArea();
        text.setText("");
        text.setWrapStyleWord(true);
        text.setLineWrap(true);
        text.setOpaque(false);
        text.setEditable(false);
        text.setFocusable(false);
        text.setFont(new Font("Verdana", Font.PLAIN, 22));
        text.setBorder(new EmptyBorder(8, 12, 8, 8));

        input = new JTextField();
        input.setFont(new Font("Verdana", Font.PLAIN, 26));
        input.setPreferredSize(new Dimension(450, 42));
        input.setEnabled(false);

        Obsluga obsluga = new Obsluga();
        input.addKeyListener(obsluga);

        logs = new JTextArea();
        logs.setText("");
        logs.setWrapStyleWord(true);
        logs.setLineWrap(true);
        logs.setOpaque(false);
        logs.setEditable(false);
        logs.setFocusable(false);
        logs.setBorder(new EmptyBorder(4, 4, 4, 4));

        panelGry.add(text, BorderLayout.CENTER);
        panelGry.add(input, BorderLayout.SOUTH);

        // -- panel po prawej stronie
        panelBoczny = new JPanel(new BorderLayout());

        // ---- panel dane do logowania + informacje
        panelLogowania = new JPanel(new GridLayout(2, 2));
        panelLogowania.setBorder(new EmptyBorder(12, 10, 12, 10));

        host = new JTextField(hostname);
        host.setFont(new Font("Verdana", Font.PLAIN, 12));
        lbStatus = new JLabel("Status: niepolaczony", SwingConstants.RIGHT);
        lbStatus.setForeground(Color.RED);

        panelLogowania.add(new JLabel("Serwer (host:port)"));
        panelLogowania.add(new JLabel(Config.VERSION, SwingConstants.RIGHT));
        panelLogowania.add(host);
        panelLogowania.add(lbStatus);

        // ---- panel z przyciskami (prawy dolny róg)
        panelDodatkowy = new JPanel(new GridLayout(2, 2));

        btnHowToPlay = new JButton("Jak grac?");
        btnAddToSerwer = new JButton("Dodaj tekst do gry");
        btnAddToSerwer.addActionListener(obsluga);
        btnAddToSerwer.setEnabled(false);
        btnReady = new JButton("Gotowosc");
        btnReady.addActionListener(obsluga);
        btnReady.setEnabled(false);
        btnLogon = new JButton("Polacz");
        btnLogon.setPreferredSize(new Dimension(42, 42));
        btnLogon.addActionListener(obsluga);

        panelDodatkowy.add(btnLogon);
        panelDodatkowy.add(btnAddToSerwer);
        panelDodatkowy.add(btnReady);
        panelDodatkowy.add(btnHowToPlay);

        // ---- panel (opakowanie) dla panelu z informacjami i przyciskami 
        JPanel opakowanie = new JPanel();
        opakowanie.setLayout(new BoxLayout(opakowanie, BoxLayout.Y_AXIS));

        opakowanie.add(panelLogowania);
        opakowanie.add(panelDodatkowy);

        panelBoczny.add(new JScrollPane(logs), BorderLayout.CENTER);
        panelBoczny.add(opakowanie, BorderLayout.SOUTH);

        // -- rozmieszczenie paneli
        add(panelGraczy, BorderLayout.NORTH);
        add(panelGry, BorderLayout.CENTER);
        add(panelBoczny, BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private void display() {

        JTextArea ta = new JTextArea();
        ta.setLineWrap(true);
        ta.setPreferredSize(new Dimension(300, 100));
        JTextField tf = new JTextField();

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("Tresc:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);
        panel2.add(new JLabel("Tytul (opcjonalnie):"));
        panel2.add(tf);

        panel.add(panel2, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(null, panel, "Dodaj prosbe z tekstem do serwera",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            System.out.println(tf.getText() + " : " + ta.getText());
        } else {
            System.out.println("cancel");
        }
    }

    private class Obsluga extends KeyAdapter implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnAddToSerwer) {
                display();
            } else if (e.getSource() == btnReady) {
                try {
                    watekKlienta.sendToSerwer.writeObject(new Packet(Command.CHANGE_READY));
                    watekKlienta.sendToSerwer.flush();
                } catch (IOException ex) {
                    addLog(ex.toString());
                }
            } else if (e.getSource() == btnLogon) {
                if (!isConnected) {
                    watekKlienta = new Klient();
                    watekKlienta.start();

                    isConnected = true;
                    btnLogon.setText("Rozlacz");
                    lbStatus.setText("Status: polaczony");
                    lbStatus.setForeground(Color.decode("#006600"));
                    btnAddToSerwer.setEnabled(true);
                } else {
                    try {
                        watekKlienta.sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                        watekKlienta.sendToSerwer.flush();
                    } catch (IOException ex) {
                        addLog(ex.toString());
                    }
                }
            }
        }

        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (zadanie.ifEqualsGoNext(input.getText())) {

                    panelGracza[0].progress.setValue(zadanie.getProgress());

                    if (Zadanie.SUCCESS) {
                        System.out.println("Zwyciestwo!");
                        text.setText("Zwyciestwo!");
                    } else {
                        text.replaceRange(null, 0, input.getText().length());
                        input.setText("");
                    }
                }
            }
        }
    }

    private class Klient extends Thread {

        private Socket socket;
        private ObjectInputStream receiveFromSerwer;
        private ObjectOutputStream sendToSerwer;

        public void run() {
            try {
                String[] hostParameters = host.getText().split(":", 2);
                socket = new Socket(hostParameters[0], new Integer(hostParameters[1]));
                sendToSerwer = new ObjectOutputStream(socket.getOutputStream());
                receiveFromSerwer = new ObjectInputStream(socket.getInputStream());

                sendToSerwer.writeObject(new Packet(Command.LOGIN_REQUEST));
                sendToSerwer.flush();

                Packet packet = null;
                while (isConnected) {

                    try {
                        packet = (Packet)receiveFromSerwer.readObject();
                    } catch (ClassNotFoundException ex) {}
                    
                    if (packet != null) {
                        Command command = packet.getCommand();
                        switch (command) {
                            case LOGIN_RESPONSE:
                                String nick = JOptionPane.showInputDialog(null, "Podaj nick (max. 6 znakow): ");
                                nick = nick.trim().toUpperCase();
                                if (nick.equals("")) {
                                    sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                                    sendToSerwer.flush();
                                    addLog("Niepoprawny nick, zostales rozlaczony.");
                                } else {
                                    if (nick.length() > 6)
                                        nick = nick.substring(0, 6);

                                    sendToSerwer.writeObject(new Packet(Command.NICK_SET, nick));
                                    sendToSerwer.flush();
                                    btnReady.setEnabled(true);
                                }
                                break;
                            case LOGOUT:
                            
                                String message = packet.getParameter();
                                if (message != null && !message.isEmpty())
                                    addLog(message);
                                    
                                isConnected = false;
                                btnLogon.setText("Polacz");
                                lbStatus.setText("Status: niepolaczony");
                                lbStatus.setForeground(Color.RED);
                                btnReady.setEnabled(false);
                                btnAddToSerwer.setEnabled(false);
                                for (PanelPlayer pp : panelGracza) {
                                    pp.join("---");
                                }
                                break;
                            case LOGOUT_PLAYER_NOTIFY:

                                String playerName = packet.getParameter();
                                int playerId = packet.getPlayerId();

                                if (playerId != -1)
                                    panelGracza[playerId].join(playerName);

                                break;
                            case UPDATE_PLAYERS_LIST:

                                PacketWithPlayersList extendedPacket = (PacketWithPlayersList)packet;
                                for (Player player : extendedPacket.getPlayers()) {
                                    panelGracza[player.getId()].join(player.getNick());
                                }
                                break;
                            
                            case CHANGE_READY:

                                int senderId = packet.getPlayerId();
                                boolean isReady = packet.getExtra();

                                panelGracza[senderId].setReadiness(isReady);
                                break;
                        }
                    }
                }
            } catch (UnknownHostException e) {
                addLog("Blad polaczenia!");
            } catch (IOException e) {
                addLog(e.toString());
            } catch (NullPointerException e) { //
                addLog(e.toString());
            } finally {
                try {
                    receiveFromSerwer.close();
                    sendToSerwer.close();
                    socket.close();
                } catch (IOException e) {
                } catch (NullPointerException e) { // 
                }
            }
        }
    }

    public void startGame() {

        ArrayList<File> files = ObslugaPlikow.getFiles();
        if (!files.isEmpty()) {
            // for (File file : files)
            //     System.out.println(file.getName());
            int filesCount = files.size();
            int randomIndex = new Random().nextInt(filesCount);
            File file = files.get(randomIndex);

            System.out.println("Zostal wylosowany " + randomIndex + " : " + file.getName());

            try {
                String taskContent = new Scanner(file, "UTF-8").useDelimiter("\\A").next();
                zadanie = new Zadanie(taskContent);
            } catch (IOException e) {
                System.out.println("Blad odczytu pliku.");
                System.exit(2);
            }

            text.setText(zadanie.getText());
            input.setEnabled(true);
            input.requestFocus();
        } else
            System.out.println("Nie ma plikow z tekstami");
    }

    private void addLog(String content) {
        logs.append(content + "\n");
        logs.setCaretPosition(logs.getDocument().getLength());
    }

    public static void main(String[] args) {
        new KlientGUI();
    }
}