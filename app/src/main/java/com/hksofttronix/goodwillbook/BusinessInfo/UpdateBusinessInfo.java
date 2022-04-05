package com.hksofttronix.goodwillbook.BusinessInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hksofttronix.goodwillbook.AddBusiness.addBusinessModel;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Util.AppBarStateChangeListener;
import com.hksofttronix.goodwillbook.ViewAttachments.ZoomImage;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
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

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;


public class UpdateBusinessInfo extends AppCompatActivity
{

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = UpdateBusinessInfo.this;

    Globalclass globalclass;
    CredentialsClient mCredentialsApiClient;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    CollapsingToolbarLayout collapsingtoolbar;
    AppBarLayout appbar;
    ImageView businesslogo;
    FloatingActionButton fab;
    TextInputLayout businessnametlo,businesscontactnotlo,pancardnumbertlo,detailtlo,addresstlo;
    EditText businessname,businesscontactno,pancardnumber,detail,address;
    TextView businessemailid;
    MaterialButton updatebusinessinfobt;

    ArrayList<addBusinessModel> arrayList = new ArrayList<>();

    public static int RESOLVE_HINT = 111;
    String logofilepath = "";

    ArrayList<String> imagesArrayList = new ArrayList<>();
    int MaxImages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_business_info);

        init();
        binding();
        onClick();
        setToolbar();
        setText();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mCredentialsApiClient = Credentials.getClient(this);
        mydatabase = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        collapsingtoolbar = findViewById(R.id.collapsingtoolbar);
        appbar = findViewById(R.id.appbar);
        businesslogo = findViewById(R.id.businesslogo);
        fab = findViewById(R.id.fab);
        businessnametlo = findViewById(R.id.businessnametlo);
        businesscontactnotlo = findViewById(R.id.businesscontactnotlo);
        pancardnumbertlo = findViewById(R.id.pancardnumbertlo);
        detailtlo = findViewById(R.id.detailtlo);
        addresstlo = findViewById(R.id.addresstlo);
        businessname = findViewById(R.id.businessname);
        businesscontactno = findViewById(R.id.businesscontactno);
        businessemailid = findViewById(R.id.businessemailid);
        pancardnumber = findViewById(R.id.pancardnumber);
        detail = findViewById(R.id.detail);
        address = findViewById(R.id.address);
        updatebusinessinfobt = findViewById(R.id.updatebusinessinfobt);

        pancardnumber.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        fab.setColorFilter(getResources().getColor(R.color.mwhite));
    }

    @SuppressLint("ClickableViewAccessibility")
    void onClick()
    {
        address.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.address) {
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


        businessemailid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestEmailId();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestStoragePermission();
            }
        });


        updatebusinessinfobt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.hideKeyboard(activity);
                if(validation())
                {
                    updateBusinessInfo();
                }
            }
        });

        businessname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                collapsingtoolbar.setTitle(businessname.getText().toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        appbar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {

                if(state == State.COLLAPSED)
                {
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow_black);
                }
                else if(state == State.EXPANDED)
                {
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow_white);
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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    boolean validation()
    {
        boolean return_value = false;

        if(businessname.getText().length() <= 2)
        {
            businessnametlo.setError("Should contain minimum 3 letters");
            return_value =  false;
        }
        else if(businesscontactno.getText().length() > 0)
        {
            String getmobileno = businesscontactno.getText().toString();
            String firstchar=getmobileno.substring(0,1);

            if(businesscontactno.getText().length()!=10)
            {
                businesscontactnotlo.setError("Invalid contact number");
                return_value = false;
            }
            else if(!firstchar.equalsIgnoreCase("6") && !firstchar.equalsIgnoreCase("7") &&
                    !firstchar.equalsIgnoreCase("8") && !firstchar.equalsIgnoreCase("9"))
            {
                businesscontactnotlo.setError("Invalid contact number");
                return_value = false;
            }
            else if(pancardnumber.getText().length() > 0)
            {
                if(!pancardnumber.getText().toString().matches(globalclass.getPancard_Pattern()))
                {
                    pancardnumbertlo.setError("Invalid pan card number");
                    return_value = false;
                }
                else if(detail.getText().length() > 0)
                {
                    if(detail.getText().length()<=9)
                    {
                        detailtlo.setError("Should contain minimum 10 letters");
                    }
                    else if(address.getText().length() > 0)
                    {
                        if(address.getText().length()<=9)
                        {
                            addresstlo.setError("Should contain minimum 10 letters");
                        }
                        else
                        {
                            businessnametlo.setErrorEnabled(false);
                            businesscontactnotlo.setErrorEnabled(false);
                            pancardnumbertlo.setErrorEnabled(false);
                            detailtlo.setErrorEnabled(false);
                            addresstlo.setErrorEnabled(false);
                            return_value = true;
                        }
                    }
                    else
                    {
                        businessnametlo.setErrorEnabled(false);
                        businesscontactnotlo.setErrorEnabled(false);
                        pancardnumbertlo.setErrorEnabled(false);
                        detailtlo.setErrorEnabled(false);
                        addresstlo.setErrorEnabled(false);
                        return_value = true;
                    }
                }
                else if(address.getText().length() > 0)
                {
                    if(address.getText().length()<=9)
                    {
                        addresstlo.setError("Should contain minimum 10 letters");
                    }
                    else
                    {
                        businessnametlo.setErrorEnabled(false);
                        businesscontactnotlo.setErrorEnabled(false);
                        pancardnumbertlo.setErrorEnabled(false);
                        detailtlo.setErrorEnabled(false);
                        addresstlo.setErrorEnabled(false);
                        return_value = true;
                    }
                }
                else
                {
                    businessnametlo.setErrorEnabled(false);
                    businesscontactnotlo.setErrorEnabled(false);
                    pancardnumbertlo.setErrorEnabled(false);
                    detailtlo.setErrorEnabled(false);
                    addresstlo.setErrorEnabled(false);
                    return_value = true;
                }

            }
            else if(detail.getText().length() > 0)
            {
                if(detail.getText().length()<=9)
                {
                    detailtlo.setError("Should contain minimum 10 letters");
                }
                else if(address.getText().length() > 0)
                {
                    if(address.getText().length()<=9)
                    {
                        addresstlo.setError("Should contain minimum 10 letters");
                    }
                    else
                    {
                        businessnametlo.setErrorEnabled(false);
                        businesscontactnotlo.setErrorEnabled(false);
                        pancardnumbertlo.setErrorEnabled(false);
                        detailtlo.setErrorEnabled(false);
                        addresstlo.setErrorEnabled(false);
                        return_value = true;
                    }
                }
                else
                {
                    businessnametlo.setErrorEnabled(false);
                    businesscontactnotlo.setErrorEnabled(false);
                    pancardnumbertlo.setErrorEnabled(false);
                    detailtlo.setErrorEnabled(false);
                    addresstlo.setErrorEnabled(false);
                    return_value = true;
                }
            }
            else if(address.getText().length() > 0)
            {
                if(address.getText().length()<=9)
                {
                    addresstlo.setError("Should contain minimum 10 letters");
                }
                else
                {
                    businessnametlo.setErrorEnabled(false);
                    businesscontactnotlo.setErrorEnabled(false);
                    pancardnumbertlo.setErrorEnabled(false);
                    detailtlo.setErrorEnabled(false);
                    addresstlo.setErrorEnabled(false);
                    return_value = true;
                }
            }
            else
            {
                businessnametlo.setErrorEnabled(false);
                businesscontactnotlo.setErrorEnabled(false);
                pancardnumbertlo.setErrorEnabled(false);
                detailtlo.setErrorEnabled(false);
                addresstlo.setErrorEnabled(false);
                return_value = true;
            }

        }
        else if(pancardnumber.getText().length() > 0)
        {
            if(!pancardnumber.getText().toString().matches(globalclass.getPancard_Pattern()))
            {
                pancardnumbertlo.setError("Invalid pan card number");
                return_value = false;
            }
            else if(detail.getText().length() > 0)
            {
                if(detail.getText().length()<=9)
                {
                    detailtlo.setError("Should contain minimum 10 letters");
                }
                else if(address.getText().length() > 0)
                {
                    if(address.getText().length()<=9)
                    {
                        addresstlo.setError("Should contain minimum 10 letters");
                    }
                    else
                    {
                        businessnametlo.setErrorEnabled(false);
                        businesscontactnotlo.setErrorEnabled(false);
                        pancardnumbertlo.setErrorEnabled(false);
                        detailtlo.setErrorEnabled(false);
                        addresstlo.setErrorEnabled(false);
                        return_value = true;
                    }
                }
                else
                {
                    businessnametlo.setErrorEnabled(false);
                    businesscontactnotlo.setErrorEnabled(false);
                    pancardnumbertlo.setErrorEnabled(false);
                    detailtlo.setErrorEnabled(false);
                    addresstlo.setErrorEnabled(false);
                    return_value = true;
                }
            }
            else if(address.getText().length() > 0)
            {
                if(address.getText().length()<=9)
                {
                    addresstlo.setError("Should contain minimum 10 letters");
                }
                else
                {
                    businessnametlo.setErrorEnabled(false);
                    businesscontactnotlo.setErrorEnabled(false);
                    pancardnumbertlo.setErrorEnabled(false);
                    detailtlo.setErrorEnabled(false);
                    addresstlo.setErrorEnabled(false);
                    return_value = true;
                }
            }
            else
            {
                businessnametlo.setErrorEnabled(false);
                businesscontactnotlo.setErrorEnabled(false);
                pancardnumbertlo.setErrorEnabled(false);
                detailtlo.setErrorEnabled(false);
                addresstlo.setErrorEnabled(false);
                return_value = true;
            }

        }
        else if(detail.getText().length() > 0)
        {
            if(detail.getText().length()<=9)
            {
                detailtlo.setError("Should contain minimum 10 letters");
            }
            else if(address.getText().length() > 0)
            {
                if(address.getText().length()<=9)
                {
                    addresstlo.setError("Should contain minimum 10 letters");
                }
                else
                {
                    businessnametlo.setErrorEnabled(false);
                    businesscontactnotlo.setErrorEnabled(false);
                    pancardnumbertlo.setErrorEnabled(false);
                    detailtlo.setErrorEnabled(false);
                    addresstlo.setErrorEnabled(false);
                    return_value = true;
                }
            }
            else
            {
                businessnametlo.setErrorEnabled(false);
                businesscontactnotlo.setErrorEnabled(false);
                pancardnumbertlo.setErrorEnabled(false);
                detailtlo.setErrorEnabled(false);
                addresstlo.setErrorEnabled(false);
                return_value = true;
            }
        }
        else if(address.getText().length() > 0)
        {
            if(address.getText().length()<=9)
            {
                addresstlo.setError("Should contain minimum 10 letters");
            }
            else
            {
                businessnametlo.setErrorEnabled(false);
                businesscontactnotlo.setErrorEnabled(false);
                pancardnumbertlo.setErrorEnabled(false);
                detailtlo.setErrorEnabled(false);
                addresstlo.setErrorEnabled(false);
                return_value = true;
            }
        }
        else
        {
            businessnametlo.setErrorEnabled(false);
            businesscontactnotlo.setErrorEnabled(false);
            pancardnumbertlo.setErrorEnabled(false);
            detailtlo.setErrorEnabled(false);
            addresstlo.setErrorEnabled(false);
            return_value = true;
        }

        return return_value;
    }

    void setText()
    {
        arrayList = mydatabase.getBusinessInfo(Integer.parseInt(globalclass.getActivebusinessacid()));

        if(!arrayList.isEmpty())
        {
            collapsingtoolbar.setTitle(globalclass.checknull(arrayList.get(0).getName()));
            businessname.setText(globalclass.checknull(arrayList.get(0).getName()));
            businesscontactno.setText(globalclass.checknull(arrayList.get(0).getContactnumber()));
            businessemailid.setText(globalclass.checknull(arrayList.get(0).getEmailid()));
            pancardnumber.setText(globalclass.checknull(arrayList.get(0).getPancardnumber()));
            detail.setText(globalclass.checknull(arrayList.get(0).getDetail()));
            address.setText(globalclass.checknull(arrayList.get(0).getAddress()));
            if(!globalclass.checknull(arrayList.get(0).getLogourl()).equalsIgnoreCase(""))
            {
                setActivebusinessicon(globalclass.getImageUrl()+arrayList.get(0).getLogourl());

                businesslogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(activity, ZoomImage.class);
                        intent.putExtra("imagename",arrayList.get(0).getLogourl());
                        startActivity(intent);
                    }
                });
            }
        }
    }

    void setActivebusinessicon(final String getactivebusinessicon)
    {
        Glide
                .with(activity)
                .load(getactivebusinessicon)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .fitCenter())
                .addListener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                        globalclass.toast_short(getResources().getString(R.string.Glide_onLoadFailed));
                        globalclass.log(TAG, "Glide_onLoadFailed");
                        String error = Log.getStackTraceString(e);
                        globalclass.sendLog(Globalclass.GlideException,TAG,globalclass.getImageUrl()+getactivebusinessicon,"Glide_onLoadFailed","",error);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {


                        return false;
                    }
                })
                .into(businesslogo);

    }

    void OpenImagePicker()
    {
        try 
        {
            FilePickerBuilder.getInstance().setMaxCount(MaxImages)
                    .setSelectedFiles(imagesArrayList)
                    .setActivityTheme(R.style.LibAppTheme)
                    .pickPhoto(activity);

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG+"_OpenImagePickerException",error);
            globalclass.sendLog(Globalclass.TryCatchException,TAG,"","OpenImagePickerException","",error);
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
                            globalclass.snackit(activity,"Storage permission required to choose logo");
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

    void updateBusinessInfo()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"updateBusinessInfo";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("businessacid", globalclass.getActivebusinessacid());
        params.put("name", businessname.getText().toString().trim());
        params.put("detail", detail.getText().toString().trim());
        params.put("location", "0,0");
        params.put("address", address.getText().toString().trim());
        params.put("contactnumber", businesscontactno.getText().toString().trim());
        params.put("emailid", businessemailid.getText().toString().trim());
        params.put("pancardnumber", pancardnumber.getText().toString().trim());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("updateBusinessInfo_RES",result);

                if(!result.equalsIgnoreCase(getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(getResources().getString(R.string.status));
                        String message = jsonObject.getString(getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(getResources().getString(R.string.success)))
                        {
                            globalclass.toast_long(message);
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
                                businessModel.setLogofilepath(logofilepath);

                                mydatabase.addorupdateBusiness(businessModel);

                                if(!logofilepath.equalsIgnoreCase(""))
                                {
                                    globalclass.startAnyService(activity,uploadBusinessLogo_Service.class);
                                }
                            }

                            if(!arrayList.isEmpty())
                            {
                                arrayList.clear();
                                setText();
                                onBackPressed();
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"updateBusinessInfo_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("updateBusinessInfo_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"updateBusinessInfo_JSONException",params.toString(),error);
                    }
                }
            }
        });
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
                    businessemailid.setText(getemailid);
                    globalclass.log(TAG,getemailid);
                }
                catch (Exception e)
                {
                    String error = Log.getStackTraceString(e);
                    globalclass.log(TAG+"_onActivityResult_CredentialException",error);
                    globalclass.sendLog(Globalclass.TryCatchException,TAG,"","onActivityResult_CredentialException","",error);
                }

            }
        }
        else if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK && data!=null) {

                try
                {
                    imagesArrayList.clear();
                    imagesArrayList.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));

                    setBusinessImage(imagesArrayList.get(0).toString());
                    globalclass.log("imagesArrayList",imagesArrayList.get(0).toString());

                    if(!imagesArrayList.isEmpty())
                    {
                        logofilepath = imagesArrayList.get(0).toString();
                    }
//                    globalclass.log("CompressFILE",globalclass.CompressFILE(activity,imagesArrayList.get(0).toString()));
                }
                catch (Exception e)
                {
                    String error = Log.getStackTraceString(e);
                    globalclass.log(TAG+"_onActivityResult_ImagePickerException",error);
                    globalclass.sendLog(Globalclass.TryCatchException,TAG,"","onActivityResult_ImagePickerException","",error);
                }
            }
        }

    }

    void setBusinessImage(String filepath)
    {
        File imgFile = new  File(filepath);
        if(imgFile.exists())
        {
            Glide.with(activity).clear(businesslogo);
            businesslogo.setImageURI(Uri.fromFile(imgFile));
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
