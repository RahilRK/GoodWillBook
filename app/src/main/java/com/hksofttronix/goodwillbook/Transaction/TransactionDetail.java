package com.hksofttronix.goodwillbook.Transaction;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hksofttronix.goodwillbook.BuildConfig;
import com.hksofttronix.goodwillbook.ContactList.ContactModel;
import com.hksofttronix.goodwillbook.EditPay.EditPay;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.Pay.addAttachmentsModel;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.ViewAttachments.ViewAttachments;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDetail extends AppCompatActivity implements LocationListener
{

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = TransactionDetail.this;

    VolleyApiCall volleyApiCall;
    Globalclass globalclass;
    Mydatabase mydatabase;

    AppBarLayout appbar;
    RelativeLayout mainlo;
    TextView name,tvgotgive,amount,businessname,debitdatetime,debitlocation,creditdatetime,creditlocation,remark,attachment;
    ImageView iv,ivapproved;
    LinearLayout approvedlo;
    SwipeRefreshLayout swipeReferesh;
    MaterialButton approvebt;

    ArrayList<ContactModel> contactDetailarrayList = new ArrayList<>();
    ArrayList<transactionModel> transactionDetailarrayList = new ArrayList<>();
    ArrayList<addAttachmentsModel> attachmentArrayList = new ArrayList<>();

    int GPS_PERMISSION = 444;
    String currentLocation = "";

    //todo Fused Api Client parameters...
    Location mLocation;
    FusedLocationProviderClient mFusedLocationClient = null;
    LocationRequest mLocationRequest;
    LocationCallback mlocationCallback;

    Menu optionMenu = null;
    MenuItem menuItem_transactionpassword;

    String transactionid;

    Bitmap screenShot = null;
    MediaPlayer mp;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        binding();
        init();
        setToolbar();
        getIntentData();
        getTransactionDetail(transactionid);
        onClick();
        getAttachmentList();
    }

    void binding()
    {
        appbar = findViewById(R.id.appbar);
        mainlo = findViewById(R.id.mainlo);
        name = findViewById(R.id.name);
        tvgotgive = findViewById(R.id.tvgotgive);
        amount = findViewById(R.id.amount);
        businessname = findViewById(R.id.businessname);
        debitdatetime = findViewById(R.id.debitdatetime);
        debitlocation = findViewById(R.id.debitlocation);
        creditdatetime = findViewById(R.id.creditdatetime);
        creditlocation = findViewById(R.id.creditlocation);
        remark = findViewById(R.id.remark);
        attachment = findViewById(R.id.attachment);
        iv = findViewById(R.id.iv);
        ivapproved = findViewById(R.id.ivapproved);
        approvedlo = findViewById(R.id.approvedlo);
        swipeReferesh = findViewById(R.id.swipeReferesh);
        approvebt = findViewById(R.id.approvebt);
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        mp = MediaPlayer.create(activity, R.raw.screenshotsound);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
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

    void getIntentData()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        if(action != null)// if came from deeplink url
        {
            Uri data = intent.getData();
            List<String> pathSegments = data.getPathSegments();
            if(pathSegments.size()>0)
            {
                String prefix=pathSegments.get(0); // This will give you prefix as path
                globalclass.log(TAG,"prefix: "+prefix);
                globalclass.log(TAG,"data: "+data.toString());
                globalclass.log(TAG,"transactionid: "+prefix);
                transactionid = prefix;
            }
        }
        else
        {
            transactionid = intent.getStringExtra("transactionid");
        }
    }

    void getTransactionDetail(String transactionid)
    {
        try
        {
            transactionDetailarrayList = mydatabase.getTransactionDetail(Integer.parseInt(transactionid));

            if(!transactionDetailarrayList.isEmpty())
            {
                if(transactionDetailarrayList.get(0).getCredituserid() == Integer.parseInt(globalclass.getuserid()))
                {
                    getContactDetail(transactionDetailarrayList.get(0).getDebitmobilenumber());
                }
                else
                {
                    getContactDetail(transactionDetailarrayList.get(0).getCreditmobilenumber());
                }

                amount.setText("₹ "+String.valueOf(transactionDetailarrayList.get(0).getAmount()));
                debitdatetime.setText(globalclass.formatDateTime_DBToUser(transactionDetailarrayList.get(0).getDebitdatetime()));
                creditdatetime.setText(globalclass.formatDateTime_DBToUser(transactionDetailarrayList.get(0).getCreditdatetime()));
                remark.setText(globalclass.checknullAndSet(transactionDetailarrayList.get(0).getRemark()));

                setTextLogic();
            }
            else
            {
                globalclass.toast_long("Unable to fetch detail, please refresh and try again!");
                onBackPressed();
            }
        }
        catch (Exception e)
        {
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","getTransactionDetail_ErrorInFunction","transactionid = "+transactionid,"");
        }
    }

    void getContactDetail(String mobilenumber)
    {
        contactDetailarrayList = mydatabase.getContactDetail(mobilenumber);
        if(!contactDetailarrayList.isEmpty())
        {
            name.setText(contactDetailarrayList.get(0).getContactName());
            setImageViewDrawable();
        }
        else
        {
            globalclass.toast_long("No contact details found for "+mobilenumber);
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

    void setTextLogic()
    {
        if(Integer.parseInt(globalclass.getuserid()) == transactionDetailarrayList.get(0).getDebituserid())
        {
            tvgotgive.setText(getResources().getString(R.string.you_gave));
            amount.setTextColor(getResources().getColor(R.color.mred));
            businessname.setText(transactionDetailarrayList.get(0).getCreditbusinessname());
        }
        else
        {
            tvgotgive.setText(getResources().getString(R.string.you_got));
            amount.setTextColor(getResources().getColor(R.color.mgreen));
            businessname.setText(transactionDetailarrayList.get(0).getDebitbusinessname());
        }

        if(transactionDetailarrayList.get(0).getIsApproved() == 0)
        {
            ivapproved.setVisibility(View.GONE);
            approvedlo.setVisibility(View.GONE);

            if(Integer.parseInt(globalclass.getuserid()) == transactionDetailarrayList.get(0).getDebituserid())
            {
                approvebt.setVisibility(View.GONE);
            }
            else
            {
                approvebt.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            ivapproved.setVisibility(View.VISIBLE);
            approvedlo.setVisibility(View.VISIBLE);
            approvebt.setVisibility(View.GONE);
        }
    }

    void onClick()
    {
        debitlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.openGoogleMap(activity,transactionDetailarrayList.get(0).getDebitlocation());
            }
        });

        creditlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.openGoogleMap(activity,transactionDetailarrayList.get(0).getCreditlocation());
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
               getTransactionDetailOnline(String.valueOf(transactionid));
            }
        });

        approvebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(globalclass.getBooleanData("transactionpassword_status"))
                {
                    showAskTransactionPassword_BS("approvebt");
                }
                else
                {
                    showApproveTransactionDialogue(activity,"Are you sure, you want to approve ?");
                }
            }
        });
    }

    void getTransactionDetailOnline(final String transactionid)
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.log(TAG,getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"getTransactionDetail";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("transactionid", transactionid);


        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();

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

                            getTransactionDetail(transactionid);
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getTransactionDetail_ErrorInFunction",params.toString(),result);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("getTransactionDetail_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getTransactionDetail_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void getAttachmentList()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"getAttachmentList";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("transactionid", transactionid);

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();

                globalclass.log("getAttachmentList_RES",result);

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
                                addAttachmentsModel model = new addAttachmentsModel();
                                model.setAttachmentid(data_object.getInt("attachmentid"));
                                model.setTransactionid(data_object.getInt("transactionid"));
                                model.setUrl(data_object.getString("url"));
                                model.setChoosefromStorage(false);

                                attachmentArrayList.add(model);
                            }

                            if(!attachmentArrayList.isEmpty())
                            {
                                attachment.setText(getResources().getString(R.string.tap_here_to_view_attachments));
                                attachment.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent intent = new Intent(activity, ViewAttachments.class);
                                        intent.putParcelableArrayListExtra("attachmentArrayList",attachmentArrayList);
                                        startActivity(intent);

                                    }
                                });
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAttachmentList_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("getAttachmentList_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getAttachmentList_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void showApproveTransactionDialogue(Activity activity, String title)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        requestLocationPermission();
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
                        showApproveTransactionDialogue(activity,"Are you sure, you want to approve ?");
                    }
                }
            }
        });

        dialog.show();
    }

    void Approvetransaction()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"Approvetransaction";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("transactionid",transactionid);
        params.put("creditlocation",currentLocation);

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("Approvetransaction_RES",result);

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
                                globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,url,"Approvetransaction_ErrorInSendingFcmNotification",params.toString(),result);
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
                                globalclass.toast_long(message);
                                onBackPressed();
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"Approvetransaction_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("Approvetransaction_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"Approvetransaction_JSONException",params.toString(),error);
                    }
                }
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_transactiondetail, menu);
        optionMenu = menu;
        menuItem_transactionpassword = menu.findItem(R.id.menu_transactionpassword).setVisible(false);
        showhideonCreateOptionsMenu(menu);
        return true;
    }

    void showhideonCreateOptionsMenu(Menu menu)
    {
        if(menu != null)
        {
            if(Integer.parseInt(globalclass.getuserid()) == transactionDetailarrayList.get(0).getDebituserid())
            {
                if(transactionDetailarrayList.get(0).getIsApproved() == 0)
                {
                    menu.findItem(R.id.menu_updatetransaction).setVisible(true);
                }
                else
                {
                    menu.findItem(R.id.menu_updatetransaction).setVisible(false);
                }
            }
            else
            {
                menu.findItem(R.id.menu_updatetransaction).setVisible(false);

                if(transactionDetailarrayList.get(0).getIsApproved() == 1)
                {
                    menu.findItem(R.id.menu_transactionpassword).setVisible(false);
                    return;
                }

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
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_sharetransactiondetails:

                requestStoragePermission();
                return true;
            case R.id.menu_updatetransaction:

                Intent intent = new Intent(activity, EditPay.class);
                intent.putExtra("mobilenumber",contactDetailarrayList.get(0).getContactNumber());
                intent.putExtra("transactionid",String.valueOf(transactionDetailarrayList.get(0).getTransactionid()));
                startActivity(intent);

                return true;
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
                            takeScreenShot();
                        }

                        if (report.getDeniedPermissionResponses().size() > 0) {

                            globalclass.log(TAG,"Storage permission Denied");
                            globalclass.snackit(activity,"Storage permission required to take screenshot and share!");
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

    void takeScreenShot()
    {
        try
        {
            if(optionMenu != null)
            {
                optionMenu.findItem(R.id.menu_sharetransactiondetails).setVisible(false);
                optionMenu.findItem(R.id.menu_updatetransaction).setVisible(false);
                optionMenu.findItem(R.id.menu_transactionpassword).setVisible(false);
            }

            screenShot = globalclass.takeScreenShot(mainlo);

            if(!screenShot.equals("") || screenShot != null)
            {
                File file = globalclass.storeScreenShot(screenShot);
                if(file.exists())
                {
                    globalclass.log(TAG,file.getPath());
                    makeScreenshotSoundAndVibrate();
                    showScreenshotDialogue(screenShot,file);
                }
                else
                {
                    String error = "screenShot file is not created/not exist";
                    globalclass.log(TAG,error);
                    globalclass.toast_short(activity.getResources().getString(R.string.error_in_takeScreenShot));
                    globalclass.sendLog(Globalclass.Screenshot,TAG,"","takeScreenShot()","",error);
                }
            }
            else
            {
                String error = "screenShot is null";
                globalclass.log(TAG,error);
                globalclass.toast_short(activity.getResources().getString(R.string.error_in_takeScreenShot));
                globalclass.sendLog(Globalclass.Screenshot,TAG,"","takeScreenShot()","",error);
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,error);
            globalclass.toast_short(activity.getResources().getString(R.string.error_in_takeScreenShot));
            globalclass.sendLog(Globalclass.Screenshot,TAG,"","takeScreenShotAndShareException","",error);
        }

        if(optionMenu != null)
        {
            optionMenu.findItem(R.id.menu_sharetransactiondetails).setVisible(true);
            showhideonCreateOptionsMenu(optionMenu);
        }
    }

    void makeScreenshotSoundAndVibrate()
    {
        try
        {
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            }
            else
            {
                vibrator.vibrate(200);
            }

            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
                mp = MediaPlayer.create(activity, R.raw.screenshotsound);
            } mp.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            String error = "makeScreenshotSoundAndVibrate error";
            globalclass.log(TAG,error);
            globalclass.toast_short(activity.getResources().getString(R.string.error_in_takeScreenShot));
            globalclass.sendLog(Globalclass.Screenshot,TAG,"","makeScreenshotSoundAndVibrateException","",error);
        }
    }

    void showScreenshotDialogue(Bitmap screenShot, final File file)
    {
        // custom dialog
        final Dialog dialog = new Dialog(activity,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.showscreenshot_dialogue);


        ImageView ivscreenshot = dialog.findViewById(R.id.ivscreenshot);
        FloatingActionButton fab = dialog.findViewById(R.id.fab);
        ivscreenshot.setImageBitmap(screenShot);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareScreenshot(file);
            }
        });

        dialog.show();
    }

    void shareScreenshot(File file)
    {
        try
        {
            Uri uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider", file);

            Intent intent = ShareCompat.IntentBuilder.from(this)
                    .setStream(uri) // uri from FileProvider
                    .setType("image/*")
                    .getIntent()
                    .setAction(Intent.ACTION_SEND) //Change if needed
                    .setDataAndType(uri, "image/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .putExtra(android.content.Intent.EXTRA_SUBJECT, "EXTRA_SUBJECT")
                    .putExtra(Intent.EXTRA_TEMPLATE, "EXTRA_TEMPLATE")
                    .putExtra(android.content.Intent.EXTRA_TEXT, "Click here to view details:\nhttp://com.hksofttronix.goodwillbook/"+transactionid);
            startActivity(Intent.createChooser(intent,"Share on"));
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,error);
            globalclass.toast_short(activity.getResources().getString(R.string.error_in_takeScreenShot));
            globalclass.sendLog(Globalclass.Screenshot,TAG,"","shareScreenshotException","",error);
        }
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
                            globalclass.snackit(activity,"Location permission required to approve");
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
                globalclass.snackit(activity,"Gps require to approve!");
            }
        }
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

        showprogress();

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
                hideprogress();

                mLocation = location;
                globalclass.log("onLocationChanged","onLocationChanged");
                currentLocation = String.valueOf(
                        mLocation.getLatitude()+","+
                                mLocation.getLongitude());

                StopFusedApi();
                Approvetransaction();
            }
            else
            {
                globalclass.toast_short("There was error getting current location try again !");
                globalclass.sendLog(Globalclass.TryCatchException,TAG,"","onLocationChanged_Exception","userid="+globalclass.getuserid(),"Location is null");
            }
        }
        catch (Exception e)
        {
            globalclass.log("onLocationChanged_Exception", Log.getStackTraceString(e));
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
    protected void onStart() {
        super.onStart();

//        requestLocationPermission();
    }

    @Override
    public void onStop() {
        super.onStop();
        StopFusedApi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getIntentData();
        getTransactionDetail(transactionid);
    }
}
