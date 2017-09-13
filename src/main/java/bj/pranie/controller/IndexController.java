package bj.pranie.controller;

import bj.pranie.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Calendar;

/**
 * Created by noon on 12.10.16.
 */

@Controller
public class IndexController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index(Model model) {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("time", TimeUtil.getTime());
        log.debug("time" + Calendar.getInstance().getTime().toString());

        return modelAndView;
    }
}
