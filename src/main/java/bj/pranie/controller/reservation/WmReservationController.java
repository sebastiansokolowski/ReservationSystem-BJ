package bj.pranie.controller.reservation;

import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.logging.Logger;

/**
 * Created by Sebastian Sokolowski on 12.10.16.
 */
@Controller
@RequestMapping(value = "/wm")
public class WmReservationController extends BaseReservationController {

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
