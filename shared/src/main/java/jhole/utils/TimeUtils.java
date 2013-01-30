package jhole.utils;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class TimeUtils {
    public static String formatInterval(long millis) {
        StringBuilder s = new StringBuilder();
        s.append(format(millis / (1000 * 60 * 60 * 24), "d"));
        s.append(format((millis / (1000*60*60)) % 24, "h"));
        s.append(format((millis / (1000*60)) % 60, "m"));
        s.append(format((millis / (1000)) % 60, "s"));

        return s.toString().trim();
    }

    private static String format(long value, String unit) {
        if (value == 0) {
            return "";
        }
        return " "+value+unit;
    }
}
