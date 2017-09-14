package bj.pranie.config;

import bj.pranie.dao.UserDao;
import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by noon on 24.10.16.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDao userDao;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        User user = userDao.findByUsername(username);

        if (user == null || !user.getUsername().equalsIgnoreCase(username)) {
            throw new BadCredentialsException("Username not found.");
        }

        if (!password.equals(user.getPassword())) {
            throw new BadCredentialsException("Wrong password.");
        }

        return new UsernamePasswordAuthenticationToken(user, password, getUserGrantedAuthority(user));
    }

    private Collection<? extends GrantedAuthority> getUserGrantedAuthority(User user) {
        List<GrantedAuthority> grantedAuths = new ArrayList<>();


        if (user.getRole().equals(UserRole.ADMIN)) {
            grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        if (user.getRole().equals(UserRole.USER)) {
            grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return grantedAuths;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}