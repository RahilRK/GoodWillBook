package com.hksofttronix.goodwillbook.Feedback;

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

import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class uploadFeedbackAttachments_Service extends Service
{
    String TAG = this.getClass().getSimpleName();

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    Context context = uploadFeedbackAttachments_Service.this;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    public static final int notif_id = 1005;

    String NotifTitle = "Sending feedback";
    String NotifText = "Please wait...";
    int success_fail_notifid = 55555;

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
            globalclass.setAlarmFor_uploadFeedbackImageViaEmails_Service();
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
        notification = new NotificationCompat.Builder(this, Globalclass.uploadFeedbackImageViaEmails_Service_Notifi_Channel_ID)
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

    void getLocalData()
    {
        Cursor cursor = null;
        try
        {
            String query = "SELECT * FROM sendfeedbackmaster\n" +
                    "WHERE id = (select min(id) FROM sendfeedbackmaster)";

            SQLiteDatabase db = mydatabase.getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if(cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024*1024*10));
            }

            int count = cursor.getCount();
            if(count > 0)
            {
                globalclass.log(TAG, String.valueOf(count)+" attachments found");

                // looping through all rows and adding to list
                if (cursor.moveToFirst())
                {
                    do
                    {
                        int id = cursor.getInt(cursor.getColumnIndex("id"));
                        String shortdesc = cursor.getString(cursor.getColumnIndex("shortdesc"));
                        String longdesc = cursor.getString(cursor.getColumnIndex("longdesc"));
                        String filepath = cursor.getString(cursor.getColumnIndex("filepath"));

                        globalclass.log(TAG,"id: "+id+
                                ", shortdesc: "+shortdesc+
                                ", longdesc: "+longdesc+
                                ", filepath: "+filepath);

                        File uploadingFile = new File(filepath);
                        if(uploadingFile.exists())
                        {
                            uploadFeedbackImageViaEmail(id,shortdesc,longdesc,globalclass.EncodeBase64(new File(globalclass.CompressFILE(context,filepath))));
                        }
                        else
                        {
                            stopService(1,id,"Sending feedback failed (Tap here to retry)","Feedback attachment photo not found",null);
                        }

                    } while (cursor.moveToNext());
                }
            }
            else
            {
                globalclass.log(TAG,"No more feedback attachments found");
                stopSelf();
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getLocalData",error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getLocalData","",error);
            stopService(1,success_fail_notifid,"Sending feedback failed (Tap here to retry)",context.getResources().getString(R.string.DbException_string),null);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }
    }

    void uploadFeedbackImageViaEmail(final int id, final String shortdesc, final String longdesc, final String base64)
    {
        final String url = globalclass.getUrl()+"uploadFeedbackImageViaEmail";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("base64String", base64);

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("uploadFeedbackImageViaEmail_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            String imagename = jsonObject.getString("imagename");
                            sendFeedbackViaEmail(id,shortdesc,longdesc,imagename);
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.log(TAG,getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"uploadFeedbackImageViaEmail_ErrorInFunction",params.toString(),message);
                                stopService(1, success_fail_notifid,"Upload failed (Tap here to retry)",context.getResources().getString(R.string.api_function_error),null);
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
                        globalclass.log("uploadFeedbackImageViaEmail_JSONException",error);
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"uploadFeedbackImageViaEmail_JSONException",params.toString(),error);
                        stopService(1, success_fail_notifid,"Sending feedback failed (Tap here to retry)",context.getResources().getString(R.string.JSONException_string),null);
                    }
                }
                else
                {
                    stopService(1, success_fail_notifid,"Sending feedback failed (Tap here to retry)",context.getResources().getString(R.string.onError_error),null);
                }
            }
        });
    }

    void sendFeedbackViaEmail(final int id, String shortdesc, String longdesc, String Imagename)
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        final String url = globalclass.getUrl()+"sendFeedbackViaEmail";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("Subject", shortdesc);
        params.put("Message", longdesc);
        params.put("Imagename", Imagename);

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("sendFeedbackViaEmail_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            mydatabase.deleteData(mydatabase.sendfeedbackmaster,"id", String.valueOf(id));
                            getLocalData();
                            stopService(0, id,"Send successfully" , "Feedback has been send successfully!",null);
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendFeedbackViaEmail_ErrorInFunction",params.toString(),result);
                                stopService(1, success_fail_notifid,"Sending feedback failed (Tap here to retry)",context.getResources().getString(R.string.onError_error),null);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("sendFeedbackViaEmail_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendFeedbackViaEmail_JSONException",params.toString(),error);
                        stopService(1, success_fail_notifid,"Sending feedback failed (Tap here to retry)",context.getResources().getString(R.string.onError_error),null);
                    }
                }
                else
                {
                    globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"",params.toString(),result);
                    stopService(1, success_fail_notifid,"Sending feedback failed (Tap here to retry)",context.getResources().getString(R.string.onError_error),null);
                }
            }
        });
    }

    void stopService(int error,int notif_id,String ContentTitle,String ContentText, Intent customIntent)
    {
        if(error == 0)
        {
            globalclass.showUploadSuccessNotif(context,notif_id, ContentTitle, ContentText,customIntent);
        }
        else
        {
            globalclass.showUploadFailedNotif(context,notif_id,ContentTitle , ContentText);
            globalclass.setAlarmFor_uploadFeedbackImageViaEmails_Service();
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.log(TAG,"onDestroy");
    }
}
