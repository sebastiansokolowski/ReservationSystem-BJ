package bj.pranie.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

/**
 * Created by noon on 15.10.16.
 */
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    private int wm;

    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    @NotNull
    private Date date;

    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "wash_time_id")
    @NotNull
    private WashTime washTime ;

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
}
