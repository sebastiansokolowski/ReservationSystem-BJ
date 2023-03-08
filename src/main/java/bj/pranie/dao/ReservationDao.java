package bj.pranie.dao;

import bj.pranie.entity.Reservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;


/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Transactional
public interface ReservationDao extends CrudRepository<Reservation, Long> {

    boolean existsByReservationTimeIdAndDateAndWm(long reservationTimeId, Date date, int wm);

    List<Reservation> findByReservationTimeIdAndDate(long reservationTimeId, Date date);

    List<Reservation> findByDate(Date date);

    List<Reservation> findByDateAndWm(Date date, int wm);

}
