package bj.pranie.controller;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.RoomDao;
import bj.pranie.dao.UserDao;
import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Sebastian Sokolowski on 12.11.18.
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String showUsers(Model model) {
        model.addAttribute("count", userDao.count());
        model.addAttribute("users", userDao.findAllByOrderByRoomAsc());
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());
        return "admin/users";
    }
}
