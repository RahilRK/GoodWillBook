package com.hksofttronix.goodwillbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.hksofttronix.goodwillbook.AddBusiness.addBusinessModel;
import com.hksofttronix.goodwillbook.ContactList.ContactModel;
import com.hksofttronix.goodwillbook.Feedback.SendFeedbackModel;
import com.hksofttronix.goodwillbook.Feedback.uploadFeedbackAttachments_Service;
import com.hksofttronix.goodwillbook.Home.HomeFrag.HomeFragModel;
import com.hksofttronix.goodwillbook.Pay.addAttachmentsModel;
import com.hksofttronix.goodwillbook.Remainder.AlarmReminderContract;
import com.hksofttronix.goodwillbook.Remainder.RemainderModel;
import com.hksofttronix.goodwillbook.Transaction.transactionModel;
import com.hksofttronix.goodwillbook.Util.appLogModel;
import com.hksofttronix.goodwillbook.Util.sendLog_Service;
import com.hksofttronix.goodwillbook.VolleyUtil.common.NotificationModel;

import java.util.ArrayList;
import java.util.List;

import static com.hksofttronix.goodwillbook.Globalclass.MyAlarmRemainderProvider;
import static com.hksofttronix.goodwillbook.Remainder.AlarmReminderContract.AlarmReminderEntry.CONTENT_URI;

public class Mydatabase extends SQLiteOpenHelper
{
    String TAG = this.getClass().getSimpleName();
    Context context;
    Globalclass globalclass;

    private static final String DATABASE_NAME="GoodWillBook";
    private static final int DATABASE_VERSION = 1;
    public static volatile Mydatabase mydatabase;

    //todo TABLES
    public String appLog = "appLog";
    public String businessaccountmaster = "businessaccountmaster";
    public String usermaster = "usermaster";
    public String attachmentmaster = "attachmentmaster";
    public String transactionmaster = "transactionmaster";
    public String homemaster = "homemaster";
    public String notificationmaster = "notificationmaster";
    public String sendfeedbackmaster = "sendfeedbackmaster";

