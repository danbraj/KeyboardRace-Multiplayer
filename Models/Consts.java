package Models;

import java.awt.Color;

public class Consts {
    public static final String VERSION = "v0.9.1";
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
    public static final int MAX_TEXTS_COUNT_IN_QUEUE = 3;
    public static final boolean ALLOWED_SENDING_TEXTS_BY_CLIENTS = true;

    public static final int CONNECTED = 1; // 0001 (flags)
    public static final int RUNNING   = 2; // 0010
    public static final int STARTED   = 4; // 0100
}