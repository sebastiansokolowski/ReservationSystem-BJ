package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.WashTimeDao;
import bj.pranie.entity.WashTime;
import bj.pranie.model.WmTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Sebastian Sokolowski on 07.09.17.
 */
@Controller
@RequestMapping("/week")
public class WeekController {
    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private WashTimeDao washTimeDao;

    @RequestMapping(method = RequestMethod.GET)
    public String week(Model model) {
//        Iterable<Reservation> reservationList = getAllReservations();
        model.addAttribute("reservations", getAllReservations());
        return "wm/week";
    }


    public LinkedHashMap<String, List<WmTime>> getAllReservations() {
        LinkedHashMap<String, List<WmTime>> stringStringMap = new LinkedHashMap<>();

        Iterator<WashTime> timeIterator = washTimeDao.findAll().iterator();
        WashTime washTime;

        Random random = new Random();
        while (timeIterator.hasNext()) {
            washTime = timeIterator.next();

            List<WmTime> reservationCounts = new ArrayList<>();
            for (int i = 0; i != 6; i++) {
                WmTime wmTime = new WmTime();
                wmTime.setWmFree(random.nextInt(4));
                wmTime.setColor(getCellColor(wmTime.getWmFree()));

                reservationCounts.add(wmTime);
            }
            String time = new SimpleDateFormat("HH:mm").format(washTime.getFromTime())
                    + " - "
                    + new SimpleDateFormat("HH:mm").format(washTime.getToTime());

            stringStringMap.put(time, reservationCounts);
        }
        return stringStringMap;
    }

    private String getCellColor(int freeSpace) {
        switch (freeSpace) {
            case 3:
                return "#1E9600";
            case 2:
                return "#408000";
            case 1:
                return "#2C5900";
            default:
                return "#FF0000";
        }
    }


}
