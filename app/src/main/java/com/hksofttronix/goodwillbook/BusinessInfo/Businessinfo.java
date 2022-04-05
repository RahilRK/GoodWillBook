package com.hksofttronix.goodwillbook.BusinessInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hksofttronix.goodwillbook.AddBusiness.addBusinessModel;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Util.AppBarStateChangeListener;
import com.hksofttronix.goodwillbook.ViewAttachments.ZoomImage;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Businessinfo extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Businessinfo.this;
    
    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    CollapsingToolbarLayout collapsingtoolbar;
    AppBarLayout appbar;
    SwipeRefreshLayout swipeReferesh;
    ImageView businesslogo;
    FloatingActionButton fabeditbusiness;
    TextView contactnumber,businessemailid,pancardnumber,detail,address,location;

    ArrayList<addBusinessModel> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_businessinfo);

        init();
        binding();
        onClick();
        setToolbar();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        collapsingtoolbar = findViewById(R.id.collapsingtoolbar);
        appbar = findViewById(R.id.appbar);
        swipeReferesh = findViewById(R.id.swipeReferesh);
        businesslogo = findViewById(R.id.businesslogo);
        fabeditbusiness = findViewById(R.id.fabeditbusiness);
        contactnumber = findViewById(R.id.contactnumber);
        businessemailid = findViewById(R.id.businessemailid);
        pancardnumber = findViewById(R.id.pancardnumber);
        detail = findViewById(R.id.detail);
        address = findViewById(R.id.address);
        location = findViewById(R.id.location);


        fabeditbusiness.setColorFilter(getResources().getColor(R.color.mwhite));
        businesslogo.setBackgroundResource(R.mipmap.ic_launcher);
    }

    void onClick()
    {
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
                getBusinessInfo();
            }
        });

        fabeditbusiness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),UpdateBusinessInfo.class));
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void setText()
    {
        arrayList = mydatabase.getBusinessInfo(Integer.parseInt(globalclass.getActivebusinessacid()));

        if(!arrayList.isEmpty())
        {
            collapsingtoolbar.setTitle(globalclass.checknullAndSet(arrayList.get(0).getName()));
            contactnumber.setText(globalclass.checknullAndSet(arrayList.get(0).getContactnumber()));
            businessemailid.setText(globalclass.checknullAndSet(arrayList.get(0).getEmailid()));
            pancardnumber.setText(globalclass.checknullAndSet(arrayList.get(0).getPancardnumber()));
            detail.setText(globalclass.checknullAndSet(arrayList.get(0).getDetail()));
            address.setText(globalclass.checknullAndSet(arrayList.get(0).getAddress()));
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

    void getBusinessInfo()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        showprogress();

        final String url = globalclass.getUrl()+"getBusinessInfo";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", globalclass.getuserid());
        params.put("businessacid", globalclass.getActivebusinessacid());

        volleyApiCall.Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                hideprogress();
                globalclass.log("getBusinessInfo_RES",result);

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

                                mydatabase.addorupdateBusiness(businessModel);
                            }

                            if(!arrayList.isEmpty())
                            {
                                arrayList.clear();
                                setText();
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                globalclass.toast_long(getResources().getString(R.string.errorInFunction_string));
                                globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getBusinessInfo_ErrorInFunction",params.toString(),result);
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
                        globalclass.log("getBusinessInfo_JSONException",error);
                        globalclass.toast_long(getResources().getString(R.string.JSONException_string));
                        globalclass.sendLog(Globalclass.ErrorApiCall,TAG,url,"getBusinessInfo_JSONException",params.toString(),error);
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
    protected void onResume() {
        super.onResume();
        setText();
    }
}
