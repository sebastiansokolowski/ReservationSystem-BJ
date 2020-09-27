package bj.pranie.model;

import javax.validation.constraints.NotNull;

/**
 * Created by Sebastian Sokolowski on 13.11.18.
 */
public class RoomEditModel {

    @NotNull
    private Long id;

    @NotNull
    private Integer room;

    @NotNull
    private Integer typeId;

    @NotNull
    private Boolean students;

    @NotNull
    private Integer peoples;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRoom() {
        return room;
    }

    public void setRoom(Integer room) {
        this.room = room;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Boolean getStudents() {
        return students;
    }

    public void setStudents(Boolean students) {
        this.students = students;
    }

    public Integer getPeoples() {
        return peoples;
    }

    public void setPeoples(Integer peoples) {
        this.peoples = peoples;
    }
}
