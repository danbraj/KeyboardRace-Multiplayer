import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

class ServerApp extends App {
    
    public static final boolean ALLOWED_SENDING_TEXTS_BY_CLIENTS = true;
    public static final int MAX_TEXTS_COUNT_IN_QUEUE = 3;

    protected ArrayList<ServerConnection> clients = new ArrayList<ServerConnection>(App.MAX_PLAYERS);
    protected ArrayList<Player> leaderboard = new ArrayList<>(App.MAX_PLAYERS);
    protected int place = 0;
    protected int playersCount = 0;
    protected boolean sendingTextsEnabled = ALLOWED_SENDING_TEXTS_BY_CLIENTS;
    protected LinkedList<String> sendedTasks = new LinkedList<String>();
    protected AtomicInteger tasksCount = new AtomicInteger(MAX_TEXTS_COUNT_IN_QUEUE);

    private ServerApp() {
        new ServerGUI(this);
    }

    public static void main(String[] args) {
        new ServerApp();
    }
}