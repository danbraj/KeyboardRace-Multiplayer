import java.awt.Color;

public class Common {

    public static boolean isStatusContainsFlag(byte flag) {
        return (App.STATUS & flag) == flag;
    }

    public static int percentage(int part, int full) {
        return (int) Math.floor(((double) part / full) * 100);
    }

    public static Color bleach(Color c, float amount)
    {
        int r = (int) ((c.getRed() * (1 - amount) / 255 + amount) * 255);
        int g = (int) ((c.getGreen() * (1 - amount) / 255 + amount) * 255);
        int b = (int) ((c.getBlue() * (1 - amount) / 255 + amount) * 255);
        return new Color(r, g, b);
    }
}