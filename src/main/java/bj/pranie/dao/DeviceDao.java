package bj.pranie.dao;

import bj.pranie.entity.Device;
import bj.pranie.entity.myEnum.DeviceType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Transactional
public interface DeviceDao extends CrudRepository<Device, Long> {

    List<Device> findByDeviceType(DeviceType deviceType);

}
