package bj.pranie.controller;

import bj.pranie.Application;
import bj.pranie.model.UserSettingsModel;
import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Sebastian Sokolowski on 12.10.16.
 */

@Controller
public class IndexController {

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("holidays", Application.HOLIDAYS);
        if (userAuthenticatedService.isAuthenticatedUser()) {
            return "redirect:/week";
        }
        return "index";
    }
}
