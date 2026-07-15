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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private int room;

    @Enumerated(EnumType.STRING)
    @NotNull
    private RoomType type;

    @NotNull
    private boolean students;

    @NotNull
    private int people;

    public Room() {
    }

    public Room(int room, RoomType type, boolean students, int people) {
        this.room = room;
        this.type = type;
        this.people = people;
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

    public boolean isStudents() {
        return students;
    }

    public void setStudents(boolean students) {
        this.students = students;
    }

    public int getPeople() {
        return people;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        return id == room.id;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", room=" + room +
                ", type=" + type +
                '}';
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
