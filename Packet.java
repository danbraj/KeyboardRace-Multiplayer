import java.io.Serializable;
import java.util.ArrayList;

class Packet implements Serializable {

    Command command;
    int senderId = -1;
    String paramString;
    boolean paramBool;
    int paramInt;

    public Packet(Command command) {
        this.command = command;
    }

    public Packet(Command command, int senderId) {
        this.command = command;
        this.senderId = senderId;
    }

    public Packet(Command command, String paramString) {
        this.command = command;
        this.paramString = paramString;
    }

    public Packet(Command command, boolean paramBool) {
        this.command = command;
        this.paramBool = paramBool;
    }

    public Packet(Command command, int senderId, String paramString) {
        this.command = command;
        this.senderId = senderId;
        this.paramString = paramString;
    }

    public Packet(Command command, int senderId, boolean paramBool) {
        this.command = command;
        this.senderId = senderId;
        this.paramBool = paramBool;
    }

    public Packet(Command command, int senderId, int paramInt) {
        this.command = command;
        this.senderId = senderId;
        this.paramInt = paramInt;
    }

    public Command getCommand() {
        return this.command;
    }

    public String getParameter() {
        return this.paramString;
    }

    public int getPlayerId() {
        return this.senderId;
    }

    public boolean getExtra() {
        return this.paramBool;
    }

    public int getProgress() {
        return this.paramInt;
    }

    public int getSenderId() {
        return this.senderId;
    }

    public void setAdditional(String value) {
        this.paramString = value;
    }

    public void setAdditional(int value) {
        this.paramInt = value;
    }

    public void setAdditional(boolean value) {
        this.paramBool = value;
    }

    public String getString() {
        return this.paramString;
    }

    public int getInt() {
        return this.paramInt;
    }

    public boolean getBool() {
        return this.paramBool;
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