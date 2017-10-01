package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.WashTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.WashTime;
import bj.pranie.model.WmModel;
import bj.pranie.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Sebastian Sokolowski on 12.10.16.
 */
@Controller
@RequestMapping(value = "/wm")
public class WmController {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private WashTimeDao washTimeDao;

    @RequestMapping(path = "/{year}/{month}/{day}/{washTimeId}", method = RequestMethod.GET)
    public String wm(@PathVariable int year,
                     @PathVariable int month,
                     @PathVariable int day,
                     @PathVariable long washTimeId,
                     Model model) throws ParseException {
        Date date = dateFormat.parse(year + "/" + month + "/" + day);

        WashTime washTime = washTimeDao.findOne(washTimeId);
        List<Reservation> reservationList = reservationDao.findByWashTimeIdAndDate(washTimeId, new java.sql.Date(date.getTime()));
        int wmFree = 3 - reservationList.size();

        model.addAttribute("dayName", getDayName(date));
        model.addAttribute("date", dateFormat.format(date));
        model.addAttribute("time", getWashTime(washTime));
        model.addAttribute("wmFree", wmFree);
        model.addAttribute("reservations", getWmModels(reservationList, date, washTime));
        model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return "wm/wm";
    }

    private String getWashTime(WashTime washTime) {
        return timeFormat.format(washTime.getFromTime()) + " - " + timeFormat.format(washTime.getToTime());
    }

    private List<WmModel> getWmModels(List<Reservation> reservationList, Date date, WashTime washTime) {
        List<WmModel> wmModels = new ArrayList<>();

        List<Integer> brokenWm = getBrokenWm();
        boolean isPast = TimeUtil.isPast(timeFormat.format(washTime.getFromTime()), dateFormat.format(date));

        for (int i = 0; i != 3; i++) {
            WmModel wmModel = new WmModel();

            Reservation reservation = null;
            for (Reservation reservation1 : reservationList
                    ) {
                if (reservation1.getWm() == i) {
                    reservation = reservation1;
                    break;
                }
            }

            if (reservation == null) {
                if (isPast || brokenWm.contains(i)) {
                    wmModel.setType(WmModel.TYPE.UNAVAILABLE);
                    wmModel.setColor("#FF0000");
                } else {
                    wmModel.setType(WmModel.TYPE.FREE);
                    wmModel.setColor("#1E9600");
                }
            } else {
                if (isMyReservation()) {
                    wmModel.setColor("#FFF200");
                } else {
                    wmModel.setColor("#FF0000");
                    wmModel.setType(WmModel.TYPE.RESERVED);
                    wmModel.setUser(reservation.getUser());
                }
            }

            wmModels.add(wmModel);
        }

        return wmModels;
    }

    private boolean isMyReservation() {
        return false;
    }

    private List<Integer> getBrokenWm() {
        return new ArrayList<>();
    }

    private boolean isRegistrationAvailable() {
        return false;
    }

    private boolean isPast() {
        return false;
    }

    private String getDayName(Date date) {
        Calendar calendar = TimeUtil.getCalendar();
        calendar.setTime(date);

        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 2:
                return "Poniedziałek";
            case 3:
                return "Wtorek";
            case 4:
                return "Środa";
            case 5:
                return "Czwartek";
            case 6:
                return "Piątek";
            case 7:
                return "Sobota";
            case 1:
                return "Niedziela";
            default:
                return "Null";

        }
    }
}
