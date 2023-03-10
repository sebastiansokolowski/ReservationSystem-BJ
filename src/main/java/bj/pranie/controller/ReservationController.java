package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.UserDao;
import bj.pranie.dao.ReservationTimeDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.ReservationTime;
import bj.pranie.entity.myEnum.DeviceType;
import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.exception.ReservationAlreadyBookedException;
import bj.pranie.model.DeviceModel;
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
public class ReservationController {
    private static Logger LOG = Logger.getLogger(ReservationController.class.getName());

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private static final long TIME_BEFORE_BLOCK_USER_UNREGISTER = TimeUnit.HOURS.toMillis(1);

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @Value("${wmCount}")
    private int wmCount;

    @RequestMapping(path = "/{year}/{month}/{day}/{reservationTimeId}", method = RequestMethod.GET)
    public ModelAndView wm(@PathVariable int year,
                           @PathVariable int month,
                           @PathVariable int day,
                           @PathVariable long reservationTimeId) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("reservation");
        setModel(year, month, day, reservationTimeId, modelAndView);
        return modelAndView;
    }

    @PostMapping(path = "/{year}/{month}/{day}/{reservationTimeId}/register")
    public ModelAndView registerWm(@PathVariable int year,
                                   @PathVariable int month,
                                   @PathVariable int day,
                                   @PathVariable long reservationTimeId,
                                   @RequestParam int deviceNumber) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("reservation");

        User user = userAuthenticatedService.getAuthenticatedUser();

        try {
            int userTokens = user.getTokens();

            if (userTokens > 0) {
                makeReservation(user, year, month, day, reservationTimeId, deviceNumber, ReservationType.USER);

                user.setTokens(userTokens - 1);
                userDao.save(user);
            } else {
                modelAndView.addObject("errorMessage", "Brak tokenów.");
            }
        } catch (ReservationAlreadyBookedException reservationAlreadyBookedException) {
            reservationAlreadyBookedException.printStackTrace();
            modelAndView.addObject("errorMessage", "Niestety pralka jest już zarezerwowana.");
        }

        setModel(year, month, day, reservationTimeId, modelAndView);
        return modelAndView;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @RequestMapping(path = "/{year}/{month}/{day}/{reservationTimeId}/unregister", method = RequestMethod.POST)
    public ModelAndView unregisterWm(@PathVariable int year,
                                     @PathVariable int month,
                                     @PathVariable int day,
                                     @PathVariable long reservationTimeId,
                                     @RequestParam long reservationId) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("reservation");

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
                modelAndView.addObject("errorMessage", "Niestety jest już za późno aby się wyrejestrować.");
                LOG.info("unregister wm TOO LATE " + reservation);
            }
        }

        setModel(year, month, day, reservationTimeId, modelAndView);
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = "/{year}/{month}/{day}/{reservationTimeId}/remove", method = RequestMethod.POST)
    public ModelAndView removeWm(@PathVariable int year,
                                 @PathVariable int month,
                                 @PathVariable int day,
                                 @PathVariable long reservationTimeId,
                                 @RequestParam long reservationId) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("reservation");

        Reservation reservation = reservationDao.findOne(reservationId);
        reservationDao.delete(reservationId);
        LOG.info("remove wm " + reservation);

        if (reservation.getType() != ReservationType.BLOCKED) {
            User user = reservation.getUser();
            user.setTokens(user.getTokens() + 1);
            userDao.save(user);
        }

        setModel(year, month, day, reservationTimeId, modelAndView);
        return modelAndView;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @RequestMapping(path = "/{year}/{month}/{day}/{reservationTimeId}/block", method = RequestMethod.POST)
    public ModelAndView blockWm(@PathVariable int year,
                                @PathVariable int month,
                                @PathVariable int day,
                                @PathVariable long reservationTimeId,
                                @RequestParam int wmNumber) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("reservation");

        User user = userAuthenticatedService.getAuthenticatedUser();

        try {
            makeReservation(user, year, month, day, reservationTimeId, wmNumber, ReservationType.BLOCKED);
        } catch (ReservationAlreadyBookedException reservationAlreadyBookedException) {
            reservationAlreadyBookedException.printStackTrace();
            modelAndView.addObject("errorMessage", "Niestety termin jest już zajęty.");
        }

        setModel(year, month, day, reservationTimeId, modelAndView);
        return modelAndView;
    }

    private boolean isUnregisterAvailable(Reservation reservation) {
        Calendar time = Calendar.getInstance();
        time.setTime(reservation.getReservationTime().getFromTime());

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

    private void setModel(int year, int month, int day, long reservationTimeId, ModelAndView modelAndView) throws ParseException {
        LocalDate localDate = new LocalDate(year, month, day);

        ReservationTime reservationTime = reservationTimeDao.findOne(reservationTimeId);
        List<Reservation> reservationList = reservationDao.findByReservationTimeIdAndDateAndDeviceType(reservationTimeId, new java.sql.Date(localDate.toDate().getTime()), DeviceType.WASHING_MACHINE);
        int freeDevices = wmCount - reservationList.size();

        modelAndView.addObject("dayName", getDayName(localDate));
        modelAndView.addObject("date", dateFormat.format(localDate.toDate()));
        modelAndView.addObject("time", getReservationTime(reservationTime));
        modelAndView.addObject("freeDevices", freeDevices);
        modelAndView.addObject("reservations", getWmModels(reservationList, localDate, reservationTime));
        modelAndView.addObject("user", userAuthenticatedService.getAuthenticatedUser());
    }

    private synchronized void makeReservation(User user, int year, int month, int day, long reservationTimeId, int deviceNumber, ReservationType reservationType) throws ReservationAlreadyBookedException {
        java.sql.Date date = getSqlDate(year, month, day);

        Reservation reservation = new Reservation();
        reservation.setDate(date);
        reservation.setUser(user);
        reservation.setReservationTime(reservationTimeDao.findOne(reservationTimeId));
        reservation.setDeviceNumber(deviceNumber);
        reservation.setType(reservationType);

        if (reservationDao.existsByReservationTimeIdAndDateAndDeviceNumber(reservationTimeId, date, deviceNumber)) {
            LOG.info("register device EXIST " + reservation);
            throw new ReservationAlreadyBookedException();
        }

        reservationDao.save(reservation);
        LOG.info("register device " + reservation);
    }

    private java.sql.Date getSqlDate(int year, int month, int day) {
        Date date = new java.sql.Date(new LocalDate(year, month, day).toDate().getTime());
        return new java.sql.Date(date.getTime());
    }

    private String getReservationTime(ReservationTime reservationTime) {
        return timeFormat.format(reservationTime.getFromTime()) + " - " + timeFormat.format(reservationTime.getToTime());
    }

    private List<DeviceModel> getWmModels(List<Reservation> reservationList, LocalDate date, ReservationTime reservationTime) {
        List<DeviceModel> deviceModels = new ArrayList<>();

        List<Integer> brokenWm = getBrokenWm();
        boolean isPast = TimeUtil.isPast(reservationTime.getFromTime(), date);

        for (int i = 0; i != wmCount; i++) {
            DeviceModel deviceModel = new DeviceModel();

            Reservation currentReservation = null;
            for (Reservation reservation : reservationList
                    ) {
                if (reservation.getDeviceNumber() == i) {
                    currentReservation = reservation;
                    break;
                }
            }

            if (currentReservation == null) {
                if (isPast) {
                    deviceModel.setType(DeviceModel.TYPE.PAST);
                    deviceModel.setColor(ColorUtil.RESERVATION_UNAVAILABLE_COLOR);
                } else if (brokenWm.contains(i)) {
                    deviceModel.setType(DeviceModel.TYPE.UNAVAILABLE);
                    deviceModel.setColor(ColorUtil.RESERVATION_UNAVAILABLE_COLOR);
                } else {
                    deviceModel.setType(DeviceModel.TYPE.FREE);
                    deviceModel.setColor(ColorUtil.RESERVATION_FREE_COLOR);
                }
            } else {
                deviceModel.setReservationId(currentReservation.getId());
                if (currentReservation.getType() == ReservationType.BLOCKED) {
                    deviceModel.setColor(ColorUtil.RESERVATION_UNAVAILABLE_COLOR);
                    deviceModel.setType(DeviceModel.TYPE.UNAVAILABLE);
                } else if (isMyReservation(currentReservation.getUser()) && isUnregisterAvailable(currentReservation)) {
                    deviceModel.setType(DeviceModel.TYPE.MY);
                    deviceModel.setColor(ColorUtil.RESERVATION_MY_COLOR);
                    deviceModel.setUser(currentReservation.getUser());
                } else {
                    deviceModel.setColor(ColorUtil.RESERVATION_UNAVAILABLE_COLOR);
                    deviceModel.setType(DeviceModel.TYPE.RESERVED);
                    deviceModel.setUser(currentReservation.getUser());
                }
            }

            deviceModels.add(deviceModel);
        }

        return deviceModels;
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
