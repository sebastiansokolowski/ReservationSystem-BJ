package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.UserDao;
import bj.pranie.dao.WashTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.WashTime;
import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.model.TimeWeekModel;
import bj.pranie.util.ColorUtil;
import bj.pranie.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sebastian Sokolowski on 07.09.17.
 */
@Controller
@RequestMapping("/week")
public class WeekController {
    private static final int RESET_TIME = 10;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private WashTimeDao washTimeDao;

    @Autowired
    private UserDao userDao;

    @RequestMapping(method = RequestMethod.GET)
    public String week(Model model) throws ParseException {
        String weekId = getCurrentWeekId();

        setModel(weekId, model);
        return "wm/week";
    }

    @RequestMapping(path = "/{weekId}", method = RequestMethod.GET)
    public String week(@PathVariable String weekId, Model model) throws ParseException {
        setModel(weekId, model);
        return "wm/week";
    }

    @RequestMapping(path = "/{weekId}/unblock", method = RequestMethod.POST)
    public String unblockDay(@PathVariable String weekId,
                             @RequestParam String date,
                             Model model) throws ParseException {
        java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse(date).getTime());
        List<Reservation> reservations = reservationDao.findByDate(sqlDate);
        for (Reservation reservation : reservations) {
            if (reservation.getType() == ReservationType.BLOCKED) {
                reservationDao.delete(reservation.getId());
            }
        }

