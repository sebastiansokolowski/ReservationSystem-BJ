package bj.pranie.controller.week;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.WashTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.WashTime;
import bj.pranie.model.TimeWeekModel;
import bj.pranie.service.UserAuthenticatedService;
import bj.pranie.util.ColorUtil;
import bj.pranie.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sebastian Sokolowski on 22.10.17.
 */
public class BaseWeekController {
    private static final int RESET_TIME = 10;

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Autowired
    ReservationDao reservationDao;

    @Autowired
    WashTimeDao washTimeDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    void setModel(String weekId, Model model) throws ParseException {
        model.addAttribute("weekId", weekId);
        model.addAttribute("weekFrame", getWeekFrame(weekId));

        List<TimeWeekModel> timeWeekModels = getTimeWeekModels(weekId);
        model.addAttribute("wmFree", getWmFree(timeWeekModels));
        model.addAttribute("timesWeek", timeWeekModels);
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());
    }

    /*
    WeekId = year-weekOfYear
    */
    String getCurrentWeekId() {
        Calendar calendar = TimeUtil.getCalendar();

        int time = calendar.get(Calendar.HOUR_OF_DAY);
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        if (today == Calendar.SUNDAY && time >= RESET_TIME) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.WEEK_OF_YEAR);
    }

    String getWeekFrame(String weekId) {
        Calendar calendar = getCalendar(weekId);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        String weekFrame = dateFormat.format(calendar.getTime()) + " - ";

        calendar.add(Calendar.DAY_OF_MONTH, 6);

        weekFrame += dateFormat.format(calendar.getTime());

        return weekFrame;
    }

    Calendar getCalendar(String weekId) {
        int year = Integer.parseInt(weekId.split("-")[0]);
        int weekOfYear = Integer.parseInt(weekId.split("-")[1]);

        Calendar calendar = TimeUtil.getCalendar();
        calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear);
        calendar.set(Calendar.YEAR, year);
        return calendar;
    }

    enum WEEK_TYPE {
        PREV, NEXT
    }

    String getSpecificWeekId(String weekId, UserWeekController.WEEK_TYPE week_type) {
        Calendar calendar = getCalendar(weekId);

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

    List<TimeWeekModel> getTimeWeekModels(String weekId) throws ParseException {
        final List<TimeWeekModel> timeWeekModels = new ArrayList<>();

        List<String> weekDays = getWeekDays(weekId);

        Iterator<WashTime> washTimeIterator = washTimeDao.findAll().iterator();
        while (washTimeIterator.hasNext()) {
            WashTime washTime = washTimeIterator.next();

            String fromTime = timeFormat.format(washTime.getFromTime());
            String toTime = timeFormat.format(washTime.getToTime());

            TimeWeekModel timeWeekModel = new TimeWeekModel();
            timeWeekModel.setTime(fromTime + " - " + toTime);

            List<TimeWeekModel.WmDate> wmDates = new ArrayList<>();
            for (String date : weekDays) {
                TimeWeekModel.WmDate wmDate = timeWeekModel.new WmDate();
                wmDate.setDate(date);

                boolean isPast = TimeUtil.isPast(fromTime, date);

                List<Reservation> reservations = getReservationsByWashTimeAndDate(washTime.getId(), date);

                int wmFree = 3;
                if (isPast) {
                    wmFree = 0;
                } else {
                    wmFree -= reservations.size();
                }
                wmDate.setWmFree(wmFree);
                wmDate.setColor(getCellColor(wmFree, isPast, isUserAuthenticatedReservation(reservations)));

                wmDates.add(wmDate);
            }
            timeWeekModel.setDates(wmDates);

            timeWeekModels.add(timeWeekModel);
        }

        return timeWeekModels;
    }

    List<String> getWeekDays(String weekId) {
        List<String> daysOfWeek = new ArrayList<>();

        Calendar now = getCalendar(weekId);
        now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 6; i++) {
            daysOfWeek.add(dateFormat.format(now.getTime()));
            now.add(Calendar.DAY_OF_MONTH, 1);
        }

        return daysOfWeek;
    }

    List<Reservation> getReservationsByWashTimeAndDate(long washTimeId, String date) throws ParseException {
        java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse(date).getTime());
        return reservationDao.findByWashTimeIdAndDate(washTimeId, sqlDate);
    }

    boolean isUserAuthenticatedReservation(List<Reservation> reservationUser) {
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

    int getWmFree(List<TimeWeekModel> timeWeekModels) {
        int freeWm = 0;
        for (TimeWeekModel timeWeekModel : timeWeekModels) {
            for (TimeWeekModel.WmDate wmDate : timeWeekModel.getDates()) {
                freeWm += wmDate.getWmFree();
            }
        }
        return freeWm;
    }

    String getCellColor(int freeSpace, boolean past, boolean myReservation) {
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
