package bj.pranie.entity;

import bj.pranie.entity.myEnum.DeviceType;
import bj.pranie.entity.myEnum.ReservationType;

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
    private int deviceNumber;

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private DeviceType deviceType;

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

    public int getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(int deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
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

        return id == that.id;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", deviceNumber=" + deviceNumber +
                ", deviceType=" + deviceType +
                ", user=" + user +
                ", type=" + type +
                ", date=" + date +
                ", reservationTime=" + reservationTime +
                '}';
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
