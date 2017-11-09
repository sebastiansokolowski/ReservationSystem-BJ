package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.UserDao;
import bj.pranie.dao.WashTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.WashTime;
import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.exception.ReservationAlreadyBookedException;
import bj.pranie.model.WmModel;
import bj.pranie.service.UserAuthenticatedService;
import bj.pranie.util.ColorUtil;
import bj.pranie.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
    public ModelAndView wm(@PathVariable int year,
                           @PathVariable int month,
                           @PathVariable int day,
                           @PathVariable long washTimeId) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("wm/wm");
        setModel(year, month, day, washTimeId, modelAndView);
        return modelAndView;
    }

    @PostMapping(path = "/{year}/{month}/{day}/{washTimeId}/register")
    public ModelAndView registerWm(@PathVariable int year,
                                   @PathVariable int month,
                                   @PathVariable int day,
                                   @PathVariable long washTimeId,
                                   @RequestParam int wmNumber) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("wm/wm");

        User user = userAuthenticatedService.getAuthenticatedUser();

        try {
            int userTokens = user.getTokens();

            if (userTokens > 0) {
                makeReservation(user, year, month, day, washTimeId, wmNumber, ReservationType.USER);

                user.setTokens(userTokens - 1);
                userDao.save(user);
            } else {
                modelAndView.addObject("errorMessage", "Brak tokenów.");
            }
        } catch (ReservationAlreadyBookedException reservationAlreadyBookedException) {
            reservationAlreadyBookedException.printStackTrace();
            modelAndView.addObject("errorMessage", "Niestety pralka jest już zarezerwowana.");
        }

        setModel(year, month, day, washTimeId, modelAndView);
        return modelAndView;
    }

    @RequestMapping(path = "/{year}/{month}/{day}/{washTimeId}/unregister", method = RequestMethod.POST)
    public ModelAndView unregisterWm(@PathVariable int year,
                                     @PathVariable int month,
                                     @PathVariable int day,
                                     @PathVariable long washTimeId,
                                     @RequestParam long reservationId) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("wm/wm");

        reservationDao.delete(reservationId);

        User user = userAuthenticatedService.getAuthenticatedUser();
        user.setTokens(user.getTokens() + 1);
        userDao.save(user);

        setModel(year, month, day, washTimeId, modelAndView);
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = "/{year}/{month}/{day}/{washTimeId}/remove", method = RequestMethod.POST)
    public ModelAndView removeWm(@PathVariable int year,
                                 @PathVariable int month,
                                 @PathVariable int day,
                                 @PathVariable long washTimeId,
                                 @RequestParam long reservationId) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("wm/wm");

        Reservation reservation = reservationDao.findOne(reservationId);
        reservationDao.delete(reservationId);

        if (reservation.getType() != ReservationType.BLOCKED) {
            User user = reservation.getUser();
            user.setTokens(user.getTokens() + 1);
            userDao.save(user);
        }

        setModel(year, month, day, washTimeId, modelAndView);
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = "/{year}/{month}/{day}/{washTimeId}/block", method = RequestMethod.POST)
    public ModelAndView blockWm(@PathVariable int year,
                                @PathVariable int month,
                                @PathVariable int day,
                                @PathVariable long washTimeId,
                                @RequestParam int wmNumber) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("wm/wm");

        User user = userAuthenticatedService.getAuthenticatedUser();

        try {
            makeReservation(user, year, month, day, washTimeId, wmNumber, ReservationType.BLOCKED);
        } catch (ReservationAlreadyBookedException reservationAlreadyBookedException) {
            reservationAlreadyBookedException.printStackTrace();
            modelAndView.addObject("errorMessage", "Niestety pralka jest już zarezerwowana.");
        }

        setModel(year, month, day, washTimeId, modelAndView);
        return modelAndView;
    }

    private void setModel(int year, int month, int day, long washTimeId, ModelAndView modelAndView) throws ParseException {
        Date date = dateFormat.parse(year + "/" + month + "/" + day);

        WashTime washTime = washTimeDao.findOne(washTimeId);
        List<Reservation> reservationList = reservationDao.findByWashTimeIdAndDate(washTimeId, new java.sql.Date(date.getTime()));
        int wmFree = 3 - reservationList.size();

        modelAndView.addObject("washTimeId", washTimeId);
        modelAndView.addObject("dayName", getDayName(date));
        modelAndView.addObject("date", dateFormat.format(date));
        modelAndView.addObject("time", getWashTime(washTime));
        modelAndView.addObject("wmFree", wmFree);
        modelAndView.addObject("reservations", getWmModels(reservationList, date, washTime));
        modelAndView.addObject("user", userAuthenticatedService.getAuthenticatedUser());
    }

    private synchronized void makeReservation(User user, int year, int month, int day, long washTimeId, int wmNumber, ReservationType reservationType) throws ReservationAlreadyBookedException {
        java.sql.Date date = getSqlDate(year, month, day);

        if (reservationDao.existsByWashTimeIdAndDateAndWm(washTimeId, date, wmNumber)) {
            throw new ReservationAlreadyBookedException();
        }


        Reservation reservation = new Reservation();
        reservation.setDate(date);
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
