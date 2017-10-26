package bj.pranie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by Sebastian Sokolowski on 25.10.17.
 */
@Configuration
public class CommonBeanConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CustomAuthenticationFailureHandler authenticationHandler() {
        return new CustomAuthenticationFailureHandler();
    }
}
