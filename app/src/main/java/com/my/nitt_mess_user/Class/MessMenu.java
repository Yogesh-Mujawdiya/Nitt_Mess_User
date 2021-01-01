package com.my.nitt_mess_user.Class;

public class MessMenu {
    String WeekName, Breakfast, Lunch, Dinner, Snack;

    public MessMenu(String weekName, String breakfast, String lunch, String dinner, String snack) {
        WeekName = weekName;
        Breakfast = breakfast;
        Lunch = lunch;
        Dinner = dinner;
        Snack = snack;
    }

    public String getWeekName() {
        return WeekName;
    }

    public void setWeekName(String weekName) {
        WeekName = weekName;
    }

    public String getBreakfast() {
        return Breakfast;
    }

    public void setBreakfast(String breakfast) {
        Breakfast = breakfast;
    }

    public String getLunch() {
        return Lunch;
    }

    public void setLunch(String lunch) {
        Lunch = lunch;
    }

    public String getDinner() {
        return Dinner;
    }

    public void setDinner(String dinner) {
        Dinner = dinner;
    }

    public String getSnack() {
        return Snack;
    }

    public void setSnack(String snack) {
        Snack = snack;
    }
}
