package bj.pranie.controller;

import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
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

    @Value("${messagePl:#{''}}")
    private String messagePl;

    @Value("${messageEng:#{''}}")
    private String messageEng;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home() {
        if (userAuthenticatedService.isAuthenticatedUser()) {
            return new ModelAndView("redirect:/type");
        }

        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login() {
        if (userAuthenticatedService.isAuthenticatedUser()) {
            return new ModelAndView("redirect:/type");
        }

        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("holidays", holidays);
        String localizedMessage = "en".equals(LocaleContextHolder.getLocale().getLanguage()) ? messageEng : messagePl;
        if (localizedMessage != null && !localizedMessage.trim().isEmpty()){
            modelAndView.addObject("message", localizedMessage);
        }
        return modelAndView;
    }
}
