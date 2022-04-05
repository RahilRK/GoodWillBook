package com.hksofttronix.goodwillbook.Pay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.hksofttronix.goodwillbook.AddBusiness.addBusinessModel;
import com.hksofttronix.goodwillbook.ContactList.ContactModel;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Transaction.transactionModel;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
import com.infideap.blockedittext.BlockEditText;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class Pay extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LocationListener
{
    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Pay.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    ImageView iv;
    TextView creditname,tvdate,tvtime;
    LinearLayout businessspinnerlo;
    Spinner business_spinner;
    TextInputLayout amounttlo,remarktlo;
    EditText amount,remark;
    LinearLayout choosedate,choosetime,add_attachmentlo;
    RecyclerView recyclerview;
    MaterialButton paybt;

    ArrayList<ContactModel> contactDetailarrayList = new ArrayList<>();

    ArrayList<addBusinessModel> arrayList = new ArrayList<>();

    ArrayList<String> selectedimagesArrayList = new ArrayList<>();
    AttachPhotos_adapter attachPhotos_adapter;

    int GPS_PERMISSION = 444;
    String currentLocation = "";
    int takeLocation=0;

    //todo Fused Api Client parameters...
    Location mLocation;
    FusedLocationProviderClient mFusedLocationClient = null;
    LocationRequest mLocationRequest;
    LocationCallback mlocationCallback;

    String creditbusinessacid = "0"; //zero when user is not reg is app...
    String currentdatetime;
    String dbDate,dbTime;
    int mYear, mMonth, mDay, mHour, mMinute;

    MenuItem menuItem_transactionpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        init();
        binding();
        getContactDetail();
        onClick();
        setToolbar();
        getBusinessList();
        setAttachPhotosAdapter();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    void binding()
    {
        iv = findViewById(R.id.iv);
        creditname = findViewById(R.id.creditname);
        tvdate = findViewById(R.id.tvdate);
        tvtime = findViewById(R.id.tvtime);
        businessspinnerlo = findViewById(R.id.businessspinnerlo);
        business_spinner = findViewById(R.id.business_spinner);
        amounttlo = findViewById(R.id.amounttlo);
        remarktlo = findViewById(R.id.remarktlo);
        amount = findViewById(R.id.amount);
        remark = findViewById(R.id.remark);
        choosedate = findViewById(R.id.choosedate);
        choosetime = findViewById(R.id.choosetime);
        add_attachmentlo = findViewById(R.id.add_attachmentlo);
        recyclerview = findViewById(R.id.recyclerview);
        paybt = findViewById(R.id.paybt);

        business_spinner.setOnItemSelectedListener(this);
        business_spinner.setPrompt(getResources().getString(R.string.choose_business));
    }

    void getContactDetail()
    {
        contactDetailarrayList = mydatabase.getContactDetail(getIntent().getStringExtra("mobilenumber"));
        if(!contactDetailarrayList.isEmpty())
        {
            creditname.setText(contactDetailarrayList.get(0).getContactName());
            setImageViewDrawable();
        }
        else
        {
            globalclass.toast_long("No contact details found for "+getIntent().getStringExtra("mobilenumber"));
            onBackPressed();
        }
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

    @SuppressLint("ClickableViewAccessibility")
    void onClick()
    {
        remark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.remark) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        add_attachmentlo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestStoragePermission();
            }
        });

        paybt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.hideKeyboard(activity);
                if(validation())
                {
                    if(globalclass.getBooleanData("transactionpassword_status"))
                    {
                        showAskTransactionPassword_BS("paybt");
                    }
                    else
                    {
                        showdoTransactionDialogue(activity,"Are you sure, you want to pay ?");
                    }

                }
            }
        });

        choosedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OpenDatePicker();
            }
        });

        choosetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tvdate.getText().length() == 0)
                {
                    globalclass.snackit(activity,"Please choose date");
                    return;
                }

                OpenTimePicker();
            }
        });
    }

    void OpenDatePicker()
    {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);

                        SimpleDateFormat userformat = new SimpleDateFormat("dd MMM yyyy");
                        SimpleDateFormat dbformat = new SimpleDateFormat("yyyy-MM-dd");
                        String userDate = userformat.format(calendar.getTime());
                        dbDate = dbformat.format(calendar.getTime());
                        tvdate.setText(userDate);

                        if(DateUtils.isToday(globalclass.datetoMiliSecond(dbDate)))
                        {
                            tvtime.setText("");
                        }
                    }
                }, mYear, mMonth, mDay);
        c.add(Calendar.MONTH, globalclass.getMinimumtransactionMonth());
        datePickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(globalclass.getMilliSecond());
        datePickerDialog.show();
    }

    void OpenTimePicker()
    {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute)
                    {
                        if(DateUtils.isToday(globalclass.datetoMiliSecond(dbDate)))
                        {
                            Calendar selectedtime = Calendar.getInstance();
                            Calendar currenttime = Calendar.getInstance();
                            selectedtime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedtime.set(Calendar.MINUTE, minute);

                            //if date is of today then time should not be more then current time
                            if(selectedtime.getTimeInMillis() <= currenttime.getTimeInMillis())
                            {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(0,0,0,hourOfDay,minute,Integer.parseInt(globalclass.getCurrentSeconds()));

                                SimpleDateFormat userformat = new SimpleDateFormat("hh:mm:ss a");
                                SimpleDateFormat dbformat = new SimpleDateFormat("HH:mm:ss");
                                String userDate = userformat.format(calendar.getTime());
                                dbTime = dbformat.format(calendar.getTime());
                                tvtime.setText(userDate);

                                currentdatetime = dbDate+" "+dbTime;
                            }
                            else
                            {
                                globalclass.snackit(activity,"Invalid time");
                            }
                        }
                        else
                        {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(0,0,0,hourOfDay,minute,Integer.parseInt(globalclass.getCurrentSeconds()));

                            SimpleDateFormat userformat = new SimpleDateFormat("hh:mm:ss a");
                            SimpleDateFormat dbformat = new SimpleDateFormat("HH:mm:ss");
                            String userDate = userformat.format(calendar.getTime());
                            dbTime = dbformat.format(calendar.getTime());
                            tvtime.setText(userDate);

                            currentdatetime = dbDate+" "+dbTime;
                        }
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    boolean validation()
    {
        if(amount.getText().length() == 0 || Integer.parseInt(amount.getText().toString().trim()) == 0)
        {
            amounttlo.setError("Invalid amount");
            return false;
        }
        else if(remark.getText().length() > 0)
        {
            if(remark.getText().length() <= 9)
            {
                remarktlo.setError("Should contain minimum 10 characters");
                return false;
            }
            else if(tvdate.getText().length() == 0)
            {
                globalclass.snackit(activity,"Please choose date");
                return false;
            }
            else if(tvtime.getText().length() == 0)
            {
                globalclass.snackit(activity,"Please choose time");
                return false;
            }
            else
            {
                amounttlo.setErrorEnabled(false);
                remarktlo.setErrorEnabled(false);
                return true;
            }
        }
        else if(tvdate.getText().length() == 0)
        {
            globalclass.snackit(activity,"Please choose date");
            return false;
        }
        else if(tvtime.getText().length() == 0)
        {
            globalclass.snackit(activity,"Please choose time");
            return false;
        }
        else
        {
            amounttlo.setErrorEnabled(false);
            remarktlo.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        globalclass.log(TAG,
                "bussid: "+arrayList.get(position).getBusinessacid()+
                ", bussname: "+arrayList.get(position).getName());
        creditbusinessacid = String.valueOf(arrayList.get(position).getBusinessacid());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    void getBusinessList()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"getBusinessList";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("mobilenumber", contactDetailarrayList.get(0).getContactNumber());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();

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

                                if(businessModel.getUserid() == Integer.parseInt(globalclass.getuserid()))
                                {
                                    if(businessModel.getBusinessacid() != Integer.parseInt(globalclass.getActivebusinessacid()))
                                    {
                                        arrayList.add(businessModel);
                                    }
                                }
                                else
                                {
                                    arrayList.add(businessModel);
                                }
                            }

                            if(!arrayList.isEmpty())
                            {
                                businessspinnerlo.setVisibility(View.VISIBLE);
                                BusinessSpinner_adapter businessSpinnerAdap = new BusinessSpinner_adapter(activity,arrayList);
                                business_spinner.setAdapter(businessSpinnerAdap);
                            }
                            else
                            {
                                businessspinnerlo.setVisibility(View.GONE);
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
                                if(!message.equalsIgnoreCase(getResources().getString(R.string.no_data_found)) &&
                                        !message.equalsIgnoreCase(getResources().getString(R.string.no_user_found)))
                                {
                                    globalclass.snackit(activity,message);
                                }
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

    void showdoTransactionDialogue(Activity activity, String title)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        doTransaction();
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

    @SuppressLint("ClickableViewAccessibility")
    void showAskTransactionPassword_BS(final String from)
    {
        final Dialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.asktransactionpassword_bs);

        final BlockEditText transactionpassword =  dialog.findViewById(R.id.transactionpassword);
        ImageView ivpassword =  dialog.findViewById(R.id.ivpassword);
        final MaterialButton paybt =  dialog.findViewById(R.id.paybt);

        if(from.equalsIgnoreCase("disable password"))
        {
            paybt.setText("Verify");
        }

        transactionpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        transactionpassword.requestFocus();

        ivpassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {

                    case MotionEvent.ACTION_UP:
                        transactionpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;

                    case MotionEvent.ACTION_DOWN:
                        transactionpassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;

                }
                return true;
            }
        });

        paybt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                globalclass.hideKeyboard(activity);
                if(from.equalsIgnoreCase("disable password"))
                {
                    if(transactionpassword.getText().length()!=6)
                    {
                        transactionpassword.requestFocus();
                        transactionpassword.setSelection(transactionpassword.getText().length());
                        globalclass.toast_short("Invalid transaction password!");
                    }
                    else if(!globalclass.to_md5(transactionpassword.getText().toString().trim()).
                            equalsIgnoreCase(globalclass.getStringData("transactionpassword")))
                    {
                        globalclass.toast_short("Incorrect transaction password!");
                    }
                    else
                    {
                        dialog.dismiss();
                        globalclass.setBooleanData("transactionpassword_status",false);
                        menuItem_transactionpassword.setTitle("Enable password");
                        globalclass.toast_short("Transaction password has been disable!");
                    }
                }
                else
                {
                    if(transactionpassword.getText().length()!=6)
                    {
                        transactionpassword.requestFocus();
                        transactionpassword.setSelection(transactionpassword.getText().length());
                        globalclass.toast_short("Invalid transaction password!");
                    }
                    else if(!globalclass.to_md5(transactionpassword.getText().toString().trim()).
                            equalsIgnoreCase(globalclass.getStringData("transactionpassword")))
                    {
                        transactionpassword.requestFocus();
                        transactionpassword.setSelection(transactionpassword.getText().length());
                        globalclass.toast_short("Incorrect transaction password!");
                    }
                    else
                    {
                        dialog.dismiss();
                        showdoTransactionDialogue(activity,"Are you sure, you want to pay ?");
                    }
                }
            }
        });

        dialog.show();
    }

    void doTransaction()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"doTransaction";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("debituserid", globalclass.getuserid());
        params.put("debitbusinessacid", globalclass.getActivebusinessacid());
        params.put("credituserid", String.valueOf(contactDetailarrayList.get(0).getUserid()));
        params.put("creditbusinessacid", creditbusinessacid);
        params.put("amount", amount.getText().toString().trim());
        params.put("remark", remark.getText().toString().trim());
        params.put("debitdatetime", currentdatetime);
        params.put("debitlocation", currentLocation);
        params.put("mobilenumber", contactDetailarrayList.get(0).getContactNumber()); //credit contact number
        params.put("isAttachmentAvailable", String.valueOf(selectedimagesArrayList.size()));

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("doTransaction_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            String notificationStatus = jsonObject.getString(getResources().getString(R.string.notificationStatus));
                            if(!notificationStatus.contains("message_id"))
                            {
                                //todo sendLog of ErrorInSendingFcmNotification
                                globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,url,"doTransaction_ErrorInSendingFcmNotification",params.toString(),result);
                            }

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
                                if(!selectedimagesArrayList.isEmpty())
                                {
                                    addAttachmentToLocalDB(String.valueOf(data_object.getInt("transactionid")));
                                }

                                globalclass.toast_long("Send to "+contactDetailarrayList.get(0).getContactName()+" successfully!");
                                onBackPressed();
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"doTransaction_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("doTransaction_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"doTransaction_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    //todo getAttachmentImage
    void addAttachmentToLocalDB(String transactionid)
    {
        for(int i=0;i<selectedimagesArrayList.size();i++)
        {
            addAttachmentsModel attachmentsModel = new addAttachmentsModel();
            attachmentsModel.setAttachmentfilepath(selectedimagesArrayList.get(i));
            attachmentsModel.setTransactionid(Integer.parseInt(transactionid));
            attachmentsModel.setForupdate(0);

            mydatabase.addAttachments(attachmentsModel);
        }

        globalclass.startAnyService(activity,uploadAttachments_Service.class);
    }

    void OpenImagePicker()
    {
        FilePickerBuilder.getInstance().setMaxCount(globalclass.getAttachmentMaxImages())
                .setSelectedFiles(selectedimagesArrayList)
                .setActivityTheme(R.style.LibAppTheme)
                .pickPhoto(activity);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FilePickerConst.REQUEST_CODE_PHOTO)
        {
            if (resultCode == Activity.RESULT_OK && data != null) {
                selectedimagesArrayList.clear();
                selectedimagesArrayList.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                attachPhotos_adapter.notifyDataSetChanged();
            }

            globalclass.log(TAG, String.valueOf(selectedimagesArrayList.size())+" images selected");
        }
        else if(requestCode == GPS_PERMISSION)
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

    void setAttachPhotosAdapter()
    {
        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        attachPhotos_adapter = new AttachPhotos_adapter(activity, selectedimagesArrayList, new AttachPhotosOnClick() {
            @Override
            public void onDelete(int position) {

                attachPhotos_adapter.deleteItem(position);
                showhide_add_attachmentlo();
            }
        });
        recyclerview.setAdapter(attachPhotos_adapter);
    }

    public void showhide_add_attachmentlo()
    {
        globalclass.log(TAG, String.valueOf(selectedimagesArrayList.size())+" images available");
        if(selectedimagesArrayList.size() == 0)
        {
            recyclerview.setVisibility(View.GONE);
        }
        else
        {
            recyclerview.setVisibility(View.VISIBLE);
        }

        if(selectedimagesArrayList.size() >= globalclass.getAttachmentMaxImages())
        {
            add_attachmentlo.setVisibility(View.GONE);
        }
        else
        {
            add_attachmentlo.setVisibility(View.VISIBLE);
        }
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
                            OpenImagePicker();
                        }

                        if (report.getDeniedPermissionResponses().size() > 0) {

                            globalclass.log(TAG,"Storage permission Denied");
                            globalclass.snackit(activity,"Storage permission required to choose image");
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
                                StartFusedApi();
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

    public boolean isGPSEnabled(Context context) {
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

    void StartFusedApi() {
        
        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mlocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    @Override
    public void onLocationChanged(final Location location) {

        try
        {
            if(location!=null)
            {
                takeLocation++;
                mLocation = location;
                globalclass.log("onLocationChanged","onLocationChanged");
                currentLocation = String.valueOf(
                        mLocation.getLatitude()+","+
                                mLocation.getLongitude());

                if(takeLocation >= 5)
                {
                    StopFusedApi();
                }
            }
            else
            {
                globalclass.toast_short("There was error getting current location try again !");
                globalclass.sendLog(Globalclass.TryCatchException,TAG,"","onLocationChanged_Exception","userid="+globalclass.getuserid(),"Location is null");
            }
        }
        catch (Exception e)
        {
            globalclass.log("onLocationChanged_Exception",Log.getStackTraceString(e));
            globalclass.toast_short("There was error getting current location try again !");
            String error = Log.getStackTraceString(e);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","onLocationChanged_Exception","userid="+globalclass.getuserid(),error);
        }

    }

    void StopFusedApi()
    {
        if (!currentLocation.equalsIgnoreCase("")) {
            mFusedLocationClient.removeLocationUpdates(mlocationCallback);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pay, menu);
        menuItem_transactionpassword = menu.findItem(R.id.menu_transactionpassword).setVisible(false);
        showhideOptionsMenu(menu);
        return true;
    }

    void showhideOptionsMenu(Menu menu)
    {
        if(globalclass.checknull(globalclass.getStringData("transactionpassword")).equalsIgnoreCase(""))
        {
            menu.findItem(R.id.menu_transactionpassword).setVisible(false);
        }
        else
        {
            menu.findItem(R.id.menu_transactionpassword).setVisible(true);
            if(globalclass.getBooleanData("transactionpassword_status"))
            {
                menu.findItem(R.id.menu_transactionpassword).setTitle("Disable password");
            }
            else
            {
                menu.findItem(R.id.menu_transactionpassword).setTitle("Enable password");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_transactionpassword:

                if(globalclass.getBooleanData("transactionpassword_status"))
                {
//                    globalclass.setBooleanData("transactionpassword_status",false);
//                    menuItem_transactionpassword.setTitle("Enable password");
//                    globalclass.snackit(activity,"Transaction password has been disable!");

                    showAskTransactionPassword_BS("disable password");
                }
                else
                {
                    globalclass.setBooleanData("transactionpassword_status",true);
                    menuItem_transactionpassword.setTitle("Disable password");
                    globalclass.snackit(activity,"Transaction password has been enable!");
                }

                return true;

            default:
                return super.onContextItemSelected(item);
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

    @Override
    protected void onStart() {
        super.onStart();

        requestLocationPermission();
    }

    @Override
    public void onStop() {
        super.onStop();
        StopFusedApi();
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        globalclass.checkAutomaticTimeAndTimeZone(activity);
        showhide_add_attachmentlo();
    }
}
