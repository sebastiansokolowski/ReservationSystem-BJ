package bj.pranie.controller.week.user;

import bj.pranie.controller.week.BaseUserWeekController;
import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wm/week")
public class UserWmWeekController extends BaseUserWeekController {

    @Value("${wmCount}")
    int devicesCount;

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.WASHING_MACHINE;
    }

    @Override
    public int getDevicesCount() {
        return devicesCount;
    }
}
