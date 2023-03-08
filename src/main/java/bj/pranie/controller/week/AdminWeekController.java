package bj.pranie.controller.week;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.User;
import bj.pranie.entity.ReservationTime;
import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
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

    @RequestMapping(path = "/{weekId}/block", method = RequestMethod.POST)
    public String blockDay(@PathVariable String weekId,
                           @RequestParam String date,
                           @RequestParam(defaultValue = "") String[] wmValues,
                           Model model) throws ParseException {
        java.sql.Date sqlDate = new java.sql.Date(dateFormat.parseDateTime(date).getMillis());

        List<Integer> wmToBlock = parseStringArratToIntegerList(wmValues);

        removeUsersRegistrations(sqlDate, wmToBlock);
        makeReservations(sqlDate, wmToBlock);

        setModel(weekId, model);
        return "wm/week";
    }

    @RequestMapping(path = "/{weekId}/unblock", method = RequestMethod.POST)
    public String unblockDay(@PathVariable String weekId,
                             @RequestParam String date,
                             @RequestParam(defaultValue = "") String[] wmValues,
                             Model model) throws ParseException {
        java.sql.Date sqlDate = new java.sql.Date(dateFormat.parseDateTime(date).getMillis());

        List<Integer> wmToUnlock = parseStringArratToIntegerList(wmValues);

        for (Integer wm : wmToUnlock) {
            List<Reservation> reservations = reservationDao.findByDateAndWm(sqlDate, wm);
            for (Reservation reservation : reservations) {
                if (reservation.getType() == ReservationType.BLOCKED) {
                    reservationDao.delete(reservation.getId());
                }
            }
        }

        setModel(weekId, model);
        return "wm/week";
    }

    void setModel(String weekId, Model model) throws ParseException {
        super.setModel(weekId, model);

        model.addAttribute("nextWeekId", getSpecificWeekId(weekId, WEEK_TYPE.NEXT));
        model.addAttribute("prevWeekId", getSpecificWeekId(weekId, WEEK_TYPE.PREV));
        model.addAttribute("wmCount", wmCount);
    }

    // private

    private List<Integer> parseStringArratToIntegerList(String[] array) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i != array.length; i++) {
            result.add(Integer.parseInt(array[i]));
        }

        return result;
    }

    private void removeUsersRegistrations(Date sqlDate, List<Integer> wmToBlock) throws ParseException {
        for (Integer wm : wmToBlock) {
            List<Reservation> reservations = reservationDao.findByDateAndWm(sqlDate, wm);

            for (Reservation reservation : reservations) {
                giveBackUserToken(reservation.getUser());
                reservationDao.delete(reservation.getId());
            }
        }
    }

    private void giveBackUserToken(User user) {
        user.setTokens(user.getTokens() + 1);

        userDao.save(user);
    }

    private void makeReservations(Date sqlDate, List<Integer> wmToBlock) {
        User admin = userAuthenticatedService.getAuthenticatedUser();

        Iterator<ReservationTime> reservationTimes = reservationTimeDao.findAll().iterator();
        while (reservationTimes.hasNext()) {
            ReservationTime reservationTime = reservationTimes.next();
            for (Integer wm : wmToBlock) {
                makeReservation(admin, sqlDate, reservationTime.getId(), wm, ReservationType.BLOCKED);
            }
        }
    }

    private void makeReservation(User user, java.sql.Date date, long reservationTimeId, int wmNumber, ReservationType reservationType) {
        Reservation reservation = new Reservation();
        reservation.setDate(date);
        reservation.setUser(user);
        reservation.setReservationTime(reservationTimeDao.findOne(reservationTimeId));
        reservation.setWm(wmNumber);
        reservation.setType(reservationType);

        reservationDao.save(reservation);
    }

}

