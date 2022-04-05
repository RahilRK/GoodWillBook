package com.hksofttronix.goodwillbook.Transaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hksofttronix.goodwillbook.ContactList.ContactModel;
import com.hksofttronix.goodwillbook.EditPay.EditPay;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.Pay.Pay;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Remainder.AddRemainder;
import com.hksofttronix.goodwillbook.Util.generatePDF;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transaction extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Transaction.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;
    generatePDF generate_pdf;

    RelativeLayout nodatafoundlo;
    ImageView iv;
    TextView creditname,tvgeneratepdf,tvgivereceive,tvgivereceiveamount,tvsetremainder;
    SwipeRefreshLayout swipeReferesh;
    MaterialButton paybt;

    int GPS_PERMISSION = 444;
    String callmobilenumber = "";

    ArrayList<transactionModel> arrayList = new ArrayList();
    RecyclerView recyclerview;
    Transaction_adapter adapter;

    ArrayList<ContactModel> contactDetailarrayList = new ArrayList<>();
    ArrayList<Integer> myBusinessAcIdarrayList = new ArrayList<>();

    View rootlo;
    ImageView ivfilterdone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        init();
        binding();
        getContactDetail();
        onClick();
        setToolbar();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
        generate_pdf = new generatePDF(activity);

        globalclass.setStringData("filteroption","");
    }

    void getContactDetail()
    {
        contactDetailarrayList = mydatabase.getContactDetail(getIntent().getStringExtra("mobilenumber"));
        if(!contactDetailarrayList.isEmpty())
        {
            callmobilenumber = contactDetailarrayList.get(0).getContactNumber();
            creditname.setText(contactDetailarrayList.get(0).getContactName());
            setImageViewDrawable();
        }
        else
        {
            globalclass.toast_long("No contact details found for "+getIntent().getStringExtra("mobilenumber"));
            onBackPressed();
        }
    }

    void binding()
    {
        nodatafoundlo = findViewById(R.id.nodatafoundlo);
        iv = findViewById(R.id.iv);
        creditname = findViewById(R.id.creditname);
        tvgeneratepdf = findViewById(R.id.tvgeneratepdf);
        tvgivereceive = findViewById(R.id.tvgivereceive);
        tvgivereceiveamount = findViewById(R.id.tvgivereceiveamount);
        tvsetremainder = findViewById(R.id.tvsetremainder);
        swipeReferesh = findViewById(R.id.swipeReferesh);
        recyclerview = findViewById(R.id.recyclerview);
        paybt = findViewById(R.id.paybt);

        tvgeneratepdf.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(activity,R.drawable.ic_pdf), null, null);
        tvgeneratepdf.setCompoundDrawablePadding(20);
        tvsetremainder.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(activity,R.drawable.ic_reminder), null, null);
        tvsetremainder.setCompoundDrawablePadding(20);
    }

    void setImageViewDrawable()
    {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        final int randomcolor = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.SANS_SERIF).bold()
                .toUpperCase()
                .endConfig()
                .buildRoundRect(contactDetailarrayList.get(0).getContactName().substring(0,1), randomcolor,10);

        if(contactDetailarrayList.get(0).getIsverified() == 1)
        {
            iv.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }
        else
        {
            iv.setImageDrawable(drawable);
        }
    }

    void onClick()
    {
        paybt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    swipeReferesh.setRefreshing(false);
                    return;
                }

                requestLocationPermission();
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
                getAllTransaction();
            }
        });

        tvgeneratepdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!arrayList.isEmpty())
                {
                    requestStoragePermission();
                }
            }
        });

        tvsetremainder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int sum = globalclass.getCustomerSum(contactDetailarrayList.get(0).getContactNumber());
                if(sum > 0)
                {
                    startActivity(new Intent(activity, AddRemainder.class).
                            putExtra("mobilenumber",contactDetailarrayList.get(0).getContactNumber()));
                }
            }
        });
    }

    void setToolbar()
    {
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getData(String startdate,String enddate,String orderbytext)
    {
        showprogress();

        arrayList.clear();
        arrayList = mydatabase.getCustomerTransaction(contactDetailarrayList.get(0).getContactNumber(),startdate,enddate,orderbytext);

        if(!arrayList.isEmpty())
        {
            nodatafoundlo.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);
            setAdapter();
        }
        else
        {
            recyclerview.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);
        }

        for(int i=0;i<mydatabase.getAllBusinessList().size();i++)
        {
            myBusinessAcIdarrayList.add(mydatabase.getAllBusinessList().get(i).getBusinessacid());
        }

        hideprogress();
        showhide();
    }

    int setSum()
    {
        int sum = globalclass.getCustomerSum(contactDetailarrayList.get(0).getContactNumber());
        if(sum > 0)
        {
            tvgivereceive.setText(getResources().getString(R.string.will_receive));
            tvgivereceiveamount.setText("₹ "+String.valueOf(sum));
            tvgivereceiveamount.setTextColor(getResources().getColor(R.color.mgreen));
        }
        else if(sum < 0)
        {
            tvgivereceive.setText(getResources().getString(R.string.to_give));
            tvgivereceiveamount.setText("₹ "+String.valueOf(sum).replace("-",""));
            tvgivereceiveamount.setTextColor(getResources().getColor(R.color.mred));
        }
        else if(sum == 0)
        {
            tvgivereceive.setText(getResources().getString(R.string.settled));
            tvgivereceiveamount.setText("₹ "+String.valueOf(sum));
            tvgivereceiveamount.setTextColor(getResources().getColor(R.color.mblack));
        }

        return sum;
    }

    void setAdapter()
    {
        adapter = new Transaction_adapter(activity, arrayList, new TransactionOnClick() {
            @Override
            public void onClick(int position, transactionModel model) {

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                Intent intent = new Intent(activity, TransactionDetail.class);
                intent.putExtra("mobilenumber",contactDetailarrayList.get(0).getContactNumber());
                intent.putExtra("transactionid",String.valueOf(model.getTransactionid()));
                startActivity(intent);
            }
        });

        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(adapter);
        SwipeDelete();
    }

    void SwipeDelete()
    {
        final Paint p = new Paint();

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {

                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                transactionModel model = arrayList.get(viewHolder.getAdapterPosition());

                if (model.getIsApproved() == 1)
                {
                    return 0;
                }
                else if(model.getCredituserid() == Integer.parseInt(globalclass.getuserid()))
                {
                   return 0;
                }
                else
                {
                    return super.getSwipeDirs(recyclerView, viewHolder);
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    return;
                }

                transactionModel model = arrayList.get(viewHolder.getAdapterPosition());
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT)
                {
                    ConfirmRemovedialogue(model,viewHolder,position);
                }
                else if (direction == ItemTouchHelper.RIGHT)
                {
                    Intent intent = new Intent(activity, EditPay.class);
                    intent.putExtra("mobilenumber",model.getCreditmobilenumber());
                    intent.putExtra("transactionid",String.valueOf(model.getTransactionid()));
                    startActivity(intent);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        p.setColor(Color.parseColor("#9E9E9E"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
//                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit);
                        icon = globalclass.drawableToBitmap(getResources().getColor(R.color.mwhite),getResources().getDrawable(R.drawable.ic_edit));
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else if(dX < 0){
                        p.setColor(Color.parseColor("#F44336"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
//                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
                        icon = globalclass.drawableToBitmap(getResources().getColor(R.color.mwhite),getResources().getDrawable(R.drawable.ic_delete));
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }


            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerview);
    }

    void ShowSnakeBarToUndo(final transactionModel model, final int position)
    {
        Snackbar snackbar = Snackbar
                .make(activity.findViewById(android.R.id.content), "Removed successfully", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //todo undo deleted item
                adapter.undoItem(model, position);
            }
        });

        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    void ConfirmRemovedialogue(final transactionModel model, final RecyclerView.ViewHolder viewHolder, final int position)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure remove ?")
                .setCancelable(false)
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        deleteTransaction(model,position);
//                        ShowSnakeBarToUndo(model,position);
                    }
                })
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        //todo refresh the adapter to prevent hiding the item from UI
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }).show();
    }

    void deleteTransaction(final transactionModel model, final int position)
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"deleteTransaction";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("transactionid", String.valueOf(model.getTransactionid()));
        params.put("creditmobilenumber", String.valueOf(model.getCreditmobilenumber()));

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("deleteTransaction_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            adapter.removeItem(position);
                            mydatabase.deleteData(mydatabase.transactionmaster,"transactionid", String.valueOf(model.getTransactionid()));
                            mydatabase.deleteHomeMaster();

                            globalclass.snackit(activity,"Removed successfully");
                            if(arrayList.isEmpty())
                            {
                                globalclass.callNotificationReceiver();
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"deleteTransaction_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("deleteTransaction_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"deleteTransaction_JSONException",params.toString(),error);
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

        showprogress();

        final String url = globalclass.getUrl()+"getAllTransaction";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();

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
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllTransaction_RES_ErrorInFunction",params.toString(),message);
                            }
                            else if(!message.equalsIgnoreCase("No data found"))
                            {
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAllTransaction_RES_ErrorInFunction",params.toString(),message);
                            }
                            else if(message.equalsIgnoreCase("No data found"))
                            {
                                mydatabase.deleteData(mydatabase.transactionmaster,"creditmobilenumber",callmobilenumber);
                                mydatabase.deleteData(mydatabase.transactionmaster,"debitmobilenumber",callmobilenumber);
                                globalclass.callNotificationReceiver();
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

    //todo location code

    void requestLocationPermission(){

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {

                            globalclass.log(TAG,"Location permission Granted");

                            if(!isGPSEnabled(activity))
                            {
                                AskForGPS();
                            }
                            else
                            {
                                Intent intent = new Intent(activity, Pay.class);
                                intent.putExtra("mobilenumber",getIntent().getStringExtra("mobilenumber"));
                                startActivity(intent);
                            }
                        }

                        if (report.getDeniedPermissionResponses().size() > 0) {
                            globalclass.log(TAG,"Location permission Denied");
                            globalclass.snackit(activity,"Location permission required to pay");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {

                        globalclass.log(TAG+"_requestAllPermissions",error.toString());
                        globalclass.sendLog(Globalclass.TryCatchException,TAG,"","_requestAllPermissions","",error.toString());
                    }
                })
                .onSameThread()
                .check();
    }

    boolean isGPSEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    void AskForGPS() {
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1 * 1000);

        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        settingsBuilder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(activity)
                .checkLocationSettings(settingsBuilder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response =
                            task.getResult(ApiException.class);
                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try
                            {
                                ResolvableApiException resolvableApiException =
                                        (ResolvableApiException) ex;
                                resolvableApiException
                                        .startResolutionForResult(activity,
                                                GPS_PERMISSION);
                            } catch (IntentSender.SendIntentException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GPS_PERMISSION)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                globalclass.log(TAG, "onActivityResult: GPS Enabled by user");
                requestLocationPermission();
            }
            else if(resultCode == Activity.RESULT_CANCELED)
            {
                globalclass.log(TAG, "onActivityResult: User rejected GPS request");
                globalclass.snackit(activity,"Gps require to pay!");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transactionlist, menu);

        MenuItem menu_filter = menu.findItem(R.id.menu_filter);
        rootlo = menu_filter.getActionView();
        ivfilterdone = rootlo.findViewById(R.id.ivfilterdone);

        if(!globalclass.getStringData("filteroption").equalsIgnoreCase(""))
        {
            ivfilterdone.setVisibility(View.VISIBLE);
        }

        if(arrayList.isEmpty())
        {
            menu_filter.setVisible(false);
        }
        else
        {
            menu_filter.setVisible(true);
        }

        rootlo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showFilter_BS();
            }
        });

        return true;
    }

    public void refreshonCreateOptionsMenu(Activity activity)
    {
        activity.invalidateOptionsMenu();
        showhide();
    }

    void showhide()
    {
        int sum = globalclass.getCustomerSum(contactDetailarrayList.get(0).getContactNumber());
        if(sum == 0)
        {
//            tvgeneratepdf.setVisibility(View.INVISIBLE);
//            tvsetremainder.setVisibility(View.INVISIBLE);
            tvgeneratepdf.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(activity,R.drawable.ic_pdf_gray), null, null);
            tvgeneratepdf.setCompoundDrawablePadding(20);
            tvgeneratepdf.setTextColor(getResources().getColor(R.color.mgray));
            tvsetremainder.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(activity,R.drawable.ic_reminder_gray), null, null);
            tvsetremainder.setCompoundDrawablePadding(20);
            tvsetremainder.setTextColor(getResources().getColor(R.color.mgray));
        }
        else
        {
//            tvgeneratepdf.setVisibility(View.VISIBLE);
//            tvsetremainder.setVisibility(View.VISIBLE);
            tvgeneratepdf.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(activity,R.drawable.ic_pdf), null, null);
            tvgeneratepdf.setCompoundDrawablePadding(20);
            tvgeneratepdf.setTextColor(getResources().getColor(R.color.mblack));
            tvsetremainder.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(activity,R.drawable.ic_reminder), null, null);
            tvsetremainder.setCompoundDrawablePadding(20);
            tvsetremainder.setTextColor(getResources().getColor(R.color.mblack));
        }

        if(arrayList.isEmpty())
        {
//            tvgeneratepdf.setVisibility(View.INVISIBLE);
            tvgeneratepdf.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(activity,R.drawable.ic_pdf_gray), null, null);
            tvgeneratepdf.setCompoundDrawablePadding(20);
            tvgeneratepdf.setTextColor(getResources().getColor(R.color.mgray));
        }
        else
        {
//            tvgeneratepdf.setVisibility(View.VISIBLE);
            tvgeneratepdf.setCompoundDrawablesWithIntrinsicBounds(null, AppCompatResources.getDrawable(activity,R.drawable.ic_pdf), null, null);
            tvgeneratepdf.setCompoundDrawablePadding(20);
            tvgeneratepdf.setTextColor(getResources().getColor(R.color.mblack));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_call:

                showCallDialogue(activity,"Sure you want to call ?");

                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    void showFilter_BS()
    {
        final Dialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.filter_bs);

        TextView tvremovefilter = dialog.findViewById(R.id.tvremovefilter);
        final RadioGroup radiogroupfilter = dialog.findViewById(R.id.radiogroupfilter);
        RadioButton radiobthightolo = dialog.findViewById(R.id.radiobthightolo);
        RadioButton radiobtlowtohigh = dialog.findViewById(R.id.radiobtlowtohigh);
        RadioButton radiobtdate = dialog.findViewById(R.id.radiobtdate);
        RadioButton radiobtdaterange = dialog.findViewById(R.id.radiobtdaterange);

        if(!globalclass.checknull(globalclass.getStringData("filteroption")).equalsIgnoreCase(""))
        {
            if(globalclass.getStringData("filteroption").equalsIgnoreCase("radiobthightolo"))
            {
                radiogroupfilter.check(radiogroupfilter.getChildAt(0).getId());
            }
            else if(globalclass.getStringData("filteroption").equalsIgnoreCase("radiobtlowtohigh"))
            {
                radiogroupfilter.check(radiogroupfilter.getChildAt(1).getId());
            }
            else if(globalclass.getStringData("filteroption").equalsIgnoreCase("radiobtdate"))
            {
                radiogroupfilter.check(radiogroupfilter.getChildAt(2).getId());
            }
            else if(globalclass.getStringData("filteroption").equalsIgnoreCase("radiobtdaterange"))
            {
                radiogroupfilter.check(radiogroupfilter.getChildAt(3).getId());
            }

            tvremovefilter.setVisibility(View.VISIBLE);
        }

        radiobthightolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.setStringData("filteroption","radiobthightolo");
                ivfilterdone.setVisibility(View.VISIBLE);
                dialog.dismiss();

                getData(globalclass.defaultstartdate,globalclass.getCurrentDate(),"amount DESC");
            }
        });

        radiobtlowtohigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.setStringData("filteroption","radiobtlowtohigh");
                ivfilterdone.setVisibility(View.VISIBLE);
                dialog.dismiss();

                getData(globalclass.defaultstartdate,globalclass.getCurrentDate(),"amount ASC");
            }
        });

        radiobtdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                openMaterialDatePicker();
            }
        });

        radiobtdaterange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                openMaterialRangeDatePicker();
            }
        });

        tvremovefilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.setStringData("filteroption","");
                ivfilterdone.setVisibility(View.GONE);
                dialog.dismiss();

                getData(globalclass.defaultstartdate,globalclass.getCurrentDate(),"lastupdatedatetime DESC");
            }
        });

        dialog.show();
    }

    void openMaterialDatePicker()
    {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        MaterialDatePicker<Long> picker = builder.build();
        picker.show(getSupportFragmentManager(), picker.toString());

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {

                globalclass.setStringData("filteroption","radiobtdate");
                ivfilterdone.setVisibility(View.VISIBLE);

                String date = globalclass.getLongToDatetime(selection,"yyyy-MM-dd");
                globalclass.log(TAG,date);
                getData(date,date,"lastupdatedatetime DESC");
            }
        });
    }

    void openMaterialRangeDatePicker()
    {
//        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
//        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
//        picker.show(getSupportFragmentManager(), picker.toString());

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.show(getSupportFragmentManager(), picker.toString());
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(Pair<Long, Long> selection) {

                globalclass.setStringData("filteroption","radiobtdaterange");
                ivfilterdone.setVisibility(View.VISIBLE);

                String startdate = globalclass.getLongToDatetime(selection.first,"yyyy-MM-dd");
                String enddate = globalclass.getLongToDatetime(selection.second,"yyyy-MM-dd");
                globalclass.log(TAG,startdate+" to "+
                        enddate);
                getData(startdate,enddate,"lastupdatedatetime DESC");
            }
        });
    }

    void showCallDialogue(Activity activity, String title)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        requestCallPermission();
                    }
                })
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .show();
    }

    void requestCallPermission()
    {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        if(callmobilenumber != null || !callmobilenumber.equalsIgnoreCase(""))
                        {
                            globalclass.makeCall(activity,callmobilenumber);
                        }
                        else
                        {
                            globalclass.toast_long("Unable to call, due to internal error!");
                            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","makeCall","callmobilenumber: "+callmobilenumber,"userid: "+globalclass.getuserid());
                        }
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response)
                    {
                        globalclass.log(TAG,"Call permission Denied");
                        globalclass.snackit(activity,"Permission required to make call");
                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {

                        globalclass.log(TAG+"_requestCallPermission",error.toString());
                        globalclass.sendLog(Globalclass.TryCatchException,TAG,"","requestCallPermission","",error.toString());
                    }
                }).check();
    }

    void requestStoragePermission()
    {
        Dexter.withActivity(activity)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {

                            globalclass.log(TAG,"Storage permission Granted");
                            generatePdf();
                        }

                        if (report.getDeniedPermissionResponses().size() > 0) {

                            globalclass.log(TAG,"Storage permission Denied");
                            globalclass.snackit(activity,"Storage permission required to save pdf!");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {

                        globalclass.log(TAG+"__requestStoragePermission",error.toString());
                        globalclass.sendLog(Globalclass.TryCatchException,TAG,"","__requestStoragePermission","",error.toString());
                    }
                })
                .onSameThread()
                .check();
    }

    void generatePdf()
    {
        try
        {
            final String createPDF = generate_pdf.createPDF(contactDetailarrayList.get(0).getContactName(),arrayList,
                    tvgivereceive.getText().toString().trim(),
                    tvgivereceiveamount.getText().toString().trim(),
                    setSum());

            if(!globalclass.checknull(createPDF).equalsIgnoreCase(""))
            {
                if(new File(createPDF).exists())
                {
                    Snackbar snackbar = Snackbar
                            .make(activity.findViewById(android.R.id.content), "Pdf generated successfully!", 3000);
                    snackbar.setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            try
                            {
                                globalclass.openPdf(activity,createPDF);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                String error = Log.getStackTraceString(e);
                                globalclass.log(TAG,"openPdfException: "+error);
                                globalclass.toast_short(getResources().getString(R.string.open_pdf_no_app_found));
                                globalclass.sendLog(Globalclass.GeneratePdf,TAG,"","openPdfException","",error);
                            }
                        }
                    });

                    snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                    snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
                    snackbar.show();
                }
                else
                {
                    String error = "PDF file not exist";
                    globalclass.log(TAG,error);
                    globalclass.toast_short("Fail to generated pdf!");
                    globalclass.sendLog(Globalclass.GeneratePdf,TAG,"",error,"",createPDF);
                }
            }
            else
            {
                globalclass.toast_short("Unable to generated pdf!");
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,"generatePdfException: "+error);
            globalclass.toast_short("Fail to generated pdf!");
            globalclass.sendLog(Globalclass.GeneratePdf,TAG,"","generatePdfException","",error);
        }

    }

    void showprogress()
    {
        RelativeLayout progresslo = findViewById(R.id.progresslo);

        if (progresslo.getVisibility() != View.VISIBLE) {
            progresslo.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, //disable all ContactOnClick/onTouch
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    void hideprogress()
    {
        RelativeLayout progresslo = findViewById(R.id.progresslo);
        if (progresslo.getVisibility() == View.VISIBLE) {
            progresslo.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //enable all ContactOnClick/onTouch
        }
    }

    public BroadcastReceiver NotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(TAG,"NotificationReceiver: onReceive");

                getData(globalclass.defaultstartdate,globalclass.getCurrentDate(),"lastupdatedatetime DESC");
                setSum();
                globalclass.setStringData("filteroption","");
                refreshonCreateOptionsMenu(activity);
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(TAG,"NotificationReceiver: onReceive\n"+error);
                globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","NotificationReceiver: onReceive","",error);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if(globalclass.checknull(globalclass.getStringData("filteroption")).equalsIgnoreCase(""))
        {
            getData(globalclass.defaultstartdate,globalclass.getCurrentDate(),"lastupdatedatetime DESC");
            setSum();
            refreshonCreateOptionsMenu(activity);
        }

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                NotificationReceiver, new IntentFilter(Globalclass.NotificationReceiver));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(NotificationReceiver);
    }
}
