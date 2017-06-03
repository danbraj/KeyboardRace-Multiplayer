import java.awt.*;

public enum Variety {
    HIDE_INPUT_CONTENT(     Debuff.INVISIBILITY,    'I', 3, Color.YELLOW),
    REVERSE_WORDS_IN_TEXT(  Debuff.REVERSE,         'R', 5, Color.GREEN),
    SHUFFLE_CHARS_IN_WORDS( Debuff.SHUFFLE,         'S', 7, Color.BLUE);

    private final Debuff debuff;
    private final char shortcut;
    private final int cost;
    private final Color color;

    private Variety(Debuff debuff, char shortcut, int cost, Color color) {
        this.debuff = debuff;
        this.shortcut = shortcut;
        this.cost = cost;
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

    public Color getColor() {
        return this.color;
    }
}