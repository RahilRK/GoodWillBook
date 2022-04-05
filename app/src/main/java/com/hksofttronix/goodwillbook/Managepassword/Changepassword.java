package com.hksofttronix.goodwillbook.Managepassword;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.button.MaterialButton;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
import com.infideap.blockedittext.BlockEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Changepassword extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Changepassword.this;
    
    Globalclass globalclass;
    VolleyApiCall volleyApiCall;

    BlockEditText currentpassword,newpassword,confirmpassword;
    MaterialButton updatepasswordbt;
    ImageView ivcurrentpassword,ivnewpassword,ivconfirmpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);

        init();
        setToolbar();
        binding();
        onClick();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        currentpassword = findViewById(R.id.currentpassword);
        newpassword = findViewById(R.id.newpassword);
        confirmpassword = findViewById(R.id.confirmpassword);
        updatepasswordbt = findViewById(R.id.updatepasswordbt);
        ivcurrentpassword = findViewById(R.id.ivcurrentpassword);
        ivnewpassword = findViewById(R.id.ivnewpassword);
        ivconfirmpassword = findViewById(R.id.ivconfirmpassword);

        currentpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    void setToolbar()
    {
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    void onClick()
    {
        ivcurrentpassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {

                    case MotionEvent.ACTION_UP:
                        currentpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;

                    case MotionEvent.ACTION_DOWN:
                        currentpassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;

                }
                return true;
            }
        });

        ivnewpassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {

                    case MotionEvent.ACTION_UP:
                        newpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;

                    case MotionEvent.ACTION_DOWN:
                        newpassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;

                }
                return true;
            }
        });

        ivconfirmpassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {

                    case MotionEvent.ACTION_UP:
                        confirmpassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;

                    case MotionEvent.ACTION_DOWN:
                        confirmpassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;

                }
                return true;
            }
        });

        updatepasswordbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validation())
                {
                    updateTransactionPassword();
                }
            }
        });
    }

    void updateTransactionPassword()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"updateTransactionPassword";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("transactionpassword",globalclass.to_md5(confirmpassword.getText().toString().trim()));

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("updateTransactionPassword_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            globalclass.setStringData("transactionpassword",globalclass.to_md5(confirmpassword.getText().toString().trim()));
                            globalclass.toast_long("Transaction password changed successfully");
                            onBackPressed();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"updateTransactionPassword_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("updateTransactionPassword_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"updateTransactionPassword_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    boolean validation()
    {
        globalclass.hideKeyboard(activity);
        String md5_currentpassword = globalclass.to_md5(currentpassword.getText().toString().trim());
        String md5_newpassword = globalclass.to_md5(newpassword.getText().toString().trim());
        String md5_confirmpassword = globalclass.to_md5(confirmpassword.getText().toString().trim());

//        globalclass.log("md5_currentpassword",md5_currentpassword);
//        globalclass.log("md5_transactionpassword",globalclass.getStringData("transactionpassword"));

        if(currentpassword.getText().length()!=6)
        {
            currentpassword.requestFocus();
            currentpassword.setSelection(currentpassword.getText().length());
            globalclass.snackit(activity,"Enter current password");
            return false;
        }
        else if(newpassword.getText().length()!=6)
        {
            newpassword.requestFocus();
            newpassword.setSelection(newpassword.getText().length());
            globalclass.snackit(activity,"Enter new password");
            return false;
        }
        else if(confirmpassword.getText().length()!=6)
        {
            confirmpassword.requestFocus();
            confirmpassword.setSelection(confirmpassword.getText().length());
            globalclass.snackit(activity,"Enter confirm password");
            return false;
        }
        else if(!md5_currentpassword.equalsIgnoreCase(globalclass.getStringData("transactionpassword")))
        {
            globalclass.snackit(activity,"Invalid current password");
            return false;
        }
        else if(!newpassword.getText().toString().trim().equalsIgnoreCase(confirmpassword.getText().toString().trim()))
        {
            globalclass.snackit(activity,"New and Confirm password should be same");
            return false;
        }
        else if(md5_currentpassword.equalsIgnoreCase(md5_newpassword))
        {
            globalclass.snackit(activity,"New password should not be same as Current password");
            return false;
        }

        else if(md5_currentpassword.equalsIgnoreCase(md5_confirmpassword))
        {
            globalclass.snackit(activity,"Confirm password should not be same as Current password");
            return false;
        }
        else
        {
            return true;
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
