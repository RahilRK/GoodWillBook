package com.hksofttronix.goodwillbook.Pay;

import android.os.Parcel;
import android.os.Parcelable;

public class addAttachmentsModel implements Parcelable
{
    String attachmentfilepath,url;
    int transactionid,attachmentid,forupdate;
    boolean choosefromStorage;

    public addAttachmentsModel() {
    }

    protected addAttachmentsModel(Parcel in) {
        attachmentfilepath = in.readString();
        url = in.readString();
        transactionid = in.readInt();
        attachmentid = in.readInt();
        forupdate = in.readInt();
        choosefromStorage = in.readByte() != 0;
    }

    public static final Creator<addAttachmentsModel> CREATOR = new Creator<addAttachmentsModel>() {
        @Override
        public addAttachmentsModel createFromParcel(Parcel in) {
            return new addAttachmentsModel(in);
        }

        @Override
        public addAttachmentsModel[] newArray(int size) {
            return new addAttachmentsModel[size];
        }
    };

    public String getAttachmentfilepath() {
        return attachmentfilepath;
    }

    public void setAttachmentfilepath(String attachmentfilepath) {
        this.attachmentfilepath = attachmentfilepath;
    }

    public int getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(int transactionid) {
        this.transactionid = transactionid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getAttachmentid() {
        return attachmentid;
    }

    public void setAttachmentid(int attachmentid) {
        this.attachmentid = attachmentid;
    }

    public boolean getChoosefromStorage() {
        return choosefromStorage;
    }

    public void setChoosefromStorage(boolean choosefromStorage) {
        this.choosefromStorage = choosefromStorage;
    }

    public int getForupdate() {
        return forupdate;
    }

    public void setForupdate(int forupdate) {
        this.forupdate = forupdate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(attachmentfilepath);
        dest.writeString(url);
        dest.writeInt(transactionid);
        dest.writeInt(attachmentid);
        dest.writeInt(forupdate);
        dest.writeByte((byte) (choosefromStorage ? 1 : 0));
    }
}
