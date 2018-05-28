public class Common {

    public static boolean isStatusContainsFlag(byte flag) {
        return (App.STATUS & flag) == flag;
    }
}