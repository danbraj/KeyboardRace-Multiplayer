import java.io.*;
import java.net.*;
import java.util.Vector;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ClientGUI extends JFrame {

    protected PanelPlayer[] panelGracza = new PanelPlayer[App.MAX_PLAYERS];
    protected JTextArea text, logs;
    protected JPanel panelGraczy, panelGry, panelBoczny, panelLogowania, panelDodatkowy;
    protected JTextField host, input;
    protected JButton btnHowToPlay, btnAddToServer, btnReady, btnLogon;
    protected JLabel lbStatus;
    protected ClientApp app;

    public ClientGUI(ClientApp app) {
        super("Klient " + App.VERSION);
        this.app = app;

        setSize(880, 600);
        setMinimumSize(new Dimension(640, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // -- panel z graczami
        panelGraczy = new JPanel(new GridLayout(App.MAX_PLAYERS, 0));
        for (int i = 0; i < App.MAX_PLAYERS; i++) {
            panelGracza[i] = new PanelPlayer(i, this);
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
        // ustawienie funkcji klawisza spacja, gdy pole tekstowe jest aktywne
        input.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "space");
        input.getActionMap().put("space", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {

                // usunięcie pierwszego wyrazu z tekstu, gdy jest zgodny z napisanym w polu tekstowym
                String stringToCut = input.getText().trim();
                if (app.zadanie.ifEqualsGoNext(stringToCut)) {

                    // aktualizacja progresu i wysłanie go do serwera (docelowo do pozostałych klientów)
                    panelGracza[app.playerId].setProgressValue(app.zadanie.getProgress());
                    app.client.sendObjectToServer(new Packet(Command.PROGRESS, app.playerId, app.zadanie.getProgress()));

                    // aktywacja dostępności umiejętności, gdy osiągnięta została odpowiednia ilość punktów
                    boolean isChanged = false;
                    int idSkill = -1, ap = panelGracza[app.playerId].addActionPointAndGet();
                    if (ap == Variety.HIDE_INPUT_CONTENT.getCost()) {
                        idSkill = 0;
                        isChanged = true;
                    } else if (ap == Variety.REVERSE_WORDS_IN_TEXT.getCost()) {
                        idSkill = 1;
                        isChanged = true;
                    } else if (ap == Variety.SHUFFLE_CHARS_IN_WORDS.getCost()) {
                        idSkill = 2;
                        isChanged = true;
                    }

                    if (isChanged)
                        for (PanelPlayer pp : panelGracza)
                            if (pp.panelId != app.playerId && !pp.getNick().isEmpty())
                                pp.setSkillAvailability(idSkill, true);

                    // sprawdzenie czy użytkownik ukończył app.zadanie
                    if (app.zadanie.isSuccess) {
                        input.setEnabled(false);

                        app.client.sendObjectToServer(new Packet(Command.WIN, app.playerId));
                        
                        text.setText("");
                        input.setText("");
                    } else {
                        app.typedChars += stringToCut.length() + 1;
                        text.setText(text.getText().substring(stringToCut.length() + 1));
                        input.setText("");
                    }
                }
            }
        });

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

        host = new JTextField(ClientApp.DEFAULT_HOSTNAME);
        host.setFont(new Font("Verdana", Font.PLAIN, 12));
        lbStatus = new JLabel("Status: niepołączony", SwingConstants.RIGHT);
        lbStatus.setForeground(Color.RED);

        panelLogowania.add(new JLabel("Serwer (host:port)"));
        panelLogowania.add(new JLabel(App.VERSION, SwingConstants.RIGHT));
        panelLogowania.add(host);
        panelLogowania.add(lbStatus);

        // ---- panel z przyciskami (prawy dolny róg)
        panelDodatkowy = new JPanel(new GridLayout(2, 2));

        Obsluga obsluga = new Obsluga();
        btnHowToPlay = new JButton("Jak grać?");
        btnHowToPlay.setEnabled(false);
        btnAddToServer = new JButton("Dodaj tekst do gry");
        btnAddToServer.addActionListener(obsluga);
        btnAddToServer.setEnabled(false);
        btnReady = new JButton("Gotowość");
        btnReady.addActionListener(obsluga);
        btnReady.setEnabled(false);
        btnLogon = new JButton("Połącz");
        btnLogon.setPreferredSize(new Dimension(42, 42));
        btnLogon.addActionListener(obsluga);

        panelDodatkowy.add(btnLogon);
        panelDodatkowy.add(btnAddToServer);
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
                if (app.checkStatusIfExistsFlag(Status.CONNECTED)) {

                    app.client.sendObjectToServer(new Packet(Command.LOGOUT));
                }
                setVisible(false);
                System.exit(0);
            }
        });
        setVisible(true);
    }

    // wyświetlenie popupa z możliwością dodania tekstu do aplikacji
    protected void displayRequestWithTextPopup() {

        JTextArea ta = new JTextArea();
        ta.setLineWrap(true);
        ta.setPreferredSize(new Dimension(600, 300));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Treść zadania:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(ta), BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, panel, "Dodaj prośbę z tekstem do serwera",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            app.client.sendObjectToServer(new Packet(Command.SEND_TEXT, ta.getText()));
        } else {
            app.client.sendObjectToServer(new Packet(Command.SEND_TEXT, ""));
        }
    }

    private class Obsluga implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnAddToServer) {
                 app.client.sendObjectToServer(new Packet(Command.SEND_TEXT_REQUEST));
            } else if (e.getSource() == btnReady) {
                 app.client.sendObjectToServer(new Packet(Command.CHANGE_READY));
            } else if (e.getSource() == btnLogon) {
                if (app.checkStatusIfExistsFlag(Status.CONNECTED)) {
                     app.client.sendObjectToServer(new Packet(Command.LOGOUT));
                } else {
                    String[] hostParameters = host.getText().split(":", 2);
                    try {
                        app.client = new Client(new Socket(hostParameters[0], new Integer(hostParameters[1])), ClientGUI.this);
                        app.client.start();
                    } catch (UnknownHostException ex) {
                    } catch (IOException ex) {
                        addToEventLog("Nie można się połączyć z serwerem [" + hostParameters[0] + "]");
                    }
                }
            }
        }
    }

    // aktualizacja wyglądu UI aplikacji w zależności od stanu połączenia
    protected void updateUI() {
        if (app.checkStatusIfExistsFlag(Status.CONNECTED)) {
            host.setEnabled(false);
            btnAddToServer.setEnabled(true);

            btnLogon.setText("Rozłącz");
            lbStatus.setText("Status: połączony");
            lbStatus.setForeground(Color.decode("#006600"));
        } else {
            input.setEnabled(false);
            input.setText("");
            text.setText("");

            host.setEnabled(true);
            btnAddToServer.setEnabled(false);
            btnLogon.setEnabled(true);
            btnReady.setEnabled(false);

            btnLogon.setText("Połącz");
            lbStatus.setText("Status: niepołączony");
            lbStatus.setForeground(Color.RED);

            for (PanelPlayer pp : panelGracza)
                pp.leave();
        }
    }

    protected void castDebuff(Debuff d) {
        if (d == Debuff.INVISIBILITY)
            this.castInvisibilityDebuff();
        else if (d == Debuff.REVERSE)
            this.castReverseDebuff();
        else if (d == Debuff.SHUFFLE)
            this.castShuffleDebuff();
    }

    protected void clearDebuff(Debuff d) {
        if (d == Debuff.INVISIBILITY)
            this.clearInvisibilityDebuff();
        else if (d == Debuff.REVERSE)
            this.clearReverseDebuff();
        else if (d == Debuff.SHUFFLE)
            this.clearShuffleDebuff();
    }

    private void castInvisibilityDebuff() {
        input.setForeground(Color.WHITE);
    }

    private void clearInvisibilityDebuff() {
        input.setForeground(Color.BLACK);
    }

    private void castReverseDebuff() {
        text.setText(this.mirror(text.getText()));
    }

    private void clearReverseDebuff() {
        text.setText(app.zadanie.getText().substring(app.typedChars));
    }

    private void castShuffleDebuff() {
        text.setText(this.shuffle(text.getText()));
    }

    private void clearShuffleDebuff() {
        text.setText(app.zadanie.getText().substring(app.typedChars));
    }

    // odwrócenie wyrazów w tekście
    private String mirror(String content) {
        String reversedContent = new StringBuilder(content).reverse().toString();
        String[] words = reversedContent.split(" ");

        StringBuilder result = new StringBuilder("");
        for (int i = words.length - 1; i >= 0; i--)
            result.append(words[i] + " ");
        return result.toString();
    }

    // pomieszanie środkowych liter w wyrazach tekstu
    private String shuffle(String content) {

        Random random = new Random();
        String badChars = ".,:;!?()\"";

        String[] words = content.split(" ");
        String result = "";
        for (int i = 0; i < words.length; i++) {

            char[] chars = words[i].toCharArray();
            int charsCount = chars.length;

            int start = 0, stop = 0;
            if (charsCount > 2) {

                for (int j = 0; j < charsCount - 1; j++) {
                    if (!badChars.contains(Character.toString(chars[j]))) {
                        start = j + 1;
                        break;
                    }
                }

                for (int j = charsCount - 1; j > 0; j--) {
                    if (!badChars.contains(Character.toString(chars[j]))) {
                        stop = j - 1;
                        break;
                    }
                }
            }

            if (start < stop) {
                for (int j = start; j < stop; j++) {
                    int zmiana = j + random.nextInt(stop - j);
                    char temp = chars[j];
                    if (zmiana > 0) {
                        chars[j] = chars[zmiana];
                        chars[zmiana] = temp;
                    }
                }
            }

            result = result + String.valueOf(chars) + " ";
        }
        return result;
    }

    protected boolean updateSkillsAvailability(Debuff c) {

        int cost = 0;
        if (c == Debuff.INVISIBILITY)
            cost = Variety.HIDE_INPUT_CONTENT.getCost();
        else if (c == Debuff.REVERSE)
            cost = Variety.REVERSE_WORDS_IN_TEXT.getCost();
        else if (c == Debuff.SHUFFLE)
            cost = Variety.SHUFFLE_CHARS_IN_WORDS.getCost();

        if (panelGracza[app.playerId].tryChangeActionPoints(cost)) {

            int currentActionPoints = panelGracza[app.playerId].getActionPoints();
            if (currentActionPoints < Variety.SHUFFLE_CHARS_IN_WORDS.getCost()) {
                for (PanelPlayer pp : panelGracza)
                    pp.setSkillAvailability(2, false);
                if (currentActionPoints < Variety.REVERSE_WORDS_IN_TEXT.getCost()) {
                    for (PanelPlayer pp : panelGracza)
                        pp.setSkillAvailability(1, false);
                    if (currentActionPoints < Variety.HIDE_INPUT_CONTENT.getCost()) {
                        for (PanelPlayer pp : panelGracza)
                            pp.setSkillAvailability(0, false);
                    }
                }
            }
            return true;
        }
        return false;
    }

    protected void addToEventLog(String content) {
        logs.append(content + "\n");
        logs.setCaretPosition(logs.getDocument().getLength());
    }
}
