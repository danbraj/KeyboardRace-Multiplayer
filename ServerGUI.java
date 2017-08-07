import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;

public class ServerGUI extends JFrame {

    protected JButton btnRunServer, btnTasks;
    protected JTextField port;
    protected JTextArea logs;
    protected ServerApp app;

    public ServerGUI(ServerApp app) {
        super("Serwer " + App.VERSION);
        this.app = app;

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

        port = new JTextField(App.DEFAULT_PORT, 8);
        btnRunServer = new JButton("Uruchom");
        btnRunServer.setPreferredSize(new Dimension(120, 30));

        panel.add(new JLabel("Port: "));
        panel.add(port);
        panel.add(btnRunServer);

        // -- panel dolny
        btnTasks = new JButton("Pokaż odebrane zadania (0)");
        btnTasks.setPreferredSize(new Dimension(450, 28));
        btnTasks.setEnabled(false);

        // -- rozmieszczenie paneli
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(logs), BorderLayout.CENTER);
        add(btnTasks, BorderLayout.SOUTH);

        for (int i = 0; i < App.MAX_PLAYERS; i++)
            app.clients.add(null);

        Server server = new Server(this);
        Obsluga obsluga = new Obsluga(server);
        btnRunServer.addActionListener(obsluga);
        btnTasks.addActionListener(obsluga);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (App.isStateContains(App.State.RUNNING)) {
                    server.terminate();
                }
                setVisible(false);
                System.exit(0);
            }
        });
        setVisible(true);
    }

    private class Obsluga implements ActionListener {

        private Server server;

        public Obsluga(Server server) {
            this.server = server;
        }

        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btnRunServer) {
                App.STATUS ^= App.State.RUNNING;
                if (App.isStateContains(App.State.RUNNING)) {

                    for (int i = 0; i < App.MAX_PLAYERS; i++)
                        app.clients.set(i, null);

                    (new Thread(server)).start();
                    //server.start();

                    btnRunServer.setText("Zatrzymaj");
                    port.setEnabled(false);
                } else {

                    server.terminate();
                    App.STATUS &= ~App.State.STARTED;

                    btnRunServer.setText("Uruchom");
                    port.setEnabled(true);
                }

            } else if (e.getSource() == btnTasks) {
                if (app.sendedTasks.peek() != null) {

                    String content;
                    synchronized (app.sendedTasks) {
                        content = app.sendedTasks.poll();
                    }

                    if (app.tasksCount.incrementAndGet() >= ServerApp.MAX_TEXTS_COUNT_IN_QUEUE)
                        btnTasks.setEnabled(false);

                    btnTasks.setText("Pokaż odebrane zadania (" + app.sendedTasks.size() + ")");
                    this.showPopup(content);
                }
            }
        }

        // wyświetlenie popupa z tekstem wysłanym przez klienta
        private void showPopup(String text) {

            JTextArea ta = new JTextArea(text);
            ta.setLineWrap(true);
            ta.setPreferredSize(new Dimension(600, 300));
            JTextField tf = new JTextField();

            JPanel panel = new JPanel(new BorderLayout());
            JPanel panel2 = new JPanel(new GridLayout(0, 1));

            panel.add(new JLabel("Treść zadania:"), BorderLayout.NORTH);
            panel.add(new JScrollPane(ta), BorderLayout.CENTER);
            panel2.add(new JLabel("Nazwa pliku [ ].txt"));
            panel2.add(tf);

            panel.add(panel2, BorderLayout.SOUTH);

            int result = JOptionPane.showConfirmDialog(null, panel, "Prośba o dodanie tekstu do aplikacji",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                FilesService.stringToFile(tf.getText() + ".txt", ta.getText());
                // todo obsługa błędów i sprawdzić poprawność nazwy pliku
            }
        }
    }

    protected void addLog(String content) {
        logs.append(content + "\n");
        logs.setCaretPosition(logs.getDocument().getLength());
    }

    // funkcja losująca zadanie (tekst do przepisania) ze zbioru plików w folderze Texts
    protected Zadanie randomizeTask() {
        ArrayList<File> files = FilesService.getFiles();
        if (!files.isEmpty()) {
            int filesCount = files.size();
            int randomIndex = new Random().nextInt(filesCount);
            File file = files.get(randomIndex);

            addLog("Serwer wylosował: [" + randomIndex + "] " + file.getName() + "");

            try {
                String taskContent = new Scanner(file, "UTF-8").useDelimiter("\\A").next();
                return new Zadanie(taskContent);
            } catch (IOException e) {
                System.out.println("Błąd odczytu pliku.");
                System.exit(2);
            }
        } else {
            addLog("Nie ma plików z tekstami");
        }
        return null;
    }
}