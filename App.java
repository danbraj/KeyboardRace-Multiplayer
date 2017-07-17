import java.awt.Color;

class App {
    public static final String VERSION = "v0.9.2";
    public static final String DEFAULT_PORT = "2345";
    public static final int MAX_PLAYERS = 4;
    public static final Color[] COLORS_OF_PLAYERS = {
        Color.decode("#6077e0"), // niebieski
        Color.decode("#ed094a"), // czerwony
        Color.decode("#4dbd02"), // zielony
        Color.decode("#e231e2"), // fioletowy
        Color.decode("#37c6c7"), // błękitny
        Color.decode("#ff8b17"), // pomaranczowy
        Color.decode("#c4e060")  // jasno zielony
    };

    protected int status = 0; // 1 - connected, 2 - running, 4 - started

    protected boolean checkStatusIfExistsFlag(int flag) {
        return (this.status & flag) == flag;
    }

    protected boolean checkStatusIfNotExistsFlag(int flag) {
        return (this.status & flag) != flag;
    }
}