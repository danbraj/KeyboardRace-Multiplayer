import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class Client extends Thread { //todo rename to ClientConnection

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    protected ClientGUI gui;

    public Client(Socket socket, ClientGUI gui) {
        this.socket = socket;
        this.gui = gui;
    }

    public void sendObjectToServer(Packet packet) {
        this.sendObjectToServer(packet, false);
    }

    public void sendObjectToServer(Packet packet, boolean reset) {
        try {
            if (reset)
                oos.reset();
            oos.writeObject(packet);
            oos.flush();
        } catch (IOException e) {
        }
    }

    public Object receiveObjectFromServer() {
        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void closeClientConnection() {
        try {
            ois.close();
            oos.close();
            socket.close();
        } catch (IOException e) {
        }
    }

    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            oos.flush();

            gui.app.status |= Status.CONNECTED;
            gui.updateUI();

            this.sendObjectToServer(new Packet(Command.LOGIN_REQUEST));

            Packet packet = null;
            while (gui.app.checkStatusIfExistsFlag(Status.CONNECTED)) {

                packet = (Packet) this.receiveObjectFromServer();
                if (packet != null) {

                    Command command = packet.getCommand();
                    if (command == Command.LOGIN_RESPONSE) {

                        // podanie nazwy użytkownika
                        String nick = JOptionPane.showInputDialog(null, "Podaj nick (max. 6 znaków): ");
                        if (nick != null) {
                            nick = nick.trim().toUpperCase();
                            if (nick.equals("")) {
                                sendObjectToServer(new Packet(Command.LOGOUT));
                                gui.addToEventLog("Niepoprawny nick, zostałeś rozłączony.");
                            } else {
                                // jeżeli nazwa użytkownika spełnia wymagania to.. poinformuj serwer
                                if (nick.length() > 6)
                                    nick = nick.substring(0, 6);

                                sendObjectToServer(new Packet(Command.NICK_SET, nick));

                                gui.app.playerId = packet.getPlayerId();
                                gui.panelGracza[gui.app.playerId].resetActionPoints();

                                gui.btnReady.setEnabled(true);
                            }
                        } else {
                            sendObjectToServer(new Packet(Command.LOGOUT));
                            gui.addToEventLog("Wylogowano.");
                        }

                    } else if (command == Command.LOGOUT) {

                        // ustawienia ui klienta po wylogowaniu
                        gui.app.status &= ~Status.CONNECTED;
                        String message = packet.getString();
                        if (message != null && !message.isEmpty())
                            gui.addToEventLog(message);

                        gui.updateUI();

                    } else if (command == Command.LOGOUT_PLAYER_NOTIFY) {

                        // ustawienia ui panela gracza, który się wylogował
                        int deserterId = packet.getPlayerId();

                        gui.app.status = packet.getBool() ? gui.app.status | Status.STARTED
                                : gui.app.status & ~Status.STARTED;
                        if ((gui.app.status & Status.STARTED) == Status.STARTED) {

                            gui.addToEventLog("Gracz " + gui.panelGracza[deserterId].getNick() + " uciekł!");

                            gui.btnLogon.setEnabled(true);
                            gui.btnReady.setEnabled(true);
                            for (PanelPlayer pp : gui.panelGracza) {
                                pp.setReadiness(false);
                                pp.setProgressValue(0);
                                pp.setPlace("");
                                pp.setSkillAvailability(0, false);
                                pp.setSkillAvailability(1, false);
                                pp.setSkillAvailability(2, false);
                            }
                            gui.input.setEnabled(false);
                            gui.input.setText("");
                            gui.text.setText("");
                        }
                        gui.panelGracza[deserterId].join("");
                        gui.panelGracza[deserterId].setProgressValue(0);
                        gui.panelGracza[gui.app.playerId].resetActionPoints();

                    } else if (command == Command.UPDATE_PLAYERS_LIST) {

                        // wczytanie nazw graczy do paneli
                        ExtendedPacket extendedPacket = (ExtendedPacket) packet;
                        for (Player player : extendedPacket.getPlayers())
                            gui.panelGracza[player.getPlayerId()].join(player.getNick());

                    } else if (command == Command.CHANGE_READY) {

                        // przełącznik koloru gotowości danego użytkownika
                        gui.panelGracza[packet.getPlayerId()].setReadiness(packet.getBool());

                    } else if (command == Command.START_GAME) {

                        // rozpoczęcie gry
                        ExtendedPacket extendedPacket = (ExtendedPacket) packet;
                        gui.app.zadanie = extendedPacket.getZadanie();

                        gui.btnReady.setEnabled(false);
                        gui.btnLogon.setEnabled(false);

                        gui.app.typedChars = 0;

                        gui.text.setText(gui.app.zadanie.getText());
                        gui.input.setEnabled(true);
                        gui.input.requestFocus();

                    } else if (command == Command.PROGRESS) {

                        // zmiana wartości progresu danego użytkownika
                        gui.panelGracza[packet.getPlayerId()].setProgressValue(packet.getInt());

                    } else if (command == Command.WIN) {

                        int senderId = packet.getPlayerId();
                        // poinformowanie o ukończeniu zadania przez danego użytkownika
                        gui.panelGracza[senderId].setPlace(packet.getInt());
                        gui.addToEventLog("Gracz " + gui.panelGracza[senderId].getNick() + " juz skończył!");

                    } else if (command == Command.SEND_TEXT_RESPONSE) {

                        // odpowiedź serwera na prośbę o pozwolenie na przesłanie tekstu do serwera
                        if (packet.getBool())
                            gui.displayRequestWithTextPopup();
                        else
                            gui.addToEventLog("Serwer odmówił żądanie o pozwolenie na przesłanie pliku.");

                    } else if (command == Command.RESET) {

                        // ogłoszenie wyników użytkowników
                        String content = "Tablica wyników:";
                        int counter = 1;
                        ExtendedPacket extendedPacket = (ExtendedPacket) packet;

                        for (Player player : extendedPacket.getPlayers())
                            content += "\n" + (counter++) + ". " + player.getNick();

                        gui.addToEventLog(content);

                        gui.app.typedChars = 0;

                        // zresetowanie ui paneli graczy
                        gui.btnLogon.setEnabled(true);
                        gui.btnReady.setEnabled(true);
                        for (PanelPlayer pp : gui.panelGracza) {
                            pp.setReadiness(false);
                            pp.setProgressValue(0);
                            pp.setPlace("");
                            pp.setSkillAvailability(0, false);
                            pp.setSkillAvailability(1, false);
                            pp.setSkillAvailability(2, false);
                        }
                        gui.panelGracza[gui.app.playerId].resetActionPoints();

                    } else if (command == Command.DEBUFF_CAST) {

                        // aktywacja działania urozmaicenia rozgrywki
                        int targetId = packet.getInt();
                        Debuff debuff = packet.getDebuff();
                        int durationTime;
                        if (debuff == Debuff.INVISIBILITY) {
                            gui.panelGracza[targetId].setSkillActivity(0, true);
                            durationTime = Variety.HIDE_INPUT_CONTENT.getDurationTime();
                        } else if (debuff == Debuff.REVERSE) {
                            gui.panelGracza[targetId].setSkillActivity(1, true);
                            durationTime = Variety.REVERSE_WORDS_IN_TEXT.getDurationTime();
                        } else if (debuff == Debuff.SHUFFLE) {
                            gui.panelGracza[targetId].setSkillActivity(2, true);
                            durationTime = Variety.SHUFFLE_CHARS_IN_WORDS.getDurationTime();
                        } else
                            durationTime = 5;

                        if (gui.app.playerId == targetId) {
                            gui.castDebuff(debuff);
                            new java.util.Timer().schedule(new java.util.TimerTask() {
                                public void run() {
                                    sendObjectToServer(new Packet(Command.DEBUFF_CLEAR, debuff, targetId));
                                    gui.clearDebuff(debuff);
                                }
                            }, durationTime * 1000);
                        }

                    } else if (command == Command.DEBUFF_CLEAR) {

                        // przywrócenie skutków urozmaicenia rozgrywki
                        int targetId = packet.getInt();
                        Debuff debuff = packet.getDebuff();
                        if (debuff == Debuff.INVISIBILITY)
                            gui.panelGracza[targetId].setSkillActivity(0, false);
                        else if (debuff == Debuff.REVERSE)
                            gui.panelGracza[targetId].setSkillActivity(1, false);
                        else if (debuff == Debuff.SHUFFLE)
                            gui.panelGracza[targetId].setSkillActivity(2, false);
                    }
                }

            }
        } catch (UnknownHostException e) {
            gui.addToEventLog("Błąd połączenia!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Prawdopodobnie serwer nie jest włączony.", "Informacja",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (NullPointerException e) {
            gui.addToEventLog(e.toString());
        } finally {
            closeClientConnection();
        }
    }
}