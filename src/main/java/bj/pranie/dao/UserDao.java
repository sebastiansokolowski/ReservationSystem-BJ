package bj.pranie.dao;

import bj.pranie.entity.Room;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
@Transactional
public interface UserDao extends CrudRepository<User, Long> {
    User findByEmail(String email);

    User findByUsername(String username);

    List<User> findByRoom(Room room);

    List<User> findByRole(UserRole role);

    User findByResetPasswordKey(String resetPasswordKey);

    List<User> findAllByOrderByRoomRoomAscRoomTypeAsc();
}
