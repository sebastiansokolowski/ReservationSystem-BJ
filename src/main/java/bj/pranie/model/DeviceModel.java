package bj.pranie.model;

import bj.pranie.entity.User;
import bj.pranie.entity.myEnum.DeviceType;

/**
 * Created by Sebastian Sokolowski on 14.09.17.
 */
public class DeviceModel {
    public enum TYPE {
        UNAVAILABLE, PAST, RESERVED, FREE, MY
    }

    private long id;

    private DeviceType deviceType;

    private String name;

    private TYPE type;

    private User user;

    private long reservationId;

    private String color;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
