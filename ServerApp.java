import Models.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerApp {

    protected int status = 0; // 1 - connected, 2 - running, 4 - started
    protected ArrayList<Connection> clients = new ArrayList<Connection>(Consts.MAX_PLAYERS);
    protected ArrayList<Player> leaderboard = new ArrayList<>(Consts.MAX_PLAYERS);
    protected int place = 0;
    protected int playersCount = 0;
    protected boolean sendingTextsEnabled = Consts.ALLOWED_SENDING_TEXTS_BY_CLIENTS;
    protected LinkedList<String> sendedTasks = new LinkedList<String>();
    protected AtomicInteger tasksCount = new AtomicInteger(Consts.MAX_TEXTS_COUNT_IN_QUEUE);

    private ServerApp() {
        new ServerGUI(this);
    }

    public static void main(String[] args) {
        new ServerApp();
    }

    protected boolean checkStatusIfExistsFlag(int flag) {
        return (this.status & flag) == flag;
    }

    protected boolean checkStatusIfNotExistsFlag(int flag) {
        return (this.status & flag) != flag;
    }
}