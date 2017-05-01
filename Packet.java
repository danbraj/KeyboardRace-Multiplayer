import java.io.Serializable;
import java.util.ArrayList;

class Packet implements Serializable {

    Command command;
    String parameter;
    int senderId = -1;
    
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

    public Command getCommand() {
        return this.command;
    }

    public String getParameter() {
        return this.parameter;
    }

    public int getPlayerId() {
        return this.senderId;
    }
}

class PacketWithPlayersList extends Packet {
    ArrayList<Player> players;

    public PacketWithPlayersList(ArrayList<Player> players) {
        super(Command.UPDATE_PLAYERS_LIST);
        this.players = players;
    }

    public ArrayList<Player> getPlayers() {
        return this.players;
    }
}