import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class PanelPlayer extends JPanel {

    JProgressBar progress;
    JPanel panelWithNick;
    JLabel labelWithNick, labelWithPlace, labelWithAP;
    JButton[] btns = new JButton[3];
    int panelId;
    int actionPoints;
    ClientGUI clientGUI;

    public PanelPlayer(int index, ClientGUI clientGUI) {
        super(new BorderLayout());
        this.panelId = index;
        this.clientGUI = clientGUI;

        JPanel color = new JPanel();
        color.setBackground(Consts.COLORS_OF_PLAYERS[index]);
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

        labelWithAP = new JLabel("", SwingConstants.CENTER);
        labelWithAP.setFont(new Font("Consolas", Font.PLAIN, 22));
        labelWithAP.setPreferredSize(new Dimension(40, 40));

        btnsBox.add(labelWithAP, BorderLayout.LINE_START);

        int counter = 0;
        ObslugaUmiejetnosci obslugaUmiejetnosci = new ObslugaUmiejetnosci();
        for (int i = 0, k = btns.length; i < k; i++) {
            btns[i] = new JButton();
            btns[i].setPreferredSize(new Dimension(40, 40));
            btns[i].setFont(new Font("Consolas", Font.PLAIN, 11));
            btns[i].setOpaque(true);
            btns[i].setEnabled(false);
            btns[i].addActionListener(obslugaUmiejetnosci);
            btnsBox.add(btns[i], BorderLayout.LINE_START);
            counter++;
        }
        btns[0].setText(Character.toString(Variety.HIDE_INPUT_CONTENT.getShortcut()));
        btns[1].setText(Character.toString(Variety.REVERSE_WORDS_IN_TEXT.getShortcut()));
        btns[2].setText(Character.toString(Variety.SHUFFLE_CHARS_IN_WORDS.getShortcut()));

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

    public void setProgressValue(int value) {
        this.progress.setValue(value);
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
        this.setProgressValue(0);
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

    private void updateActionPointsInfo(int value) {
        this.labelWithAP.setText(Integer.toString(value));
    }

    public int getActionPoints() {
        return this.actionPoints;
    }

    public void resetActionPoints() {
        this.actionPoints = 0;
        this.updateActionPointsInfo(0);
    }

    private void setActionPoints(int value) {
        this.actionPoints = value;
        this.updateActionPointsInfo(value);
    }

    public void addActionPoint() {
        this.updateActionPointsInfo(++this.actionPoints);
    }

    public int addActionPointAndGet() {
        int currentAP = ++this.actionPoints;
        this.updateActionPointsInfo(currentAP);
        return currentAP;
    }

    public boolean tryChangeActionPoints(int changeValue) {
        int newValue = this.getActionPoints() - changeValue;
        if (newValue >= 0) {
            this.setActionPoints(newValue);
            return true;
        } else
            return false;
    }

    protected void setSkillAvailability(int idSkill, boolean availability) {
        this.btns[idSkill].setEnabled(availability);
    }

    protected void setSkillActivity(int idSkill, boolean activity) {
        if (activity) {
            if (idSkill == 0)
                this.btns[idSkill].setBackground(Variety.HIDE_INPUT_CONTENT.getColor());
            else if (idSkill == 1)
                this.btns[idSkill].setBackground(Variety.REVERSE_WORDS_IN_TEXT.getColor());
            else if (idSkill == 2)
                this.btns[idSkill].setBackground(Variety.SHUFFLE_CHARS_IN_WORDS.getColor());
        } else
            this.btns[idSkill].setBackground(null);
    }

    private class ObslugaUmiejetnosci implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            if (e.getSource() == btns[0]) {
                if (clientGUI.updateSkillsAvailability(Debuff.INVISIBILITY)) {
                    clientGUI.client.sendObjectToServer(new Packet(Command.DEBUFF_CAST, Debuff.INVISIBILITY, panelId));
                }
            } else if (e.getSource() == btns[1]) {
                if (clientGUI.updateSkillsAvailability(Debuff.REVERSE)) {
                    clientGUI.client.sendObjectToServer(new Packet(Command.DEBUFF_CAST, Debuff.REVERSE, panelId));
                }
            } else if (e.getSource() == btns[2]) {
                if (clientGUI.updateSkillsAvailability(Debuff.SHUFFLE)) {
                    clientGUI.client.sendObjectToServer(new Packet(Command.DEBUFF_CAST, Debuff.SHUFFLE, panelId));
                }
            }
        }
    }
}