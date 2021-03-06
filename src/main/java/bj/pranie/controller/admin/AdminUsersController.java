package bj.pranie.controller.admin;

import bj.pranie.dao.RoomDao;
import bj.pranie.dao.UserDao;
import bj.pranie.entity.Room;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.model.UserEditModel;
import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Sebastian Sokolowski on 12.11.18.
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUsersController {
    private static Logger LOG = Logger.getLogger(AdminUsersController.class.getName());

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String showUsers(Model model) {
        model.addAttribute("count", userDao.count());
        model.addAttribute("users", userDao.findAllByOrderByRoomRoomAscRoomTypeAsc());
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());
        return "admin/users";
    }

    @RequestMapping(value = "/delete/{userId}", method = RequestMethod.GET)
    public ModelAndView removeUser(@PathVariable long userId) {
        User user = userDao.findOne(userId);
        if (user != null) {
            userDao.delete(user);
        }
        LOG.info("remove user " + user);
        return new ModelAndView("redirect:/admin/users");
    }

    @RequestMapping(value = "/edit/{userId}", method = RequestMethod.GET)
    public String showUser(@PathVariable long userId,
                           Model model) {
        List<Room> rooms = roomDao.findAllByOrderByRoomAscTypeAsc();

        User user = userDao.findOne(userId);

        UserEditModel userEditModel = new UserEditModel();
        userEditModel.setId(user.getId());
        userEditModel.setUsername(user.getUsername());
        userEditModel.setName(user.getName());
        userEditModel.setSurname(user.getSurname());
        userEditModel.setEmail(user.getEmail());
        userEditModel.setTokens(user.getTokens());
        userEditModel.setBlocked(user.isBlocked());
        userEditModel.setRoomId(user.getRoom().getId());
        userEditModel.setRoleId(user.getRole().ordinal());

        model.addAttribute("userModel", userEditModel);
        model.addAttribute("rooms", rooms);
        model.addAttribute("userRoles", UserRole.values());
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());

        return "admin/user";
    }

    @RequestMapping(value = "/edit/{userId}", method = RequestMethod.POST)
    public ModelAndView saveUser(@PathVariable long userId,
                                 @ModelAttribute("userEditModel") UserEditModel userEditModel) {

        User user = userDao.findOne(userId);

        LOG.info("edit user data from " + user);

        user.setUsername(userEditModel.getUsername());
        user.setName(userEditModel.getName());
        user.setSurname(userEditModel.getSurname());
        user.setEmail(userEditModel.getEmail());
        user.setTokens(userEditModel.getTokens());
        user.setBlocked(userEditModel.getBlocked());
        user.setRole(UserRole.values()[userEditModel.getRoleId()]);
        user.setRoom(roomDao.findOne(userEditModel.getRoomId()));
        userDao.save(user);

        LOG.info("edit user data to " + user);

        return new ModelAndView("redirect:/admin/users");
    }
}