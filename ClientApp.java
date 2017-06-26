import Models.*;

class ClientApp {

    private String hostname = "localhost:2345";
    private int status = 0; // 0 - not connected, 1 - connected
    private int playerId;
    private Zadanie zadanie;
    private int typedChars = 0;

    public static void main(String[] args) {
        if (Consts.MAX_PLAYERS <= Consts.COLORS_OF_PLAYERS.length)
            new ClientGUI();
        else
            System.exit(2);
    }
}