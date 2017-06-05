package Models;

import java.awt.Color;

public enum Variety {
    HIDE_INPUT_CONTENT(     Debuff.INVISIBILITY,    'I', 4,  10, Color.decode("#FFD64D")),   // żółty
    REVERSE_WORDS_IN_TEXT(  Debuff.REVERSE,         'R', 8,  20, Color.decode("#578CB5")),   // niebieski
    SHUFFLE_CHARS_IN_WORDS( Debuff.SHUFFLE,         'S', 10, 25, Color.decode("#4FA833"));   // zielony

    private final Debuff debuff;
    private final char shortcut;
    private final int cost;
    private final int durationTime;
    private final Color color;

    private Variety(Debuff debuff, char shortcut, int cost, int durationTime, Color color) {
        this.debuff = debuff;
        this.shortcut = shortcut;
        this.cost = cost;
        this.durationTime = durationTime;
        this.color = color;
    }

    public Debuff getDebuff(){
        return this.debuff;
    }

    public char getShortcut() {
        return this.shortcut;
    }

    public int getCost() {
        return this.cost;
    }

    public int getDurationTime() {
        return this.durationTime;
    }

    public Color getColor() {
        return this.color;
    }
}