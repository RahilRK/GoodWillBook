package com.hksofttronix.goodwillbook.Transaction;

public class transactionModel
{
    int id,transactionid,debituserid,debitbusinessacid,credituserid,creditbusinessacid,amount,isApproved;
    String debitusername,debitbusinessname,debitdatetime,debitlocation,creditusername,creditbusinessname,creditdatetime,creditlocation,
            remark,debitdbdatetime,creditdbdatetime,debitmobilenumber,creditmobilenumber,lastupdatedatetime;

    public transactionModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(int transactionid) {
        this.transactionid = transactionid;
    }

    public int getDebituserid() {
        return debituserid;
    }

    public void setDebituserid(int debituserid) {
        this.debituserid = debituserid;
    }

    public int getDebitbusinessacid() {
        return debitbusinessacid;
    }

    public void setDebitbusinessacid(int debitbusinessacid) {
        this.debitbusinessacid = debitbusinessacid;
    }

    public int getCredituserid() {
        return credituserid;
    }

    public void setCredituserid(int credituserid) {
        this.credituserid = credituserid;
    }

    public int getCreditbusinessacid() {
        return creditbusinessacid;
    }

    public void setCreditbusinessacid(int creditbusinessacid) {
        this.creditbusinessacid = creditbusinessacid;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(int isApproved) {
        this.isApproved = isApproved;
    }

    public String getDebitusername() {
        return debitusername;
    }

    public void setDebitusername(String debitusername) {
        this.debitusername = debitusername;
    }

    public String getDebitbusinessname() {
        return debitbusinessname;
    }

    public void setDebitbusinessname(String debitbusinessname) {
        this.debitbusinessname = debitbusinessname;
    }

    public String getDebitdatetime() {
        return debitdatetime;
    }

    public void setDebitdatetime(String debitdatetime) {
        this.debitdatetime = debitdatetime;
    }

    public String getDebitlocation() {
        return debitlocation;
    }

    public void setDebitlocation(String debitlocation) {
        this.debitlocation = debitlocation;
    }

    public String getCreditusername() {
        return creditusername;
    }

    public void setCreditusername(String creditusername) {
        this.creditusername = creditusername;
    }

    public String getCreditbusinessname() {
        return creditbusinessname;
    }

    public void setCreditbusinessname(String creditbusinessname) {
        this.creditbusinessname = creditbusinessname;
    }

    public String getCreditdatetime() {
        return creditdatetime;
    }

    public void setCreditdatetime(String creditdatetime) {
        this.creditdatetime = creditdatetime;
    }

    public String getCreditlocation() {
        return creditlocation;
    }

    public void setCreditlocation(String creditlocation) {
        this.creditlocation = creditlocation;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDebitdbdatetime() {
        return debitdbdatetime;
    }

    public void setDebitdbdatetime(String debitdbdatetime) {
        this.debitdbdatetime = debitdbdatetime;
    }

    public String getCreditdbdatetime() {
        return creditdbdatetime;
    }

    public void setCreditdbdatetime(String creditdbdatetime) {
        this.creditdbdatetime = creditdbdatetime;
    }

    public String getDebitmobilenumber() {
        return debitmobilenumber;
    }

    public void setDebitmobilenumber(String debitmobilenumber) {
        this.debitmobilenumber = debitmobilenumber;
    }

    public String getCreditmobilenumber() {
        return creditmobilenumber;
    }

    public void setCreditmobilenumber(String creditmobilenumber) {
        this.creditmobilenumber = creditmobilenumber;
    }

    public String getLastupdatedatetime() {
        return lastupdatedatetime;
    }

    public void setLastupdatedatetime(String lastupdatedatetime) {
        this.lastupdatedatetime = lastupdatedatetime;
    }
}
