package bj.pranie.service;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Created by Sebastian Sokolowski on 31.10.17.
 */
@Service
public class UserAuthenticatedService {

    @Autowired
    private UserDao userDao;

    public boolean isAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof User) {
            return true;
        }
        return false;
    }

    public User getAuthenticatedUser() {
        if (isAuthenticatedUser()) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            return userDao.findOne(user.getId());
        }

        return null;
    }
}
