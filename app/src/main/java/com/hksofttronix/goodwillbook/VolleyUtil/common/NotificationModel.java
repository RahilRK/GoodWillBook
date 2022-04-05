package com.hksofttronix.goodwillbook.VolleyUtil.common;

public class NotificationModel
{
    int id,notif_id,userid,transactionid,hasread;
    String type,title,text,senddatetime;
    boolean isSelected;

    public NotificationModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(int transactionid) {
        this.transactionid = transactionid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenddatetime() {
        return senddatetime;
    }

    public void setSenddatetime(String senddatetime) {
        this.senddatetime = senddatetime;
    }

    public int getNotif_id() {
        return notif_id;
    }

    public void setNotif_id(int notif_id) {
        this.notif_id = notif_id;
    }

    public int getHasread() {
        return hasread;
    }

    public void setHasread(int hasread) {
        this.hasread = hasread;
    }

    public boolean getisSelected() {
        return isSelected;
    }

    public void setisSelected(boolean selected) {
        isSelected = selected;
    }
}
