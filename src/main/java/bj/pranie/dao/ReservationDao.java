package bj.pranie.dao;

import bj.pranie.entity.Reservation;
import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;


/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Transactional
public interface ReservationDao extends CrudRepository<Reservation, Long> {

    boolean existsByReservationTimeIdAndDateAndDeviceNumberAndDeviceType(long reservationTimeId, Date date, int deviceNumber, DeviceType deviceType);

    List<Reservation> findByReservationTimeIdAndDateAndDeviceType(long reservationTimeId, Date date, DeviceType deviceType);

    List<Reservation> findByDateAndDeviceNumberAndDeviceType(Date date, int deviceNumber, DeviceType deviceType);

}
