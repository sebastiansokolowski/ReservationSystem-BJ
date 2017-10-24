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
    PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        User user = userDao.findByUsername(username);

        if (user == null || !user.getUsername().equalsIgnoreCase(username)) {
            throw new BadCredentialsException("Username not found.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Wrong password.");
        }

        return new UsernamePasswordAuthenticationToken(user, password, getUserGrantedAuthority(user));
    }

    private Collection<? extends GrantedAuthority> getUserGrantedAuthority(User user) {
        List<GrantedAuthority> grantedAuths = new ArrayList<>();


        if (user.getRole().equals(UserRole.ADMIN)) {
            grantedAuths.add(new SimpleGrantedAuthority("ADMIN"));
        }

        if (user.getRole().equals(UserRole.USER)) {
            grantedAuths.add(new SimpleGrantedAuthority("USER"));
        }
        return grantedAuths;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(
                UsernamePasswordAuthenticationToken.class);
    }
}
