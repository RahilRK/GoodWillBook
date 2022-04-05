package com.hksofttronix.goodwillbook.ContactList;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.hksofttronix.goodwillbook.Globalclass.RefreshAllRemainder;

public class contactList_Service extends Service
{
    String TAG = this.getClass().getSimpleName();

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    Context context = contactList_Service.this;

    ArrayList<ContactModel> contact_arrayList = new ArrayList();
    ArrayList<ContactModel> goodwill_arrayList = new ArrayList();
    ArrayList<ContactModel> arrayList = new ArrayList();

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    public static final int notif_id = 1002;

    String NotifTitle = "Syncing contacts";
    String NotifText = "Please wait...";
    int success_fail_notifid = 22222;

    boolean show_notification = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();
        if(extras != null)
        {
            show_notification = extras.getBoolean("show_notification");
            Log.e(TAG, "Has extras: "+show_notification);
        }
        else
        {
            show_notification = true;
            Log.e(TAG, "Has extras not available");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
        createNotification();
        globalclass.log(TAG,"onCreate");

        if(!globalclass.isInternetPresent() || !globalclass.userHadLoggedIn())
        {
            globalclass.log(TAG,"Service stop due to no internet connection or user not logged in!");
            globalclass.setAlarmFor_contactList_Service();
            stopSelf();
            return;
        }

        getAllContacts get = new getAllContacts();
        get.execute();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(context);
        mydatabase = Mydatabase.getInstance(context);
        volleyApiCall = new VolleyApiCall(context);
        notificationManager = NotificationManagerCompat.from(context);
    }

    void createNotification()
    {
        notification = new NotificationCompat.Builder(this, Globalclass.uploadBusinessLogo_Service_Notifi_Channel_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NotifTitle)
                .setContentText(NotifText)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setProgress(100,0,false);


        startForeground(notif_id, notification.build());
        notificationManager.notify(notif_id,notification.build());
    }

    void updateNotification(int progress_percentage)
    {
        notification
                .setProgress(100,progress_percentage,false)
                .setContentTitle(NotifTitle)
                .setContentText(NotifText);

        notificationManager.notify(notif_id,notification.build());
    }

    class getAllContacts extends AsyncTask<Void,Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try
            {
                ContactModel contactModel;

                String contactname = "";
                String contactnumber = "";

                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {

                        int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                        if (hasPhoneNumber > 0)
                        {
                            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            contactname = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)); //get contact name

                            contactModel = new ContactModel();
                            contactModel.setContactName(contactname);

                            //get contact number
                            Cursor phoneCursor = contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{id},
                                    null);

                            if (phoneCursor.moveToNext())
                            {
                                contactnumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                contactModel.setViewcontactNumber(contactnumber); //original string

                                contactnumber = contactnumber.replaceAll(globalclass.getContactfilter_regex(),""); //remove extra sign
                                if(contactnumber.length()>=10)
                                {
                                    contactnumber = contactnumber.substring(contactnumber.length() - 10); //get last 10 charactera
                                }

                                contactModel.setContactNumber(contactnumber); //trim string
                            }