        setModel(weekId, model);
        return "wm/week";
    }

    @RequestMapping(path = "/{weekId}/block", method = RequestMethod.POST)
    public String blockDay(@PathVariable String weekId,
                           @RequestParam String date,
                           Model model) throws ParseException {
        User admin = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse(date).getTime());
        List<Reservation> reservations = reservationDao.findByDate(sqlDate);
        for (Reservation reservation : reservations) {
            reservationDao.delete(reservation.getId());

            User user = reservation.getUser();
            user.setTokens(user.getTokens() + 1);
            userDao.save(user);
        }

        Iterator<WashTime> washTimes = washTimeDao.findAll().iterator();
        while (washTimes.hasNext()) {
            WashTime washTime = washTimes.next();
            for (int i = 0; i != 3; i++) {
                makeReservation(admin, sqlDate, washTime.getId(), i, ReservationType.BLOCKED);
            }
        }

        setModel(weekId, model);
        return "wm/week";
    }

    // private

    private void makeReservation(User user, java.sql.Date date, long washTimeId, int wmNumber, ReservationType reservationType) {
        Reservation reservation = new Reservation();
        reservation.setDate(date);
        reservation.setUser(user);
        reservation.setWashTime(washTimeDao.findOne(washTimeId));
        reservation.setWm(wmNumber);
        reservation.setType(reservationType);

        reservationDao.save(reservation);
    }

    private enum WEEK_TYPE {
        PREV, NEXT
    }

    private void setModel(String weekId, Model model) {
        model.addAttribute("weekId", weekId);
        if (isAuthAdmin() || isBeforeCurrentWeekId(weekId)) {
            model.addAttribute("nextWeekId", getSpecificWeekId(weekId, WEEK_TYPE.NEXT));
        }
        model.addAttribute("prevWeekId", getSpecificWeekId(weekId, WEEK_TYPE.PREV));
        model.addAttribute("weekFrame", getWeekFrame(weekId));

        List<TimeWeekModel> timeWeekModels = getWeekReservations(weekId);
        model.addAttribute("wmFree", getWmFree(timeWeekModels));
        model.addAttribute("timesWeek", timeWeekModels);

        model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private boolean isAuthAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            if (user.getRole() == UserRole.ADMIN) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /*
    WeekId = year-weekOfYear
     */
    private String getCurrentWeekId() {
        Calendar calendar = TimeUtil.getCalendar();

        int time = calendar.get(Calendar.HOUR_OF_DAY);
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        if (today == Calendar.SUNDAY && time > RESET_TIME) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private boolean isBeforeCurrentWeekId(String weekId) {
        try {
            int year = Integer.parseInt(weekId.split("-")[0]);
            int week = Integer.parseInt(weekId.split("-")[1]);
            Calendar calendar = TimeUtil.getCalendar();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.WEEK_OF_YEAR, week);

            String currentWeekId = getCurrentWeekId();
            int currentYear = Integer.parseInt(currentWeekId.split("-")[0]);
            int currentWeek = Integer.parseInt(currentWeekId.split("-")[1]);
            Calendar currentCalendar = TimeUtil.getCalendar();
            currentCalendar.set(Calendar.YEAR, currentYear);
            currentCalendar.set(Calendar.WEEK_OF_YEAR, currentWeek);

            return calendar.before(currentCalendar);
        } catch (Exception e) {
            return false;
        }
    }

    private String getSpecificWeekId(String weekId, WEEK_TYPE week_type) {
        Calendar calendar = TimeUtil.getCalendar();

        int year = Integer.parseInt(weekId.split("-")[0]);
        int week = Integer.parseInt(weekId.split("-")[1]);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.WEEK_OF_YEAR, week);

        switch (week_type) {
            case NEXT:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case PREV:
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
        }

        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR);
    }

    private String getWeekFrame(String weekId) {
        Calendar calendar = TimeUtil.getCalendar();

        int year = Integer.parseInt(weekId.split("-")[0]);
        int weekOfTheYear = Integer.parseInt(weekId.split("-")[1]);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.WEEK_OF_YEAR, weekOfTheYear);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        String weekFrame = dateFormat.format(calendar.getTime()) + " - ";

        calendar.add(Calendar.DAY_OF_MONTH, 6);

        weekFrame += dateFormat.format(calendar.getTime());

        return weekFrame;
    }

    public List<TimeWeekModel> getWeekReservations(String weekId) {
        List<TimeWeekModel> timeWeekModels = new ArrayList<>();

        List<String> weekDays = getWeekDays(weekId);
        List<WashTime> washTimes = getWashTimes();

        for (WashTime washTime : washTimes
                ) {
            TimeWeekModel timeWeekModel = new TimeWeekModel();

            String fromTime = timeFormat.format(washTime.getFromTime());
            String toTime = timeFormat.format(washTime.getToTime());
            timeWeekModel.setTime(fromTime + " - " + toTime);

            List<TimeWeekModel.WmDate> wmDates = new ArrayList<>();
            for (String date : weekDays) {
                TimeWeekModel.WmDate wmDate = timeWeekModel.new WmDate();
                wmDate.setDate(date);

                boolean isPast = TimeUtil.isPast(fromTime, date);

                List<Reservation> reservations = getWmFree(washTime.getId(), date);

                int wmFree = 3;
                if (isPast) {
                    wmFree = 0;
                } else {
                    wmFree -= reservations.size();
                }
                wmDate.setWmFree(wmFree);

                wmDate.setColor(getCellColor(wmFree, isPast, isMyReservation(reservations)));

                wmDates.add(wmDate);
            }
            timeWeekModel.setDates(wmDates);

            timeWeekModels.add(timeWeekModel);
        }

        return timeWeekModels;
    }

    private List<WashTime> getWashTimes() {
        List<WashTime> washTimes = new ArrayList<>();
        Iterator<WashTime> washTimeIterator = washTimeDao.findAll().iterator();
        while (washTimeIterator.hasNext())
            washTimes.add(washTimeIterator.next());

        return washTimes;
    }

    private boolean isMyReservation(List<Reservation> reservationUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            for (Reservation reservation : reservationUser) {
                if (currentUser.getId() == reservation.getUser().getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getWeekDays(String weekId) {
        List<String> daysOfWeek = new ArrayList<>();

        int year = Integer.parseInt(weekId.split("-")[0]);
        int weekOfYear = Integer.parseInt(weekId.split("-")[1]);

        Calendar now = TimeUtil.getCalendar();
        now.set(Calendar.WEEK_OF_YEAR, weekOfYear);
        now.set(Calendar.YEAR, year);
        now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 6; i++) {
            daysOfWeek.add(dateFormat.format(now.getTime()));
            now.add(Calendar.DAY_OF_MONTH, 1);
        }

        return daysOfWeek;
    }

    private List<Reservation> getWmFree(long washTimeId, String date) {
        java.sql.Date sqlDate = null;
        try {
            sqlDate = new java.sql.Date(dateFormat.parse(date).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return reservationDao.findByWashTimeIdAndDate(washTimeId, sqlDate);
    }

    private int getWmFree(List<TimeWeekModel> timeWeekModels) {
        int freeWm = 0;
        for (TimeWeekModel timeWeekModel : timeWeekModels) {
            for (TimeWeekModel.WmDate wmDate : timeWeekModel.getDates()) {
                freeWm += wmDate.getWmFree();
            }
        }
        return freeWm;
    }

    private String getCellColor(int freeSpace, boolean past, boolean myReservation) {
        if (past) {
            return ColorUtil.RESERVATION_UNAVAILABLE_COLOR;
        } else if (myReservation) {
            return ColorUtil.RESERVATION_MY_COLOR;
        }

        switch (freeSpace) {
            case 3:
                return ColorUtil.RESERVATION_FREE_COLOR;
            case 2:
                return ColorUtil.RESERVATION_TWO_FREE_COLOR;
            case 1:
                return ColorUtil.RESERVATION_ONE_FREE_COLOR;
            default:
                return ColorUtil.RESERVATION_UNAVAILABLE_COLOR;
        }
    }
}

