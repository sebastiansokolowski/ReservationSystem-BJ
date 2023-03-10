package bj.pranie.controller.week.admin;

import bj.pranie.controller.week.BaseAdminWeekController;
import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/td/week")
public class AdminTdWeekController extends BaseAdminWeekController {

    @Value("${tdCount}")
    int devicesCount;

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.TUMBLE_DRYER;
    }

    @Override
    public int getDevicesCount() {
        return devicesCount;
    }
}
