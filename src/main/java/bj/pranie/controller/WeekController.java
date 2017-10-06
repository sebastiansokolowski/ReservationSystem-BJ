package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.WashTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.WashTime;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.model.TimeWeekModel;
import bj.pranie.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    // private

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
        model.addAttribute("wmFree", getWmFree(weekId));
        model.addAttribute("timesWeek", getWeekReservations(weekId));
        model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private boolean isAuthAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof User) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(new Date());
        int time = calendar.get(Calendar.HOUR_OF_DAY);
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        if (today == Calendar.SUNDAY && time > RESET_TIME) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        } else {
            calendar.add(Calendar.DAY_OF_WEEK, -today + Calendar.MONDAY);
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

    boolean isMyReservation(List<Reservation> reservations) {
        //TODO:
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

    private Long getWmFree(String weekId) {
        Long wmFree = washTimeDao.count() * 6 * 3;
        String weekFrame = getWeekFrame(weekId);
        java.sql.Date fromDate = new java.sql.Date(TimeUtil.getCalendar().getTime().getTime());
        java.sql.Date toDate = null;
        try {
            toDate = new java.sql.Date(dateFormat.parse(weekFrame.split("-")[1]).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        wmFree -= reservationDao.countByDatesBetween(fromDate, toDate);

        return wmFree;
    }

    private String getCellColor(int freeSpace, boolean past, boolean myReservation) {
        if (past) {
            return "#FF0000";
        } else if (myReservation) {
            return "#FFF200";
        }

        switch (freeSpace) {
            case 3:
                return "#1E9600";
            case 2:
                return "#408000";
            case 1:
                return "#2C5900";
            default:
                return "#FF0000";
        }
    }
}

