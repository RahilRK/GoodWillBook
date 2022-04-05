package com.hksofttronix.goodwillbook.Transaction;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
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

import java.util.HashMap;
import java.util.Map;

import static com.hksofttronix.goodwillbook.Globalclass.RefreshAllRemainder;

public class transaction_Service extends Service
{
    String TAG = this.getClass().getSimpleName();

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    Context context = transaction_Service.this;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    public static final int notif_id = 1004;

    String NotifTitle = "Refreshing transaction";
    String NotifText = "Please wait...";
    int success_fail_notifid = 44444;

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
    public IBinder onBind(Intent intent)
    {

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
            globalclass.setAlarmFor_transaction_Service();
            stopSelf();
            return;
        }

        getAllTransaction();
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
        notification = new NotificationCompat.Builder(this, Globalclass.transaction_Service_Notifi_Channel_ID)
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

    void getAllTransaction()
    {
        final String url = globalclass.getUrl()+"getAllTransaction";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("getAllTransaction_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            mydatabase.deleteTransactionMaster();
                            mydatabase.deleteHomeMaster();

                            JSONArray data_jsonArray = jsonObject.getJSONArray("data");
                            for(int i=0;i<data_jsonArray.length();i++)
                            {
                                JSONObject data_object = data_jsonArray.getJSONObject(i);
                                transactionModel model = new transactionModel();
                                model.setDebitusername(data_object.getString("debitusername"));
                                model.setDebitmobilenumber(data_object.getString("debitmobilenumber"));
                                model.setCreditusername(data_object.getString("creditusername"));
                                model.setCreditmobilenumber(data_object.getString("creditmobilenumber"));
                                model.setDebitbusinessname(data_object.getString("debitbusinessname"));
                                model.setCreditbusinessname(data_object.getString("creditbusinessname"));
                                model.setTransactionid(data_object.getInt("transactionid"));
                                model.setDebituserid(data_object.getInt("debituserid"));
                                model.setDebitbusinessacid(data_object.getInt("debitbusinessacid"));
                                model.setCredituserid(data_object.getInt("credituserid"));
                                model.setCreditbusinessacid(data_object.getInt("creditbusinessacid"));
                                model.setAmount(data_object.getInt("amount"));
                                model.setRemark(data_object.getString("remark"));
                                model.setDebitdbdatetime(data_object.getString("debitdbdatetime"));
                                model.setDebitdatetime(data_object.getString("debitdatetime"));
                                model.setDebitlocation(data_object.getString("debitlocation"));
                                model.setIsApproved(data_object.getInt("isApproved"));
                                model.setCreditdbdatetime(data_object.getString("creditdbdatetime"));
                                model.setCreditdatetime(data_object.getString("creditdatetime"));
                                model.setCreditlocation(data_object.getString("creditlocation"));
                                model.setLastupdatedatetime(data_object.getString("lastupdatedatetime"));

                                mydatabase.addorupdateTransactionDetail(model);
                            }

                            if(data_jsonArray.length() > 0)
                            {
                                globalclass.callNotificationReceiver();
                                stopService(0, success_fail_notifid,"Refreshed successfully","Transaction data refreshed successfully!");
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllTransaction_RES_ErrorInFunction",params.toString(),message);
                                stopService(1,success_fail_notifid,"Refresh failed (Tap here to retry)",context.getResources().getString(R.string.api_function_error));
                            }
                            else if(message.equalsIgnoreCase("No data found"))
                            {
                                mydatabase.deleteTransactionMaster();
                                mydatabase.deleteHomeMaster();
                                globalclass.callNotificationReceiver();
                                stopSelf();
                            }
                            else
                            {
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllTransaction_RES_ErrorInFunction",params.toString(),message);
                                stopService(1,success_fail_notifid,"Refresh failed (Tap here to retry)",context.getResources().getString(R.string.api_function_error));
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("getAllTransaction_JSONException",error);
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllTransaction_JSONException",params.toString(),error);
                        stopService(1,success_fail_notifid,"Refresh failed (Tap here to retry)",context.getResources().getString(R.string.JSONException_string));
                    }
                }
                else
                {
                    stopService(1,success_fail_notifid,"Refresh failed (Tap here to retry)",context.getResources().getString(R.string.onError_error));
                }
            }
        });
    }

    void stopService(int error,int notif_id,String ContentTitle,String ContentText)
    {
        if(error == 0)
        {
            if(show_notification)
            {
                globalclass.showUploadSuccessNotif(context,notif_id, ContentTitle, ContentText,null);
            }
            else
            {
                globalclass.sendLog(RefreshAllRemainder,TAG,"","stopService","","All transactions has been sync successfully of userid = "+globalclass.getuserid()+" on "+globalclass.getLongToDatetime(globalclass.getMilliSecond(),"dd-MM-yyyy hh:mm:ss aa"));
            }
        }
        else
        {
            if(show_notification)
            {
                globalclass.showUploadFailedNotif(context,notif_id, ContentTitle, ContentText);
            }

            globalclass.setAlarmFor_transaction_Service();
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.log(TAG,"onDestroy");
    }
}
