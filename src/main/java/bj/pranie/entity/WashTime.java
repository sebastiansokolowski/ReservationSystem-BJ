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
    @GeneratedValue(strategy = GenerationType.AUTO)
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
}
