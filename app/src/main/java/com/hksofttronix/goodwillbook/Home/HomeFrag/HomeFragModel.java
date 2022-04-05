package com.hksofttronix.goodwillbook.Home.HomeFrag;

public class HomeFragModel
{
    String viewcontactNumber,contactname,mobilenumber,username,lastupdatedatetime;
    int give_receive_sum,isverified,businessacid;

    public HomeFragModel() {
    }

    public String getViewcontactNumber() {
        return viewcontactNumber;
    }

    public void setViewcontactNumber(String viewcontactNumber) {
        this.viewcontactNumber = viewcontactNumber;
    }

    public String getContactname() {
        return contactname;
    }

    public void setContactname(String contactname) {
        this.contactname = contactname;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getGive_receive_sum() {
        return give_receive_sum;
    }

    public void setGive_receive_sum(int give_receive_sum) {
        this.give_receive_sum = give_receive_sum;
    }

    public int getIsverified() {
        return isverified;
    }

    public void setIsverified(int isverified) {
        this.isverified = isverified;
    }

    public int getBusinessacid() {
        return businessacid;
    }

    public void setBusinessacid(int businessacid) {
        this.businessacid = businessacid;
    }

    public String getLastupdatedatetime() {
        return lastupdatedatetime;
    }

    public void setLastupdatedatetime(String lastupdatedatetime) {
        this.lastupdatedatetime = lastupdatedatetime;
    }
}
