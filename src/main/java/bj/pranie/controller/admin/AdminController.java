package bj.pranie.controller.admin;

import bj.pranie.dao.RoomDao;
import bj.pranie.dao.UserDao;
import bj.pranie.entity.Room;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.RoomType;
import bj.pranie.entity.myEnum.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Sebastian Sokolowski on 19.10.16.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${studentsLastRoom}")
    int studentsLastRoom;

    @ResponseBody
    @RequestMapping(value = "/registrationRooms", method = RequestMethod.GET)
    public String registrationRooms() {
        registrationAllRooms();
        return "Success!!";
    }

    @ResponseBody
    @RequestMapping(value = "/enableHolidaySystem", method = RequestMethod.GET)
    public String enableHolidaySystem() {
        removeAllStudentsAccounts();
        createHolidaysAccounts();
        return "Success!!";
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
        Room room = new Room(actualRoom, RoomType.A, true, 3);
        roomDao.save(room);
        room = new Room(actualRoom, RoomType.B, true, 2);
        roomDao.save(room);
        room = new Room(actualRoom, RoomType.C, true, 1);
        roomDao.save(room);

        // iteration floor which is 15 (without first 1, 2, 3)
        for (int i = 1; i != 15; i++) {
            //left
            actualRoom += 1;
            room = new Room(actualRoom, RoomType.A, true, 3);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.B, true, 2);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.C, true, 1);
            roomDao.save(room);

            //center
            actualRoom += 1;
            room = new Room(actualRoom, RoomType.A, true, 3);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.B, true, 2);
            roomDao.save(room);

            //right
            actualRoom += 1;
            room = new Room(actualRoom, RoomType.A, true, 3);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.B, true, 2);
            roomDao.save(room);
            room = new Room(actualRoom, RoomType.C, true, 1);
            roomDao.save(room);
        }
    }

    private void removeAllStudentsAccounts() {
        Iterable<User> iterable = userDao.findAll();
        for (User user : iterable) {
            if (user.getRole() == UserRole.ADMIN) {
                continue;
            }
            if (user.getRoom() == null) {
                continue;
            }
            if (user.getRoom().getRoom() > studentsLastRoom) {
                continue;
            }

            userDao.delete(user.getId());
        }
    }

    private void createHolidaysAccounts() {
        List<Room> rooms = roomDao.findAllByOrderByRoomAscTypeAsc();

        for (Room room : rooms) {
            if (room.getRoom() > studentsLastRoom) {
                continue;
            }

            String hashedPassword = passwordEncoder.encode("bursa");
            String username = room.getRoom() + room.getType().toString().toLowerCase();

            User user = new User();
            user.setUsername(username);
            user.setPassword(hashedPassword);
            user.setRoom(room);
            user.setBlocked(false);
            user.setRole(UserRole.GROUP);
            user.setTokens(room.getPeoples());

            userDao.save(user);
        }
    }
}
