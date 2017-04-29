import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class PanelPlayer extends JPanel {

    //todo JLabel ze statusem gracza, albo zmiana koloru (public JLabel status;)?
    public JProgressBar progress;
    JPanel color, panelWithNick;
    JLabel labelWithNick;
    JButton[] btns = new JButton[3];

    boolean isReady = false;
    String nick = "Player".toUpperCase();
    int numberOfWins = 0;
    int actionPoints = 0;

    public PanelPlayer(int idColor) {
        super(new BorderLayout());

        color = new JPanel();
        color.setBackground(this.getColorById(idColor));
        color.setPreferredSize(new Dimension(40, 40));

        JLabel labelWithNumberOfWins = new JLabel("0", SwingConstants.CENTER);
        labelWithNumberOfWins.setFont(new Font("Consolas", Font.PLAIN, 22));

        color.add(labelWithNumberOfWins);

        panelWithNick = new JPanel();
        panelWithNick.setBackground(Color.decode("#ffcccc"));

        labelWithNick = new JLabel(nick + (idColor + 1), SwingConstants.CENTER);
        labelWithNick.setFont(new Font("Consolas", Font.PLAIN, 22));
        labelWithNick.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        panelWithNick.add(labelWithNick);

        JPanel btnsBox = new JPanel(new GridLayout(0, 4));

        JLabel labelWithAP = new JLabel("0", SwingConstants.CENTER);
        labelWithAP.setFont(new Font("Consolas", Font.PLAIN, 22));
        labelWithAP.setPreferredSize(new Dimension(40, 40));

        btnsBox.add(labelWithAP, BorderLayout.LINE_START);

        int counter = 0;
        for (JButton btn : btns) {
            btn = new JButton();
            btn.setPreferredSize(new Dimension(40, 40));
            btnsBox.add(btn, BorderLayout.LINE_START);
            btn.setText(Integer.toString(counter));
            btn.setFont(new Font("Consolas", Font.PLAIN, 11));
            btn.setOpaque(true);
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
        case 0: return Color.decode("#6077E0"); // niebieski
        case 1: return Color.decode("#ED094A"); // czerwony
        case 2: return Color.decode("#4DBD02"); // zielony
        case 3: return Color.decode("#E231E2"); // fioletowy
        case 4: return Color.decode("#37C6C7"); // błękitny
        case 5: return Color.decode("#FF8B17"); // pomaranczowy
        default: return Color.GRAY;
        }
    }

    public void changeReady() {
        this.isReady = !this.isReady;
        panelWithNick.setBackground(Color.decode(this.isReady ? "#ccffcc" : "#ffcccc"));
    }

    public boolean getReady() {
        return this.isReady;
    }

    public void join(String nick) {
        this.nick = nick;
        this.labelWithNick.setText(nick);
    }
}