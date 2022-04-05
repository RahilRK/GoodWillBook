package com.hksofttronix.goodwillbook.Feedback;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hksofttronix.goodwillbook.BuildConfig;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class SendFeedback extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = SendFeedback.this;

    Globalclass globalclass;
    VolleyApiCall volleyApiCall;
    Mydatabase mydatabase;

    ImageView ivfeedbackimg,ivdelete;
    TextInputLayout shortdesctlo,longdesctlo;
    EditText shortdesc,longdesc;
    MaterialButton sendfeedbackbt;

    ArrayList<String> selectedimagesArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_feedback);

        init();
        binding();
        setToolbar();
        onClick();

        if(BuildConfig.DEBUG)
        {
            shortdesc.setText("This is subject");
            longdesc.setText("This is message,This is message,This is message,");
        }
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding()
    {
        ivfeedbackimg = findViewById(R.id.ivfeedbackimg);
        ivdelete = findViewById(R.id.ivdelete);
        shortdesctlo = findViewById(R.id.shortdesctlo);
        longdesctlo = findViewById(R.id.longdesctlo);
        shortdesc = findViewById(R.id.shortdesc);
        longdesc = findViewById(R.id.longdesc);
        sendfeedbackbt = findViewById(R.id.sendfeedbackbt);
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

    void onClick()
    {
        ivfeedbackimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestStoragePermission();
            }
        });

        ivdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedimagesArrayList.clear();
                showhide_ivdelete();
            }
        });

        sendfeedbackbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validation())
                {
                    if(!selectedimagesArrayList.isEmpty())
                    {
                        SendFeedbackModel model = new SendFeedbackModel();
                        model.setShortdesc(shortdesc.getText().toString().trim());
                        model.setLongdesc(longdesc.getText().toString().trim());
                        model.setFilepath(selectedimagesArrayList.get(0));

                        mydatabase.insertfeedback(model);
                        globalclass.toast_long("We will notify you, when your feedback will be send!");
                        onBackPressed();
                    }
                    else
                    {
                        sendFeedbackViaEmail();
                    }
                }
            }
        });

    }

    boolean validation()
    {
        if(shortdesc.length() <= 9)
        {
            shortdesctlo.setError("Should contain minimum 10 characters");
            return false;
        }
        else if(longdesc.length() <= 19)
        {
            longdesctlo.setError("Should contain minimum 20 characters");
            return false;
        }
        else
        {
            shortdesctlo.setErrorEnabled(false);
            longdesctlo.setErrorEnabled(false);
            return true;
        }
    }

    void sendFeedback()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"sendFeedback";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        if(!selectedimagesArrayList.isEmpty())
        {
            params.put("url", globalclass.EncodeBase64(new File(globalclass.CompressFILE(activity,selectedimagesArrayList.get(0)))));
        }
        else
        {
            params.put("url", "");
        }

        params.put("shortdesc", shortdesc.getText().toString().trim());
        params.put("longdesc", longdesc.getText().toString().trim());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("sendFeedback_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            globalclass.toast_short(message);
                            onBackPressed();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendFeedback_ErrorInFunction",params.toString(),result);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("sendFeedback_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendFeedback_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void sendFeedbackViaEmail()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"sendFeedbackViaEmail";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("Subject", shortdesc.getText().toString().trim());
        params.put("Message", longdesc.getText().toString().trim());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("sendFeedbackViaEmail_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            globalclass.toast_short(message);
                            onBackPressed();
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendFeedbackViaEmail_ErrorInFunction",params.toString(),result);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        globalclass.log("sendFeedbackViaEmail_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"sendFeedbackViaEmail_JSONException",params.toString(),error);
                    }
                }
            }
        });
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

    void OpenImagePicker()
    {
        selectedimagesArrayList.clear();
        FilePickerBuilder.getInstance().setMaxCount(1)
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

                File imgFile = new  File(selectedimagesArrayList.get(0));
                if(imgFile.exists())
                {
                    Glide.with(activity).clear(ivfeedbackimg);
                    ivfeedbackimg.setImageURI(Uri.fromFile(imgFile));
                }
            }
        }
    }

    void showhide_ivdelete()
    {
        if(selectedimagesArrayList.isEmpty())
        {
            ivfeedbackimg.setImageDrawable(getResources().getDrawable(R.drawable.ic_gallery));
            ivdelete.setVisibility(View.GONE);
        }
        else
        {
            ivdelete.setVisibility(View.VISIBLE);
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
    protected void onResume() {
        super.onResume();
        showhide_ivdelete();
    }
}
