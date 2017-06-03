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
    private int status = 0; // 1 - connected

    private PanelPlayer[] panelGracza = new PanelPlayer[Consts.MAX_PLAYERS];
    private int playerId;
    private int actionPoints = 0;

    private Zadanie zadanie;
    private Klient client;

    public KlientGUI() {
        super("Klient " + Consts.VERSION);
        setSize(880, 600);
        setMinimumSize(new Dimension(640, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // -- panel z graczami
        panelGraczy = new JPanel(new GridLayout(Consts.MAX_PLAYERS, 0));
        //panelGraczy.setLayout(new GridLayout(7, 0));
        for (int i = 0; i < Consts.MAX_PLAYERS; i++) {
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
        panelLogowania.add(new JLabel(Consts.VERSION, SwingConstants.RIGHT));
        panelLogowania.add(host);
        panelLogowania.add(lbStatus);

        // ---- panel z przyciskami (prawy dolny róg)
        panelDodatkowy = new JPanel(new GridLayout(2, 2));

        btnHowToPlay = new JButton("Jak grac?");
        btnHowToPlay.setEnabled(false); // tmp
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
                if ((status & Consts.CONNECTED) == Consts.CONNECTED) {
                    try {
                        client.sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                        client.sendToSerwer.flush();
                    } catch (IOException ex) {
                        addLog(ex.toString());
                    }
                }
                setVisible(false);
                System.exit(0);
            }
        });
        setVisible(true);
    }

    private void displayRequestWithTextPopup() {

        JTextArea ta = new JTextArea();
        ta.setLineWrap(true);
        ta.setPreferredSize(new Dimension(600, 300));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Tresc zadania:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, panel, "Dodaj prosbe z tekstem do serwera",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                client.sendToSerwer.writeObject(new Packet(Command.SEND_TEXT, ta.getText()));
                client.sendToSerwer.flush();
            } catch (IOException ex) {
                addLog(ex.toString());
            }
        } else {
            try {
                client.sendToSerwer.writeObject(new Packet(Command.SEND_TEXT, ""));
                client.sendToSerwer.flush();
            } catch (IOException ex) {
                addLog(ex.toString());
            }
        }
    }

    private class Obsluga extends KeyAdapter implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnAddToSerwer) {
                try {
                    client.sendToSerwer.writeObject(new Packet(Command.SEND_TEXT_REQUEST));
                    client.sendToSerwer.flush();
                } catch (IOException ex) {
                    addLog(ex.toString());
                }
            } else if (e.getSource() == btnReady) {
                try {
                    client.sendToSerwer.writeObject(new Packet(Command.CHANGE_READY));
                    client.sendToSerwer.flush();
                } catch (IOException ex) {
                    addLog(ex.toString());
                }
            } else if (e.getSource() == btnLogon) {
                if ((status & Consts.CONNECTED) == Consts.CONNECTED) {
                    try {
                        client.sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                        client.sendToSerwer.flush();
                    } catch (IOException ex) {
                        addLog(ex.toString());
                    }
                } else {
                    client = new Klient();
                    client.start();
                }
            }
        }

        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                if (zadanie.ifEqualsGoNext(input.getText())) {

                    panelGracza[playerId].progress.setValue(zadanie.getProgress());

                    try {
                        client.sendToSerwer.writeObject(new Packet(Command.PROGRESS, playerId, zadanie.getProgress()));
                        client.sendToSerwer.flush();
                    } catch (IOException ex) {
                        addLog(ex.toString());
                    }

                    if (zadanie.isSuccess) {
                        input.setEnabled(false);
                        try {
                            client.sendToSerwer.writeObject(new Packet(Command.WIN, playerId));
                            client.sendToSerwer.flush();
                        } catch (IOException ex) {
                            addLog(ex.toString());
                        }
                        text.setText("");
                        input.setText("");
                    } else {
                        text.replaceRange(null, 0, input.getText().length());
                        input.setText("");
                    }
                }
            }
        }
    }

    private void updateUI() {
        if ((status & Consts.CONNECTED) == Consts.CONNECTED) {
            host.setEnabled(false);
            btnAddToSerwer.setEnabled(true);

            btnLogon.setText("Rozlacz");
            lbStatus.setText("Status: polaczony");
            lbStatus.setForeground(Color.decode("#006600"));
        } else {
            input.setEnabled(false);
            input.setText("");
            text.setText("");

            host.setEnabled(true);
            btnAddToSerwer.setEnabled(false);
            btnLogon.setEnabled(true);
            btnReady.setEnabled(false);

            btnLogon.setText("Polacz");
            lbStatus.setText("Status: niepolaczony");
            lbStatus.setForeground(Color.RED);

            for (PanelPlayer pp : panelGracza)
                pp.leave();
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

                status = status | Consts.CONNECTED;
                updateUI();

                sendToSerwer.writeObject(new Packet(Command.LOGIN_REQUEST));
                sendToSerwer.flush();

                Packet packet = null;
                while ((status & Consts.CONNECTED) == Consts.CONNECTED) {

                    try {
                        packet = (Packet) receiveFromSerwer.readObject();
                        if (packet != null) {

                            Command command = packet.getCommand();
                            if (command == Command.LOGIN_RESPONSE) {

                                // podanie nazwy użytkownika
                                String nick = JOptionPane.showInputDialog(null, "Podaj nick (max. 6 znakow): ");
                                if (nick != null) {
                                    nick = nick.trim().toUpperCase();
                                    if (nick.equals("")) {
                                        sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                                        addLog("Niepoprawny nick, zostales rozlaczony.");
                                    } else {
                                        // jeżeli nazwa użytkownika spełnia wymagania to.. poinformuj serwer
                                        if (nick.length() > 6)
                                            nick = nick.substring(0, 6);

                                        sendToSerwer.writeObject(new Packet(Command.NICK_SET, nick));

                                        playerId = packet.getPlayerId();
                                        panelGracza[playerId].resetActionPoints();

                                        btnReady.setEnabled(true);
                                    }
                                } else {
                                    sendToSerwer.writeObject(new Packet(Command.LOGOUT));
                                    addLog("Wylogowano.");
                                }

                            } else if (command == Command.LOGOUT) {

                                // ustawienia ui clienta po wylogowaniu
                                String message = packet.getString();
                                if (message != null && !message.isEmpty())
                                    addLog(message);

                                status = status & ~Consts.CONNECTED;
                                updateUI();

                            } else if (command == Command.LOGOUT_PLAYER_NOTIFY) {

                                // ustawienia ui panela gracza, który się wylogował
                                int playerId = packet.getPlayerId();

                                status = packet.getBool() ? status | Consts.STARTED : status & ~Consts.STARTED;
                                if ((status & Consts.STARTED) == Consts.STARTED) {

                                    addLog("Gracz " + panelGracza[playerId].getNick() + " uciekl!");

                                    btnLogon.setEnabled(true);
                                    btnReady.setEnabled(true);
                                    for (PanelPlayer pp : panelGracza) {
                                        pp.setReadiness(false);
                                        pp.progress.setValue(0);
                                        pp.setPlace("");
                                    }
                                    input.setEnabled(false);
                                    input.setText("");
                                    text.setText("");
                                }
                                panelGracza[playerId].join("-");
                                panelGracza[playerId].progress.setValue(0);

                            } else if (command == Command.UPDATE_PLAYERS_LIST) {

                                // wczytanie nazw graczy do paneli
                                ExtendedPacket extendedPacket = (ExtendedPacket) packet;
                                for (Player player : extendedPacket.getPlayers())
                                    panelGracza[player.getPlayerId()].join(player.getNick());

                            } else if (command == Command.CHANGE_READY) {

                                // przełącznik koloru gotowości danego użytkownika
                                panelGracza[packet.getPlayerId()].setReadiness(packet.getBool());

                            } else if (command == Command.START_GAME) {

                                // rozpoczęcie gry
                                ExtendedPacket extendedPacket = (ExtendedPacket) packet;
                                zadanie = extendedPacket.getZadanie();

                                btnReady.setEnabled(false);
                                btnLogon.setEnabled(false);

                                text.setText(zadanie.getText());
                                input.setEnabled(true);
                                input.requestFocus();

                            } else if (command == Command.PROGRESS) {

                                // zmiana wartości progresu danego użytkownika
                                panelGracza[packet.getPlayerId()].progress.setValue(packet.getInt());

                            } else if (command == Command.WIN) {

                                int senderId = packet.getPlayerId();
                                // poinformowanie o ukończeniu zadania przez danego użytkownika
                                panelGracza[senderId].setPlace(packet.getInt());
                                addLog("Gracz " + panelGracza[senderId].getNick() + " juz skonczyl!");

                            } else if (command == Command.SEND_TEXT_RESPONSE) {

                                // odpowiedź serwera na prośbę o pozwolenie na przesłanie tekstu do serwera
                                if (packet.getBool())
                                    displayRequestWithTextPopup();
                                else
                                    addLog("Serwer odmowil zadanie o pozwolenia na przeslanie pliku.");

                            } else if (command == Command.RESET) {

                                // ogłoszenie wyników użytkowników
                                String content = "Tablica wynikow:";
                                int counter = 1;
                                ExtendedPacket extendedPacket = (ExtendedPacket) packet;

                                for (Player player : extendedPacket.getPlayers())
                                    content += "\n" + (counter++) + ". " + player.nick;

                                addLog(content);

                                // zresetowanie ui paneli graczy
                                btnLogon.setEnabled(true);
                                btnReady.setEnabled(true);
                                for (PanelPlayer pp : panelGracza) {
                                    pp.setReadiness(false);
                                    pp.progress.setValue(0);
                                    pp.setPlace("");
                                }
                            }
                            sendToSerwer.flush();
                        }
                    } catch (ClassNotFoundException ex) {
                    }
                }
            } catch (UnknownHostException e) {
                addLog("Blad polaczenia!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Prawdopodobnie serwer nie jest wlaczony.", "Informacja",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            } catch (NullPointerException e) {
                addLog(e.toString());
            } finally {
                try {
                    receiveFromSerwer.close();
                    sendToSerwer.close();
                    socket.close();
                } catch (IOException e) {
                } catch (NullPointerException e) {
                }
            }
        }
    }

    private class PanelPlayer extends JPanel {

        public JProgressBar progress;
        JPanel color, panelWithNick;
        JLabel labelWithNick, labelWithPlace, labelWithAP;
        JButton[] btns = new JButton[3];
        int panelId;

        public PanelPlayer(int idColor) {
            super(new BorderLayout());
            this.panelId = idColor;

            color = new JPanel();
            color.setBackground(this.getColorById(idColor));
            color.setPreferredSize(new Dimension(40, 40));

            labelWithPlace = new JLabel("", SwingConstants.CENTER);
            labelWithPlace.setFont(new Font("Consolas", Font.PLAIN, 22));

            color.add(labelWithPlace);

            panelWithNick = new JPanel();
            panelWithNick.setBackground(Color.decode("#ffcccc"));

            labelWithNick = new JLabel("", SwingConstants.CENTER);
            labelWithNick.setFont(new Font("Consolas", Font.PLAIN, 22));
            labelWithNick.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            panelWithNick.add(labelWithNick);

            JPanel btnsBox = new JPanel(new GridLayout(0, 4));

            labelWithAP = new JLabel("", SwingConstants.CENTER); // 0
            labelWithAP.setFont(new Font("Consolas", Font.PLAIN, 22));
            labelWithAP.setPreferredSize(new Dimension(40, 40));

            btnsBox.add(labelWithAP, BorderLayout.LINE_START);

            int counter = 0;
            Umiejetnosc umiejetnosc = new Umiejetnosc();
            for (int i = 0, k = btns.length; i < k; i++) {
                btns[i] = new JButton();
                btns[i].setPreferredSize(new Dimension(40, 40));
                btnsBox.add(btns[i], BorderLayout.LINE_START);
                btns[i].setText(Integer.toString(counter));
                btns[i].setFont(new Font("Consolas", Font.PLAIN, 11));
                btns[i].setOpaque(true);
                btns[i].setEnabled(false);
                btns[i].addActionListener(umiejetnosc);
                counter++;
            }

            JPanel left = new JPanel(new BorderLayout());
            left.setPreferredSize(new Dimension(340, 340));

            progress = new JProgressBar();

            // -- rozmieszczenie paneli (z wylaczeniem progressbara)
            left.add(color, BorderLayout.LINE_START);
            left.add(panelWithNick, BorderLayout.CENTER);
            left.add(btnsBox, BorderLayout.LINE_END);

            // -- rozmieszczenie paneli
            add(left, BorderLayout.LINE_START);
            add(progress, BorderLayout.CENTER);
        }

        private Color getColorById(int id) {
            switch (id) {
            case 0:
                return Color.decode("#6077E0"); // niebieski
            case 1:
                return Color.decode("#ED094A"); // czerwony
            case 2:
                return Color.decode("#4DBD02"); // zielony
            case 3:
                return Color.decode("#E231E2"); // fioletowy
            case 4:
                return Color.decode("#37C6C7"); // błękitny
            case 5:
                return Color.decode("#FF8B17"); // pomaranczowy
            default:
                return Color.GRAY;
            }
        }

        public void setReadiness(boolean isReady) {
            this.panelWithNick.setBackground(Color.decode(isReady ? "#ccffcc" : "#ffcccc"));
        }

        public void join(String nick) {
            this.labelWithNick.setText(nick);
        }

        public String getNick() {
            return this.labelWithNick.getText();
        }

        public void leave() {
            this.progress.setValue(0);
            this.labelWithNick.setText("");
            this.panelWithNick.setBackground(Color.decode("#ffcccc"));
            this.labelWithAP.setText("");
        }

        public void setPlace(int place) {
            this.labelWithPlace.setText(Integer.toString(place));
        }

        public void setPlace(String text) {
            this.labelWithPlace.setText(text);
        }

        public void resetActionPoints() {
            this.labelWithAP.setText("0");
        }

        private int getActionPoints() {
            return new Integer(this.labelWithAP.getText());
        }

        private void setActionPoints(int value) {
            this.labelWithAP.setText(Integer.toString(value));
        }

        public boolean tryChangeActionPoints(int changeValue) {
            int newValue = this.getActionPoints() - changeValue;
            if (newValue >= 0) {
                this.setActionPoints(newValue);
                return true;
            } else
                return false;
        }

        private class Umiejetnosc implements ActionListener {

            public void actionPerformed(ActionEvent e) {

                if (e.getSource() == btns[0]) {
                    addLog(Command.DEBUFF_INVISIBILITY + "\nSender: " + playerId + "\nTarget: " + panelId);
                } else if (e.getSource() == btns[1]) {
                    addLog(Command.DEBUFF_REVERSE + "\nSender: " + playerId + "\nTarget: " + panelId);
                } else if (e.getSource() == btns[2]) {
                    addLog(Command.DEBUFF_SHUFFLE + "\nSender: " + playerId + "\nTarget: " + panelId);
                }
            }
        }
    }

    private void addLog(String content) {
        logs.append(content + "\n");
        logs.setCaretPosition(logs.getDocument().getLength());
    }

    public static void main(String[] args) {
        new KlientGUI();
    }
}