package bj.pranie.dao;

import bj.pranie.entity.WashTime;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Transactional
public interface WashTimeDao extends CrudRepository<WashTime, Long> {

    List<WashTime> findAllByOrderByIdAsc();

}