    public Mydatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
        globalclass = Globalclass.getInstance(context);

    }

    public static Mydatabase getInstance(Context context) {

        if (mydatabase == null) { //Check for the first time

            synchronized (Mydatabase.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (mydatabase == null) mydatabase = new Mydatabase(context);
            }
        }

        return mydatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase sqldb) {

        globalclass.log(TAG,"onCreate");

        String query = "CREATE TABLE IF NOT EXISTS "+businessaccountmaster+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,businessacid INTEGER DEFAULT 0,userid INTEGER DEFAULT 0,name TEXT DEFAULT '',detail TEXT DEFAULT '',logourl TEXT DEFAULT '',location TEXT DEFAULT '',address TEXT DEFAULT '',contactnumber TEXT DEFAULT '',emailid TEXT DEFAULT '', pancardnumber TEXT DEFAULT '',logofilepath TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+appLog+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,Dir TEXT DEFAULT '',Name TEXT DEFAULT '',data TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+usermaster+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,userid INTEGER DEFAULT 0,name TEXT DEFAULT '',mobilenumber TEXT DEFAULT '',viewcontactNumber TEXT DEFAULT '',isverified INTEGER DEFAULT 0,contactImage TEXT DEFAULT '',goodwilluser INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+attachmentmaster+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,attachmentfilepath TEXT DEFAULT '',transactionid INTEGER DEFAULT 0,forupdate INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+transactionmaster+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,transactionid INTEGER DEFAULT 0,debituserid INTEGER DEFAULT 0,debitusername TEXT DEFAULT '',debitmobilenumber TEXT DEFAULT '',debitbusinessacid INTEGER DEFAULT 0," +
                "debitbusinessname TEXT DEFAULT '',debitdatetime TEXT DEFAULT '',debitlocation TEXT DEFAULT '',credituserid INTEGER DEFAULT 0,creditusername TEXT DEFAULT '',creditmobilenumber TEXT DEFAULT '',creditbusinessacid INTEGER DEFAULT 0,creditbusinessname TEXT DEFAULT '',creditdatetime TEXT DEFAULT '',creditlocation TEXT DEFAULT ''," +
                "amount INTEGER DEFAULT 0,remark TEXT DEFAULT '',isApproved INTEGER DEFAULT 0,debitdbdatetime TEXT DEFAULT '',creditdbdatetime TEXT DEFAULT '',lastupdatedatetime TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+homemaster+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,viewcontactNumber TEXT DEFAULT '',contactname TEXT DEFAULT '',mobilenumber TEXT DEFAULT '',username TEXT DEFAULT '',give_receive_sum INTEGER DEFAULT 0,isverified INTEGER DEFAULT 0,businessacid INTEGER DEFAULT 0,lastupdatedatetime TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+notificationmaster+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,notif_id INTEGER DEFAULT 0,type TEXT DEFAULT '',title TEXT DEFAULT '',text TEXT DEFAULT '',userid INTEGER DEFAULT 0,transactionid INTEGER DEFAULT 0,senddatetime TEXT DEFAULT '',hasread INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+sendfeedbackmaster+" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,shortdesc TEXT DEFAULT '',longdesc TEXT DEFAULT '',filepath TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS "+ AlarmReminderContract.AlarmReminderEntry.TABLE_NAME+" (_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,title TEXT DEFAULT '',mobilenumber TEXT DEFAULT '',date TEXT DEFAULT '',time TEXT DEFAULT '',active INTEGER DEFAULT 1)";
        sqldb.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        globalclass.log(TAG,"onUpgrade");
    }

    //todo appLog
    public boolean insertLog_inDB(appLogModel model)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("Dir",model.getDir());
            cv.put("Name",model.getName());
            cv.put("data",model.getData());

            sqldb.insert(appLog, null, cv);

            globalclass.startAnyService(context, sendLog_Service.class);
            return true;
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_insertLog_inDBException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","insertLog_inDBException","",error);
            return false;
        }
    }

    public int getAppLogList()
    {
        int total = 0;

        Cursor cursor = null;
        try
        {
            String query = "SELECT * FROM "+appLog+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getAppLogListException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAppLogListException","userid="+globalclass.getuserid(),error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    //todo businessaccountmaster
    public boolean addorupdateBusiness(addBusinessModel businessModel)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("businessacid",businessModel.getBusinessacid());
            cv.put("userid",globalclass.getuserid());
            cv.put("name",globalclass.checknull(businessModel.getName()));
            cv.put("detail",globalclass.checknull(businessModel.getDetail()));
            cv.put("logourl",globalclass.checknull(businessModel.getLogourl()));
            cv.put("location",globalclass.checknull(businessModel.getLocation()));
            cv.put("address",globalclass.checknull(businessModel.getAddress()));
            cv.put("contactnumber",globalclass.checknull(businessModel.getContactnumber()));
            cv.put("emailid",globalclass.checknull(businessModel.getEmailid()));
            cv.put("pancardnumber",globalclass.checknull(businessModel.getPancardnumber()));
            cv.put("logofilepath",globalclass.checknull(businessModel.getLogofilepath()));

            if(getBusinessCount(businessModel.getBusinessacid()) == 0)
            {
                sqldb.insert(businessaccountmaster, null, cv);
            }
            else
            {
                sqldb.update(businessaccountmaster, cv, "businessacid = ?",new String[]{String.valueOf(businessModel.getBusinessacid())});
            }

            return true;
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_addorupdateBusinessException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","addorupdateBusinessException","",error);
            return false;
        }
    }

    public ArrayList<addBusinessModel> getAllBusinessList()
    {
        ArrayList<addBusinessModel> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            String query = "SELECT  * FROM " + businessaccountmaster+" ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {

                    addBusinessModel businessModel = new addBusinessModel();
                    businessModel.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    businessModel.setBusinessacid(cursor.getInt(cursor.getColumnIndex("businessacid")));
                    businessModel.setUserid(cursor.getInt(cursor.getColumnIndex("userid")));
                    businessModel.setName(cursor.getString(cursor.getColumnIndex("name")));
                    businessModel.setDetail(cursor.getString(cursor.getColumnIndex("detail")));
                    businessModel.setLogourl(cursor.getString(cursor.getColumnIndex("logourl")));
                    businessModel.setLocation(cursor.getString(cursor.getColumnIndex("location")));
                    businessModel.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    businessModel.setContactnumber(cursor.getString(cursor.getColumnIndex("contactnumber")));
                    businessModel.setEmailid(cursor.getString(cursor.getColumnIndex("emailid")));
                    businessModel.setPancardnumber(cursor.getString(cursor.getColumnIndex("pancardnumber")));

                    arrayList.add(businessModel);
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getAllBusinessList",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAllBusinessList","",error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public ArrayList<addBusinessModel> getBusinessInfo(int businessacid)
    {
        ArrayList<addBusinessModel> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            String query = "SELECT  * FROM " + businessaccountmaster+" where businessacid = "+businessacid+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {

                    addBusinessModel businessModel = new addBusinessModel();
                    businessModel.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    businessModel.setBusinessacid(cursor.getInt(cursor.getColumnIndex("businessacid")));
                    businessModel.setUserid(cursor.getInt(cursor.getColumnIndex("userid")));
                    businessModel.setName(cursor.getString(cursor.getColumnIndex("name")));
                    businessModel.setDetail(cursor.getString(cursor.getColumnIndex("detail")));
                    businessModel.setLogourl(cursor.getString(cursor.getColumnIndex("logourl")));
                    businessModel.setLocation(cursor.getString(cursor.getColumnIndex("location")));
                    businessModel.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    businessModel.setContactnumber(cursor.getString(cursor.getColumnIndex("contactnumber")));
                    businessModel.setEmailid(cursor.getString(cursor.getColumnIndex("emailid")));
                    businessModel.setPancardnumber(cursor.getString(cursor.getColumnIndex("pancardnumber")));

                    arrayList.add(businessModel);

                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getBusinessInfo",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getBusinessInfo","",error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public int getBusinessCount(int businessacid)
    {
        int total = 0;
        Cursor cursor = null;
        try
        {
            String query = "SELECT count(id) as total from "+businessaccountmaster+"\n" +
                    "where businessacid = "+businessacid+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {

                    total =  cursor.getInt(cursor.getColumnIndex("total"));

                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getBusinessCountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getBusinessCountException","businessacid="+businessacid,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    public int getAllPendingUploadBusinessLogo()
    {
        int total = 0;

        Cursor cursor = null;
        try
        {
            String query = "SELECT * from businessaccountmaster\n" +
                    "WHERE logofilepath IS NOT NULL AND logofilepath != ''";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();
            globalclass.log(TAG,"Total "+total+" business's logo are pending to upload");
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getAllPendingUploadBusinessLogo",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAllPendingUploadBusinessLogo","",error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    //todo usermaster
    public boolean addorupdateUserDetail(ContactModel contactModel)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("userid",contactModel.getUserid());
            cv.put("name",globalclass.checknull(contactModel.getContactName()));
            cv.put("mobilenumber",globalclass.checknull(contactModel.getContactNumber()));
            cv.put("viewcontactNumber",globalclass.checknull(contactModel.getViewcontactNumber()));
            cv.put("isverified",globalclass.checknull(String.valueOf(contactModel.getIsverified())));
            cv.put("contactImage",globalclass.checknull(contactModel.getContactImage()));
            cv.put("goodwilluser",globalclass.checknull(String.valueOf(contactModel.getGoodwilluser())));

            if(getUserCount(contactModel) == 0)
            {
                sqldb.insert(usermaster, null, cv);
            }
            else
            {
                sqldb.update(usermaster, cv, "mobilenumber = ?",new String[]{String.valueOf(contactModel.getContactNumber())});
            }

            return true;
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_addorupdateUserDetailException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","addorupdateUserDetailException","",error);
            return false;
        }
    }

    public int getUserCount(ContactModel contactModel)
    {
        int total = 0;
        Cursor cursor = null;
        try
        {
            String query = "SELECT count(id) as total from "+usermaster+"\n" +
                    "where mobilenumber = "+contactModel.getContactNumber()+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {

                    total =  cursor.getInt(cursor.getColumnIndex("total"));

                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getUserCountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getUserCountException","mobilenumber="+contactModel.getContactNumber(),error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    public ArrayList<ContactModel> getAllContact()
    {
        ArrayList<ContactModel> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            String query = "SELECT  * FROM " + usermaster+" order by isverified DESC";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {

                    ContactModel contactModel = new ContactModel();
                    contactModel.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    contactModel.setUserid(cursor.getInt(cursor.getColumnIndex("userid")));
                    contactModel.setContactName(cursor.getString(cursor.getColumnIndex("name")));
                    contactModel.setContactNumber(cursor.getString(cursor.getColumnIndex("mobilenumber")));
                    contactModel.setViewcontactNumber(cursor.getString(cursor.getColumnIndex("viewcontactNumber")));
                    contactModel.setIsverified(cursor.getInt(cursor.getColumnIndex("isverified")));
                    contactModel.setContactImage(cursor.getString(cursor.getColumnIndex("contactImage")));
                    contactModel.setGoodwilluser(cursor.getInt(cursor.getColumnIndex("goodwilluser")));

                    if(!contactModel.getContactNumber().equalsIgnoreCase(globalclass.getmobilenumber()))
                    {
                        //don't show my own number in contact list
                        arrayList.add(contactModel);
                    }
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getAllContact",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAllContact","",error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public ArrayList<ContactModel> searchContact(String text)
    {
        ArrayList<ContactModel> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            String query = "SELECT * from usermaster\n" +
                    "where name LIKE '%"+text+"%'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {

                    ContactModel contactModel = new ContactModel();
                    contactModel.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    contactModel.setUserid(cursor.getInt(cursor.getColumnIndex("userid")));
                    contactModel.setContactName(cursor.getString(cursor.getColumnIndex("name")));
                    contactModel.setContactNumber(cursor.getString(cursor.getColumnIndex("mobilenumber")));
                    contactModel.setViewcontactNumber(cursor.getString(cursor.getColumnIndex("viewcontactNumber")));
                    contactModel.setIsverified(cursor.getInt(cursor.getColumnIndex("isverified")));
                    contactModel.setContactImage(cursor.getString(cursor.getColumnIndex("contactImage")));
                    contactModel.setGoodwilluser(cursor.getInt(cursor.getColumnIndex("goodwilluser")));

                    if(!contactModel.getContactNumber().equalsIgnoreCase(globalclass.getmobilenumber()))
                    {
                        //don't show my own number in contact list
                        arrayList.add(contactModel);
                    }
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getAllContact",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAllContact","",error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public ArrayList<ContactModel> getContactDetail(String mobilenumber)
    {
        ArrayList<ContactModel> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            String query = "SELECT * FROM usermaster\n" +
                    "WHERE mobilenumber = '"+mobilenumber+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {

                    ContactModel contactModel = new ContactModel();
                    contactModel.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    contactModel.setUserid(cursor.getInt(cursor.getColumnIndex("userid")));
                    contactModel.setContactName(cursor.getString(cursor.getColumnIndex("name")));
                    contactModel.setContactNumber(cursor.getString(cursor.getColumnIndex("mobilenumber")));
                    contactModel.setViewcontactNumber(cursor.getString(cursor.getColumnIndex("viewcontactNumber")));
                    contactModel.setIsverified(cursor.getInt(cursor.getColumnIndex("isverified")));
                    contactModel.setContactImage(cursor.getString(cursor.getColumnIndex("contactImage")));
                    contactModel.setGoodwilluser(cursor.getInt(cursor.getColumnIndex("goodwilluser")));

                    arrayList.add(contactModel);
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getContactDetail",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getContactDetail","",error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    //todo attachmentmaster
    public boolean addAttachments(addAttachmentsModel model)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("attachmentfilepath",model.getAttachmentfilepath());
            cv.put("transactionid",model.getTransactionid());
            cv.put("forupdate",model.getForupdate());

            sqldb.insert(attachmentmaster, null, cv);

            return true;
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_addAttachmentsException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","_addAttachmentsException","",error);
            return false;
        }
    }

    public int check_isAttachmentAvailable(int transactionid)
    {
        int total = 0;

        Cursor cursor = null;
        try
        {
            String query = "SELECT * FROM attachmentmaster\n" +
                    "where transactionid = "+transactionid+" ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();
            globalclass.log(TAG,total+" attachments are available from transactionid: "+transactionid);
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_check_isAttachmentAvailable",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","_check_isAttachmentAvailable", String.valueOf(transactionid),error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    public int getAttachmentList()
    {
        int total = 0;

        Cursor cursor = null;
        try
        {
            String query = "SELECT * from attachmentmaster";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getAttachmentList",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAttachmentList","",error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    //todo transactionmaster
    public boolean addorupdateTransactionDetail(transactionModel model)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("transactionid",globalclass.checknull(String.valueOf(model.getTransactionid())));
            cv.put("debituserid",globalclass.checknull(String.valueOf(model.getDebituserid())));
            cv.put("debitusername",globalclass.checknull(model.getDebitusername()));
            cv.put("debitmobilenumber",globalclass.checknull(model.getDebitmobilenumber()));
            cv.put("debitbusinessacid",globalclass.checknull(String.valueOf(model.getDebitbusinessacid())));
            cv.put("debitbusinessname",globalclass.checknull(model.getDebitbusinessname()));
            cv.put("debitdatetime",globalclass.checknull(model.getDebitdatetime()));
            cv.put("debitlocation",globalclass.checknull(model.getDebitlocation()));
            cv.put("credituserid",globalclass.checknull(String.valueOf(model.getCredituserid())));
            cv.put("creditusername",globalclass.checknull(model.getCreditusername()));
            cv.put("creditmobilenumber",globalclass.checknull(model.getCreditmobilenumber()));
            cv.put("creditbusinessacid",globalclass.checknull(String.valueOf(model.getCreditbusinessacid())));
            cv.put("creditbusinessname",globalclass.checknull(model.getCreditbusinessname()));
            cv.put("creditdatetime",globalclass.checknull(model.getCreditdatetime()));
            cv.put("creditlocation",globalclass.checknull(model.getCreditlocation()));
            cv.put("amount",globalclass.checknull(String.valueOf(model.getAmount())));
            cv.put("remark",globalclass.checknull(model.getRemark()));
            cv.put("isApproved",globalclass.checknull(String.valueOf(model.getIsApproved())));
            cv.put("debitdbdatetime",globalclass.checknull(model.getDebitdbdatetime()));
            cv.put("creditdbdatetime",globalclass.checknull(model.getCreditdbdatetime()));
            cv.put("lastupdatedatetime",globalclass.checknull(model.getLastupdatedatetime()));

            if(getTransactionCount(model) == 0)
            {
                sqldb.insert(transactionmaster, null, cv);
            }
            else
            {
                sqldb.update(transactionmaster, cv, "transactionid = ?",new String[]{String.valueOf(model.getTransactionid())});
            }

            return true;
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_addorupdateTransactionDetailException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","addorupdateTransactionDetailException","",error);
            return false;
        }
    }

    public int getTransactionCount(transactionModel model)
    {
        int total = 0;

        Cursor cursor = null;
        try
        {
            String query = "SELECT * FROM `transactionmaster` WHERE transactionid = "+model.getTransactionid()+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getTransactionCountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getTransactionCountException","transactionid="+model.getTransactionid(),error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    public int getTransactionList()
    {
        int total = 0;

        Cursor cursor = null;
        try
        {
            String query = "SELECT * FROM `transactionmaster`";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getTransactionListException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getTransactionListException","userid="+globalclass.getuserid(),error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    public ArrayList<transactionModel> getCustomerTransaction(String customerMobileNumber,String startdate,String enddate ,String orderbytext)
    {
        ArrayList<transactionModel> arrayList = new ArrayList<>();
        String query = "";

        Cursor cursor = null;
        try
        {
            query = "SELECT * from transactionmaster\n" +
                    "WHERE \n" +
                    "(debitbusinessacid = "+globalclass.getActivebusinessacid()+" OR creditbusinessacid = "+globalclass.getActivebusinessacid()+")\n" +
                    "AND\n" +
                    "(debitmobilenumber = "+customerMobileNumber+" OR creditmobilenumber = "+customerMobileNumber+")\n" +
                    "AND\n" +
                    "(debitdatetime BETWEEN '"+startdate+" 00:00:00' AND '"+enddate+" 23:59:59'  OR\n" +
                    "creditdatetime BETWEEN '"+startdate+" 00:00:00' AND '"+enddate+" 23:59:59')\n" +
                    "ORDER BY "+orderbytext+"";

//            globalclass.log(TAG,query);

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(cursor.getCount() > 0)
            {
                if (cursor.moveToFirst()) {
                    do {

                        transactionModel model = new transactionModel();
                        model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        model.setTransactionid(cursor.getInt(cursor.getColumnIndex("transactionid")));
                        model.setDebituserid(cursor.getInt(cursor.getColumnIndex("debituserid")));
                        model.setDebitusername(cursor.getString(cursor.getColumnIndex("debitusername")));
                        model.setDebitmobilenumber(cursor.getString(cursor.getColumnIndex("debitmobilenumber")));
                        model.setDebitbusinessacid(cursor.getInt(cursor.getColumnIndex("debitbusinessacid")));
                        model.setDebitbusinessname(cursor.getString(cursor.getColumnIndex("debitbusinessname")));
                        model.setDebitdatetime(cursor.getString(cursor.getColumnIndex("debitdatetime")));
                        model.setDebitlocation(cursor.getString(cursor.getColumnIndex("debitlocation")));
                        model.setCredituserid(cursor.getInt(cursor.getColumnIndex("credituserid")));
                        model.setCreditusername(cursor.getString(cursor.getColumnIndex("creditusername")));
                        model.setCreditmobilenumber(cursor.getString(cursor.getColumnIndex("creditmobilenumber")));
                        model.setCreditbusinessacid(cursor.getInt(cursor.getColumnIndex("creditbusinessacid")));
                        model.setCreditbusinessname(cursor.getString(cursor.getColumnIndex("creditbusinessname")));
                        model.setCreditdatetime(cursor.getString(cursor.getColumnIndex("creditdatetime")));
                        model.setCreditlocation(cursor.getString(cursor.getColumnIndex("creditlocation")));
                        model.setAmount(cursor.getInt(cursor.getColumnIndex("amount")));
                        model.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
                        model.setIsApproved(cursor.getInt(cursor.getColumnIndex("isApproved")));
                        model.setDebitdbdatetime(cursor.getString(cursor.getColumnIndex("debitdbdatetime")));
                        model.setCreditdbdatetime(cursor.getString(cursor.getColumnIndex("creditdbdatetime")));
                        model.setLastupdatedatetime(cursor.getString(cursor.getColumnIndex("lastupdatedatetime")));


                        arrayList.add(model);

                    } while (cursor.moveToNext());
                }
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getCustomerTransactionException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getCustomerTransaction",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public ArrayList<transactionModel> getTransactionDetail(int transactionid)
    {
        ArrayList<transactionModel> arrayList = new ArrayList<>();
        String query = "";

        Cursor cursor = null;
        try
        {
            query = "SELECT * FROM "+transactionmaster+"\n" +
                    "WHERE transactionid = "+transactionid+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(cursor.getCount() > 0)
            {
                if (cursor.moveToFirst()) {
                    do {

                        transactionModel model = new transactionModel();
                        model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        model.setTransactionid(cursor.getInt(cursor.getColumnIndex("transactionid")));
                        model.setDebituserid(cursor.getInt(cursor.getColumnIndex("debituserid")));
                        model.setDebitusername(cursor.getString(cursor.getColumnIndex("debitusername")));
                        model.setDebitmobilenumber(cursor.getString(cursor.getColumnIndex("debitmobilenumber")));
                        model.setDebitbusinessacid(cursor.getInt(cursor.getColumnIndex("debitbusinessacid")));
                        model.setDebitbusinessname(cursor.getString(cursor.getColumnIndex("debitbusinessname")));
                        model.setDebitdatetime(cursor.getString(cursor.getColumnIndex("debitdatetime")));
                        model.setDebitlocation(cursor.getString(cursor.getColumnIndex("debitlocation")));
                        model.setCredituserid(cursor.getInt(cursor.getColumnIndex("credituserid")));
                        model.setCreditusername(cursor.getString(cursor.getColumnIndex("creditusername")));
                        model.setCreditmobilenumber(cursor.getString(cursor.getColumnIndex("creditmobilenumber")));
                        model.setCreditbusinessacid(cursor.getInt(cursor.getColumnIndex("creditbusinessacid")));
                        model.setCreditbusinessname(cursor.getString(cursor.getColumnIndex("creditbusinessname")));
                        model.setCreditdatetime(cursor.getString(cursor.getColumnIndex("creditdatetime")));
                        model.setCreditlocation(cursor.getString(cursor.getColumnIndex("creditlocation")));
                        model.setAmount(cursor.getInt(cursor.getColumnIndex("amount")));
                        model.setRemark(cursor.getString(cursor.getColumnIndex("remark")));
                        model.setIsApproved(cursor.getInt(cursor.getColumnIndex("isApproved")));
                        model.setDebitdbdatetime(cursor.getString(cursor.getColumnIndex("debitdbdatetime")));
                        model.setCreditdbdatetime(cursor.getString(cursor.getColumnIndex("creditdbdatetime")));
                        model.setLastupdatedatetime(cursor.getString(cursor.getColumnIndex("lastupdatedatetime")));

                        arrayList.add(model);

                    } while (cursor.moveToNext());
                }
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getCustomerTransactionException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getCustomerTransaction",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public int getTotalDebitAmount(String customerMobileNumber)
    {
        String TotalDebitAmount = "0";
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "SELECT sum(amount)  as TotalDebitAmount from "+transactionmaster+"\n" +
                    "WHERE isApproved = 1\n" +
                    "AND debitbusinessacid = "+globalclass.getActivebusinessacid()+"\n" +
                    "AND\n" +
                    "(debitmobilenumber = "+customerMobileNumber+" OR creditmobilenumber = "+customerMobileNumber+") ";

//            globalclass.log(TAG,query);

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if (cursor.moveToFirst()) {
                do {

                    TotalDebitAmount =  cursor.getString(cursor.getColumnIndex("TotalDebitAmount"));
                    if(TotalDebitAmount == null)
                    {
                        TotalDebitAmount = "0";
                    }

                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getTotalDebitAmountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getTotalDebitAmountException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return Integer.parseInt(TotalDebitAmount);
    }

    public int getTotalCreditAmount(String customerMobileNumber)
    {
        String TotalCreditAmount = "0";
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "SELECT sum(amount)  as TotalCreditAmount from "+transactionmaster+"\n" +
                    "WHERE isApproved = 1\n" +
                    "AND creditbusinessacid = "+globalclass.getActivebusinessacid()+"\n" +
                    "AND\n" +
                    "(debitmobilenumber = "+customerMobileNumber+" OR creditmobilenumber = "+customerMobileNumber+") ";

//            globalclass.log(TAG,query);

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if (cursor.moveToFirst()) {
                do {

                    TotalCreditAmount =  cursor.getString(cursor.getColumnIndex("TotalCreditAmount"));
                    if(TotalCreditAmount == null)
                    {
                        TotalCreditAmount = "0";
                    }

                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getTotalCreditAmountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getTotalCreditAmountException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return Integer.parseInt(TotalCreditAmount);
    }

    public int getTotalDebitAmountwithFilter(String customerMobileNumber,String startdate,String enddate)
    {
        String TotalDebitAmount = "0";
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "SELECT sum(amount)  as TotalDebitAmount from "+transactionmaster+"\n" +
                    "WHERE isApproved = 1\n" +
                    "AND debitbusinessacid = "+globalclass.getActivebusinessacid()+"\n" +
                    "AND\n" +
                    "(debitmobilenumber = "+customerMobileNumber+" OR creditmobilenumber = "+customerMobileNumber+") " +
                    "AND\n" +
                    " (debitdatetime BETWEEN '"+startdate+" 00:00:00' AND '"+enddate+" 23:59:59')";

//            globalclass.log(TAG,query);

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if (cursor.moveToFirst()) {
                do {

                    TotalDebitAmount =  cursor.getString(cursor.getColumnIndex("TotalDebitAmount"));
                    if(TotalDebitAmount == null)
                    {
                        TotalDebitAmount = "0";
                    }

                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getTotalDebitAmountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getTotalDebitAmountException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return Integer.parseInt(TotalDebitAmount);
    }

    public int getTotalCreditAmountwithFilter(String customerMobileNumber,String startdate,String enddate)
    {
        String TotalCreditAmount = "0";
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "SELECT sum(amount)  as TotalCreditAmount from "+transactionmaster+"\n" +
                    "WHERE isApproved = 1\n" +
                    "AND creditbusinessacid = "+globalclass.getActivebusinessacid()+"\n" +
                    "AND\n" +
                    "(debitmobilenumber = "+customerMobileNumber+" OR creditmobilenumber = "+customerMobileNumber+") " +
                    "AND\n" +
                    " (creditdatetime BETWEEN '"+startdate+" 00:00:00' AND '"+enddate+" 23:59:59')";

//            globalclass.log(TAG,query);

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if (cursor.moveToFirst()) {
                do {

                    TotalCreditAmount =  cursor.getString(cursor.getColumnIndex("TotalCreditAmount"));
                    if(TotalCreditAmount == null)
                    {
                        TotalCreditAmount = "0";
                    }

                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getTotalCreditAmountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getTotalCreditAmountException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return Integer.parseInt(TotalCreditAmount);
    }

    //todo homemaster
    public void addDataToHomeMaster(String activeBusinessAcId)
    {
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select viewcontactNumber,name as contactname,debitmobilenumber as mobilenumber,debitusername as username\n" +
                    "from transactionmaster t\n" +
                    "INNER JOIN usermaster u on u.mobilenumber = t.debitmobilenumber\n" +
                    "where debitbusinessacid = "+activeBusinessAcId+" or creditbusinessacid = "+activeBusinessAcId+"\n" +
                    "UNION\n" +
                    "select viewcontactNumber,name as contactname,creditmobilenumber as mobilenumber,creditusername as username\n" +
                    "from transactionmaster t\n" +
                    "INNER JOIN usermaster u on u.mobilenumber = t.creditmobilenumber\n" +
                    "where debitbusinessacid = "+activeBusinessAcId+" or creditbusinessacid = "+activeBusinessAcId+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(getTransactionList()>0) //first check, any transaction had occured!
            {
                if(cursor.getCount()>0)
                {
                    if (cursor.moveToFirst()) {
                        do {

                            HomeFragModel model = new HomeFragModel();
                            model.setViewcontactNumber(cursor.getString(cursor.getColumnIndex("viewcontactNumber")));
                            model.setContactname(cursor.getString(cursor.getColumnIndex("contactname")));
                            model.setMobilenumber(cursor.getString(cursor.getColumnIndex("mobilenumber")));
                            model.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                            model.setGive_receive_sum(globalclass.getCustomerSum(model.getMobilenumber()));
                            model.setIsverified(getContactDetail(model.getMobilenumber()).get(0).getIsverified());
                            model.setBusinessacid(Integer.parseInt(activeBusinessAcId));
                            model.setLastupdatedatetime(getHomeLastUpdateDateTime(model));

                            if(!model.getMobilenumber().equalsIgnoreCase(globalclass.getmobilenumber()))
                            {
                                //don't show my own number in contact list
                                addorupdateHomeMaster(model);
                            }

                        } while (cursor.moveToNext());
                    }
                }

            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getHomeDataException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getHomeDataException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }
    }

    public boolean addorupdateHomeMaster(HomeFragModel model)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("viewcontactNumber",model.getViewcontactNumber());
            cv.put("contactname",model.getContactname());
            cv.put("mobilenumber",model.getMobilenumber());
            cv.put("username",model.getUsername());
            cv.put("give_receive_sum",model.getGive_receive_sum());
            cv.put("isverified",model.getIsverified());
            cv.put("businessacid",model.getBusinessacid());
            cv.put("lastupdatedatetime",model.getLastupdatedatetime());

            if(checkMobileNumberAndBussAcIdInHomeMaster(model) == 0)
            {
                sqldb.insert(homemaster, null, cv);
            }
            else
            {
                sqldb.update(homemaster, cv, "mobilenumber =? and businessacid = ?",new String[]{String.valueOf(model.getMobilenumber()),String.valueOf(model.getBusinessacid())});
            }

            return true;
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_addorupdateHomeMasterException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","addorupdateHomeMasterException","",error);
            return false;
        }
    }

    public int checkMobileNumberAndBussAcIdInHomeMaster(HomeFragModel model)
    {
        int total = 0;

        Cursor cursor = null;
        try
        {
            String query = "SELECT * from "+homemaster+" where mobilenumber = "+model.getMobilenumber()+" and businessacid = "+model.getBusinessacid()+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_checkMobileNumberInHomeMasterException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","checkMobileNumberInHomeMasterException","",error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    public String getHomeLastUpdateDateTime(HomeFragModel model)
    {
        String lastupdatedatetime = "";
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select max(lastupdatedatetime) as lastupdatedatetime from transactionmaster\n" +
                    "where debitmobilenumber = "+model.getMobilenumber()+" or creditmobilenumber = "+model.getMobilenumber()+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if (cursor.moveToFirst()) {
                do {

                    lastupdatedatetime =  cursor.getString(cursor.getColumnIndex("lastupdatedatetime"));

                } while (cursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getHomeLastUpdateDateTimeException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getHomeLastUpdateDateTimeException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return lastupdatedatetime;
    }

    public ArrayList<HomeFragModel> getHomeList(String text, String sort_filter)
    {
        ArrayList<HomeFragModel> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try
        {
            if(sort_filter.equalsIgnoreCase(""))
            {
                if(text.equalsIgnoreCase(""))
                {
                    query = "select * from "+homemaster+" where businessacid = "+globalclass.getActivebusinessacid()+" order by lastupdatedatetime DESC";
                }
                else
                {
                    query = "select * from "+homemaster+" where businessacid = "+globalclass.getActivebusinessacid()+" AND contactname LIKE '%"+text+"%'";
                }
            }
            else
            {
                query = "select * from "+homemaster+" where businessacid = "+globalclass.getActivebusinessacid()+" AND give_receive_sum "+sort_filter+" 0";
                globalclass.log(TAG,query);
            }


            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(getTransactionList()>0) //first check, any transaction had occured!
            {
                if(cursor.getCount()>0)
                {
                    if (cursor.moveToFirst()) {
                        do {
                            HomeFragModel model = new HomeFragModel();
                            model.setViewcontactNumber(cursor.getString(cursor.getColumnIndex("viewcontactNumber")));
                            model.setContactname(cursor.getString(cursor.getColumnIndex("contactname")));
                            model.setMobilenumber(cursor.getString(cursor.getColumnIndex("mobilenumber")));
                            model.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                            model.setGive_receive_sum(cursor.getInt(cursor.getColumnIndex("give_receive_sum")));
                            model.setIsverified(cursor.getInt(cursor.getColumnIndex("isverified")));
                            model.setBusinessacid(cursor.getInt(cursor.getColumnIndex("businessacid")));

                            if(!model.getMobilenumber().equalsIgnoreCase(globalclass.getmobilenumber()))
                            {
                                //don't show my own number in contact list
                                arrayList.add(model);
                            }


                        } while (cursor.moveToNext());
                    }
                }

            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getHomeDataException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getHomeDataException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public int getHomeSumBusinessWise(String activeBusinessAcId,String LessthenGreaterthen)
    {
        String sum = "0";

        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select sum(give_receive_sum) as sum from homemaster\n" +
                    "where give_receive_sum "+LessthenGreaterthen+" 0\n" +
                    "AND businessacid = "+activeBusinessAcId+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(getTransactionList()>0) //first check, any transaction had occured!
            {
                if(cursor.getCount()>0)
                {
                    if (cursor.moveToFirst()) {
                        do {

                            sum = cursor.getString(cursor.getColumnIndex("sum"));
                            if(sum == null)
                            {
                                sum = "0";
                            }

                        } while (cursor.moveToNext());
                    }
                }

            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getHomeSumBusinessWiseException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getHomeSumBusinessWiseException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return Integer.parseInt(sum);
    }

    public ArrayList<HomeFragModel> getHomeDetail(String mobilenumber)
    {
        ArrayList<HomeFragModel> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select * from "+homemaster+" where mobilenumber = "+mobilenumber+" ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(getTransactionList()>0) //first check, any transaction had occured!
            {
                if(cursor.getCount()>0)
                {
                    if (cursor.moveToFirst()) {
                        do {
                            HomeFragModel model = new HomeFragModel();
                            model.setViewcontactNumber(cursor.getString(cursor.getColumnIndex("viewcontactNumber")));
                            model.setContactname(cursor.getString(cursor.getColumnIndex("contactname")));
                            model.setMobilenumber(cursor.getString(cursor.getColumnIndex("mobilenumber")));
                            model.setUsername(cursor.getString(cursor.getColumnIndex("username")));
                            model.setGive_receive_sum(cursor.getInt(cursor.getColumnIndex("give_receive_sum")));
                            model.setIsverified(cursor.getInt(cursor.getColumnIndex("isverified")));
                            model.setBusinessacid(cursor.getInt(cursor.getColumnIndex("businessacid")));

                            if(!model.getMobilenumber().equalsIgnoreCase(globalclass.getmobilenumber()))
                            {
                                //don't show my own number in contact list
                                arrayList.add(model);
                            }


                        } while (cursor.moveToNext());
                    }
                }

            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getHomeDataException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getHomeDataException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    //todo notificationmaster
    public boolean addorupdateNotificationDetail(NotificationModel model)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("notif_id",model.getNotif_id());
            cv.put("userid",model.getUserid());
            cv.put("transactionid",model.getTransactionid());
            cv.put("type",globalclass.checknull(model.getType()));
            cv.put("title",globalclass.checknull(model.getTitle()));
            cv.put("text",globalclass.checknull(model.getText()));
            cv.put("senddatetime",globalclass.checknull(model.getSenddatetime()));
            cv.put("hasread",model.getHasread());

            if(getNotificationCount(model) == 0)
            {
                sqldb.insert(notificationmaster, null, cv);
            }
            else
            {
                sqldb.update(notificationmaster, cv, "notif_id = ?",new String[]{String.valueOf(model.getNotif_id())});
            }

            return true;
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_addorupdateNotificationDetailException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","addorupdateNotificationDetailException","",error);
            return false;
        }
    }

    public int getNotificationCount(NotificationModel model)
    {
        int total = 0;
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "SELECT * FROM "+notificationmaster+" WHERE notif_id = "+model.getNotif_id()+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getNotificationCountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getNotificationCountException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    public ArrayList<NotificationModel> getNotificationList()
    {
        ArrayList<NotificationModel> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select * from "+notificationmaster+" where userid = "+globalclass.getuserid()+" order by senddatetime DESC";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(getTransactionList()>0) //first check, any transaction had occured!
            {
                if(cursor.getCount()>0)
                {
                    if (cursor.moveToFirst()) {
                        do {

                            NotificationModel model = new NotificationModel();
                            model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                            model.setNotif_id(cursor.getInt(cursor.getColumnIndex("notif_id")));
                            model.setUserid(cursor.getInt(cursor.getColumnIndex("userid")));
                            model.setTransactionid(cursor.getInt(cursor.getColumnIndex("transactionid")));
                            model.setType(cursor.getString(cursor.getColumnIndex("type")));
                            model.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                            model.setText(cursor.getString(cursor.getColumnIndex("text")));
                            model.setSenddatetime(cursor.getString(cursor.getColumnIndex("senddatetime")));
                            model.setHasread(cursor.getInt(cursor.getColumnIndex("hasread")));

                            arrayList.add(model);

                        } while (cursor.moveToNext());
                    }
                }

            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getNotificationListException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getNotificationListException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public int getUnreadNotificationCount()
    {
        String total = "0";

        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select count(id) as total from notificationmaster\n" +
                    "where hasread = 0";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(getTransactionList()>0) //first check, any transaction had occured!
            {
                if(cursor.getCount()>0)
                {
                    if (cursor.moveToFirst()) {
                        do {

                            total = cursor.getString(cursor.getColumnIndex("total"));
                            if(total == null)
                            {
                                total = "0";
                            }

                        } while (cursor.moveToNext());
                    }
                }

            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getUnreadNotificationCountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getUnreadNotificationCountException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return Integer.parseInt(total);
    }

    //todo sendfeedbackmaster
    public boolean insertfeedback(SendFeedbackModel model)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("shortdesc",model.getShortdesc());
            cv.put("longdesc",model.getLongdesc());
            cv.put("filepath",model.getFilepath());

            sqldb.insert(sendfeedbackmaster, null, cv);

            globalclass.startAnyService(context, uploadFeedbackAttachments_Service.class);
            return true;
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_insertLog_inDBException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","insertLog_inDBException","",error);
            return false;
        }
    }

    public int getFeedbackCount()
    {
        int total = 0;

        Cursor cursor = null;
        try
        {
            String query = "SELECT * FROM "+sendfeedbackmaster+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getTransactionListException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getTransactionListException","userid="+globalclass.getuserid(),error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    //todo remaindermaster
    public ArrayList<RemainderModel> getRemainderList()
    {
        ArrayList<RemainderModel> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select * from remaindermaster order by _id ASC";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(cursor.getCount()>0)
            {
                if (cursor.moveToFirst()) {
                    do {

                        RemainderModel model = new RemainderModel();
                        model.setId(cursor.getInt(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry._ID)));
                        model.setTitle(cursor.getString(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE)));
                        model.setDate(cursor.getString(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE)));
                        model.setTime(cursor.getString(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME)));
                        model.setMobilenumber(cursor.getString(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_MOBNO)));
                        model.setActive(cursor.getInt(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE)));

                        arrayList.add(model);

                    } while (cursor.moveToNext());
                }
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getNotificationListException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getNotificationListException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    public int checkRemainderAlreadySet(String mobilenumber)
    {
        int total = 0;
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "SELECT * FROM remaindermaster WHERE mobilenumber = "+mobilenumber+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            total = cursor.getCount();
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getNotificationCountException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getNotificationCountException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return total;
    }

    public ArrayList<RemainderModel> getRemainderDetail(String mobilenumber)
    {
        ArrayList<RemainderModel> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select * from remaindermaster WHERE mobilenumber = "+mobilenumber+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(cursor.getCount()>0)
            {
                if (cursor.moveToFirst()) {
                    do {

                        RemainderModel model = new RemainderModel();
                        model.setId(cursor.getInt(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry._ID)));
                        model.setTitle(cursor.getString(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE)));
                        model.setDate(cursor.getString(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE)));
                        model.setTime(cursor.getString(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME)));
                        model.setMobilenumber(cursor.getString(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_MOBNO)));
                        model.setActive(cursor.getInt(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE)));

                        arrayList.add(model);

                    } while (cursor.moveToNext());
                }
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getNotificationListException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getNotificationListException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }

        return arrayList;
    }

    //todo delete
    public void deleteData(String tablename, String columnname, String value)
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();
            String query="Delete from "+tablename+" where "+columnname+" = '"+ value +"'";
            sqldb.execSQL(query);
            globalclass.log(TAG+"_deleteData",query);
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_deleteData",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","deleteData","",error);
        }

    }

    public void deleteUserMaster() //this is called to remove entry from homemaster/refresh home, when any transaction is been removed
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();
            String query="Delete from "+usermaster+"";
            sqldb.execSQL(query);
            globalclass.log(TAG+"_deleteUserMaster",query);
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_deleteData",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","deleteData","",error);
        }

    }

    public void deleteHomeMaster() //this is called to remove entry from homemaster/refresh home, when any transaction is been removed
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();
            String query="Delete from "+homemaster+"";
            sqldb.execSQL(query);
            globalclass.log(TAG+"_deleteHomeMaster",query);
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_deleteData",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","deleteData","",error);
        }

    }

    public void deleteTransactionMaster() //this is called to remove entry from homemaster/refresh home, when any transaction is been removed
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();
            String query="Delete from "+transactionmaster+"";
            sqldb.execSQL(query);
            globalclass.log(TAG+"_deleteTransactionMaster",query);
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_deleteData",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","deleteData","",error);
        }

    }

    public void deleteNotificationMaster() //this is called to remove entry from homemaster/refresh home, when any transaction is been removed
    {
        try
        {
            SQLiteDatabase sqldb = this.getWritableDatabase();
            String query="Delete from "+notificationmaster+"";
            sqldb.execSQL(query);
            globalclass.log(TAG+"_deleteNotificationMaster",query);
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_deleteData",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","deleteData","",error);
        }

    }

    public void removeAllRemainders()
    {
        String query = "";
        Cursor cursor = null;
        try
        {
            query = "select * from remaindermaster order by _id ASC";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(cursor.getCount()>0)
            {
                if (cursor.moveToFirst()) {
                    do {

                        RemainderModel model = new RemainderModel();
                        model.setId(cursor.getInt(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry._ID)));

                        Uri removedUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(model.getId()));
                        int rowsDeleted = context.getContentResolver().delete(removedUri, null, null);
                        globalclass.cancelRemainderAlarm(context, Globalclass.Remainder_ACTION,Globalclass.Remainder_requestID, removedUri);

                        if (rowsDeleted == 0)
                        {
                            // fail or error occured
                            String error = "Unable to removeAllRemainders";
                            globalclass.log(TAG,error);
                            globalclass.sendLog(MyAlarmRemainderProvider,TAG,"","removeAllRemainders","",error);
                        }
                        else
                        {
                            //success
                            globalclass.log(TAG,"Deleted Remainder Uri: "+removedUri);
                        }

                    } while (cursor.moveToNext());
                }
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getNotificationListException",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getNotificationListException",query,error);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }
    }

    public void clearDatabase()
    {
        SQLiteDatabase sqldb = this.getWritableDatabase();
        Cursor cursor = sqldb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        //noinspection TryFinallyCanBeTryWithResources not available with API < 19
        try {
            List<String> tables = new ArrayList<>(cursor.getCount());

            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0));
            }

            for (String table : tables) {
                if (table.startsWith("sqlite_") || table.equalsIgnoreCase("notification")) {
                    continue;
                }
                sqldb.execSQL("DROP TABLE IF EXISTS " + table);
                globalclass.log(TAG+"_cleardatabase", "Dropped table " + table);
            }
        } finally {
            if(cursor != null) { cursor.close(); }
            onCreate(sqldb);
        }
    }
}
