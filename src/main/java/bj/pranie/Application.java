package bj.pranie;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by Sebastian Sokolowski on 13.09.17.
 */
@SpringBootApplication
public class Application {
    static Logger log = Logger.getLogger(Application.class.getName());

    public final static int RESET_TIME = 10;
    public final static int USER_TOKENS_PER_WEEK = 1;
    public final static int STUDENTS_LAST_ROOM = 45;
    public final static boolean HOLIDAYS = true;

    @Autowired
    private UserDao userDao;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() {
        startTimerToResetUsersTokens();
    }

    private void startTimerToResetUsersTokens() {
        DateTime nowCalendar = TimeUtil.getCalendar();
        DateTime nextReset = getNextResetTime();

        long delay = nextReset.getMillis() - nowCalendar.getMillis();

        float hourDelay = (delay / (float) (1000 * 60 * 60 * 24));
        log.info("Delay to start reset users tokens: " + hourDelay + " days.");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                resetUsersTokens();

                startTimerToResetUsersTokens();
            }
        }, delay);
    }

    private DateTime getNextResetTime() {
        DateTime now = TimeUtil.getCalendar();

        DateTime nextReset = TimeUtil.getCalendar()
                .withHourOfDay(RESET_TIME)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0);

        int sunday = DateTimeConstants.SUNDAY;

        if (sunday > now.getDayOfWeek()) {
            nextReset = nextReset.plusDays(sunday - now.getDayOfWeek());
        } else if (sunday == now.getDayOfWeek() && RESET_TIME <= now.getHourOfDay()) {
            nextReset = nextReset.plusWeeks(1);
        }

        return nextReset;
    }

    private void resetUsersTokens() {
        Iterable<User> iterable = userDao.findAll();
        Iterator<User> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getRoom() != null && user.getRole() == UserRole.USER &&
                    HOLIDAYS && user.getRoom().getRoom() <= STUDENTS_LAST_ROOM) {
                user.setTokens(user.getRoom().getPeoples());
            } else {
                user.setTokens(USER_TOKENS_PER_WEEK);
            }
        }

        userDao.save(iterable);
    }
}
