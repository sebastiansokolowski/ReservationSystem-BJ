package bj.pranie.controller;

import bj.pranie.dao.RoomDao;
import bj.pranie.dao.UserDao;
import bj.pranie.entity.Room;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import bj.pranie.model.UserRegistrationModel;
import bj.pranie.model.UserSettingsModel;
import bj.pranie.service.UserAuthenticatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @Value("${tokensPerWeek}")
    int tokensPerWeek;

    @Value("${holidays}")
    private boolean holidays;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public String userSettings(Model model) {
        model.addAttribute("userSettingsModel", new UserSettingsModel());
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());
        return "user/settings";
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    @RequestMapping(value = "/settings", method = RequestMethod.POST)
    public ModelAndView saveUserSettings(@ModelAttribute("userSettingsModel") @Valid UserSettingsModel userSettingsModel, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();

        User user = userAuthenticatedService.getAuthenticatedUser();

        if (!passwordEncoder.matches(userSettingsModel.getPassword(), user.getPassword())) {
            bindingResult.rejectValue("password", "error.userRegistrationModel", "Podane hasło jest nieprawidłowe.");
        }

        User userExist;
        if (userSettingsModel.isSetNewUsername()) {
            userExist = userDao.findByUsername(userSettingsModel.getNewUsername());
            if (userExist != null) {
                bindingResult.rejectValue("newUsername", "error.userSettingsModel", "Podana nazwa użytkownika istnieje już w bazie.");
            }
        }
        if (userSettingsModel.isSetNewEmail()) {
            userExist = userDao.findByEmail(userSettingsModel.getNewEmail());
            if (userExist != null) {
                bindingResult.rejectValue("newEmail", "error.userSettingsModel", "Podany adres email istnieje już w bazie.");
            }
        }
        if (userSettingsModel.isSetNewPassword()) {
            if (!userSettingsModel.getNewPassword().equals(userSettingsModel.getNewPasswordRepeat())) {
                bindingResult.rejectValue("newPasswordRepeat", "error.userRegistrationModel", "Powtórzone hasło jest różne od wpisanego.");
            }
        }

        if (!bindingResult.hasErrors()) {
            if (userSettingsModel.isSetNewUsername()) {
                user.setUsername(userSettingsModel.getNewUsername());
            }
            if (userSettingsModel.isSetNewEmail()) {
                user.setEmail(userSettingsModel.getNewEmail());
            }
            if (userSettingsModel.isSetNewPassword()) {
                String hashedPassword = passwordEncoder.encode(userSettingsModel.getNewPassword());
                user.setPassword(hashedPassword);
            }

            userDao.save(user);

            modelAndView.addObject("successMessage", "Zmiany zostały zachowane pomyślnie.");
        }

        modelAndView.addObject("user", userAuthenticatedService.getAuthenticatedUser());
        modelAndView.setViewName("user/settings");
        return modelAndView;
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registrationForm(Model model) {
        List<Room> rooms = roomDao.findAllByOrderByRoomAscTypeAsc();
        if (holidays) {
            rooms = removeStudentRooms(rooms);
        }
        model.addAttribute("userRegistrationModel", new UserRegistrationModel());
        model.addAttribute("rooms", rooms);
        return "user/registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createUser(@ModelAttribute("userRegistrationModel") @Valid UserRegistrationModel userRegistrationModel, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();

        User userExist = userDao.findByEmail(userRegistrationModel.getEmail());
        if (userExist != null) {
            bindingResult.rejectValue("email", "error.userRegistrationModel", "Podany adres email istnieje już w bazie.");
        }
        userExist = userDao.findByUsername(userRegistrationModel.getUsername());
        if (userExist != null) {
            bindingResult.rejectValue("username", "error.userRegistrationModel", "Podana nazwa użytkownika istnieje już w bazie.");
        }
        if (!userRegistrationModel.getPassword().equals(userRegistrationModel.getPasswordRepeat())) {
            bindingResult.rejectValue("passwordRepeat", "error.userRegistrationModel", "Powtórzone hasło jest różne od wpisanego.");
        }
        Room room = roomDao.findOne(userRegistrationModel.getRoomId());
        if (room == null) {
            bindingResult.rejectValue("roomId", "error.userRegistrationModel", "Wybierz pokój z listy.");
        } else if (userDao.findByRoom(room).size() >= room.getPeoples()) {
            bindingResult.rejectValue("roomId", "error.userRegistrationModel", "Brak miejsca w wybranym pokoju.");
        }

        if (!bindingResult.hasErrors()) {
            String hashedPassword = passwordEncoder.encode(userRegistrationModel.getPassword());

            User user = new User();
            user.setUsername(userRegistrationModel.getUsername());
            user.setPassword(hashedPassword);
            user.setName(userRegistrationModel.getName());
            user.setSurname(userRegistrationModel.getSurname());
            user.setEmail(userRegistrationModel.getEmail());
            user.setRoom(room);
            user.setBlocked(false);
            user.setRole(UserRole.USER);
            user.setTokens(tokensPerWeek);

            userDao.save(user);

            modelAndView.addObject("successMessage", "Rejestracja przebiegła pomyślnie.");
        }
        modelAndView.addObject("rooms", roomDao.findAll());

        modelAndView.setViewName("user/registration");
        return modelAndView;
    }

    private List<Room> removeStudentRooms(List<Room> rooms) {
        List<Room> newRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (!room.isStudents()) {
                newRooms.add(room);
            }
        }

        return newRooms;
    }
}
