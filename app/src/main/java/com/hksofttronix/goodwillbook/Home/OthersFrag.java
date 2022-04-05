package com.hksofttronix.goodwillbook.Home;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.beautycoder.pflockscreen.security.PFResult;
import com.beautycoder.pflockscreen.security.PFSecurityManager;
import com.beautycoder.pflockscreen.security.callbacks.PFPinCodeHelperCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hksofttronix.goodwillbook.AddBusiness.Addbusiness;
import com.hksofttronix.goodwillbook.BarcodeScanner;
import com.hksofttronix.goodwillbook.BusinessInfo.Businessinfo;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.LockScreen;
import com.hksofttronix.goodwillbook.Managepassword.Changepassword;
import com.hksofttronix.goodwillbook.Managepassword.NewOrResetpassword;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.NotificationSettings.Settings;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Profile.Viewprofile;
import com.hksofttronix.goodwillbook.Remainder.RemainderList;
import com.hksofttronix.goodwillbook.Feedback.SendFeedback;
import com.hksofttronix.goodwillbook.Verification;
import com.hksofttronix.goodwillbook.VerifyOtp;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.hksofttronix.goodwillbook.Globalclass.RefreshAll_ACTION;
import static com.hksofttronix.goodwillbook.Globalclass.RefreshAll_requestID;
import static com.hksofttronix.goodwillbook.Remainder.AlarmReminderContract.AlarmReminderEntry.CONTENT_URI_RefreshAll;


/**
 * A simple {@link Fragment} subclass.
 */
public class OthersFrag extends Fragment {


    public OthersFrag() {
        // Required empty public constructor
    }

    String TAG = this.getClass().getSimpleName();

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    AppCompatActivity activity;

    View myview;

