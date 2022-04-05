package com.hksofttronix.goodwillbook.Profile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Updateprofile extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Updateprofile.this;

    Globalclass globalclass;
    CredentialsClient mCredentialsApiClient;
    VolleyApiCall volleyApiCall;

    TextInputLayout usernametlo;
    EditText username;
    TextView useremailid;
    MaterialButton updateprofilebt;

    public static int RESOLVE_HINT = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateprofile);

        init();
        binding();
        setText();
        onClick();
        setToolbar();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mCredentialsApiClient = Credentials.getClient(this);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        usernametlo = findViewById(R.id.usernametlo);
        username = findViewById(R.id.username);
        useremailid = findViewById(R.id.emailid);
        updateprofilebt = findViewById(R.id.updateprofilebt);
    }

    void setText()
    {
        username.setText(globalclass.checknullAndSet(globalclass.getStringData("username")));
        if(!globalclass.checknullAndSet(globalclass.getStringData("useremailid")).equalsIgnoreCase(""))
        {
            useremailid.setText(globalclass.getStringData("useremailid"));
        }
    }

    void onClick()
    {
        updateprofilebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.hideKeyboard(activity);
                if(validation())
                {
                    updateProfile();
                }
            }
        });

        useremailid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               requestEmailId();
            }
        });
    }

    void setToolbar()
    {
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    boolean validation()
    {
        if(username.getText().length()<=3)
        {
            usernametlo.setError("Name should contain minimum 3 letters");
            return false;
        }
        else
        {
            usernametlo.setErrorEnabled(false);
            return true;
        }
    }

    void updateProfile()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"updateProfile";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("name", username.getText().toString().trim());
        params.put("emailid", useremailid.getText().toString().trim());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("updateProfile_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            globalclass.setStringData("username",username.getText().toString().trim());
                            globalclass.setStringData("useremailid",useremailid.getText().toString().trim());
                            globalclass.toast_long(message);
                            onBackPressed();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"updateProfile_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("updateProfile_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"updateProfile_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void requestEmailId()
    {
        HintRequest hintRequest = new HintRequest.Builder()
                .setAccountTypes(IdentityProviders.GOOGLE)
                .build();

        PendingIntent intent =mCredentialsApiClient.getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_requestEmailIdException",error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","requestEmailIdException","",error);
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
                    String getemailid = credential.getId();
                    useremailid.setText(getemailid);
                    globalclass.log(TAG,getemailid);
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
}
