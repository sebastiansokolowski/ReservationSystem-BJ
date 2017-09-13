package bj.pranie.controller;

import bj.pranie.dao.RoomDao;
import bj.pranie.dao.UserDao;
import bj.pranie.dto.UserRegisterDto;
import bj.pranie.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

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


    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String registerForm(Model model) {
        model.addAttribute("userRegister", new UserRegisterDto());
        model.addAttribute("rooms", roomDao.findAll());
        return "user/register";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String createUser(@ModelAttribute UserRegisterDto userRegisterDto, Model model) {

        return "register";
    }

    // private

    private Map<String, String> getRoomMap() {
        Map<String, String> roomMap = new HashMap<>();
        for (Room room : roomDao.findAll()
                ) {
            roomMap.put(room.getId()+"", room.getRoom() + room.getType().name());
        }
        return roomMap;
    }

}
