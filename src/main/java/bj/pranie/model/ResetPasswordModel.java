package bj.pranie.model;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * Created by Sebastian Sokolowski on 10.08.16.
 */
public class ResetPasswordModel {

    @Length(min = 5, max = 30, message = "Hasło musi być dłuższe niż 3 znaki i krótsze niż 30.")
    @NotNull
    private String newPassword;

    @NotNull
    private String newPasswordRepeat;

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
}
