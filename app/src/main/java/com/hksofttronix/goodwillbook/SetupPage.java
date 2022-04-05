package com.hksofttronix.goodwillbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.hksofttronix.goodwillbook.AddBusiness.addBusinessModel;
import com.hksofttronix.goodwillbook.ContactList.contactList_Service;
import com.hksofttronix.goodwillbook.MainActivity.MainActivity;
import com.hksofttronix.goodwillbook.Transaction.transactionModel;
import com.hksofttronix.goodwillbook.VolleyUtil.common.NotificationModel;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SetupPage extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = SetupPage.this;
    
    VolleyApiCall volleyApiCall;
    Globalclass globalclass;
    Mydatabase mydatabase;

    ArrayList<addBusinessModel> business_arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_page);
        getSupportActionBar().hide();

        init();

        getNotificationList();
        globalclass.startAnyService(activity,contactList_Service.class);

//        if(!globalclass.getBooleanData("transfer data"))
//        {
//            globalclass.StartAllServices();
//        }
//
//        getBusinessList();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void getBusinessList()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        final String url = globalclass.getUrl()+"getBusinessList";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("mobilenumber", globalclass.getmobilenumber());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("getBusinessList_RES",result);

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

                               mydatabase.addorupdateBusiness(businessModel);
                               business_arrayList.add(businessModel);
                           }

                            if(globalclass.getBooleanData("transfer data"))
                            {
                                if(!business_arrayList.isEmpty())
                                {
                                    globalclass.setIntData("businessacid",business_arrayList.get(0).getBusinessacid());

                                    int bussOneBusinessacid = business_arrayList.get(0).getBusinessacid();
                                    String bussOneName = business_arrayList.get(0).getName();

                                    int bussTwoBusinessacid = business_arrayList.get(1).getBusinessacid();
                                    String bussTwoName = business_arrayList.get(1).getName();

                                    askForTransferData(bussOneBusinessacid,bussOneName,bussTwoBusinessacid,bussTwoName);
                                }
                            }
                            else
                            {
                                if(!business_arrayList.isEmpty())
                                {
                                    globalclass.setIntData("businessacid",business_arrayList.get(0).getBusinessacid());
                                }

                                getAllTransaction();
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getBusinessList_ErrorInFunction",params.toString(),result);
                            }
                            else
                            {
                                globalclass.snackit(activity,message);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("getBusinessList_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getBusinessList_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void getAllTransaction()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

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

//                            if(data_jsonArray.length() > 0)
//                            {
//
//                            }

                            startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllTransaction_RES_ErrorInFunction",params.toString(),message);
                            }
                            else if(message.equalsIgnoreCase("No data found"))
                            {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            }
                            else if(!message.equalsIgnoreCase("No data found"))
                            {
                                globalclass.toast_long(message);
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllTransaction_RES_ErrorInFunction",params.toString(),message);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("getAllTransaction_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllTransaction_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void getNotificationList()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.log(TAG,getResources().getString(R.string.noInternetConnection));
            return;
        }

        final String url = globalclass.getUrl()+"getNotificationList";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("getNotificationList_RES",result);

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
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("registerUser_JSONException",error);
                        globalclass.log("getNotificationList_JSONException",error);
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getNotificationList_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void askForTransferData(final int bussOneBusinessacid, String bussOneName, final int bussTwoBusinessacid, String bussTwoName)
    {
        // custom dialog
        final Dialog dialog = new Dialog(activity,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.transfer_data_dialogue);
        dialog.setCancelable(false);

        final LinearLayout asktransferdatalo = dialog.findViewById(R.id.asktransferdatalo);
        final LinearLayout progresslo = dialog.findViewById(R.id.progresslo);
        TextView asktransfering_datatext = dialog.findViewById(R.id.asktransfering_datatext);
        TextView transfering_datatext = dialog.findViewById(R.id.transfering_datatext);
        MaterialButton transferdatanobt = dialog.findViewById(R.id.transferdatanobt);
        MaterialButton transferdatayesbt = dialog.findViewById(R.id.transferdatayesbt);

        asktransfering_datatext.setText("Do you want to transfer data from "+bussOneName+" to "+bussTwoName+"?");
        transfering_datatext.setText("Hold on, transfering data from "+bussOneName+" to "+bussTwoName+"!");

        transferdatayesbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                asktransferdatalo.setVisibility(View.GONE);
                progresslo.setVisibility(View.VISIBLE);
                transferData(bussOneBusinessacid,bussTwoBusinessacid,dialog);
            }
        });

        transferdatanobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

                getAllTransaction();
            }
        });


        dialog.show();
    }

    void transferData(int bussOneBusinessacid, final int bussTwoBusinessacid, final Dialog dialog)
    {
        final String url = globalclass.getUrl()+"transferData";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("credituserid", globalclass.getuserid());
        params.put("bussOneBusinessacid", String.valueOf(bussOneBusinessacid));  // buss one which is created automatically
        params.put("creditbusinessacid", String.valueOf(bussTwoBusinessacid)); // buss two which is newly created by user

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("transferData_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            dialog.dismiss();
                            globalclass.setBooleanData("transfer data",false);
                            globalclass.setIntData("businessacid", bussTwoBusinessacid);
                            
                            getAllTransaction();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"transferData_ErrorInFunction",params.toString(),result);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("transferData_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"transferData_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    public BroadcastReceiver ContactSyncCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(TAG,"ContactSyncCompleteReceiver: onReceive");

                getBusinessList();
                globalclass.saveRemainderForRefreshAll();
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(TAG,"ContactSyncCompleteReceiver: onReceive\n"+error);
                globalclass.sendLog(Globalclass.TryCatchException,TAG,"","ContactSyncCompleteReceiver: onReceive","",error);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                ContactSyncCompleteReceiver, new IntentFilter(Globalclass.ContactSyncCompleteReceiver));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(ContactSyncCompleteReceiver);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        globalclass.snackit(activity,getResources().getString(R.string.account_setup));
    }
}
