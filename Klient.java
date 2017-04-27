import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Klient extends JFrame {

    private JTextArea text, logs;
    private JPanel panelGraczy, panelGry, panelBoczny, panelLogowania, panelDodatkowy;
    private JTextField host, input;
    private JButton btnHowToPlay, btnAddToSerwer, btnReady, btnLogon; 
    private JLabel lbStatus;

    private String hostname = "localhost:2345";
	private boolean isConnected = false;

    public Klient() {

		super("Klient v0.2");
		setSize(880, 600);
        setMinimumSize(new Dimension(640, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
        
        // -- panel z graczami
        panelGraczy = new JPanel(new GridLayout(6, 0));
        panelGraczy.setPreferredSize(new Dimension(240, 240));
        panelGraczy.setBackground(Color.BLUE); //

        // -- panel gry
        text = new JTextArea();
        text.setText("Pole tekstowe");
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

        logs = new JTextArea();
        logs.setText("Logi aplikacji");
        logs.setWrapStyleWord(true);
        logs.setLineWrap(true);
        logs.setOpaque(false);
        logs.setEditable(false);
        logs.setFocusable(false);
        logs.setBorder(new EmptyBorder(4, 4, 4, 4));

        panelGry = new JPanel(new BorderLayout());
        panelGry.setBackground(Color.LIGHT_GRAY);

        panelGry.add(text, BorderLayout.CENTER);
        panelGry.add(input, BorderLayout.SOUTH);

        // -- panel po prawej stronie
        panelBoczny = new JPanel(new BorderLayout());
        panelBoczny.setBackground(Color.MAGENTA); //

        panelLogowania = new JPanel(new GridLayout(2, 2));
        panelLogowania.setBorder(new EmptyBorder(12, 10, 12, 10));
        panelLogowania.setBackground(Color.GREEN); //

        host = new JTextField(hostname);
        host.setFont(new Font("Verdana", Font.PLAIN, 12));

        panelLogowania.add(new JLabel("Serwer (host:port)"));
        panelLogowania.add(new JLabel("v0.2", SwingConstants.RIGHT));
        panelLogowania.add(host);

        lbStatus = new JLabel("Status: niepolaczony", SwingConstants.RIGHT);
        lbStatus.setForeground(Color.RED);
        panelLogowania.add(lbStatus);

        panelDodatkowy = new JPanel(new GridLayout(2, 2));
        panelDodatkowy.setBackground(Color.RED); //

        btnHowToPlay = new JButton("Jak grac?");
        btnHowToPlay.setEnabled(false);
        btnAddToSerwer = new JButton("Dodaj tekst do gry");
        btnAddToSerwer.setEnabled(false);
        btnReady = new JButton("Gotowy");
        btnReady.setEnabled(false);
        btnLogon = new JButton("Polacz");
        btnLogon.setEnabled(false);
        btnLogon.setPreferredSize(new Dimension(42, 42));

        panelDodatkowy.add(btnLogon);
        panelDodatkowy.add(btnAddToSerwer);
        panelDodatkowy.add(btnReady);
        panelDodatkowy.add(btnHowToPlay);

        JPanel opakowanie = new JPanel();
        opakowanie.setLayout(new BoxLayout(opakowanie, BoxLayout.Y_AXIS));

        opakowanie.add(panelLogowania);
        opakowanie.add(panelDodatkowy);
        
        panelBoczny.add(new JScrollPane(logs), BorderLayout.CENTER);
        panelBoczny.add(opakowanie, BorderLayout.SOUTH);

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

    public static void main(String[] args) {
        new Klient();
    }
}