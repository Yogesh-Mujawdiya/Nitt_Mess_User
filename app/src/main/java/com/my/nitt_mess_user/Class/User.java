package com.my.nitt_mess_user.Class;

public class User {
    String Name, RollNo, Mobile, MessId, MessName;

    public User(String name, String rollNo, String mobile, String messId, String messName) {
        Name = name;
        RollNo = rollNo;
        Mobile = mobile;
        MessId = messId;
        MessName = messName;
    }



    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getRollNo() {
        return RollNo;
    }

    public void setRollNo(String rollNo) {
        RollNo = rollNo;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getMessId() {
        return MessId;
    }

    public void setMessId(String messId) {
        MessId = messId;
    }

    public String getMessName() {
        return MessName;
    }

    public void setMessName(String messName) {
        MessName = messName;
    }
}
