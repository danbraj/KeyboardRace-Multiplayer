public class Common {

    public static boolean isStatusContainsFlag(byte flag) {
        return (App.STATUS & flag) == flag;
    }

    public static int percentage(int part, int full) {
        return (int) Math.floor(((double) part / full) * 100);
    }
}