package bj.pranie.controller.week;

import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.ParseException;

/**
 * Created by Sebastian Sokolowski on 07.09.17.
 */
public abstract class BaseUserWeekController extends BaseWeekController {

    private static final String ADMIN_PAGE_REDIRECT = "redirect:/admin/%s/week";

    @RequestMapping(method = RequestMethod.GET)
    public String week(Model model) throws ParseException {
        if (isAdminAuthenticated()) {
            return String.format(ADMIN_PAGE_REDIRECT, getDeviceType().getPathName());
        }

        String weekId = getCurrentWeekId();

        setModel(weekId, model);
        model.addAttribute("prevWeekButton", true);
        return getWeekView();
    }

    @RequestMapping(path = "/last", method = RequestMethod.GET)
    public String lastWeek(Model model) throws ParseException {
        if (isAdminAuthenticated()) {
            return String.format(ADMIN_PAGE_REDIRECT, getDeviceType().getPathName());
        }

        String weekId = getCurrentWeekId();
        String prevWeekId = getSpecificWeekId(weekId, WEEK_TYPE.PREV);

        setModel(prevWeekId, model);
        model.addAttribute("nextWeekButton", true);
        return getWeekView();
    }

    private boolean isAdminAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser.getRole() == UserRole.ADMIN) {
                return true;
            }
        }
        return false;
    }
}

