package bj.pranie.dto;

import bj.pranie.entity.Room;

/**
 * Created by noon on 10.08.16.
 */
public class UserRegisterDto extends UserCredentialsDto {
    private Room room;
    private String name;
    private String email;
    private String retryPassword;

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRetryPassword() {
        return retryPassword;
    }

    public void setRetryPassword(String retryPassword) {
        this.retryPassword = retryPassword;
    }
}
