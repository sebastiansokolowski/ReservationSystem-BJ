package bj.pranie.controller.week;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.ReservationTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.ReservationTime;
import bj.pranie.entity.myEnum.DeviceType;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Sebastian Sokolowski on 22.10.17.
 */
public abstract class BaseWeekController {
    private static final int RESET_TIME = 20;

    static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd");
    static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Autowired
    ReservationDao reservationDao;

    @Autowired
    ReservationTimeDao reservationTimeDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    public abstract DeviceType getDeviceType();

    public abstract int getDevicesCount();

    String getWeekView() {
        return "week/" + getDeviceType().getPathName();
    }

    public void setModel(String weekId, Model model) throws ParseException {
        model.addAttribute("weekId", weekId);
        model.addAttribute("weekFrame", getWeekFrame(weekId));

        List<TimeWeekModel> timeWeekModels = getTimeWeekModels(weekId);
        model.addAttribute("freeDevices", getFreeDevices(timeWeekModels));
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

        return dateTime.getWeekyear() + "-" + dateTime.getWeekOfWeekyear();
    }

    String getWeekFrame(String weekId) {
        LocalDate localDate = getLocalDateByWeekId(weekId).withDayOfWeek(DateTimeConstants.MONDAY);

        return localDate.toString(dateFormat) + " - " + localDate.plusDays(6).toString(dateFormat);
    }

    LocalDate getLocalDateByWeekId(String weekId) {
        int year = Integer.parseInt(weekId.split("-")[0]);
        int weekOfYear = Integer.parseInt(weekId.split("-")[1]);

        return new LocalDate().withWeekyear(year).withWeekOfWeekyear(weekOfYear).withDayOfWeek(DateTimeConstants.MONDAY);
    }

    enum WEEK_TYPE {
        PREV, NEXT
    }

    String getSpecificWeekId(String weekId, BaseUserWeekController.WEEK_TYPE week_type) {
        LocalDate localDate = getLocalDateByWeekId(weekId);

        switch (week_type) {
            case NEXT:
                localDate = localDate.plusWeeks(1);
                break;
            case PREV:
                localDate = localDate.minusWeeks(1);
                break;
        }

        return localDate.getWeekyear() + "-" + localDate.getWeekOfWeekyear();
    }

    List<TimeWeekModel> getTimeWeekModels(String weekId) throws ParseException {
        final List<TimeWeekModel> timeWeekModels = new ArrayList<>();

        List<LocalDate> weekDays = getWeekDays(weekId);

        for (ReservationTime reservationTime : reservationTimeDao.findAll()) {
            TimeWeekModel timeWeekModel = new TimeWeekModel();
            timeWeekModel.setTime(timeFormat.format(reservationTime.getFromTime()) + " - " + timeFormat.format(reservationTime.getToTime()));

            List<TimeWeekModel.Date> dates = new ArrayList<>();
            for (LocalDate localDate : weekDays) {
                TimeWeekModel.Date date = timeWeekModel.new Date();
                date.setDate(localDate.toString(dateFormat));

                boolean isPast = TimeUtil.isPast(reservationTime.getFromTime(), localDate);

                List<Reservation> reservations = getReservationsByReservationTimeAndDate(reservationTime.getId(), localDate, getDeviceType());

                int freeDevices = getDevicesCount();
                if (isPast) {
                    freeDevices = 0;
                } else {
                    freeDevices -= reservations.size();
                }
                date.setFreeDevices(freeDevices);
                date.setColor(getCellColor(freeDevices, isPast, isUserAuthenticatedReservation(reservations)));

                dates.add(date);
            }
            timeWeekModel.setDates(dates);

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

    List<Reservation> getReservationsByReservationTimeAndDate(long reservationTimeId, LocalDate date, DeviceType deviceType) throws ParseException {
        java.sql.Date sqlDate = new java.sql.Date(date.toDate().getTime());
        return reservationDao.findByReservationTimeIdAndDateAndDeviceType(reservationTimeId, sqlDate, deviceType);
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

    int getFreeDevices(List<TimeWeekModel> timeWeekModels) {
        int freeDevices = 0;
        for (TimeWeekModel timeWeekModel : timeWeekModels) {
            for (TimeWeekModel.Date date : timeWeekModel.getDates()) {
                freeDevices += date.getFreeDevices();
            }
        }
        return freeDevices;
    }

    String getCellColor(int freeSpace, boolean past, boolean myReservation) {
        if (past) {
            return ColorUtil.RESERVATION_UNAVAILABLE_COLOR;
        } else if (myReservation) {
            return ColorUtil.RESERVATION_MY_COLOR;
        }

        if (freeSpace > 2 && freeSpace <= getDevicesCount()) {
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
