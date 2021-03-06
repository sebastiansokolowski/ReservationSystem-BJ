package bj.pranie.config;

import bj.pranie.config.exception.UserBlockedException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Sebastian Sokolowski on 24.10.16.
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String usernameOrEmail = authentication.getName();
        String password = (String) authentication.getCredentials();

        User user = getUser(usernameOrEmail);

        if (user == null) {
            throw new BadCredentialsException("Nieprawidłowy login lub email");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Nieprawidłowe hasło");
        }

        if (user.isBlocked()) {
            throw new UserBlockedException("Konto zostało zablokowane");
        }

        return new UsernamePasswordAuthenticationToken(user, password, getUserGrantedAuthority(user));
    }

    private User getUser(String usernameOrEmail) {
        User user = userDao.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userDao.findByEmail(usernameOrEmail);
        }

        return user;
    }

    private Collection<? extends GrantedAuthority> getUserGrantedAuthority(User user) {
        List<GrantedAuthority> grantedAuths = new ArrayList<>();


        if (user.getRole().equals(UserRole.ADMIN)) {
            grantedAuths.add(new SimpleGrantedAuthority("ADMIN"));
        }

        if (user.getRole().equals(UserRole.USER)) {
            grantedAuths.add(new SimpleGrantedAuthority("USER"));
        }

        if (user.getRole().equals(UserRole.GROUP)) {
            grantedAuths.add(new SimpleGrantedAuthority("GROUP"));
        }
        return grantedAuths;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }
}
