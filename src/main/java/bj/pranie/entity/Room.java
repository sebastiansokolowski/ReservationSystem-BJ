package bj.pranie.entity;

import bj.pranie.entity.myEnum.RoomType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by Sebastian Sokolowski on 15.10.16.
 */
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private int room;

    @Enumerated(EnumType.STRING)
    @NotNull
    private RoomType type;

    @NotNull
    private int peoples;

    public Room() {
    }

    public Room(int room, RoomType type, int peoples) {
        this.room = room;
        this.type = type;
        this.peoples = peoples;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public int getPeoples() {
        return peoples;
    }

    public void setPeoples(int peoples) {
        this.peoples = peoples;
    }
}
