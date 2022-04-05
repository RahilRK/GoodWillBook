package com.hksofttronix.goodwillbook.Home.NotificationFrag;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.MainActivity.MainActivity;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Transaction.TransactionDetail;
import com.hksofttronix.goodwillbook.VolleyUtil.common.NotificationModel;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFrag extends Fragment
{

    public NotificationFrag() {
        // Required empty public constructor
    }

    String TAG = this.getClass().getSimpleName();


    AppCompatActivity activity;
    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    View myview;

    ArrayList<NotificationModel> arrayList = new ArrayList();
    Notification_adapter adapter;

    Toolbar toolbar;
    ImageView iv_delete,iv_select_all;
    SwipeRefreshLayout swipeReferesh;
    LinearLayout mainlo;
    RelativeLayout nodatafoundlo;
    RecyclerView recyclerview;

    boolean isMultiselect;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof AppCompatActivity)
        {
            activity  = (AppCompatActivity) context;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myview = view;
        init();
        binding();
//        setToolbar();
        onClick();
    }

    void init()
    {
        globalclass  = Globalclass.getInstance(activity);
        mydatabase  = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        toolbar =  myview.findViewById(R.id.toolbar);
        iv_delete =  myview.findViewById(R.id.iv_delete);
        iv_select_all =  myview.findViewById(R.id.iv_select_all);
        swipeReferesh = myview.findViewById(R.id.swipeReferesh);
        mainlo = myview.findViewById(R.id.mainlo);
        nodatafoundlo = myview.findViewById(R.id.nodatafoundlo);
        recyclerview = myview.findViewById(R.id.recyclerview);
    }

    void setToolbar()
    {
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
    }

    void onClick()
    {
        iv_select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(adapter.getSparseBooleanArraySize() < arrayList.size())
                {
                    adapter.SelectAll();
                    updateUI();
                }
                else
                {
                    adapter.UnSelectAll();
                    updateUI();
                }
            }
        });

        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            showRemoveNotificationDialogue(activity,"Are you sure you want to remove notification?");
            }
        });

        swipeReferesh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    swipeReferesh.setRefreshing(false);
                    return;
                }

                swipeReferesh.setRefreshing(false);
                getNotificationList();
            }
        });
    }

    void getData()
    {
        showprogress();

        mydatabase.addDataToHomeMaster(globalclass.getActivebusinessacid());
        arrayList.clear();
        arrayList = mydatabase.getNotificationList();

        if(!arrayList.isEmpty())
        {
            nodatafoundlo.setVisibility(View.GONE);
            mainlo.setVisibility(View.VISIBLE);
            setAdapter();
            updateUI();
        }
        else
        {
            mainlo.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);
        }

        hideprogress();
    }

    void getNotificationList() //from online
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_short(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"getNotificationList";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
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
                            mydatabase.deleteNotificationMaster();

                            arrayList.clear();
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
                                arrayList.add(model);
                            }

                            if(!arrayList.isEmpty())
                            {
                                nodatafoundlo.setVisibility(View.GONE);
                                mainlo.setVisibility(View.VISIBLE);
                                getData();
                            }
                            else
                            {
                                mainlo.setVisibility(View.GONE);
                                nodatafoundlo.setVisibility(View.VISIBLE);
                            }
                        }
                        else
                        {
                            globalclass.snackit(activity,message);
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

    void setAdapter()
    {
        adapter = new Notification_adapter(activity, arrayList, new NotificationOnClick() {
            @Override
            public void viewDetail(int position, NotificationModel model) {

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_short(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if(isMultiselect)
                {
                    toggleSelection(position, model);
                }
                else
                {
                    if(model.getHasread() == 0)
                    {
                        readNotification(model);
                    }

                    Intent intent = new Intent(activity, TransactionDetail.class);
                    intent.putExtra("transactionid",String.valueOf(model.getTransactionid()));
                    startActivity(intent);
                }
            }

            @Override
            public void onLongPress(int position, NotificationModel model) {

                toggleSelection(position,model);
            }
        });

        recyclerview.setLayoutManager(new LinearLayoutManager(activity));
        recyclerview.setAdapter(adapter);
    }

    void showRemoveNotificationDialogue(Activity activity, String title)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        giveNotifiIdList();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .show();
    }

    void giveNotifiIdList()
    {
        ArrayList<String> deleteItemIdArraylist = new ArrayList<>();
        deleteItemIdArraylist.clear();

        try
        {
            for(int m=0;m<arrayList.size();m++)
            {
                if(arrayList.get(m).getisSelected())
                {
                    String getid = String.valueOf(arrayList.get(m).getNotif_id());
                    deleteItemIdArraylist.add(getid);
                }
            }

            String notif_id_list = TextUtils.join(",", deleteItemIdArraylist);
            globalclass.log(TAG, "notif_id_list: "+notif_id_list);
            deleteMultipleNotificationById(notif_id_list);
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log("giveNotifiIdListException",error);
            globalclass.toast_long("Unable to deleted, please try again later!");
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","giveNotifiIdListException","",error);
            ((MainActivity)activity).setBudgetCount();
            adapter.clearSelections();
            getData();
        }
    }

    void deleteMultipleNotificationById(String notif_id_list)
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"deleteMultipleNotificationById";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("notif_id_list", notif_id_list);

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("deleteMultipleNotificationById_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            removeFromLocalDatabase();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"deleteMultipleNotificationById_ErrorInFunction",params.toString(),result);
                            }
                            else if(message.equalsIgnoreCase("No data found"))
                            {
                                updateUI();
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
                        globalclass.log("deleteMultipleNotificationById_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"deleteMultipleNotificationById_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void removeFromLocalDatabase()
    {
        try
        {
            for(int m=0;m<arrayList.size();m++)
            {
                if(arrayList.get(m).getisSelected())
                {
                    String getid = String.valueOf(arrayList.get(m).getNotif_id());
                    int pos = m;
                    globalclass.log(TAG, "pos: "+pos);
                    adapter.removeItem(pos);
                    mydatabase.deleteData(mydatabase.notificationmaster,"notif_id",getid);
                    ((MainActivity)activity).setBudgetCount();
                    m--;
                }
            }
            adapter.clearSelections();
            updateUI();
            if(arrayList.isEmpty())
            {
                mainlo.setVisibility(View.GONE);
                nodatafoundlo.setVisibility(View.VISIBLE);
            }
            globalclass.snackit(activity,"Removed successfully!");
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log("removeFromLocalDatabaseException",error);
            globalclass.toast_long("Unable to deleted, please try again later!");
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","removeFromLocalDatabaseException","",error);
            ((MainActivity)activity).setBudgetCount();
            adapter.clearSelections();
            getData();
        }
    }

    void toggleSelection(int position, NotificationModel model) {

        adapter.AddandRemoveInSparseBooleanArray(position);
        int count = adapter.getSparseBooleanArraySize();

        if(count>0)
        {
            isMultiselect=true;
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setTitle(""+count);
        }
        else
        {
            isMultiselect=false;
            toolbar.setTitle("");
            toolbar.setVisibility(View.GONE);
        }

        if(model.getisSelected())
        {
            model.setisSelected(false);
        }
        else
        {
            model.setisSelected(true);
        }
    }

    void updateUI()
    {
        int size = adapter.getSparseBooleanArraySize();

        if(size>0)
        {
            isMultiselect=true;
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setTitle(""+size);
        }
        else
        {
            isMultiselect=false;
            toolbar.setTitle("");
            toolbar.setVisibility(View.GONE);
        }
    }

    void readNotification(final NotificationModel model)
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        final String url = globalclass.getUrl()+"readNotification";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("notif_id", String.valueOf(model.getNotif_id()));

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                globalclass.log("readNotification_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            model.setHasread(1);
                            mydatabase.addorupdateNotificationDetail(model);
                            globalclass.log(TAG,"Notification read successfully");
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"readNotification_ErrorInFunction",params.toString(),result);
                            }
                            else
                            {
                                if(message.equalsIgnoreCase(getResources().getString(R.string.no_user_found)))
                                {
                                    globalclass.toast_long(message);
                                }
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("readNotification_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"readNotification_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    public BroadcastReceiver NotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(TAG,"NotificationReceiver: onReceive");
                getData();
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(TAG,"NotificationReceiver: onReceive\n"+error);
                globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","NotificationReceiver: onReceive","",error);
            }
        }
    };


    void showprogress()
    {
        RelativeLayout progresslo = myview.findViewById(R.id.progresslo);

        if (progresslo.getVisibility() != View.VISIBLE) {
            progresslo.setVisibility(View.VISIBLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, //disable all ContactOnClick/onTouch
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    void hideprogress()
    {
        RelativeLayout progresslo = myview.findViewById(R.id.progresslo);
        if (progresslo.getVisibility() == View.VISIBLE) {
            progresslo.setVisibility(View.GONE);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //enable all ContactOnClick/onTouch
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                NotificationReceiver, new IntentFilter(Globalclass.NotificationReceiver));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(NotificationReceiver);
    }
}
