import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JTextArea;

public class ServerConnection extends Thread {

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Player player;
    private boolean isConnected = true;
    protected ServerGUI gui;

    public ServerConnection(Socket socket, ServerGUI gui) {
        this.socket = socket;
        this.gui = gui;
    }

    public void sendObjectToClient(Packet packet) {
        this.sendObjectToClient(packet, false);
    }

    public void sendObjectToClient(Packet packet, boolean reset) {
        try {
            if (reset)
                oos.reset();
            oos.writeObject(packet);
            oos.flush();
        } catch (IOException e) {
        }
    }

    public Object receiveObjectFromClient() {
        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void closeConnection() {
        try {
            ois.close();
            oos.close();
            socket.close();
        } catch (IOException e) {
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            oos.flush();

            Packet packet = null;
            while (gui.app.checkStatusIfExistsFlag(Consts.RUNNING) && isConnected) {

                packet = (Packet) this.receiveObjectFromClient();
                if (packet != null) {

                    Command command = packet.getCommand();
                    if (command == Command.LOGIN_REQUEST) {

                        String connectionIp = socket.getInetAddress().getHostAddress();

                        if (gui.app.checkStatusIfNotExistsFlag(Consts.STARTED)) {

                            gui.addLog("Użytkownik " + connectionIp + " próbuje się połączyć.");

                            // stwórz gracza i dodaj do listy, jeżeli jest wolne miejsce
                            boolean areFreeSlots = false;
                            synchronized (gui.app.clients) {
                                for (int index = 0, k = gui.app.clients.size(); index < k; index++) {
                                    if (gui.app.clients.get(index) == null) {
                                        gui.app.clients.set(index, this);
                                        player = new Player(index);
                                        areFreeSlots = true;

                                        sendObjectToClient(new Packet(Command.LOGIN_RESPONSE, player.getPlayerId()));
                                        gui.addLog("Użytkownik " + connectionIp + " został połączony (SLOT "
                                                + player.getPlayerId() + ").");
                                        break;
                                    }
                                }
                            }

                            if (!areFreeSlots) {
                                sendObjectToClient(new Packet(Command.LOGOUT, "Niestety nie ma wolnych miejsc :<"));
                                gui.addLog("Użytkownik " + connectionIp
                                        + " został rozłączony, z powodu braku wolnego miejsca.");
                            }
                        } else {
                            sendObjectToClient(new Packet(Command.LOGOUT, "Niestety rozgrywka już się rozpoczęła :<"));
                            gui.addLog("Użytkownik " + connectionIp
                                    + " został rozłączony, ponieważ rozgrywka już się rozpoczęła.");
                        }

                    } else if (command == Command.LOGOUT) {

                        // poinformowanie pozostałych użytkowników o wylogowującym się użytkowników
                        for (ServerConnection client : gui.app.clients) {
                            if (client != null && client != this)
                                client.sendObjectToClient(new Packet(Command.LOGOUT_PLAYER_NOTIFY, player.getPlayerId(),
                                        gui.app.checkStatusIfExistsFlag(Consts.STARTED)));
                        }

                        // usunięcie użytkownika z listy użytkowników
                        sendObjectToClient(new Packet(Command.LOGOUT, player.getPlayerId()));//
                        gui.addLog("Użytkownik " + socket.getInetAddress().getHostAddress()
                                + " został rozłączony (SLOT " + player.getPlayerId() + ").");

                        this.isConnected = false;
                        synchronized (gui.app.clients) {
                            gui.app.clients.set(player.getPlayerId(), null);
                        }

                        // zatrzymanie rozgrywki, jeżeli ktoś wyszedł w trakcie gry
                        if (gui.app.checkStatusIfExistsFlag(Consts.STARTED))
                            gui.app.status &= ~Consts.STARTED;

                    } else if (command == Command.NICK_SET) {

                        player.setNick(packet.getString());

                        synchronized (gui.app.clients) {
                            ArrayList<Player> players = new ArrayList<Player>();
                            for (ServerConnection client : gui.app.clients)
                                if (client != null)
                                    players.add(client.player);

                            // aktualizacja użytkownków dla nowego użytkownika 
                            // poinformowanie innych użytkowników o nowym użytkowniku
                            ExtendedPacket extendedPacket = new ExtendedPacket(Command.UPDATE_PLAYERS_LIST, players);
                            for (ServerConnection client : gui.app.clients)
                                if (client != null)
                                    client.sendObjectToClient(extendedPacket, true);
                        }

                    } else if (command == Command.CHANGE_READY) {

                        // zmiana gotowości użytkownika i sprawdzenie czy wszyscy pozostali są gotowi
                        boolean isReady = player.toggleAndGetReady();
                        boolean isReadyAll = true;
                        Packet newPacket = new Packet(Command.CHANGE_READY, player.getPlayerId(), isReady);
                        for (ServerConnection client : gui.app.clients) {
                            if (client != null) {
                                if (isReadyAll)
                                    if (!client.player.isReady)
                                        isReadyAll = false;
                                client.sendObjectToClient(newPacket);
                            }
                        }

                        // jeżeli wszyscy użytkownicy byli gotowi to startuje gra
                        if (isReadyAll) {
                            gui.app.status |= Consts.STARTED;

                            synchronized (gui.app.leaderboard) {
                                gui.app.leaderboard.clear();
                            }

                            // wylosowanie zadania oraz jego przydzielenie do użytkowników i tym samym start rozgrywki 
                            Zadanie zadanie = gui.randomizeTask();
                            if (zadanie != null) {
                                synchronized (gui.app.clients) {
                                    gui.app.playersCount = 0;
                                    Packet extendedPacket = new ExtendedPacket(Command.START_GAME, zadanie);
                                    for (ServerConnection client : gui.app.clients) {
                                        if (client != null) {
                                            gui.app.playersCount++;
                                            client.sendObjectToClient(extendedPacket);
                                            client.getPlayer().setUnready();
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
                        Packet newPacket = new Packet(Command.PROGRESS, senderId, progress);
                        for (ServerConnection client : gui.app.clients) {
                            if (client != null)
                                client.sendObjectToClient(newPacket);
                        }

                    } else if (command == Command.WIN) {

                        int winnerId = packet.getPlayerId();

                        synchronized (gui.app.clients) {
                            gui.app.place++;
                            Packet newPacket = new Packet(Command.WIN, winnerId, gui.app.place);
                            for (ServerConnection client : gui.app.clients) {
                                if (client != null)
                                    client.sendObjectToClient(newPacket);
                            }
                        }

                        // dodanie użytkownika, który skończył zadanie do listy
                        synchronized (gui.app.leaderboard) {
                            gui.app.leaderboard.add(player);
                        }

                        // jeżeli wszyscy ukończyli zadanie, następuje ogłoszenie wyników
                        if (gui.app.leaderboard.size() == gui.app.playersCount) {
                            gui.app.place = 0;
                            gui.app.status &= ~Consts.STARTED;
                            synchronized (gui.app.clients) {
                                ExtendedPacket ep = new ExtendedPacket(Command.RESET, gui.app.leaderboard);
                                for (ServerConnection client : gui.app.clients) {
                                    if (client != null)
                                        client.sendObjectToClient(ep, true);
                                }
                            }
                            gui.app.leaderboard.clear();
                        }

                    } else if (command == Command.SEND_TEXT_REQUEST) {

                        // jeżeli ustawiono w konfiguracji możliwość wysyłania tekstów to..
                        if (gui.app.sendingTextsEnabled) {
                            synchronized (gui.app.tasksCount) {
                                // jeżeli nie został osiągnięty limit tekstów w poczekalni to..
                                if (gui.app.tasksCount.get() > 0) {
                                    gui.app.tasksCount.decrementAndGet();
                                    sendObjectToClient(new Packet(Command.SEND_TEXT_RESPONSE, true));
                                } else
                                    sendObjectToClient(new Packet(Command.SEND_TEXT_RESPONSE, false));
                            }
                        } else
                            sendObjectToClient(new Packet(Command.SEND_TEXT_RESPONSE, false));

                    } else if (command == Command.SEND_TEXT) {

                        // dodanie wysłanego od klienta tekstu do listy
                        String text = packet.getString().trim();
                        if (!text.isEmpty()) {
                            synchronized (gui.app.sendedTasks) {
                                gui.app.sendedTasks.offer(text);
                            }
                            gui.addLog("Klient przesłał prośbę z tekstem.");
                            gui.btnTasks.setText("Pokaż odebrane zadania (" + gui.app.sendedTasks.size() + ")");
                            gui.btnTasks.setEnabled(true);
                        } else
                            gui.app.tasksCount.incrementAndGet();

                    } else if (command == Command.DEBUFF_CAST || command == Command.DEBUFF_CLEAR) {

                        // przekazanie otrzymanego pakietu do wszystkich klientów
                        for (ServerConnection client : gui.app.clients)
                            if (client != null)
                                client.sendObjectToClient(packet);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            closeConnection();
        }
    }
}