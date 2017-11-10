package bj.pranie.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

/**
 * Created by Sebastian Sokolowski on 03.02.17.
 */
public class TimeUtil {
    static Logger log = Logger.getLogger(TimeUtil.class.getName());

    private static DateTimeZone zone = DateTimeZone.forID("Europe/Warsaw");

    public static DateTime getCalendar() {
        DateTime dateTime = new DateTime(zone);
        return dateTime;
    }

    public static boolean isPast(Time time, LocalDate date) {
        DateTime now = getCalendar();

        DateTime dateTime = new DateTime();
        dateTime = dateTime.withTime(new LocalTime(time.getTime()));
        dateTime = dateTime.withDate(date);

        return dateTime.isBefore(now.toInstant());
    }
}
