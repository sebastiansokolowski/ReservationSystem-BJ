package bj.pranie.controller.reservation;

import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Sebastian Sokolowski on 12.10.16.
 */
@Controller
@RequestMapping(value = "/wm")
public class WmReservationController extends BaseReservationController {

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.WASHING_MACHINE;
    }

}
