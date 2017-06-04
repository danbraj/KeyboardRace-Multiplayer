import java.awt.Color;

public enum Variety {
    HIDE_INPUT_CONTENT(     Debuff.INVISIBILITY,    'I', 3, 10, Color.YELLOW),
    REVERSE_WORDS_IN_TEXT(  Debuff.REVERSE,         'R', 5, 25, Color.GREEN),
    SHUFFLE_CHARS_IN_WORDS( Debuff.SHUFFLE,         'S', 7, 25, Color.BLUE);

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