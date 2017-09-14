package bj.pranie.controller;

import bj.pranie.dao.RoomDao;
import bj.pranie.dao.UserDao;
import bj.pranie.entity.Room;
import bj.pranie.entity.User;
import bj.pranie.model.UserRegistrationModel;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

/**
 * Created by noon on 10.08.16.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoomDao roomDao;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationModel());
        model.addAttribute("rooms", roomDao.findAll());
        return "user/registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ModelAndView createUser(@ModelAttribute("user") @Valid UserRegistrationModel userRegistrationModel, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();

        User userExist = userDao.findByEmail(userRegistrationModel.getEmail());
        if (userExist != null) {
            bindingResult.rejectValue("email", "error.user", "Podany adres email istnieje już w bazie.");
        }
        userExist = userDao.findByUsername(userRegistrationModel.getUsername());
        if (userExist != null) {
            bindingResult.rejectValue("username", "error.user", "Podana nazwa użytkownika istnieje już w bazie.");
        }
        if (!userRegistrationModel.getPassword().equals(userRegistrationModel.getPasswordRepeat())) {
            bindingResult.rejectValue("passwordRepeat", "error.user", "Powtórzone hasło jest różne od wpisanego.");
        }
        Room room = roomDao.findOne(userRegistrationModel.getRoomId());
        if (room == null) {
            bindingResult.rejectValue("room", "error.user", "Wybierz pokój z listy.");
        } else if (userDao.findByRoom(room).size() >= room.getPeoples()) {
            bindingResult.rejectValue("room", "error.user", "Brak miejsca w wybranym pokoju.");
        }

        if (!bindingResult.hasErrors()) {
            ModelMapper modelMapper = new ModelMapper();
            User user = modelMapper.map(userRegistrationModel, User.class);

            userDao.save(user);

            modelAndView.addObject("successMessage", "Rejestracja przebiegła pomyślnie.");
        }
        modelAndView.addObject("rooms", roomDao.findAll());

        modelAndView.setViewName("user/registration");
        return modelAndView;
    }

}
