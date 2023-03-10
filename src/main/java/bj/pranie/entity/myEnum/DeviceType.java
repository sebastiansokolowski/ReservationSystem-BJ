package bj.pranie.entity.myEnum;

/**
 * Created by Sebastian Sokolowski on 01.10.17.
 */
public enum DeviceType {
    WASHING_MACHINE("wm"), TUMBLE_DRYER("td");

    private final String path;

    DeviceType(String path) {
        this.path = path;
    }

    public String getPathName() {
        return path;
    }
}
