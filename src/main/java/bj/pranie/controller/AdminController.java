package bj.pranie.controller;

import bj.pranie.dao.RoomDao;
import bj.pranie.dao.UserDao;
import bj.pranie.entity.Room;
import bj.pranie.entity.myEnum.RoomType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by noon on 19.10.16.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoomDao roomDao;

    @RequestMapping(value = "/registrationRooms", method = RequestMethod.GET)
    public String registrationRooms(Model model) {
        registrationAllRooms();
        return "user/registration";
    }

    /**
     * registration all rooms from 3 to 45 (A,B,C).
     */
    private void registrationAllRooms() {
        //remove all rooms
        roomDao.deleteAll();

        //registration all rooms

        //right room 3 at first floor
        int actualRoom = 3;
        Room room = new Room(actualRoom, RoomType.A, 3);
        roomDao.save(room);
        room = new Room(actualRoom, RoomType.B, 2);
        roomDao.save(room);
        room = new Room(actualRoom, RoomType.C, 1);
        roomDao.save(room);

        // iteration floor which is 15 (without first 1, 2, 3)
        for (int i = 1; i != 15; i++) {
            //left
            actualRoom += 1;
            room = new Room(actualRoom, RoomType.A, 3);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.B, 2);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.C, 1);
            roomDao.save(room);

            //center
            actualRoom += 1;
            room = new Room(actualRoom, RoomType.A, 3);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.B, 2);
            roomDao.save(room);

            //right
            actualRoom += 1;
            room = new Room(actualRoom, RoomType.A, 3);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.B, 2);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.C, 1);
            roomDao.save(room);
        }
    }
}
