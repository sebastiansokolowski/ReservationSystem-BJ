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
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Sebastian Sokolowski on 22.10.17.
 */
public class BaseWeekController {
    private static final int RESET_TIME = 10;

    static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd");
    static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Autowired
    ReservationDao reservationDao;

    @Autowired
    WashTimeDao washTimeDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @Value("${wmCount}")
    int wmCount;

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
        DateTime dateTime = TimeUtil.getCalendar();

        int time = dateTime.getHourOfDay();
        int today = dateTime.getDayOfWeek();
        if (today == DateTimeConstants.SUNDAY && time >= RESET_TIME) {
            dateTime = dateTime.plusWeeks(1);
        }

        return dateTime.getYear() + "-" + dateTime.getWeekOfWeekyear();
    }

    String getWeekFrame(String weekId) {
        LocalDate localDate = getLocalDateByWeekId(weekId).withDayOfWeek(DateTimeConstants.MONDAY);

        return localDate.toString(dateFormat) + " - " + localDate.plusDays(6).toString(dateFormat);
    }

    LocalDate getLocalDateByWeekId(String weekId) {
        int year = Integer.parseInt(weekId.split("-")[0]);
        int weekOfYear = Integer.parseInt(weekId.split("-")[1]);

        //todo: improve ugly fast fix
        return new LocalDate().withDayOfWeek(DateTimeConstants.MONDAY).withWeekOfWeekyear(weekOfYear).withYear(year).
                withDayOfWeek(DateTimeConstants.MONDAY);
    }

    enum WEEK_TYPE {
        PREV, NEXT
    }

    String getSpecificWeekId(String weekId, UserWeekController.WEEK_TYPE week_type) {
        LocalDate localDate = getLocalDateByWeekId(weekId);

        switch (week_type) {
            case NEXT:
                localDate = localDate.plusWeeks(1);
                break;
            case PREV:
                localDate = localDate.minusWeeks(1);
                break;
        }

        return localDate.getYear() + "-" + localDate.getWeekOfWeekyear();
    }

    List<TimeWeekModel> getTimeWeekModels(String weekId) throws ParseException {
        final List<TimeWeekModel> timeWeekModels = new ArrayList<>();

        List<LocalDate> weekDays = getWeekDays(weekId);

        Iterator<WashTime> washTimeIterator = washTimeDao.findAll().iterator();
        while (washTimeIterator.hasNext()) {
            WashTime washTime = washTimeIterator.next();

            TimeWeekModel timeWeekModel = new TimeWeekModel();
            timeWeekModel.setTime(timeFormat.format(washTime.getFromTime()) + " - " + timeFormat.format(washTime.getToTime()));

            List<TimeWeekModel.WmDate> wmDates = new ArrayList<>();
            for (LocalDate localDate : weekDays) {
                TimeWeekModel.WmDate wmDate = timeWeekModel.new WmDate();
                wmDate.setDate(localDate.toString(dateFormat));

                boolean isPast = TimeUtil.isPast(washTime.getFromTime(), localDate);

                List<Reservation> reservations = getReservationsByWashTimeAndDate(washTime.getId(), localDate);

                int wmFree = wmCount;
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

    List<LocalDate> getWeekDays(String weekId) {
        List<LocalDate> daysOfWeek = new ArrayList<>();

        LocalDate localDate = getLocalDateByWeekId(weekId).withDayOfWeek(DateTimeConstants.MONDAY);

        for (int i = 0; i < 6; i++) {
            daysOfWeek.add(localDate.plusDays(i));
        }

        return daysOfWeek;
    }

    List<Reservation> getReservationsByWashTimeAndDate(long washTimeId, LocalDate date) throws ParseException {
        java.sql.Date sqlDate = new java.sql.Date(date.toDate().getTime());
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

        if (freeSpace > 2 && freeSpace <= wmCount) {
            return ColorUtil.RESERVATION_FREE_COLOR;
        }

        switch (freeSpace) {
            case 2:
                return ColorUtil.RESERVATION_TWO_FREE_COLOR;
            case 1:
                return ColorUtil.RESERVATION_ONE_FREE_COLOR;
            default:
                return ColorUtil.RESERVATION_UNAVAILABLE_COLOR;
        }
    }
}
