class ClientApp extends App {

    public static final String DEFAULT_HOSTNAME = "localhost:" + App.DEFAULT_PORT;
    public static final String[] COLOR_CODES_OF_PLAYERS = { "#6077e0", "#ed094a", "#4dbd02", "#e231e2", "#37c6c7",
            "#ff8b17", "#c4e060" }; // colors: niebieski, czerwony, zielony, fioletowy, błękitny, pomaranczowy,
                                    // jasno zielony

    protected ClientConnection client;
    protected int playerId;
    protected Zadanie zadanie; // <- typedChars?
    protected int typedChars = 0;

    private ClientApp() {
        new ClientGUI(this);
    }

    public static void main(String[] args) {
        if (App.MAX_PLAYERS <= COLOR_CODES_OF_PLAYERS.length)
            new ClientApp();
        else
            System.exit(2);
    }
}