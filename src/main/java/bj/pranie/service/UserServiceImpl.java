package bj.pranie.service;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.Room;
import bj.pranie.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Sebastian Sokolowski on 24.10.17.
 */
@Service
public class UserServiceImpl implements UserDao {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public List<User> findByRoom(Room room) {
        return findByRoom(room);
    }

    @Override
    public User save(User entity) {
        hashPassword(entity);
        return userDao.save(entity);
    }

    @Override
    public <S extends User> Iterable<S> save(Iterable<S> entities) {
        entities.forEach(s -> hashPassword(s));
        return userDao.save(entities);
    }

    private User hashPassword(User user) {
        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);

        return user;
    }

    @Override
    public User findOne(Long aLong) {
        return userDao.findOne(aLong);
    }

    @Override
    public boolean exists(Long aLong) {
        return userDao.exists(aLong);
    }

    @Override
    public Iterable<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public Iterable<User> findAll(Iterable<Long> longs) {
        return userDao.findAll(longs);
    }

    @Override
    public long count() {
        return userDao.count();
    }

    @Override
    public void delete(Long aLong) {
        userDao.delete(aLong);
    }

    @Override
    public void delete(User entity) {
        userDao.delete(entity);
    }

    @Override
    public void delete(Iterable<? extends User> entities) {
        userDao.delete(entities);
    }

    @Override
    public void deleteAll() {
        userDao.deleteAll();
    }
}
