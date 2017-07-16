import java.util.ArrayList;

public class ExtendedPacket extends Packet {

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