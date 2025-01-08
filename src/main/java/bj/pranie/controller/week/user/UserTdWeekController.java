package bj.pranie.controller.week.user;

import bj.pranie.controller.week.BaseUserWeekController;
import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/td/week")
public class UserTdWeekController extends BaseUserWeekController {

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.TUMBLE_DRYER;
    }

}
