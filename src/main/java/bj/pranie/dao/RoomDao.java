package bj.pranie.dao;

import bj.pranie.entity.Room;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Transactional
public interface RoomDao extends CrudRepository<Room, Long> {
}
