class ClientApp {

    protected int status = 0; // 0 - not connected, 1 - connected
    protected Client client;
    protected int playerId;
    protected Zadanie zadanie; // <- typedChars?
    protected int typedChars = 0;

    private ClientApp() {
        new ClientGUI(this);
    }

    public static void main(String[] args) {
        if (Consts.MAX_PLAYERS <= Consts.COLORS_OF_PLAYERS.length)
            new ClientApp();
        else
            System.exit(2);
    }

    protected boolean checkStatusIfExistsFlag(int flag) {
        return (this.status & flag) == flag;
    }

    protected boolean checkStatusIfNotExistsFlag(int flag) {
        return (this.status & flag) != flag;
    }
}