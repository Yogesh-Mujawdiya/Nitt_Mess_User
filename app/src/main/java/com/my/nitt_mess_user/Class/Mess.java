package com.my.nitt_mess_user.Class;

import java.util.Dictionary;

public class Mess {
    String Id, Name;
    int TotalCount, Allocate;
    Dictionary Menu;

    public Mess(String id, String name, int totalCount, int allocate, Dictionary menu) {
        Id = id;
        Name = name;
        TotalCount = totalCount;
        Allocate = allocate;
        Menu = menu;
    }

    public Dictionary getMenu() {
        return Menu;
    }

    public void setMenu(Dictionary menu) {
        Menu = menu;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getTotalCount() {
        return TotalCount;
    }

    public void setTotalCount(int totalCount) {
        TotalCount = totalCount;
    }

    public int getAllocate() {
        return Allocate;
    }

    public void setAllocate(int allocate) {
        Allocate = allocate;
    }

    public boolean isFull(){
        return TotalCount==Allocate;
    }
}
