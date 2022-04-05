package com.hksofttronix.goodwillbook.ContactList;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactModel implements Parcelable {

    int id,userid,isverified,goodwilluser;
    String contactName,contactNumber,contactImage;
    String viewcontactNumber;
    int randomcolor;

    public ContactModel() {
    }

    protected ContactModel(Parcel in) {
        id = in.readInt();
        userid = in.readInt();
        isverified = in.readInt();
        goodwilluser = in.readInt();
        contactName = in.readString();
        contactNumber = in.readString();
        contactImage = in.readString();
        viewcontactNumber = in.readString();
        randomcolor = in.readInt();
    }

    public static final Creator<ContactModel> CREATOR = new Creator<ContactModel>() {
        @Override
        public ContactModel createFromParcel(Parcel in) {
            return new ContactModel(in);
        }

        @Override
        public ContactModel[] newArray(int size) {
            return new ContactModel[size];
        }
    };

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactImage() {
        return contactImage;
    }

    public void setContactImage(String contactImage) {
        this.contactImage = contactImage;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getIsverified() {
        return isverified;
    }

    public void setIsverified(int isverified) {
        this.isverified = isverified;
    }

    public String getViewcontactNumber() {
        return viewcontactNumber;
    }

    public void setViewcontactNumber(String viewcontactNumber) {
        this.viewcontactNumber = viewcontactNumber;
    }

    public int getGoodwilluser() {
        return goodwilluser;
    }

    public void setGoodwilluser(int goodwilluser) {
        this.goodwilluser = goodwilluser;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRandomcolor() {
        return randomcolor;
    }

    public void setRandomcolor(int randomcolor) {
        this.randomcolor = randomcolor;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(userid);
        dest.writeInt(isverified);
        dest.writeInt(goodwilluser);
        dest.writeString(contactName);
        dest.writeString(contactNumber);
        dest.writeString(contactImage);
        dest.writeString(viewcontactNumber);
        dest.writeInt(randomcolor);
    }
}
