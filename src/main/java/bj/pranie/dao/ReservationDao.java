package bj.pranie.dao;

import bj.pranie.entity.Reservation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by noon on 10.08.16.
 */
@Transactional
public interface ReservationDao extends CrudRepository<Reservation, Long> {
}
