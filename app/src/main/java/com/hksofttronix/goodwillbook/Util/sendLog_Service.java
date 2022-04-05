package com.hksofttronix.goodwillbook.Util;

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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hksofttronix.goodwillbook.BuildConfig;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.VolleyUtil.common.MySingleton;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class sendLog_Service extends Service
{
    String TAG = this.getClass().getSimpleName();

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    Context context = sendLog_Service.this;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    public static final int notif_id = 1005;

    String NotifTitle = "Sending error logs";
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

        if(BuildConfig.DEBUG)
        {
            stopSelf();
            return;
        }

        if(!globalclass.isInternetPresent() || !globalclass.userHadLoggedIn())
        {
            globalclass.log(TAG,"Service stop due to no internet connection or user not logged in!");
            globalclass.setAlarmFor_sendLog_Service();
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
        notification = new NotificationCompat.Builder(this, Globalclass.sendLog_Channel_ID)
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

    public void getLocalData()
    {
        Cursor cursor = null;
        try
        {
            String query = "SELECT  * FROM " + mydatabase.appLog+" ";

            SQLiteDatabase db = mydatabase.getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            if(cursor.getCount() == 0)
            {
                globalclass.log("getAllLogs","No more logs are available");
                stopSelf();
            }
            else
            {
                // looping through all rows and adding to list
                if (cursor.moveToFirst()) {
                    do {

                        appLogModel model = new appLogModel();
                        model.setId(cursor.getInt(cursor.getColumnIndex("id")));
                        model.setDir(cursor.getString(cursor.getColumnIndex("Dir")));
                        model.setName(cursor.getString(cursor.getColumnIndex("Name")));
                        model.setData(cursor.getString(cursor.getColumnIndex("data")));

                        globalclass.log("getAllLogs","id:"+model.getId()+", Dir:"+model.getDir()+", Name:"+model.getName()+", data:"+model.getData());

                        sendLog(model);

                    } while (cursor.moveToNext());
                }
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getLocalData",error);
            globalclass.toast_long(context.getResources().getString(R.string.DbException_string));
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getLocalData","",error);
            stopSelf();
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }
    }

    void sendLog(appLogModel model)
    {
        final String url = globalclass.getUrl()+"logThis";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("dir", model.getDir());
        params.put("fileName", model.getName());
        params.put("msg", model.getData());
        params.put("id", String.valueOf(model.getId()));
        params.put("mobilenumber", globalclass.getmobilenumber());

        StringRequest strREQ = new StringRequest(Request.Method.POST, url, new Response.Listener < String > ()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString(getResources().getString(R.string.status));
//                    String message = jsonObject.getString(getResources().getString(R.string.message));

                    if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                    {
//                        globalclass.log(TAG,message);
                        mydatabase.deleteData(mydatabase.appLog,"id",jsonObject.getString("id"));
                        stopSelf();
                    }
                    else
                    {
//                        if(message.contains("Error"))
//                        {
//                            globalclass.log(TAG,getResources().getString(R.string.errorInFunction_string));
//                            globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendLog_ErrorInFunction",params.toString(),message);
//                            stopSelf();
//                        }
//                        else
//                        {
//                            globalclass.log(TAG,message);
//                            stopSelf();
//                        }

                        stopSelf();
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();

                    String error = Log.getStackTraceString(e);
                    globalclass.log("sendLog_JSONException",error);
                    globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendLog_JSONException",params.toString(),error);
                    stopSelf();
                }

            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log("sendLog_onErrorResponse",error);
                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendLog_onErrorResponse",params.toString(),error);
                stopSelf();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }
        };

        MySingleton.getInstance(context).addToRequestQueue(strREQ);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.setAlarmFor_sendLog_Service();
        globalclass.log(TAG,"onDestroy");
    }
}
