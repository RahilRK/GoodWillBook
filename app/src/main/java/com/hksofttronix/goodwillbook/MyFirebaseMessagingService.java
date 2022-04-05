package com.hksofttronix.goodwillbook;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hksofttronix.goodwillbook.ContactList.ContactModel;
import com.hksofttronix.goodwillbook.Transaction.transactionModel;
import com.hksofttronix.goodwillbook.VolleyUtil.common.NotificationModel;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    String TAG = this.getClass().getSimpleName();
    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;
    Context context = MyFirebaseMessagingService.this;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        init();
        globalclass.log(TAG,"onMessageReceived");

        if (remoteMessage.getData().size() > 0)
        {
            globalclass.log(TAG, "Message data payload: " + remoteMessage.getData().toString());
            handleRemoteMessage(remoteMessage);
        }
    }

    void init()
    {
        globalclass = Globalclass.getInstance(context);
        mydatabase = Mydatabase.getInstance(context);
        volleyApiCall = new VolleyApiCall(context);
    }

    void handleRemoteMessage(RemoteMessage remoteMessage)
    {
        try
        {
            try
            {
                Map<String, String> params = remoteMessage.getData();
                JSONObject jsonObject = new JSONObject(params);
                String type = jsonObject.getString("type");

                if(type.equalsIgnoreCase("Amount received") ||
                        type.equalsIgnoreCase("Amount approved") ||
                        type.equalsIgnoreCase("Transaction update"))
                {
                    String transactionid = jsonObject.getString("transactionid");
                    String notif_id = jsonObject.getString("notif_id");

                    getTransactionDetail(transactionid);
                    getNotificationData(notif_id);
                    handleNotification(type,jsonObject);
                }
                else  if(type.equalsIgnoreCase("Delete transaction"))
                {
                    String transactionid = jsonObject.getString("transactionid");
                    mydatabase.deleteData(mydatabase.transactionmaster,"transactionid", transactionid);
                    mydatabase.deleteData(mydatabase.notificationmaster,"transactionid", transactionid);
                    mydatabase.deleteHomeMaster();

                    globalclass.callNotificationReceiver();
                }
                else  if(type.equalsIgnoreCase("deleteAttachment"))
                {
                    String notif_id = jsonObject.getString("notif_id");

                    getNotificationData(notif_id);
                    handleNotification(type,jsonObject);
                }
                else if(type.equalsIgnoreCase("New user transfer data"))
                {
                    String transactionid = jsonObject.getString("transactionid");

                    getTransactionDetail(transactionid);
                    updateContactDetailsToIsVerified(jsonObject.getString("creditmobilenumber"));
                    globalclass.callNotificationReceiver();
                }

            }
            catch (JSONException e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log("handleNotifData_JSONException",error);
                globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","handleNotifData_JSONException",remoteMessage.getData().toString(),error);
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log("handleNotifData_Exception",error);
            globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","handleNotifData_Exception",remoteMessage.getData().toString(),error);

        }
    }

    void handleNotification(String type, JSONObject jsonObject)
    {
        //Amount received
        if(type.equalsIgnoreCase("Amount received") &&
                globalclass.getamountreceivedkey_value())
        {
            globalclass.showFcmNotif_ForAmountReceivedOrApproved(context,jsonObject);
        }
        else if(type.equalsIgnoreCase("Amount received") &&
                !globalclass.getamountreceivedkey_value())
        {
            globalclass.showFcmSilentNotif_ForAmountReceivedOrApproved(context,jsonObject);
        }

        //Amount approved
        if(type.equalsIgnoreCase("Amount approved") &&
                globalclass.getamountapprovedkey_value())
        {
            globalclass.showFcmNotif_ForAmountReceivedOrApproved(context,jsonObject);
        }
        else if(type.equalsIgnoreCase("Amount approved") &&
                !globalclass.getamountapprovedkey_value())
        {
            globalclass.showFcmSilentNotif_ForAmountReceivedOrApproved(context,jsonObject);
        }

        //Transaction update
        if(type.equalsIgnoreCase("Transaction update") &&
                globalclass.gettransactionupdatedkey_value())
        {
            globalclass.showFcmNotif_ForAmountReceivedOrApproved(context,jsonObject);
        }
        else if(type.equalsIgnoreCase("Transaction update") &&
                !globalclass.gettransactionupdatedkey_value())
        {
            globalclass.showFcmSilentNotif_ForAmountReceivedOrApproved(context,jsonObject);
        }

        //deleteAttachment
        if(type.equalsIgnoreCase("deleteAttachment") &&
                globalclass.gettransactionattachmentdeletedkey_value())
        {
            globalclass.showFcmNotif_ForAmountReceivedOrApproved(context,jsonObject);
        }
        else if(type.equalsIgnoreCase("deleteAttachment") &&
                !globalclass.gettransactionattachmentdeletedkey_value())
        {
            globalclass.showFcmSilentNotif_ForAmountReceivedOrApproved(context,jsonObject);
        }
    }

    void getTransactionDetail(String transactionid)
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.log(TAG,getResources().getString(R.string.noInternetConnection));
            return;
        }

        final String url = globalclass.getUrl()+"getTransactionDetail";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("transactionid", transactionid);


        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("getTransactionDetail_RES",result);

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
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getTransactionDetail_ErrorInFunction",params.toString(),result);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("getTransactionDetail_JSONException",error);
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getTransactionDetail_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void getNotificationData(String notif_id)
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.log(TAG,getResources().getString(R.string.noInternetConnection));
            return;
        }

        final String url = globalclass.getUrl()+"getNotificationData";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("notif_id", notif_id);


        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("getNotificationData_RES",result);

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
                                NotificationModel model = new NotificationModel();
                                model.setNotif_id(data_object.getInt("id"));
                                model.setUserid(data_object.getInt("userid"));
                                model.setTransactionid(data_object.getInt("transactionid"));
                                model.setType(data_object.getString("type"));
                                model.setTitle(data_object.getString("title"));
                                model.setText(data_object.getString("text"));
                                model.setSenddatetime(data_object.getString("senddatetime"));
                                model.setHasread(data_object.getInt("hasread"));

                                mydatabase.addorupdateNotificationDetail(model);
                            }

                            globalclass.callNotificationReceiver();
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("getNotificationData_JSONException",error);
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getNotificationData_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void updateContactDetailsToIsVerified(String mobilenumber)
    {
        ArrayList<ContactModel> arrayList = mydatabase.getContactDetail(mobilenumber);

        ContactModel model = new ContactModel();
        model.setId(arrayList.get(0).getId());
        model.setUserid(arrayList.get(0).getUserid());
        model.setContactName(arrayList.get(0).getContactName());
        model.setContactNumber(arrayList.get(0).getContactNumber());
        model.setViewcontactNumber(arrayList.get(0).getViewcontactNumber());
        model.setIsverified(1);
        model.setContactImage(arrayList.get(0).getContactImage());
        model.setGoodwilluser(1);

        mydatabase.addorupdateUserDetail(model);
        globalclass.showNewUserNotification(context,model);
    }
}
