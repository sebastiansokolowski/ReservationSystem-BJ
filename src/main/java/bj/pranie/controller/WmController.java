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
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Sebastian Sokolowski on 12.10.16.
 */
@Controller
@RequestMapping(value = "/wm")
public class WmController {
    private static Logger LOG = Logger.getLogger(TimeUtil.class.getName());

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private static final long TIME_BEFORE_BLOCK_USER_UNREGISTER = TimeUnit.HOURS.toMillis(1);

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private WashTimeDao washTimeDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @Value("${wmCount}")
    private int wmCount;

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

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @RequestMapping(path = "/{year}/{month}/{day}/{washTimeId}/unregister", method = RequestMethod.POST)
    public ModelAndView unregisterWm(@PathVariable int year,
                                     @PathVariable int month,
                                     @PathVariable int day,
                                     @PathVariable long washTimeId,
                                     @RequestParam long reservationId) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("wm/wm");

        User user = userAuthenticatedService.getAuthenticatedUser();
        Reservation reservation = reservationDao.findOne(reservationId);

        if (user != null && reservation != null &&
                reservation.getUser().getId() == user.getId()) {
            if (isUnregisterAvailable(reservation)) {
                reservationDao.delete(reservationId);
                LOG.info("unregister wm " + reservation);

                user.setTokens(user.getTokens() + 1);
                userDao.save(user);
            } else {
                modelAndView.addObject("errorMessage", "Niestety już za późno aby się wyrejestrować.");
                LOG.info("unregister wm TOO LATE " + reservation);
            }
        }

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
        LOG.info("remove wm " + reservation);

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

    private boolean isUnregisterAvailable(Reservation reservation) {
        Calendar time = Calendar.getInstance();
        time.setTime(reservation.getWashTime().getFromTime());

        DateTime reservationDate = new DateTime(reservation.getDate())
                .withHourOfDay(time.get(Calendar.HOUR_OF_DAY))
                .withMinuteOfHour(time.get(Calendar.MINUTE));

        DateTime now = TimeUtil.getCalendar()
                .plus(TIME_BEFORE_BLOCK_USER_UNREGISTER);

        if (now.isBefore(reservationDate)) {
            return true;
        }

        return false;
    }

    private void setModel(int year, int month, int day, long washTimeId, ModelAndView modelAndView) throws ParseException {
        LocalDate localDate = new LocalDate(year, month, day);

        WashTime washTime = washTimeDao.findOne(washTimeId);
        List<Reservation> reservationList = reservationDao.findByWashTimeIdAndDate(washTimeId, new java.sql.Date(localDate.toDate().getTime()));
        int wmFree = wmCount - reservationList.size();

        modelAndView.addObject("washTimeId", washTimeId);
        modelAndView.addObject("dayName", getDayName(localDate));
        modelAndView.addObject("date", dateFormat.format(localDate.toDate()));
        modelAndView.addObject("time", getWashTime(washTime));
        modelAndView.addObject("wmFree", wmFree);
        modelAndView.addObject("reservations", getWmModels(reservationList, localDate, washTime));
        modelAndView.addObject("user", userAuthenticatedService.getAuthenticatedUser());
    }

    private synchronized void makeReservation(User user, int year, int month, int day, long washTimeId, int wmNumber, ReservationType reservationType) throws ReservationAlreadyBookedException {
        java.sql.Date date = getSqlDate(year, month, day);

        Reservation reservation = new Reservation();
        reservation.setDate(date);
        reservation.setUser(user);
        reservation.setWashTime(washTimeDao.findOne(washTimeId));
        reservation.setWm(wmNumber);
        reservation.setType(reservationType);

        if (reservationDao.existsByWashTimeIdAndDateAndWm(washTimeId, date, wmNumber)) {
            LOG.info("register wm EXIST " + reservation);
            throw new ReservationAlreadyBookedException();
        }

        reservationDao.save(reservation);
        LOG.info("register wm " + reservation);
    }

    private java.sql.Date getSqlDate(int year, int month, int day) {
        Date date = new java.sql.Date(new LocalDate(year, month, day).toDate().getTime());
        return new java.sql.Date(date.getTime());
    }

    private String getWashTime(WashTime washTime) {
        return timeFormat.format(washTime.getFromTime()) + " - " + timeFormat.format(washTime.getToTime());
    }

    private List<WmModel> getWmModels(List<Reservation> reservationList, LocalDate date, WashTime washTime) {
        List<WmModel> wmModels = new ArrayList<>();

        List<Integer> brokenWm = getBrokenWm();
        boolean isPast = TimeUtil.isPast(washTime.getFromTime(), date);

        for (int i = 0; i != wmCount; i++) {
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
                } else if (isMyReservation(currentReservation.getUser()) && isUnregisterAvailable(currentReservation)) {
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

    private String getDayName(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case DateTimeConstants.MONDAY:
                return "Poniedziałek";
            case DateTimeConstants.TUESDAY:
                return "Wtorek";
            case DateTimeConstants.WEDNESDAY:
                return "Środa";
            case DateTimeConstants.THURSDAY:
                return "Czwartek";
            case DateTimeConstants.FRIDAY:
                return "Piątek";
            case DateTimeConstants.SATURDAY:
                return "Sobota";
            case DateTimeConstants.SUNDAY:
                return "Niedziela";
            default:
                return "Null";

        }
    }
}
