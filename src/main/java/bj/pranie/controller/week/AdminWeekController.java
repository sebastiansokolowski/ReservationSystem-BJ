package bj.pranie.controller.week;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.WashTime;
import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sebastian Sokolowski on 07.09.17.
 */
@Controller
@RequestMapping("/admin/week")
public class AdminWeekController extends BaseWeekController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

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
        java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse(date).getTime());

        removeUsersRegistrations(sqlDate);
        makeReservations(sqlDate);

        setModel(weekId, model);
        return "wm/week";
    }

    void setModel(String weekId, Model model) throws ParseException {
        super.setModel(weekId, model);

        String nextWeekId = getSpecificWeekId(weekId, WEEK_TYPE.NEXT);
        model.addAttribute("nextWeekId", nextWeekId);
        model.addAttribute("prevWeekId", getSpecificWeekId(weekId, WEEK_TYPE.PREV));
    }

    // private

    private void removeUsersRegistrations(Date sqlDate) throws ParseException {
        List<Reservation> reservations = reservationDao.findByDate(sqlDate);
        for (Reservation reservation : reservations) {
            giveBackUserToken(reservation.getUser());

            reservationDao.delete(reservation.getId());
        }
    }

    private void giveBackUserToken(User user) {
        user.setTokens(user.getTokens() + 1);

        userDao.save(user);
    }

    private void makeReservations(Date sqlDate) {
        User admin = userAuthenticatedService.getAuthenticatedUser();

        Iterator<WashTime> washTimes = washTimeDao.findAll().iterator();
        while (washTimes.hasNext()) {
            WashTime washTime = washTimes.next();
            for (int i = 0; i != 3; i++) {
                makeReservation(admin, sqlDate, washTime.getId(), i, ReservationType.BLOCKED);
            }
        }
    }

    private void makeReservation(User user, java.sql.Date date, long washTimeId, int wmNumber, ReservationType reservationType) {
        Reservation reservation = new Reservation();
        reservation.setDate(date);
        reservation.setUser(user);
        reservation.setWashTime(washTimeDao.findOne(washTimeId));
        reservation.setWm(wmNumber);
        reservation.setType(reservationType);

        reservationDao.save(reservation);
    }

}

