import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;

public class SerwerGUI extends JFrame {

    //private Vector<> clients = new Vector<>(8);
    private JButton btnRunSerwer;
    private JTextField port, cmdLine;
    private JTextArea logs;

    private int portNumber = 2345;
    private boolean isRunning = false;

    public SerwerGUI() {

        super("Serwer v0.31");
        setSize(450, 320);
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

        setVisible(true);
    }

    private class Obsluga implements ActionListener {

        private Serwer serwer;

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnRunSerwer) {
                isRunning = !isRunning;
                if (isRunning) {
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
                    //todo tworzenie wątku połączenia
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

    private void addLog(String content) {
        logs.append(content);
        logs.setCaretPosition(logs.getDocument().getLength());
    }

    public static void main(String[] args) {
        new SerwerGUI();
    }
}