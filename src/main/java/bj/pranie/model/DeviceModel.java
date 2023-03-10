package bj.pranie.model;

import bj.pranie.entity.User;

/**
 * Created by Sebastian Sokolowski on 14.09.17.
 */
public class DeviceModel {
    public enum TYPE {
        UNAVAILABLE, PAST, RESERVED, FREE, MY
    }

    private TYPE type;

    private User user;

    private long reservationId;

    private String color;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
