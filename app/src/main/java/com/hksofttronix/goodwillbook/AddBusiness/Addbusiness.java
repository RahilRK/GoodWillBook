package com.hksofttronix.goodwillbook.AddBusiness;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.MainActivity.MainActivity;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.SetupPage;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Addbusiness extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Addbusiness.this;
    
    Globalclass globalclass;
    VolleyApiCall volleyApiCall;
    Mydatabase mydatabase;

    TextInputLayout businessnametlo;
    EditText businessname;
    MaterialButton addbusinessbt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addbusiness);

        init();
        binding();
        setToolbar();
        onclick();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        businessnametlo = findViewById(R.id.businessnametlo);
        businessname = findViewById(R.id.businessname);
        addbusinessbt = findViewById(R.id.addbusinessbt);
    }

    void setToolbar()
    {
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void onclick()
    {
        businessname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hideKeyboard(activity);

                    if(validation())
                    {
                        addBusinessAC();
                    }

                }
                return false;
            }
        });

        addbusinessbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.hideKeyboard(activity);

                if(validation())
                {
                    addBusinessAC();
                }
            }
        });
    }

    boolean validation()
    {
        if(businessname.getText().length()>=3)
        {
            String getbusinessname = businessname.getText().toString().trim();

            if(!getbusinessname.matches("[A-Za-z & -]+"))
            {
                businessnametlo.setError("Invalid business name");
                return false;
            }
            else
            {
                businessnametlo.setErrorEnabled(false);
                return true;
            }
        }
        else
        {
            businessnametlo.setError("Should contain minimum 3 letters");
            return false;
        }
    }

    void addBusinessAC()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"addBusinessAC";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("mobilenumber", globalclass.getmobilenumber());
        params.put("name", businessname.getText().toString().trim());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("addBusinessAC_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            if(globalclass.getBooleanData("transfer data"))
                            {
                                showprogress();
                                startActivity(new Intent(getApplicationContext(), SetupPage.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            }
                            else if(globalclass.getBooleanData("new registered user"))
                            {
                                globalclass.setBooleanData("new registered user",false);
                                showprogress();
                                startActivity(new Intent(getApplicationContext(), SetupPage.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            }
                            else
                            {
                                int businessacid = jsonObject.getInt("businessacid");
                                globalclass.setIntData("businessacid", businessacid);

                                addBusinessModel businessModel = new addBusinessModel();
                                businessModel.setBusinessacid(businessacid);
                                businessModel.setName(businessname.getText().toString().trim());

                                if(mydatabase.addorupdateBusiness(businessModel)) //if added or updated business locally
                                {
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"addBusinessAC_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("addBusinessAC_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"addBusinessAC_JSONException",params.toString(),error);
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
}
