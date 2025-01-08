package bj.pranie.controller.week.admin;

import bj.pranie.controller.week.BaseAdminWeekController;
import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/wm/week")
public class AdminWmWeekController extends BaseAdminWeekController {

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.WASHING_MACHINE;
    }

}
