package bj.pranie.dao;

import bj.pranie.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by noon on 10.08.16.
 */
@Transactional
public interface UserDao extends CrudRepository<User, Long> {
    User findByEmail(String email);

    User findByUsername(String username);


}
