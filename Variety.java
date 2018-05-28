import java.awt.Color;

public enum Variety {
    HIDE_INPUT_CONTENT(     Debuff.INVISIBILITY,    'I', 4,  10, "#FFD64D"),   // żółty
    REVERSE_WORDS_IN_TEXT(  Debuff.REVERSE,         'R', 8,  20, "#578CB5"),   // niebieski
    SHUFFLE_CHARS_IN_WORDS( Debuff.SHUFFLE,         'S', 10, 25, "#4FA833");   // zielony

    private final Debuff debuff;
    private final char shortcut;
    private final int cost;
    private final int durationTime;
    private final String colorCode;

    private Variety(Debuff debuff, char shortcut, int cost, int durationTime, String colorCode) {
        this.debuff = debuff;
        this.shortcut = shortcut;
        this.cost = cost;
        this.durationTime = durationTime;
        this.colorCode = colorCode;
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
        return Color.decode(this.colorCode);
    }
}