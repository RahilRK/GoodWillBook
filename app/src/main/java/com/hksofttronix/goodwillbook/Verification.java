package com.hksofttronix.goodwillbook;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class Verification extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Verification.this;
    
    Globalclass globalclass;
    CredentialsClient mCredentialsApiClient;
    VolleyApiCall volleyApiCall;

    TextInputLayout mobilenotf;
    EditText mobileno;
    MaterialButton sendotpbt;


    public static int RESOLVE_HINT = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        init();
        binding();
        setToolbar();
        onClick();
        requestMobilenumber();

        if(BuildConfig.DEBUG)
        {
            mobileno.setText(getResources().getString(R.string.my_mobno));
        }
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mCredentialsApiClient = Credentials.getClient(this);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        mobilenotf = findViewById(R.id.mobilenotf);
        mobileno = findViewById(R.id.mobileno);
        sendotpbt = findViewById(R.id.sendotpbt);
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

    void onClick()
    {
        mobileno.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hideKeyboard(activity);

                    if(!globalclass.isInternetPresent())
                    {
                        globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                        return false;
                    }

                    if(validation())
                    {
                        globalclass.setStringData("from",TAG);
                        Intent intent = new Intent(activity,VerifyOtp.class);
                        intent.putExtra("mobilenumber",mobileno.getText().toString());
                        startActivity(intent);
                    }

                }
                return false;
            }
        });

        sendotpbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.hideKeyboard(activity);

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                requestAllPermissions();
            }
        });
    }

    boolean validation()
    {
        if(mobileno.getText().length()>0)
        {
            String getmobileno = mobileno.getText().toString();
            String firstchar=getmobileno.substring(0,1);

            if(mobileno.getText().length()!=10)
            {
                mobilenotf.setError("Invalid mobile number");
                return false;
            }
            else if(!firstchar.equalsIgnoreCase("6") && !firstchar.equalsIgnoreCase("7") &&
                    !firstchar.equalsIgnoreCase("8") && !firstchar.equalsIgnoreCase("9"))
            {
                mobilenotf.setError("Invalid mobile number");
                return false;
            }
            else
            {
                mobilenotf.setErrorEnabled(false);
                return true;
            }
        }
        else
        {
            mobilenotf.setError("Invalid mobile number");
            return false;
        }
    }

    void requestMobilenumber()
    {
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent =mCredentialsApiClient.getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_requestMobilenumberException",error);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {

                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);

                try
                {
                    String number = credential.getId();
                    String final_number = number;
                    if (number.length() > 10) {
                        final_number = number.substring(number.length() - 10);
                    }
                    mobileno.setText(final_number);
//                    sendotpbt.callOnClick();
                }
                catch (Exception e)
                {
                    String error = Log.getStackTraceString(e);
                    globalclass.log(TAG+"_onActivityResultException",error);
                    globalclass.sendLog(Globalclass.TryCatchException,TAG,"","onActivityResultException","",error);
                }

            }
        }
    }

    void requestAllPermissions(){

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {

                            globalclass.log(TAG,"All permissions are granted by user!");
                            if(validation())
                            {
                                globalclass.setStringData("from",TAG);
                                Intent intent = new Intent(activity,VerifyOtp.class);
                                intent.putExtra("mobilenumber",mobileno.getText().toString());
                                startActivity(intent);
                            }
                        }

                        if (report.getDeniedPermissionResponses().size() > 0) {

                            globalclass.log(TAG,"Some permissions are Denied by user!");
                            globalclass.snackit(activity,"Please allow permission to proceed further!");
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

}
