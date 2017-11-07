package bj.pranie.model;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * Created by Sebastian Sokolowski on 13.09.17.
 */
public class UserRegistrationModel {
    @Length(min = 2, max = 30, message = "Imię musi być dłuższe niż 2 znaki i krótsze niż 30.")
    @NotNull
    private String name;

    @Length(min = 2, max = 30, message = "Nazwisko musi być dłuższe niż 2 znaki i krótsze niż 30.")
    @NotNull
    private String surname;

    @Email(message = "Błędny adres email.")
    @NotNull
    private String email;

    @Length(min = 3, max = 30, message = "Nazwa użytkownika musi być dłuższe niż 3 znaki i krótsza niż 30.")
    @NotNull
    private String username;

    @Length(min = 5, max = 30, message = "Hasło musi być dłuższe niż 3 znaki i krótsze niż 30.")
    @NotNull
    private String password;

    @NotNull
    private String passwordRepeat;

    @NotNull
    private Long roomId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.trim();
    }

    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    public void setPasswordRepeat(String passwordRepeat) {
        this.passwordRepeat = passwordRepeat.trim();
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
