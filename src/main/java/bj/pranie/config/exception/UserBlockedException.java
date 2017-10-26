package bj.pranie.config.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Created by Sebastian Sokolowski on 26.10.17.
 */
public class UserBlockedException extends AuthenticationException {
    public UserBlockedException(String msg, Throwable t) {
        super(msg, t);
    }

    public UserBlockedException(String msg) {
        super(msg);
    }
}