    TextView name,viewprofile;
    ImageView myqrcode,creditscore;
    TextView viewbusinessinfo,addnewbusiness,generatereport,viewremainder,createapplock, managetransactionpassword,notification_settings,sendfeedback,faq,tandc,logout;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof AppCompatActivity)
        {
            activity  = (AppCompatActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_others, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myview = view;
        init();
        binding(view);
        setText();
        onclick();
        setCompoundDrawables();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding(View view)
    {
        name = view.findViewById(R.id.name);
        viewprofile = view.findViewById(R.id.viewprofile);
        myqrcode = view.findViewById(R.id.myqrcode);
        creditscore = view.findViewById(R.id.creditscore);
        viewbusinessinfo = view.findViewById(R.id.viewbusinessinfo);
        addnewbusiness = view.findViewById(R.id.addnewbusiness);
        generatereport = view.findViewById(R.id.generatereport);
        viewremainder = view.findViewById(R.id.viewremainder);
        createapplock = view.findViewById(R.id.createapplock);
        managetransactionpassword = view.findViewById(R.id.managetransactionpassword);
        notification_settings = view.findViewById(R.id.notification_settings);
        sendfeedback = view.findViewById(R.id.sendfeedback);
        faq = view.findViewById(R.id.faq);
        tandc = view.findViewById(R.id.tandc);
        logout = view.findViewById(R.id.logout);
    }

    void setText()
    {
        if(!globalclass.checknull(globalclass.getStringData("username")).equalsIgnoreCase(""))
        {
            name.setText(globalclass.getStringData("username"));
        }

//        if(!globalclass.getStringData(Globalclass.lockcode).equalsIgnoreCase(""))
//        {
//            createapplock.setText(getResources().getString(R.string.manageapplock));
//        }
    }

    void onclick()
    {
        viewprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(activity, Viewprofile.class));
            }
        });

        myqrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(activity, BarcodeScanner.class));
            }
        });

        viewbusinessinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(activity, Businessinfo.class));
            }
        });

        addnewbusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getUserTotalBusiness();
            }
        });

        viewremainder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(activity, RemainderList.class));
            }
        });

        createapplock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(activity, LockScreen.class).putExtra("goto","managecode"));
            }
        });

        managetransactionpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(globalclass.checknull(globalclass.getStringData("transactionpassword")).equalsIgnoreCase(""))
                {
                    startActivity(new Intent(activity, NewOrResetpassword.class));
                }
                else
                {
                    showManagePassword_BS();
                }
            }
        });

        notification_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(activity, Settings.class));
            }
        });

        sendfeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(activity, SendFeedback.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAskLogoutDialogue(activity,"Sure you want to logout ?");


            }
        });

    }

    void setCompoundDrawables()
    {
//        Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.ic_business);
//        drawable = DrawableCompat.wrap(drawable);
//        DrawableCompat.setTint(drawable.mutate(),getResources().getColor(R.color.colorPrimaryDark));
//        drawable.setBounds( 0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        viewbusinessinfo.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_business), null, null, null);
        viewbusinessinfo.setCompoundDrawablePadding(30);

        addnewbusiness.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_addbusiness), null, null, null);
        addnewbusiness.setCompoundDrawablePadding(30);

        generatereport.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_generatereport), null, null, null);
        generatereport.setCompoundDrawablePadding(30);

        viewremainder.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_reminder), null, null, null);
        viewremainder.setCompoundDrawablePadding(30);

        createapplock.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_applock), null, null, null);
        createapplock.setCompoundDrawablePadding(30);

        managetransactionpassword.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_password), null, null, null);
        managetransactionpassword.setCompoundDrawablePadding(30);

        notification_settings.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_notificationsettings), null, null, null);
        notification_settings.setCompoundDrawablePadding(30);

        sendfeedback.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_sendfeedback), null, null, null);
        sendfeedback.setCompoundDrawablePadding(30);

        faq.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_faq), null, null, null);
        faq.setCompoundDrawablePadding(30);

        tandc.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_tandc), null, null, null);
        tandc.setCompoundDrawablePadding(30);

        logout.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(activity,R.drawable.ic_logout), null, null, null);
        logout.setCompoundDrawablePadding(30);
    }

    void showManagePassword_BS()
    {
        final Dialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.managepassword_bs);

        TextView tvchangepassword =  dialog.findViewById(R.id.tvchangepassword);
        TextView tvforgotpassword =  dialog.findViewById(R.id.tvforgotpassword);

        tvchangepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                startActivity(new Intent(activity, Changepassword.class));
            }
        });

        tvforgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                showAskVerificationDialogue(activity,"We will send you otp to verify you mobilenumber!");
            }
        });

        dialog.show();
    }

    void getUserTotalBusiness()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"getUserTotalBusiness";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("getUserTotalBusiness_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            int totalbusiness = jsonObject.getInt("totalbusiness");
                            if(totalbusiness >= 2)
                            {
                                globalclass.showDialogue(activity,"Unable to add business","Maximum 2 business are allowed to add!");
                            }
                            else
                            {
                                startActivity(new Intent(activity, Addbusiness.class));
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
                        globalclass.log("getUserTotalBusiness_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getUserTotalBusiness_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void showAskVerificationDialogue(final Activity activity, String title)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Send otp", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        globalclass.setStringData("from",TAG);
                        Intent intent = new Intent(activity, VerifyOtp.class);
                        intent.putExtra("mobilenumber",globalclass.getStringData("mobilenumber"));
                        startActivity(intent);
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

    void showAskLogoutDialogue(final Activity activity, String title)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        PFSecurityManager.getInstance().getPinCodeHelper().delete(new PFPinCodeHelperCallback<Boolean>() {
                            @Override
                            public void onResult(PFResult<Boolean> result)
                            {

                                globalclass.cancelRefreshAllRemainderAlarm(activity,RefreshAll_ACTION,RefreshAll_requestID,CONTENT_URI_RefreshAll);
                                globalclass.unsubscribeToTopic(globalclass.getStringData("mobilenumber"));
                                globalclass.clearPref();
                                mydatabase.removeAllRemainders();
                                mydatabase.clearDatabase();

                                startActivity(new Intent(activity, Verification.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            }
                        });

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
}
