package com.hksofttronix.goodwillbook.AddBusiness;

public class addBusinessModel
{
    int id,businessacid,userid;
    String name,detail,logourl,location,address,contactnumber,emailid,pancardnumber;
    String logofilepath = "";

    public addBusinessModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBusinessacid() {
        return businessacid;
    }

    public void setBusinessacid(int businessacid) {
        this.businessacid = businessacid;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getLogourl() {
        return logourl;
    }

    public void setLogourl(String logourl) {
        this.logourl = logourl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactnumber() {
        return contactnumber;
    }

    public void setContactnumber(String contactnumber) {
        this.contactnumber = contactnumber;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public String getPancardnumber() {
        return pancardnumber;
    }

    public void setPancardnumber(String pancardnumber) {
        this.pancardnumber = pancardnumber;
    }

    public String getLogofilepath() {
        return logofilepath;
    }

    public void setLogofilepath(String logofilepath) {
        this.logofilepath = logofilepath;
    }
}
