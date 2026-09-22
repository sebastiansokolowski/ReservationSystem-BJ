package bj.pranie.controller;

import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Sebastian Sokolowski on 22.10.17.
 */
@Controller
public class ReservationTypeController {

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @Value("${tokensPerWeek}")
    private int tokensPerWeek;

    @Value("${resetTime}")
    private int resetTime;

    @RequestMapping(value = "/type", method = RequestMethod.GET)
    public String userSettings(Model model) {
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());
        model.addAttribute("tokensPerWeek", tokensPerWeek);
        model.addAttribute("resetTime", String.format("%02d:00", resetTime));
        return "type";
    }
}
