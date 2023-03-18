package bj.pranie.model;

import java.util.List;

/**
 * Created by Sebastian Sokolowski on 14.09.17.
 */
public class TimeWeekModel {

    private String time;
    private List<Date> dates;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<Date> getDates() {
        return dates;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    public class Date {
        String date;
        String color;
        int freeDevices;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public int getFreeDevices() {
            return freeDevices;
        }

        public void setFreeDevices(int freeDevices) {
            this.freeDevices = freeDevices;
        }
    }
}
