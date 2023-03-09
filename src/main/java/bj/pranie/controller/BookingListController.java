package bj.pranie.controller;

import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Sebastian Sokolowski on 22.10.17.
 */
@Controller
public class BookingListController {

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @RequestMapping(value = "/booking_list", method = RequestMethod.GET)
    public String userSettings(Model model) {
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());
        return "booking_list";
    }
}
