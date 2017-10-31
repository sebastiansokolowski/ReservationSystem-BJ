package bj.pranie.model;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * Created by Sebastian Sokolowski on 01.10.17.
 */
public class UserSettingsModel {

    @NotNull
    private boolean setNewUsername;

    @Length(min = 3, max = 30, message = "Nazwa użytkownika musi być dłuższe niż 3 znaki i krótsza niż 30.")
    private String newUsername;

    @NotNull
    private boolean setNewEmail;

    @Email(message = "Błędny adres email.")
    private String newEmail;

    @NotNull
    private boolean setNewPassword;

    @Length(min = 5, max = 30, message = "Hasło musi być dłuższe niż 3 znaki i krótsze niż 30.")
    private String newPassword;

    private String newPasswordRepeat;

    @NotNull(message = "Proszę podać aktualnie używane hasło")
    private String password;

    public boolean isSetNewUsername() {
        return setNewUsername;
    }

    public void setSetNewUsername(boolean setNewUsername) {
        this.setNewUsername = setNewUsername;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public boolean isSetNewEmail() {
        return setNewEmail;
    }

    public void setSetNewEmail(boolean setNewEmail) {
        this.setNewEmail = setNewEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public boolean isSetNewPassword() {
        return setNewPassword;
    }

    public void setSetNewPassword(boolean setNewPassword) {
        this.setNewPassword = setNewPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordRepeat() {
        return newPasswordRepeat;
    }

    public void setNewPasswordRepeat(String newPasswordRepeat) {
        this.newPasswordRepeat = newPasswordRepeat;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
