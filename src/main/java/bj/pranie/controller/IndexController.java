package bj.pranie.controller;

import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
    public String index() {
        if (userAuthenticatedService.isAuthenticatedUser()) {
            return "redirect:/week";
        }
        return "index";
    }
}
