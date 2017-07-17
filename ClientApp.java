class ClientApp extends App {

    public static final String DEFAULT_HOSTNAME = "localhost:" + App.DEFAULT_PORT;

    protected Client client;
    protected int playerId;
    protected Zadanie zadanie; // <- typedChars?
    protected int typedChars = 0;

    private ClientApp() {
        new ClientGUI(this);
    }

    public static void main(String[] args) {
        if (App.MAX_PLAYERS <= App.COLORS_OF_PLAYERS.length)
            new ClientApp();
        else
            System.exit(2);
    }
}