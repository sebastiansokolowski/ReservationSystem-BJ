package bj.pranie.controller;

import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Sebastian Sokolowski on 12.10.16.
 */

@Controller
public class IndexController {

    @Value("${holidays}")
    private boolean holidays;

    @Value("${message:#{''}}")
    private String message;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index() {
        if (userAuthenticatedService.isAuthenticatedUser()) {
            return new ModelAndView("redirect:/type");
        }
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("holidays", holidays);
        if (message != null && !message.isEmpty()){
            modelAndView.addObject("message", message);
        }
        return modelAndView;
    }
}
