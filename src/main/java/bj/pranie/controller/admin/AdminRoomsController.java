package bj.pranie.controller.admin;

import bj.pranie.dao.ReservationDao;
import bj.pranie.dao.RoomDao;
import bj.pranie.entity.Room;
import bj.pranie.entity.myEnum.RoomType;
import bj.pranie.model.RoomEditModel;
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
 * Created by Sebastian Sokolowski on 27.09.20.
 */
@Controller
@RequestMapping("/admin/rooms")
public class AdminRoomsController {
    private static Logger LOG = Logger.getLogger(AdminRoomsController.class.getName());

    @Autowired
    private ReservationDao reservationDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private UserAuthenticatedService userAuthenticatedService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String showRooms(Model model) {
        model.addAttribute("count", roomDao.count());
        model.addAttribute("rooms", roomDao.findAllByOrderByRoomAscTypeAsc());
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());
        return "admin/rooms";
    }

    @RequestMapping(value = "/delete/{roomId}", method = RequestMethod.GET)
    public ModelAndView removeRoom(@PathVariable long roomId) {
        Room room = roomDao.findOne(roomId);
        if (room != null) {
            roomDao.delete(room);
        }
        LOG.info("remove room " + room);
        return new ModelAndView("redirect:/admin/rooms");
    }

    @RequestMapping(value = "/edit/{roomId}", method = RequestMethod.GET)
    public String showRoom(@PathVariable long roomId,
                           Model model) {
        List<Room> rooms = roomDao.findAllByOrderByRoomAscTypeAsc();

        Room room = roomDao.findOne(roomId);

        RoomEditModel roomEditModel = new RoomEditModel();
        roomEditModel.setId(room.getId());
        roomEditModel.setRoom(room.getRoom());
        roomEditModel.setStudents(room.isStudents());
        roomEditModel.setPeoples(room.getPeoples());
        roomEditModel.setTypeId(room.getType().ordinal());

        model.addAttribute("roomModel", roomEditModel);
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("user", userAuthenticatedService.getAuthenticatedUser());

        return "admin/room";
    }

    @RequestMapping(value = "/edit/{roomId}", method = RequestMethod.POST)
    public ModelAndView saveRoom(@PathVariable long roomId,
                                 @ModelAttribute("roomEditModel") RoomEditModel roomEditModel) {

        Room room = roomDao.findOne(roomId);

        LOG.info("edit room data from " + room);

        room.setType(RoomType.values()[roomEditModel.getTypeId()]);
        room.setRoom(roomEditModel.getRoom());
        room.setStudents(roomEditModel.getStudents());
        room.setPeoples(roomEditModel.getPeoples());
        roomDao.save(room);

        LOG.info("edit room data to " + room);

        return new ModelAndView("redirect:/admin/rooms");
    }
}