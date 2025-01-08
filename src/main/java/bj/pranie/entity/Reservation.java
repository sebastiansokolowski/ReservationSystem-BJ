package bj.pranie.entity;

import bj.pranie.entity.myEnum.ReservationType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.Objects;

/**
 * Created by Sebastian Sokolowski on 15.10.16.
 */
@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id")
    @NotNull
    private Device device;

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
    @JoinColumn(name = "reservation_time_id")
    @NotNull
    private ReservationTime reservationTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
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

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(ReservationTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id && Objects.equals(device, that.device) && Objects.equals(user, that.user) && type == that.type && Objects.equals(date, that.date) && Objects.equals(reservationTime, that.reservationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, device, user, type, date, reservationTime);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", device=" + device +
                ", user=" + user +
                ", type=" + type +
                ", date=" + date +
                ", reservationTime=" + reservationTime +
                '}';
    }
}
