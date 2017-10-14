package bj.pranie.dao;

import bj.pranie.entity.Reservation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;


/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Transactional
public interface ReservationDao extends CrudRepository<Reservation, Long> {
    @Query("select count(b) from Reservation b where b.date between ?1 and ?2 and b.date between ?1 and ?2")
    Long countByDatesBetween(Date fromDate, Date toDate);

    List<Reservation> findByWashTimeIdAndDate(long washTimeId, Date date);

    List<Reservation> findByDate(Date date);
}
