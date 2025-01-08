package bj.pranie.entity;

import bj.pranie.entity.myEnum.DeviceType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Created by Sebastian Sokolowski on 15.10.16.
 */
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.ORDINAL)
    @NotNull
    private DeviceType deviceType;

    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return id == device.id && deviceType == device.deviceType && Objects.equals(name, device.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceType, name);
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", deviceType=" + deviceType +
                ", name='" + name + '\'' +
                '}';
    }
}
