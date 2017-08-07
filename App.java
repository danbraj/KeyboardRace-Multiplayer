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

    /**
     * Status aplikacji
     */
    public static byte STATUS = 0;

    /**
     * Metoda sprawdzająca czy zmienna statyczna <code>App.STATUS</code>
     * zawiera flagę podaną jako parametr
     */
    public static boolean isStateContains(byte flag) {
        return (App.STATUS & flag) == flag;
    }

    /**
     * Klasa zawierająca wszystkie możliwe wartości flag, które można przypisać
     * do zmiennej statycznej <code>App.STATUS</code>
     */
    public static class State {
        public static final byte CONNECTED = 1;  // 0001 (flags)
        public static final byte RUNNING = 2;    // 0010
        public static final byte STARTED = 4;    // 0100
    }
}