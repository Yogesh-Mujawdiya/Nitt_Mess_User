package com.my.nitt_mess_user.Class;

public class Leave {
    String id, from, to, reason, rollNo, messId, status;

    public Leave(String from, String to, String reason, String rollNo, String messId, String status) {
        this.from = from;
        this.to = to;
        this.reason = reason;
        this.rollNo = rollNo;
        this.messId = messId;
        this.status = status;
    }

    public Leave(String id, String from, String to, String reason, String rollNo, String messId, String status) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.reason = reason;
        this.rollNo = rollNo;
        this.messId = messId;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getMessId() {
        return messId;
    }

    public void setMessId(String messId) {
        this.messId = messId;
    }
}
