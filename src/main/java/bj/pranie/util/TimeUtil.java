package bj.pranie.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by noon on 03.02.17.
 */
public class TimeUtil {
    private static TimeZone timeZone = TimeZone.getTimeZone("Europe/Warsaw");
    private static Calendar calendar = Calendar.getInstance(timeZone);

    public static String getTime() {
        return calendar.get(Calendar.HOUR_OF_DAY) + ":"+ calendar.get(Calendar.MINUTE);
    }
}
