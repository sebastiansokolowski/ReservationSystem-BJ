package bj.pranie.config;

import bj.pranie.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Sebastian Sokolowski on 13.10.16.
 */
@Configuration
@ComponentScan
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        List<String> permitUrls = new ArrayList<>(Arrays.asList("/",
                "/logout",
                "/week", "/week/*",
                "/wm/*/*/*/*/",
                "/user/restorePassword", "/user/resetPassword",
                "/images/*",
                "/favicon.ico",
                "/js/*"));

        List<String> denyUrls = new ArrayList<>();

        if (Application.HOLIDAYS) {
            denyUrls.add("/user/regulations");
            denyUrls.add("/user/registration");
            denyUrls.add("/user/settings");
        } else {
            permitUrls.add("/user/regulations");
            permitUrls.add("/user/registration");
            permitUrls.add("/user/settings");
        }

        http.authorizeRequests()
                .antMatchers(permitUrls.toArray(new String[permitUrls.size()])).permitAll()
                .antMatchers(denyUrls.toArray(new String[denyUrls.size()])).denyAll()
                .antMatchers("/admin").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/week")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll();
    }

    @Autowired
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider);
    }
}