package bj.pranie.model;

import bj.pranie.entity.User;

/**
 * Created by Sebastian Sokolowski on 14.09.17.
 */
public class WmModel {
    public enum TYPE {
        PAST, RESERVED, FREE, BROKEN
    }

    TYPE type;

    User user;

    String color;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
