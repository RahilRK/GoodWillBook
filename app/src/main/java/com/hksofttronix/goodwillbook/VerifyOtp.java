package com.hksofttronix.goodwillbook;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hksofttronix.goodwillbook.AddBusiness.Addbusiness;
import com.hksofttronix.goodwillbook.Managepassword.NewOrResetpassword;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
import com.infideap.blockedittext.BlockEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyOtp extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = VerifyOtp.this;
    
    Globalclass globalclass;
    FirebaseAuth mAuth;
    CountDownTimer cTimer = null;
    VolleyApiCall volleyApiCall;

    BlockEditText edcode;

    String getmVerificationId;
    String getmobilenumber;
    int resendOtpCount = 0;

    TextView tvtimer;
    MaterialButton verifyotpbt,resendotpbt;

    FirebaseFirestore firestoredb = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        init();
        binding();
        onclick();
        setToolbar();
        getmobilenumber = getIntent().getStringExtra("mobilenumber");
        globalclass.log(TAG,getmobilenumber);
        sentOtp(getmobilenumber);

        if(BuildConfig.DEBUG)
        {
            edcode.setText(getResources().getString(R.string.code));
//            globalclass.toast_short("Verified successfully");
//            handleIntent();
        }

        globalclass.log(TAG,"fromdata: "+globalclass.getStringData("from"));
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mAuth = FirebaseAuth.getInstance();
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        tvtimer = findViewById(R.id.tvtimer);
        verifyotpbt = findViewById(R.id.verifyotpbt);
        resendotpbt = findViewById(R.id.resendotpbt);
        edcode = findViewById(R.id.code);
    }

    void onclick()
    {
        verifyotpbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                globalclass.hideKeyboard(activity);
                if(edcode.getText().length()!=6)
                {
                    globalclass.snackit(activity,"Please enter the otp");
                }
                else if(edcode.getText().length()==6)
                {
                    showprogress();
                    verifyCode(edcode.getText());
                }

            }
        });

        resendotpbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                globalclass.hideKeyboard(activity);

                resendOtpCount++;
                if(resendOtpCount >= 3)
                {
                    globalclass.showDialogue(activity,"Unable to proceed","Your maximum attempt has been done, please try after sometime");
                }
                else
                {
//                    startTimer();
                    sentOtp(getmobilenumber);
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    //TODO #1 sentOtp
    void sentOtp(String getMobileNo)
    {
        try
        {
            showprogress();
//            startTimer();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    "+91"+getMobileNo,        // Phone number to verify
                    30,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    activity,               // Activity (for callback binding)
                    mCallBack);        // OnVerificationStateChangedCallbacksPhoneAuthActivity.java

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_sentOtpException",error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","sentOtpException","",error);
        }

    }

    //TODO #2
    PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String mVerificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(mVerificationId, forceResendingToken);
            getmVerificationId=mVerificationId;
            globalclass.log(TAG+"_onCodeSent",getmVerificationId);
            hideprogress();
            startTimer();

            if(edcode.getText().length() == 6)
            {
                verifyCode(edcode.getText()); //add this to autoverify otp
            }
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();

            if (code != null) {

                edcode.setText(code);
                verifyCode(code);
                globalclass.log(TAG+"_onVerificationCompleted",code);
            }
            else
            {
                globalclass.log(TAG+"_onVerificationCompleted","Code is Null");
                globalclass.sendLog(Globalclass.TryCatchException,TAG,"","onVerificationCompleted","","Code is Null");
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

            globalclass.toast_long("Unable to send code due to bad network connection");
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_onVerificationFailed", error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","onVerificationFailed","",error);
        }
    };

    //TODO #3 verify code
    void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(getmVerificationId, code);
        signInWithCredential(credential);
    }

    void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser userdetail = task.getResult().getUser();
                            String userid = userdetail.getUid();
                            globalclass.log(TAG,"auth_userid: "+userid);

                            hideprogress();
                            globalclass.toast_short("Verified successfully");
                            handleIntent();
                        } else {

                            hideprogress();
                            globalclass.toast_short("Invalid Otp");
                            globalclass.log(TAG+"_signInWithCredential",task.getException().getMessage());
                            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","signInWithCredential","",task.getException().getMessage());
                        }
                    }
                });
    }

    void handleIntent()
    {
        if(globalclass.getStringData("from").equalsIgnoreCase("OthersFrag"))
        {
            startActivity(new Intent(getApplicationContext(), NewOrResetpassword.class));
            finish();
        }
        else if(globalclass.getStringData("from").equalsIgnoreCase("LockScreen"))
        {
            startActivity(new Intent(getApplicationContext(), LockScreen.class).putExtra("goto","createcode"));
            finish();
        }
        else
        {
            registerUser();
        }

        globalclass.setStringData("from","");
    }

    void registerUser()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"registerUser";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("mobilenumber", getmobilenumber);
        params.put("DeviceName", ""+ Build.MANUFACTURER);
        params.put("DeviceModel", ""+Build.MODEL);
        params.put("AndroidVersion", ""+ Build.VERSION.RELEASE);
        params.put("AppVersion", ""+BuildConfig.VERSION_NAME);

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("registerUser_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = "";

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            globalclass.subscribeToTopic(getmobilenumber);
                            JSONArray data_jsonArray = jsonObject.getJSONArray("data");
                            for (int i=0;i<data_jsonArray.length();i++)
                            {
                                JSONObject data_object = data_jsonArray.getJSONObject(i);
                                message = data_object.getString(getResources().getString(R.string.message));
                                int totalbusiness = data_object.getInt("totalbusiness");

                                globalclass.setStringData("message",data_object.getString("message"));
                                globalclass.setIntData("userid",data_object.getInt("userid"));
                                if(globalclass.checknull(data_object.getString("name")).equalsIgnoreCase(""))
                                {
                                    globalclass.setStringData("username",getResources().getString(R.string.temp_username));
                                }
                                else
                                {
                                    globalclass.setStringData("username",data_object.getString("name"));
                                }
                                globalclass.setStringData("mobilenumber",data_object.getString("mobilenumber"));
                                globalclass.setStringData("useremailid",data_object.getString("emailid"));
                                globalclass.setStringData("transactionpassword",data_object.getString("transactionpassword"));
                                globalclass.setIntData("isverified",data_object.getInt("isverified"));

                                if(!globalclass.checknull(globalclass.getStringData("transactionpassword")).equalsIgnoreCase(""))
                                {
                                    globalclass.setBooleanData("transactionpassword_status",true);
                                }

                                if(message.equalsIgnoreCase("User already exists"))
                                {
                                    if(totalbusiness == 0)
                                    {
                                        //add business
                                        globalclass.setBooleanData("new registered user",true);
                                        globalclass.setBooleanData("transfer data",false);
                                        startActivity(new Intent(getApplicationContext(), Addbusiness.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    }
                                    else
                                    {
                                        //SetupPage
                                        startActivity(new Intent(getApplicationContext(), SetupPage.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    }
                                }
                                else if(message.equalsIgnoreCase("Register successfully"))
                                {
                                    //add business
                                    globalclass.setBooleanData("new registered user",true);
                                    globalclass.setBooleanData("transfer data",false);
                                    startActivity(new Intent(getApplicationContext(), Addbusiness.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                                else if(message.equalsIgnoreCase("User already exists, please add business"))
                                {
                                    //add business and ask for transfer data
                                    globalclass.setBooleanData("transfer data",true);
                                    startActivity(new Intent(getApplicationContext(), Addbusiness.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"registerUser_ErrorInFunction",params.toString(),result);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("registerUser_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"registerUser_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    //start timer function
    void startTimer() {

        tvtimer.setVisibility(View.VISIBLE);

        cTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {

                String getSecond = ""+ millisUntilFinished / 1000;
//                globalclass.log(TAG+"_onTick","seconds remaining: " + getSecond);
                if(Integer.parseInt(getSecond) == 0 || Integer.parseInt(getSecond) == 1)
                {
                    tvtimer.setText(getSecond+" second to go!");
                }
                else
                {
                    tvtimer.setText(getSecond+" seconds to go!");
                }
                tvtimer.setTextColor(getResources().getColor(R.color.colorPrimary));
                resendotpbt.setVisibility(View.GONE);
                verifyotpbt.setVisibility(View.VISIBLE);

            }
            public void onFinish() {

                cTimer = null;
                tvtimer.setText("Otp has been expired!");
                tvtimer.setTextColor(getResources().getColor(R.color.mgray));
                verifyotpbt.setVisibility(View.GONE);
                resendotpbt.setVisibility(View.VISIBLE);
            }
        };
        cTimer.start();
    }

    //cancel timer
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
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

    void showStopVerificationDialogue(Activity activity, String message)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        cancelTimer();
                        finishAffinity();
                        finish();
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

    @Override
    public void onBackPressed() {

        if(cTimer == null)
        {
            globalclass.log(TAG,"timer is not running");
            super.onBackPressed();
            cancelTimer();
        }
        else
        {
            globalclass.log(TAG,"timer is running");
            showStopVerificationDialogue(activity,"Verification is pending, do you still want to cancel verification and exit?");
        }
    }
}
