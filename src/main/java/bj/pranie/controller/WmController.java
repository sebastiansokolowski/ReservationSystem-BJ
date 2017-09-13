package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by noon on 12.10.16.
 */
@Controller
@RequestMapping(value = "/wm")
public class WmController {

    @Autowired
    private ReservationDao reservationDao;

    @RequestMapping(method = RequestMethod.GET)
    public String wm(Model model, @RequestParam("data") String data, @RequestParam("time") String time) {
        return "wm/wm";
    }
}
