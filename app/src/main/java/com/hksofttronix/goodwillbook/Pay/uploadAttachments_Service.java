package com.hksofttronix.goodwillbook.Pay;

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
import com.hksofttronix.goodwillbook.Transaction.TransactionDetail;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class uploadAttachments_Service extends Service
{
    String TAG = this.getClass().getSimpleName();

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    Context context = uploadAttachments_Service.this;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    public static final int notif_id = 1003;

    String NotifTitle = "Uploading attachments";
    String NotifText = "Please wait...";
    int success_fail_notifid = 33333;

    int total = 0;
    int sum = 0;

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
            globalclass.setAlarmFor_uploadAttachments_Service();
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
        notification = new NotificationCompat.Builder(this, Globalclass.uploadAttachments_Service_Notifi_Channel_ID)
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

    void getLocalData()
    {
        Cursor cursor = null;
        try
        {
            String query = "SELECT * FROM attachmentmaster\n" +
                    "WHERE id = (select min(id) FROM attachmentmaster)";

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
                        int transactionid = cursor.getInt(cursor.getColumnIndex("transactionid"));
                        int forupdate = cursor.getInt(cursor.getColumnIndex("forupdate"));
                        String attachmentfilepath = cursor.getString(cursor.getColumnIndex("attachmentfilepath"));

                        globalclass.log(TAG,"id: "+id+
                                ", transactionid: "+transactionid+
                                ", forupdate: "+forupdate+
                                ", attachmentfilepath: "+attachmentfilepath);

                        if(total == 0)
                        {
                            total = mydatabase.check_isAttachmentAvailable(transactionid);
                        }

                        sum++;
                        int progress_percentage = sum * 100 / total;
                        NotifText = +sum+"/"+total;
                        updateNotification(progress_percentage);

                        File uploadingFile = new File(attachmentfilepath);
                        if(uploadingFile.exists())
                        {
                            uploadAttachment(transactionid,id,forupdate,globalclass.EncodeBase64(new File(globalclass.CompressFILE(context,attachmentfilepath))));
                        }
                        else
                        {
                            stopService(1,id,"Upload failed (Tap here to retry)","Attachment photo not found",null);
                        }

                    } while (cursor.moveToNext());
                }
            }
            else
            {
                globalclass.log(TAG,"No more attachments found");
                stopSelf();
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_getLocalData",error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getLocalData","",error);
            stopService(1,3001,"Upload failed (Tap here to retry)",context.getResources().getString(R.string.DbException_string),null);
        }
        finally
        {
            if(cursor != null) { cursor.close(); }
        }
    }

    void uploadAttachment(final int transactionid, int id, int forupdate, String base64)
    {
        final String url = globalclass.getUrl()+"uploadAttachment";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("url", base64);
        params.put("transactionid", String.valueOf(transactionid));
        params.put("id", String.valueOf(id));
        params.put("isAttachmentAvailable", String.valueOf(mydatabase.check_isAttachmentAvailable(transactionid)));
        params.put("forupdate", String.valueOf(forupdate));

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("uploadAttachment_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));
                        String notificationStatus = jsonObject.getString(getResources().getString(R.string.notificationStatus));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            if(!notificationStatus.contains("message_id"))
                            {
                                //todo sendLog ErrorInSendingFcmNotification
                                globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,url,"uploadAttachment_ErrorInSendingFcmNotification",params.toString(),result);
                            }

                            String id = jsonObject.getString("id");
                            mydatabase.deleteData(mydatabase.attachmentmaster,"id",id);
                            if(mydatabase.check_isAttachmentAvailable(transactionid) == 0)
                            {
                                total = mydatabase.check_isAttachmentAvailable(transactionid);
                                sum = 0;

                                Intent customIntent = new Intent(context, TransactionDetail.class);
                                customIntent.putExtra("transactionid",String.valueOf(transactionid));
                                stopService(0, Integer.parseInt(id),"Uploaded successfully" , "Attachments uploaded successfully!",customIntent);
                            }
//                            else
//                            {
//
//                            }

                            getLocalData();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.log(TAG,getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"uploadAttachment_ErrorInFunction",params.toString(),message);
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
                        globalclass.log("uploadAttachment_JSONException",error);
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"uploadAttachment_JSONException",params.toString(),error);
                        stopService(1, success_fail_notifid,"Upload failed (Tap here to retry)",context.getResources().getString(R.string.JSONException_string),null);
                    }
                }
                else
                {
                    stopService(1, success_fail_notifid,"Upload failed (Tap here to retry)",context.getResources().getString(R.string.onError_error),null);
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
            globalclass.setAlarmFor_uploadAttachments_Service();
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.log(TAG,"onDestroy");
    }
}
