package bj.pranie.model;

import java.util.List;

/**
 * Created by Sebastian Sokolowski on 14.09.17.
 */
public class TimeWeekModel {

    private String time;
    private List<WmDate> dates;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<WmDate> getDates() {
        return dates;
    }

    public void setDates(List<WmDate> dates) {
        this.dates = dates;
    }

    public class WmDate {
        String date;
        String color;
        int wmFree;

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

        public int getWmFree() {
            return wmFree;
        }

        public void setWmFree(int wmFree) {
            this.wmFree = wmFree;
        }
    }
}
