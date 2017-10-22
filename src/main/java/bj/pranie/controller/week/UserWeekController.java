package bj.pranie.controller.week;

import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.text.ParseException;

/**
 * Created by Sebastian Sokolowski on 07.09.17.
 */
@Controller
@RequestMapping("/week")
public class UserWeekController extends BaseWeekController {

    private static String ADMIN_PAGE_REDIRECT = "redirect:/admin/week";

    @RequestMapping(method = RequestMethod.GET)
    public String week(Model model) throws ParseException {
        if (isAdminAuthenticated()) {
            return ADMIN_PAGE_REDIRECT;
        }

        String weekId = getCurrentWeekId();

        setModel(weekId, model);
        model.addAttribute("prevWeekButton", true);
        return "wm/week";
    }

    @RequestMapping(path = "/last", method = RequestMethod.GET)
    public String lastWeek(Model model) throws ParseException {
        if (isAdminAuthenticated()) {
            return ADMIN_PAGE_REDIRECT;
        }

        String weekId = getCurrentWeekId();
        String prevWeekId = getSpecificWeekId(weekId, WEEK_TYPE.PREV);

        setModel(prevWeekId, model);
        model.addAttribute("nextWeekButton", true);
        return "wm/week";
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

