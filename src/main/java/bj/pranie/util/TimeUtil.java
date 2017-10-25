package bj.pranie.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Sebastian Sokolowski on 03.02.17.
 */
public class TimeUtil {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private static TimeZone timeZone = TimeZone.getTimeZone("Europe/Warsaw");

    public static Calendar getCalendar(){
        Calendar calendar = Calendar.getInstance(timeZone);
        return calendar;
    }

    public static boolean isPast(String time, String date) {
        Calendar now = TimeUtil.getCalendar();

        Calendar calendar = TimeUtil.getCalendar();
        try {
            calendar.setTime(format.parse(date + " " + time));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        if (now.before(calendar)) {
            return false;
        } else {
            return true;
        }
    }
}