                            //get contact image
                            Cursor imageCursor = null;
                            try
                            {
                                imageCursor = context.getContentResolver().query(
                                        ContactsContract.Data.CONTENT_URI,
                                        null,
                                        ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                                                + ContactsContract.Data.MIMETYPE + "='"
                                                + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                                        null);
                                if (imageCursor != null)
                                {
                                    if (!imageCursor.moveToFirst())
                                    {
                                        // no photo
                                    }
                                } else {
                                    // error in cursor process
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
                                    .parseLong(id));
                            Uri imageuri = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                            contactModel.setContactImage(String.valueOf(imageuri));

                            globalclass.log(TAG,"contactname: "+contactModel.getContactName()+", contactnumber: "+contactModel.getContactNumber()+", contactimage: "+contactModel.getContactImage());
                            phoneCursor.close();
                            imageCursor.close();

                            if(contactModel.getContactNumber().length() == 10)
                            {
                                String getmobileno = contactModel.getContactNumber();
                                String firstchar=getmobileno.substring(0,1);

                                if(firstchar.equalsIgnoreCase("6") ||
                                        firstchar.equalsIgnoreCase("7") ||
                                        firstchar.equalsIgnoreCase("8") ||
                                        firstchar.equalsIgnoreCase("9"))
                                {
                                    contact_arrayList.add(contactModel);
                                }
                            }
                        }
                    }

                }
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(TAG+"_getAllContacts_Exception",error);
                globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAllContacts_Exception","",error);
                stopService(1,success_fail_notifid,"Contact sync failed (Tap here to retry)","Something went wrong in syncing contacts, please try again later",null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(!contact_arrayList.isEmpty())
            {
                getAllZeroCreditUser();
            }
            else
            {
                stopSelf();
            }
        }
    }

    void getAllZeroCreditUser()
    {
        final String url = globalclass.getUrl()+"getAllZeroCreditUser";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log(TAG+"_getAllZeroCreditUser_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            mydatabase.deleteUserMaster();

                            JSONArray data_jsonArray = jsonObject.getJSONArray("data");
                            for(int i=0;i<data_jsonArray.length();i++)
                            {
                                JSONObject data_object = data_jsonArray.getJSONObject(i);
                                int userid = data_object.getInt("userid");
                                String name = data_object.getString("name");
                                String mobilenumber = data_object.getString("mobilenumber");
                                int isverified = data_object.getInt("isverified");

                                ContactModel contactModel = new ContactModel();
                                contactModel.setUserid(userid);
                                contactModel.setContactNumber(mobilenumber);
                                contactModel.setIsverified(isverified);

                                goodwill_arrayList.add(contactModel);
                            }

                            syncdata();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.log(TAG,getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllZeroCreditUser_ErrorInFunction",params.toString(),message);
                                stopService(1,success_fail_notifid,"Contact sync failed (Tap here to retry)",context.getResources().getString(R.string.api_function_error),null);
                            }
                            else
                            {
                                globalclass.log(TAG,message);
                                stopSelf();
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log(TAG+"_getAllZeroCreditUser_JSONException",error);
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllZeroCreditUser_JSONException",params.toString(),error);
                        stopService(1,success_fail_notifid,"Contact sync failed (Tap here to retry)",context.getResources().getString(R.string.JSONException_string),null);
                    }
                }
            }
        });
    }

    void syncdata()
    {
        try
        {
            if(!contact_arrayList.isEmpty() && !goodwill_arrayList.isEmpty())
            {
                int sum = 0;
                int total_contactlist = contact_arrayList.size();
                int progress_percentage = 0;

                for(int i=0;i<contact_arrayList.size();i++)
                {
                    String contactname = contact_arrayList.get(i).getContactName();
                    String contactnumber = contact_arrayList.get(i).getContactNumber();
                    String viewcontactNumber = contact_arrayList.get(i).getViewcontactNumber();
                    String contactimage = contact_arrayList.get(i).getContactImage();

                    int sizeof = goodwill_arrayList.size();
                    for(int j=0;j<goodwill_arrayList.size();j++)
                    {
                        if(goodwill_arrayList.get(j).getContactNumber().equalsIgnoreCase(contactnumber))
                        {
                            ContactModel contactModel = new ContactModel();
                            contactModel.setUserid(goodwill_arrayList.get(j).getUserid());
                            contactModel.setContactName(contactname);
                            contactModel.setContactNumber(contactnumber);
                            contactModel.setViewcontactNumber(viewcontactNumber);
                            contactModel.setIsverified(goodwill_arrayList.get(j).getIsverified());
                            contactModel.setContactImage(contactimage);
                            contactModel.setGoodwilluser(1);

                            mydatabase.addorupdateUserDetail(contactModel);
                            arrayList.add(contactModel);

                            //progress_percentage code
                            sum++;
                            progress_percentage = sum * 100 / total_contactlist;
                            globalclass.log(TAG+"_progress_percentage", String.valueOf(progress_percentage));
//                            SystemClock.sleep(50);
                            updateNotification(progress_percentage);
                            NotifText = String.valueOf(progress_percentage)+"%";
                            if(progress_percentage == 100)
                            {
                                Intent customIntent = new Intent(context,Contactlist.class);
                                stopService(0, success_fail_notifid,"Synced successfully","Contact synced successfully",customIntent);
                            }
                            break;
                        }
                        else
                        {
                            if(sizeof == j+1)
                            {
                                ContactModel contactModel = new ContactModel();
                                contactModel.setUserid(0);
                                contactModel.setContactName(contactname);
                                contactModel.setContactNumber(contactnumber);
                                contactModel.setViewcontactNumber(viewcontactNumber);
                                contactModel.setIsverified(0);
                                contactModel.setContactImage(contactimage);
                                contactModel.setGoodwilluser(0);

                                mydatabase.addorupdateUserDetail(contactModel);
                                arrayList.add(contactModel);


                                //progress_percentage code
                                sum++;
                                progress_percentage = sum * 100 / total_contactlist;
                                globalclass.log(TAG+"_progress_percentage", String.valueOf(progress_percentage));
//                                SystemClock.sleep(50);
                                updateNotification(progress_percentage);
                                NotifText = String.valueOf(progress_percentage)+"%";
                                if(progress_percentage == 100)
                                {
                                    Intent customIntent = new Intent(context,Contactlist.class);
                                    stopService(0, success_fail_notifid,"Synced successfully","Contact synced successfully",customIntent);
                                }
                                sizeof = 0;
                            }
                        }
                    }
                }
            }

            arrayList(arrayList);
            globalclass.log(TAG,"contact sync successfully");
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getAllContacts_Exception",error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAllContacts_Exception","",error);
            stopService(1,success_fail_notifid,"Contact sync failed (Tap here to retry)","Something went wrong in syncing contacts, please try again later",null);
        }
    }

    void arrayList(ArrayList<ContactModel> arrayList)
    {
        for(int i=0;i<arrayList.size();i++)
        {
            globalclass.log(TAG+"_arrayList",
                    "userid: "+arrayList.get(i).getUserid()+
                            ", contactName: "+arrayList.get(i).getContactName()+
                            ", contactNumber: "+arrayList.get(i).getContactNumber()+
                            ", viewcontactNumber: "+arrayList.get(i).getViewcontactNumber()+
                            ", isverified: "+arrayList.get(i).getIsverified()+
                            ", goodwilluser: "+arrayList.get(i).getGoodwilluser());
        }

        globalclass.log("arrayList_contactsize", String.valueOf(contact_arrayList.size()));
        globalclass.log("arrayList_size", String.valueOf(arrayList.size()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.log(TAG,"onDestroy");
        globalclass.callContactSyncCompleteReceiver();
    }

    void stopService(int error,int notif_id,String ContentTitle,String ContentText, Intent customIntent)
    {
        if(error == 0)
        {
            if(show_notification)
            {
                globalclass.showUploadSuccessNotif(context,notif_id, ContentTitle, ContentText,customIntent);
            }
            else
            {
                globalclass.sendLog(RefreshAllRemainder,TAG,"","stopService","","All contacts has been sync successfully of userid = "+globalclass.getuserid()+" on "+globalclass.getLongToDatetime(globalclass.getMilliSecond(),"dd-MM-yyyy hh:mm:ss aa"));
            }
        }
        else
        {
            if(show_notification)
            {
                globalclass.showUploadFailedNotif(context,notif_id,ContentTitle , ContentText);
            }

            globalclass.setAlarmFor_contactList_Service();
        }

        stopSelf();
    }
}
