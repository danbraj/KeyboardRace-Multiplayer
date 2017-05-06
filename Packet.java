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

class ExtendedPacket extends Packet {

    ArrayList<Player> players;
    Zadanie zadanie;

    public ExtendedPacket(Command command, ArrayList<Player> players) {
        super(command);
        this.players = players;
    }

    public ExtendedPacket(Command command, Zadanie zadanie) {
        super(command);
        this.zadanie = zadanie;
    }

    public ArrayList<Player> getPlayers() {
        return this.players;
    }

    public Zadanie getZadanie() {
        return this.zadanie;
    }
}