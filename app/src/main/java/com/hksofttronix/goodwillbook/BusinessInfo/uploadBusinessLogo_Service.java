package com.hksofttronix.goodwillbook.BusinessInfo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hksofttronix.goodwillbook.AddBusiness.addBusinessModel;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class uploadBusinessLogo_Service extends Service {

    String TAG = this.getClass().getSimpleName();

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    Context context = uploadBusinessLogo_Service.this;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    public static final int notif_id = 1001;
    public static final int success_fail_notifid = 11111;

    String NotifTitle = "Checking data";
    String NotifText = "Please wait...";

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
            globalclass.setAlarmFor_uploadBusinessLogo_Service();
            stopSelf();
            return;
        }

        getLocalData();
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
                .setProgress(0,0,true);


        startForeground(notif_id, notification.build());
        notificationManager.notify(notif_id,notification.build());
    }

    void updateNotification()
    {
        notification.setContentTitle(NotifTitle).setContentText(NotifText);
        notificationManager.notify(notif_id,notification.build());
    }

    void getLocalData()
    {
        Cursor cursor = null;
        try
        {
            String query = "SELECT * from businessaccountmaster\n" +
                    "WHERE id = (SELECT min(id) as id from businessaccountmaster where logofilepath != '')";

            SQLiteDatabase db = mydatabase.getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            int count = cursor.getCount();
            if(count > 0)
            {
                globalclass.log(TAG, String.valueOf(count)+" businesslogo found for update");

                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {

                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        int businessacid = cursor.getInt(cursor.getColumnIndex("businessacid"));
                        int userid = cursor.getInt(cursor.getColumnIndex("userid"));
                        String name = cursor.getString(cursor.getColumnIndex("name"));
                        String detail = cursor.getString(cursor.getColumnIndex("detail"));
                        String logourl = cursor.getString(cursor.getColumnIndex("logourl"));
                        String location = cursor.getString(cursor.getColumnIndex("location"));
                        String address = cursor.getString(cursor.getColumnIndex("address"));
                        String contactnumber = cursor.getString(cursor.getColumnIndex("contactnumber"));
                        String emailid = cursor.getString(cursor.getColumnIndex("emailid"));
                        String pancardnumber = cursor.getString(cursor.getColumnIndex("pancardnumber"));
                        String logofilepath = cursor.getString(cursor.getColumnIndex("logofilepath"));

                        globalclass.log(TAG,"id: "+id+", businessacid: "+businessacid+", userid: "+userid+", name: "+name+
                                ", detail: "+detail+", logourl: "+logourl+", location: "+location+", address: "+address+
                                ", contactnumber: "+contactnumber+", emailid: "+emailid+", pancardnumber: "+pancardnumber+
                                ", logofilepath: "+logofilepath);

                        NotifTitle = "Uploading logo of "+name+" ";
                        NotifText = "Please wait";

                        updateNotification();


                        File uploadingFile = new File(logofilepath);
                        if(uploadingFile.exists())
                        {
                            uploadBusinessLogo(businessacid,userid,name,globalclass.EncodeBase64(new File(globalclass.CompressFILE(context,logofilepath))));
                        }
                        else
                        {
                            stopService(1,businessacid,"Uploading failed (Tap here to retry)","Uploading file not found",null);
                        }

                    } while (cursor.moveToNext());
                }
            }
            else
            {
                globalclass.log(TAG,"No businesslogo found for update");
                stopSelf();
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getAllBusinessList",error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getAllBusinessList","",error);
            stopService(1,success_fail_notifid,"Uploading failed (Tap here to retry)",context.getResources().getString(R.string.DbException_string),null);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }
    }

    void uploadBusinessLogo(final int businessacid, int userid, final String name, String base64)
    {
        final String url = globalclass.getUrl()+"uploadBusinessLogo";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", String.valueOf(userid));
        params.put("businessacid", String.valueOf(businessacid));
        params.put("url", base64);

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("uploadBusinessLogo_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            JSONArray data_jsonArray = jsonObject.getJSONArray("data");
                            for(int i=0;i<data_jsonArray.length();i++)
                            {
                                JSONObject data_object = data_jsonArray.getJSONObject(i);
                                addBusinessModel businessModel = new addBusinessModel();
                                businessModel.setBusinessacid(data_object.getInt("businessacid"));
                                businessModel.setUserid(data_object.getInt("userid"));
                                businessModel.setName(data_object.getString("name"));
                                businessModel.setDetail(data_object.getString("detail"));
                                businessModel.setLogourl(data_object.getString("logourl"));
                                businessModel.setLocation(data_object.getString("location"));
                                businessModel.setAddress(data_object.getString("address"));
                                businessModel.setContactnumber(data_object.getString("contactnumber"));
                                businessModel.setEmailid(data_object.getString("emailid"));
                                businessModel.setPancardnumber(data_object.getString("pancardnumber"));
                                businessModel.setLogofilepath("");

                                mydatabase.addorupdateBusiness(businessModel);
                            }

                            Intent customIntent = new Intent(context,Businessinfo.class);
                            stopService(0,success_fail_notifid,"Uploaded successfully","Logo of "+name+" uploaded successfully!",customIntent);
//                            globalclass.showUploadSuccessNotif(context,businessacid, , "Logo of "+name+" uploaded successfully!");
                            getLocalData();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.log(TAG,getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"uploadBusinessLogo_ErrorInFunction",params.toString(),message);
                                stopService(1,businessacid,"Uploading failed (Tap here to retry)",context.getResources().getString(R.string.api_function_error),null);
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
                        globalclass.log("uploadBusinessLogo_JSONException",error);
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"uploadBusinessLogo_JSONException",params.toString(),error);
                        stopService(1,businessacid,"Uploading failed (Tap here to retry)",context.getResources().getString(R.string.JSONException_string),null);
                    }
                }
                else
                {
                    stopService(1,businessacid,"Uploading failed (Tap here to retry)",context.getResources().getString(R.string.onError_error),null);
                }
            }
        });
    }

    void stopService(int error,int notif_id,String ContentTitle,String ContentText, Intent customIntent)
    {
        if(error == 0)
        {
            globalclass.showUploadSuccessNotif(context,notif_id,ContentTitle , ContentText, customIntent);
        }
        else
        {
            globalclass.showUploadFailedNotif(context,notif_id, ContentTitle, ContentText);
            globalclass.setAlarmFor_uploadBusinessLogo_Service();
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.log(TAG,"onDestroy");
    }
}
