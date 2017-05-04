import java.io.Serializable;
import java.util.ArrayList;

class Packet implements Serializable {

    Command command;
    String parameter;
    int senderId = -1;
    boolean extra;
    int progress;

    public Packet(Command command) {
        this.command = command;
    }

    public Packet(Command command, String parameter) {
        this.command = command;
        this.parameter = parameter;
    }

    public Packet(Command command, int senderId, String parameter) {
        this.command = command;
        this.senderId = senderId;
        this.parameter = parameter;
    }

    public Packet(Command command, int senderId, boolean extra) {
        this.command = command;
        this.senderId = senderId;
        this.extra = extra;
    }

    public Packet(Command command, int senderId, int progress) {
        this.command = command;
        this.senderId = senderId;
        this.progress = progress;
    }

    public Packet(Command command, int senderId) {
        this.command = command;
        this.senderId = senderId;
    }

    public Command getCommand() {
        return this.command;
    }

    public String getParameter() {
        return this.parameter;
    }

    public int getPlayerId() {
        return this.senderId;
    }

    public boolean getExtra() {
        return this.extra;
    }

    public int getProgress() {
        return this.progress;
    }
}

class PacketWithPlayer extends Packet {
    Player player;

    public PacketWithPlayer(Command command, Player player, String parameter) {
        super(command, parameter);
        this.player = player;
    }
}

class PacketWithPlayersList extends Packet {
    ArrayList<Player> players;

    public PacketWithPlayersList(ArrayList<Player> players) {
        super(Command.UPDATE_PLAYERS_LIST);
        this.players = players;
    }

    public PacketWithPlayersList(Command command, ArrayList<Player> players) {
        super(command);
        this.players = players;
    }

    public ArrayList<Player> getPlayers() {
        return this.players;
    }
}

class PacketWithTask extends Packet {
    Zadanie zadanie;

    public PacketWithTask(Zadanie zadanie) {
        super(Command.START_GAME);
        this.zadanie = zadanie;
    }

    public Zadanie getZadanie() {
        return this.zadanie;
    }
}