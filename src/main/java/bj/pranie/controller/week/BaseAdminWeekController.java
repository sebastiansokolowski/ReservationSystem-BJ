package bj.pranie.controller.week;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.Device;
import bj.pranie.entity.Reservation;
import bj.pranie.entity.ReservationTime;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Sokolowski on 07.09.17.
 */
public abstract class BaseAdminWeekController extends BaseWeekController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @RequestMapping(method = RequestMethod.GET)
    public String week(Model model) throws ParseException {
        String weekId = getCurrentWeekId();

        setModel(weekId, model);
        return getWeekView();
    }

    @RequestMapping(path = "/{weekId}", method = RequestMethod.GET)
    public String week(@PathVariable String weekId, Model model) throws ParseException {
        setModel(weekId, model);

        return getWeekView();
    }

    @RequestMapping(path = "/{weekId}/block", method = RequestMethod.POST)
    public String blockDay(@PathVariable String weekId,
                           @RequestParam String date,
                           @RequestParam(defaultValue = "") String[] deviceIds,
                           Model model) throws ParseException {
        java.sql.Date sqlDate = new java.sql.Date(dateFormat.parseDateTime(date).getMillis());

        List<Long> deviceIdsToBlock = parseStringArratToLongList(deviceIds);

        removeUsersRegistrations(sqlDate, deviceIdsToBlock);
        makeReservations(sqlDate, deviceIdsToBlock);

        setModel(weekId, model);
        return getWeekView();
    }

    @RequestMapping(path = "/{weekId}/unblock", method = RequestMethod.POST)
    public String unblockDay(@PathVariable String weekId,
                             @RequestParam String date,
                             @RequestParam(defaultValue = "") String[] deviceIds,
                             Model model) throws ParseException {
        java.sql.Date sqlDate = new java.sql.Date(dateFormat.parseDateTime(date).getMillis());

        List<Long> deviceIdsToUnlock = parseStringArratToLongList(deviceIds);

        List<Reservation> reservations = reservationDao.findByDate(sqlDate);
        for (Reservation reservation : reservations) {
            if (reservation.getDevice().getDeviceType() != getDeviceType()) {
                continue;
            }
            if (!deviceIdsToUnlock.contains(reservation.getDevice().getId())) {
                continue;
            }
            if (reservation.getType() != ReservationType.BLOCKED) {
                continue;
            }
            reservationDao.delete(reservation.getId());
        }

        setModel(weekId, model);
        return getWeekView();
    }

    public void setModel(String weekId, Model model) throws ParseException {
        super.setModel(weekId, model);

        model.addAttribute("nextWeekId", getSpecificWeekId(weekId, WEEK_TYPE.NEXT));
        model.addAttribute("prevWeekId", getSpecificWeekId(weekId, WEEK_TYPE.PREV));
        model.addAttribute("devices", getDeviceModels());
    }

    // private

    private List<Long> parseStringArratToLongList(String[] array) {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i != array.length; i++) {
            result.add(Long.parseLong(array[i]));
        }

        return result;
    }

    private void removeUsersRegistrations(Date sqlDate, List<Long> deviceIdsToBlock) {
        List<Reservation> reservations = reservationDao.findByDate(sqlDate);
        for (Reservation reservation : reservations) {
            if (reservation.getDevice().getDeviceType() != getDeviceType()) {
                continue;
            }
            if (!deviceIdsToBlock.contains(reservation.getDevice().getId())) {
                continue;
            }
            if (reservation.getType() == ReservationType.USER){
                giveBackUserToken(reservation.getUser());
            }
            reservationDao.delete(reservation.getId());
        }
    }

    private void giveBackUserToken(User user) {
        user.setTokens(user.getTokens() + 1);

        userDao.save(user);
    }

    private void makeReservations(Date sqlDate, List<Long> devicesToBlock) {
        User admin = userAuthenticatedService.getAuthenticatedUser();

        for (ReservationTime reservationTime : reservationTimeDao.findAll()) {
            for (Long deviceId : devicesToBlock) {
                makeReservation(admin, sqlDate, reservationTime.getId(), deviceId, ReservationType.BLOCKED);
            }
        }
    }

    private void makeReservation(User user, java.sql.Date date, long reservationTimeId, long deviceId, ReservationType reservationType) {
        Device device = deviceDao.findOne(deviceId);

        Reservation reservation = new Reservation();
        reservation.setDate(date);
        reservation.setUser(user);
        reservation.setReservationTime(reservationTimeDao.findOne(reservationTimeId));
        reservation.setDevice(device);
        reservation.setType(reservationType);

        reservationDao.save(reservation);
    }

}

