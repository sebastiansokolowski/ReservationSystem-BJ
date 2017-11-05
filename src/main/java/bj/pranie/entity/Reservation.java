package bj.pranie.entity;

import bj.pranie.entity.myEnum.ReservationType;
import bj.pranie.entity.myEnum.RoomType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

/**
 * Created by Sebastian Sokolowski on 15.10.16.
 */
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private int wm;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ReservationType type;


    @NotNull
    private Date date;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "wash_time_id")
    @NotNull
    private WashTime washTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getWm() {
        return wm;
    }

    public void setWm(int wm) {
        this.wm = wm;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ReservationType getType() {
        return type;
    }

    public void setType(ReservationType type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public WashTime getWashTime() {
        return washTime;
    }

    public void setWashTime(WashTime washTime) {
        this.washTime = washTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reservation that = (Reservation) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
