import java.net.Socket;
import java.util.ArrayList;

public class ServerConnection extends Connection implements Runnable {

    private Player player;
    private boolean isConnected = true;
    protected ServerGUI gui;

    public ServerConnection(Socket socket, ServerGUI gui) {
        super(socket);
        this.gui = gui;
    }

    public Player getPlayer() {
        return this.player;
    }

    public void run() {
        Packet packet = null;
        while (Common.isStatusContainsFlag(Status.RUNNING) && isConnected) {

            packet = receivePacket();
            if (packet != null) {

                Command command = packet.getCommand();
                if (command == Command.LOGIN_REQUEST) {

                    String connectionIp = socket.getInetAddress().getHostAddress();

                    if (!Common.isStatusContainsFlag(Status.STARTED)) {

                        gui.addLog("Użytkownik " + connectionIp + " próbuje się połączyć.");

                        // stwórz gracza i dodaj do listy, jeżeli jest wolne miejsce
                        boolean areFreeSlots = false;
                        synchronized (gui.app.clients) {
                            for (int index = 0, k = gui.app.clients.size(); index < k; index++) {
                                if (gui.app.clients.get(index) == null) {
                                    gui.app.clients.set(index, this);
                                    player = new Player(index);
                                    areFreeSlots = true;

                                    sendPacket(new Packet(Command.LOGIN_RESPONSE, player.getPlayerId()));
                                    gui.addLog("Użytkownik " + connectionIp + " został połączony (SLOT "
                                            + player.getPlayerId() + ").");
                                    break;
                                }
                            }
                        }

                        if (!areFreeSlots) {
                            sendPacket(new Packet(Command.LOGOUT, "Niestety nie ma wolnych miejsc :<"));
                            gui.addLog("Użytkownik " + connectionIp
                                    + " został rozłączony, z powodu braku wolnego miejsca.");
                        }
                    } else {
                        sendPacket(new Packet(Command.LOGOUT, "Niestety rozgrywka już się rozpoczęła :<"));
                        gui.addLog("Użytkownik " + connectionIp
                                + " został rozłączony, ponieważ rozgrywka już się rozpoczęła.");
                    }

                } else if (command == Command.LOGOUT) {

                    // poinformowanie pozostałych użytkowników o wylogowującym się użytkowników
                    for (ServerConnection connection : gui.app.clients) {
                        if (connection != null && connection != this)
                            connection.sendPacket(new Packet(Command.LOGOUT_PLAYER_NOTIFY, player.getPlayerId(),
                                    Common.isStatusContainsFlag(Status.STARTED)));
                    }

                    // usunięcie użytkownika z listy użytkowników
                    sendPacket(new Packet(Command.LOGOUT, player.getPlayerId()));
                    gui.addLog("Użytkownik " + socket.getInetAddress().getHostAddress() + " został rozłączony (SLOT "
                            + player.getPlayerId() + ").");

                    this.isConnected = false;
                    synchronized (gui.app.clients) {
                        gui.app.clients.set(player.getPlayerId(), null);//zamiast closeConnection nullowanie
                    }

                    // zatrzymanie rozgrywki, jeżeli ktoś wyszedł w trakcie gry
                    if (Common.isStatusContainsFlag(Status.STARTED))
                        App.STATUS &= ~Status.STARTED;

                } else if (command == Command.NICK_SET) {

                    player.setNick(packet.getString());

                    synchronized (gui.app.clients) {
                        ArrayList<Player> players = new ArrayList<Player>();
                        for (ServerConnection connection : gui.app.clients)
                            if (connection != null)
                                players.add(connection.player);

                        // aktualizacja użytkownków dla nowego użytkownika 
                        // poinformowanie innych użytkowników o nowym użytkowniku
                        ExtendedPacket extendedPacket = new ExtendedPacket(Command.UPDATE_PLAYERS_LIST, players);
                        for (ServerConnection connection : gui.app.clients)
                            if (connection != null)
                                connection.sendPacket(extendedPacket, true);
                    }

                } else if (command == Command.CHANGE_READY) {

                    // zmiana gotowości użytkownika i sprawdzenie czy wszyscy pozostali są gotowi
                    boolean isReady = player.toggleAndGetReady();
                    boolean isReadyAll = true;
                    Packet newPacket = new Packet(Command.CHANGE_READY, player.getPlayerId(), isReady);
                    for (ServerConnection connection : gui.app.clients) {
                        if (connection != null) {
                            if (isReadyAll)
                                if (!connection.player.isReady)
                                    isReadyAll = false;
                            connection.sendPacket(newPacket);
                        }
                    }

                    // jeżeli wszyscy użytkownicy byli gotowi to startuje gra
                    if (isReadyAll) {
                        synchronized (gui.app.leaderboard) {
                            gui.app.leaderboard.clear();
                        }

                        // wylosowanie zadania oraz jego przydzielenie do użytkowników i tym samym start rozgrywki 
                        Zadanie zadanie = gui.randomizeTask();
                        if (zadanie != null) {
                            App.STATUS |= Status.STARTED;
                            synchronized (gui.app.clients) {
                                gui.app.playersCount = 0;
                                Packet extendedPacket = new ExtendedPacket(Command.START_GAME, zadanie);
                                for (ServerConnection connection : gui.app.clients) {
                                    if (connection != null) {
                                        gui.app.playersCount++;
                                        connection.sendPacket(extendedPacket);
                                        connection.getPlayer().setUnready();
                                    }
                                }
                            }
                        } else {
                            // TODO: gdy nie ma żadnych zadań
                        }
                    }

                } else if (command == Command.PROGRESS) {

                    // poinformowanie użytkowników o zmieniającym się progresie
                    int senderId = packet.getPlayerId();
                    int progress = packet.getInt();
                    Packet newPacket = new Packet(Command.PROGRESS, senderId, progress);
                    for (ServerConnection connection : gui.app.clients) {
                        if (connection != null)
                            connection.sendPacket(newPacket);
                    }

                } else if (command == Command.WIN) {

                    int winnerId = packet.getPlayerId();

                    synchronized (gui.app.clients) {
                        gui.app.place++;
                        Packet newPacket = new Packet(Command.WIN, winnerId, gui.app.place);
                        for (ServerConnection connection : gui.app.clients) {
                            if (connection != null)
                                connection.sendPacket(newPacket);
                        }
                    }

                    // dodanie użytkownika, który skończył zadanie do listy
                    synchronized (gui.app.leaderboard) {
                        gui.app.leaderboard.add(player);
                    }

                    // jeżeli wszyscy ukończyli zadanie, następuje ogłoszenie wyników
                    if (gui.app.leaderboard.size() == gui.app.playersCount) {
                        gui.app.place = 0;
                        App.STATUS &= ~Status.STARTED;
                        synchronized (gui.app.clients) {
                            ExtendedPacket ep = new ExtendedPacket(Command.RESET, gui.app.leaderboard);
                            for (ServerConnection connection : gui.app.clients) {
                                if (connection != null)
                                    connection.sendPacket(ep, true);
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
                                sendPacket(new Packet(Command.SEND_TEXT_RESPONSE, true));
                            } else
                                sendPacket(new Packet(Command.SEND_TEXT_RESPONSE, false));
                        }
                    } else
                        sendPacket(new Packet(Command.SEND_TEXT_RESPONSE, false));

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
                    for (ServerConnection connection : gui.app.clients)
                        if (connection != null)
                            connection.sendPacket(packet);
                }
            }
        }
        closeConnection();
    }
}