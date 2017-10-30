package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.UserDao;
import bj.pranie.dao.WashTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.WashTime;
import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.model.WmModel;
import bj.pranie.service.UserAuthenticatedService;
import bj.pranie.util.ColorUtil;
import bj.pranie.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

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

        model.addAttribute("washTimeId", washTimeId);
        model.addAttribute("dayName", getDayName(date));
        model.addAttribute("date", dateFormat.format(date));
        model.addAttribute("time", getWashTime(washTime));
        model.addAttribute("wmFree", wmFree);
        model.addAttribute("reservations", getWmModels(reservationList, date, washTime));
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());
        return "wm/wm";
    }

    @RequestMapping(path = "/{year}/{month}/{day}/{washTimeId}/unregister", method = RequestMethod.POST)
    public String unregisterWm(@PathVariable int year,
                               @PathVariable int month,
                               @PathVariable int day,
                               @PathVariable long washTimeId,
                               @RequestParam long reservationId) {
        reservationDao.delete(reservationId);

        User user = userAuthenticatedService.getAuthenticatedUser();
        user.setTokens(user.getTokens() + 1);
        userDao.save(user);

        return "redirect:/wm/" + year + "/" + month + "/" + day + "/" + washTimeId;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = "/{year}/{month}/{day}/{washTimeId}/remove", method = RequestMethod.POST)
    public String removeWm(@PathVariable int year,
                           @PathVariable int month,
                           @PathVariable int day,
                           @PathVariable long washTimeId,
                           @RequestParam long reservationId) {
        Reservation reservation = reservationDao.findOne(reservationId);
        reservationDao.delete(reservationId);

        User user = reservation.getUser();
        user.setTokens(user.getTokens() + 1);
        userDao.save(user);

        return "redirect:/wm/" + year + "/" + month + "/" + day + "/" + washTimeId;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = "/{year}/{month}/{day}/{washTimeId}/block", method = RequestMethod.POST)
    public String blockWm(@PathVariable int year,
                          @PathVariable int month,
                          @PathVariable int day,
                          @PathVariable long washTimeId,
                          @RequestParam int wmNumber) {
        User user = userAuthenticatedService.getAuthenticatedUser();

        makeReservation(user, year, month, day, washTimeId, wmNumber, ReservationType.BLOCKED);

        return "redirect:/wm/" + year + "/" + month + "/" + day + "/" + washTimeId;
    }

    @PostMapping(path = "/{year}/{month}/{day}/{washTimeId}/register")
    public String registerWm(@PathVariable int year,
                             @PathVariable int month,
                             @PathVariable int day,
                             @PathVariable long washTimeId,
                             @RequestParam int wmNumber) {
        User user = userAuthenticatedService.getAuthenticatedUser();

        makeReservation(user, year, month, day, washTimeId, wmNumber, ReservationType.USER);

        user.setTokens(user.getTokens() - 1);
        userDao.save(user);

        return "redirect:/wm/" + year + "/" + month + "/" + day + "/" + washTimeId;
    }

    private void makeReservation(User user, int year, int month, int day, long washTimeId, int wmNumber, ReservationType reservationType) {
        Reservation reservation = new Reservation();
        reservation.setDate(getSqlDate(year, month, day));
        reservation.setUser(user);
        reservation.setWashTime(washTimeDao.findOne(washTimeId));
        reservation.setWm(wmNumber);
        reservation.setType(reservationType);

        reservationDao.save(reservation);
    }

    private java.sql.Date getSqlDate(int year, int month, int day) {
        try {
            Date date = dateFormat.parse(year + "/" + month + "/" + day);
            return new java.sql.Date(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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

            Reservation currentReservation = null;
            for (Reservation reservation : reservationList
                    ) {
                if (reservation.getWm() == i) {
                    currentReservation = reservation;
                    break;
                }
            }

            if (currentReservation == null) {
                if (isPast) {
                    wmModel.setType(WmModel.TYPE.PAST);
                    wmModel.setColor(ColorUtil.RESERVATION_UNAVAILABLE_COLOR);
                } else if (brokenWm.contains(i)) {
                    wmModel.setType(WmModel.TYPE.UNAVAILABLE);
                    wmModel.setColor(ColorUtil.RESERVATION_UNAVAILABLE_COLOR);
                } else {
                    wmModel.setType(WmModel.TYPE.FREE);
                    wmModel.setColor(ColorUtil.RESERVATION_FREE_COLOR);
                }
            } else {
                wmModel.setReservationId(currentReservation.getId());
                if (currentReservation.getType() == ReservationType.BLOCKED) {
                    wmModel.setColor(ColorUtil.RESERVATION_UNAVAILABLE_COLOR);
                    wmModel.setType(WmModel.TYPE.UNAVAILABLE);
                } else if (isMyReservation(currentReservation.getUser())) {
                    wmModel.setType(WmModel.TYPE.MY);
                    wmModel.setColor(ColorUtil.RESERVATION_MY_COLOR);
                    wmModel.setUser(currentReservation.getUser());
                } else {
                    wmModel.setColor(ColorUtil.RESERVATION_UNAVAILABLE_COLOR);
                    wmModel.setType(WmModel.TYPE.RESERVED);
                    wmModel.setUser(currentReservation.getUser());
                }
            }

            wmModels.add(wmModel);
        }

        return wmModels;
    }

    private boolean isMyReservation(User reservationUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser.getId() == reservationUser.getId()) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> getBrokenWm() {
        return new ArrayList<>();
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
