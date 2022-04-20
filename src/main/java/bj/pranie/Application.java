package bj.pranie;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.UserDao;
import bj.pranie.dao.WashTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.WashTime;
import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.util.TimeUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Created by Sebastian Sokolowski on 13.09.17.
 */
@SpringBootApplication
public class Application {
    static Logger log = Logger.getLogger(Application.class.getName());

    public final static int RESET_TIME = 20;
    public final static int USER_TOKENS_PER_WEEK = 1;
    public final static int STUDENTS_LAST_ROOM = 45;
    public final static int SUNDAY_RESERVATIONS_AVAILABLE_FROM = 10;
    public final static int SUNDAY_RESERVATIONS_AVAILABLE_TO = 20;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private WashTimeDao washTimeDao;

    @Value("${wmCount}")
    private int wmCount;

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
                blockNotAvailableRegistrationsOnSunday();
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
            if (user.getRole() == UserRole.GROUP) {
                user.setTokens(user.getRoom().getPeoples());
            } else {
                user.setTokens(USER_TOKENS_PER_WEEK);
            }
        }

        userDao.save(iterable);
    }

    private void blockNotAvailableRegistrationsOnSunday() {
        List<User> admins = userDao.findByRole(UserRole.ADMIN);
        if (admins.isEmpty()) {
            log.info("blockNotAvailableRegistrationsOnSunday: no admin accounts have found!");
            return;
        }
        User user = admins.get(0);
        DateTime nextSunday = TimeUtil.getCalendar().plusWeeks(1);
        java.sql.Date nextSundayDate = new java.sql.Date(nextSunday.toDate().getTime());
        List<WashTime> washTimes = washTimeDao.findAllByOrderByIdAsc();
        for (WashTime washTime : washTimes) {
            if (washTime.getFromTime().getHours() <= SUNDAY_RESERVATIONS_AVAILABLE_FROM ||
                    washTime.getFromTime().getHours() >= SUNDAY_RESERVATIONS_AVAILABLE_TO) {
                for (int wmNumber = 0; wmNumber != wmCount; wmNumber++) {
                    Reservation reservation = new Reservation();
                    reservation.setDate(nextSundayDate);
                    reservation.setUser(user);
                    reservation.setWashTime(washTime);
                    reservation.setWm(wmNumber);
                    reservation.setType(ReservationType.BLOCKED);

                    reservationDao.save(reservation);
                }
            }
        }
    }
}
