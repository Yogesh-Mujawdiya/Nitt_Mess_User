package com.my.nitt_mess_user.Class;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class LastTakenFood {
    String type, date, time;
    boolean feedBack;

    public LastTakenFood(String type, String date, String time, boolean feedBack) {
        this.type = type;
        this.date = date;
        this.time = time;
        this.feedBack = feedBack;
    }

    public String getType() {
        return type;
    }

    public boolean isBreakfast(){
        return type.equals("Breakfast");
    }
    public boolean isLunch(){
        return type.equals("Breakfast");
    }
    public boolean isDinner(){
        return type.equals("Breakfast");
    }
    public boolean isSnack(){
        return type.equals("Breakfast");
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public boolean isToday(){
        GregorianCalendar c = new GregorianCalendar();
        String Today = c.get(Calendar.DAY_OF_MONTH)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.YEAR);
        return date.equals(Today);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isFeedBack() {
        return feedBack;
    }

    public void setFeedBack(boolean feedBack) {
        this.feedBack = feedBack;
    }
}
