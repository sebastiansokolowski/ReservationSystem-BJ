package bj.pranie;

import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.service.UserServiceImpl;
import bj.pranie.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.Calendar;
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

    private final static int RESET_TIME = 10;

    @Autowired
    private UserServiceImpl userService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() {
        startTimerToResetUsersTokens();
    }

    private void startTimerToResetUsersTokens() {
        Calendar nowCalendar = TimeUtil.getCalendar();
        Calendar nextReset = getNextResetTime();

        long delay = nextReset.getTimeInMillis() - nowCalendar.getTimeInMillis();

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

    private Calendar getNextResetTime() {
        Calendar nowCalendar = TimeUtil.getCalendar();

        Calendar nextReset = TimeUtil.getCalendar();
        nextReset.set(Calendar.HOUR_OF_DAY, RESET_TIME);
        nextReset.set(Calendar.MINUTE, 0);
        nextReset.set(Calendar.SECOND, 0);


        if (nowCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY &&
                nowCalendar.get(Calendar.HOUR_OF_DAY) >= RESET_TIME ||
                nowCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            do {
                nextReset.add(Calendar.DAY_OF_WEEK, 1);
            } while (nextReset.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY);
        }

        return nextReset;
    }

    private void resetUsersTokens() {
        Iterable<User> iterable = userService.findAll();
        Iterator<User> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getRole() == UserRole.USER) {
                user.setTokens(2);
            }
        }

        userService.save(iterable);
    }
}
