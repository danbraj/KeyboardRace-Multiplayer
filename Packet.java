import java.io.Serializable;

public class Packet implements Serializable {

    Command command;
    int senderId = -1;
    String paramString;
    boolean paramBool;
    int paramInt;
    Debuff debuff;

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

    public Packet(Command command, Debuff debuff, int paramInt) {
        this.command = command;
        this.debuff = debuff;
        this.paramInt = paramInt;
    }

    public Command getCommand() {
        return this.command;
    }

    public Debuff getDebuff() {
        return this.debuff;
    }

    public int getPlayerId() {
        return this.senderId;
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