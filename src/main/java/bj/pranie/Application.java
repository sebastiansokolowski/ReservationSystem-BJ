package bj.pranie;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Sebastian Sokolowski on 13.09.17.
 */
@SpringBootApplication
public class Application {
    static Logger log = Logger.getLogger(Application.class.getName());

    private final static int RESET_TIME = 10;

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
        Calendar nowCalendar = TimeUtil.getCalendar();

        Calendar nextReset = TimeUtil.getCalendar();
        nextReset.set(Calendar.HOUR_OF_DAY, RESET_TIME);
        nextReset.set(Calendar.MINUTE, 0);
        nextReset.set(Calendar.SECOND, 0);

        while (nextReset.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            nextReset.add(Calendar.DAY_OF_WEEK, 1);
        }
        long delay = nextReset.getTimeInMillis() - nowCalendar.getTimeInMillis();

        float hourDelay = (delay / (float) (1000 * 60 * 60 * 24));
        log.info("Delay to start reset users tokens: " + hourDelay + " days.");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                resetUsersTokens();
            }
        }, delay);
    }

    private void resetUsersTokens() {
        Iterable<User> iterable = userDao.findAll();
        Iterator<User> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getRole() == UserRole.USER) {
                user.setTokens(2);
            }
        }

        userDao.save(iterable);
    }
}
