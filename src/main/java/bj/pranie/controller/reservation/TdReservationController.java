package bj.pranie.controller.reservation;

import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Sebastian Sokolowski on 12.10.16.
 */
@Controller
@RequestMapping(value = "/td")
public class TdReservationController extends BaseReservationController {

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
