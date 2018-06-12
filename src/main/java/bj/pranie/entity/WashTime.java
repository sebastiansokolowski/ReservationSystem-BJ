package bj.pranie.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Time;

/**
 * Created by Sebastian Sokolowski on 06.09.17.
 */
@Entity
@Table(name = "wash_times")
public class WashTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private Time fromTime;

    @NotNull
    private Time toTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Time getFromTime() {
        return fromTime;
    }

    public void setFromTime(Time fromTime) {
        this.fromTime = fromTime;
    }

    public Time getToTime() {
        return toTime;
    }

    public void setToTime(Time toTime) {
        this.toTime = toTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WashTime washTime = (WashTime) o;

        return id == washTime.id;
    }

    @Override
    public String toString() {
        return "WashTime{" +
                "id=" + id +
                ", fromTime=" + fromTime +
                ", toTime=" + toTime +
                '}';
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
