package bj.pranie.model;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
public class RestorePasswordModel {

    @Email(message = "Proszę podać prawidłowy adres email.")
    @NotNull
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }
}
