package bj.pranie;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by Sebastian Sokolowski on 13.09.17.
 */
@SpringBootApplication
public class Application {
    static Logger log = Logger.getLogger(Application.class.getName());

    @Autowired
    private UserDao userDao;

    @Value("${resetTime}")
    int resetTime;

    @Value("${tokensPerWeek}")
    int tokensPerWeek;

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
                .withHourOfDay(resetTime)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0);

        int sunday = DateTimeConstants.SUNDAY;

        if (sunday > now.getDayOfWeek()) {
            nextReset = nextReset.plusDays(sunday - now.getDayOfWeek());
        } else if (sunday == now.getDayOfWeek() && resetTime <= now.getHourOfDay()) {
            nextReset = nextReset.plusWeeks(1);
        }

        return nextReset;
    }

    private void resetUsersTokens() {
        Iterable<User> iterable = userDao.findAll();
        for (User user : iterable) {
            if (user.getRole() == UserRole.GROUP) {
                user.setTokens(user.getRoom().getPeoples());
            } else {
                user.setTokens(tokensPerWeek);
            }
        }

        userDao.save(iterable);
    }
}
