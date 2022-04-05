package com.hksofttronix.goodwillbook.ViewAttachments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.location.LocationServices;
import com.hksofttronix.goodwillbook.EditPay.EditAttachPhotosOnClick;
import com.hksofttronix.goodwillbook.EditPay.EditPay;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.Pay.addAttachmentsModel;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import java.util.ArrayList;

public class ViewAttachments extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = ViewAttachments.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    RecyclerView recyclerview;
    ArrayList<addAttachmentsModel> attachmentArrayList = new ArrayList<>();
    ViewAttachments_adapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attachments);

        init();
        binding();
        setToolbar();
        getIntentData();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void getIntentData()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.snackit(activity,getResources().getString(R.string.noInternetConnection));
        }

        attachmentArrayList = getIntent().getParcelableArrayListExtra("attachmentArrayList");
        setAdapter();
    }

    void binding()
    {
        recyclerview = findViewById(R.id.recyclerview);
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

    void setAdapter()
    {
        recyclerview.setLayoutManager(new GridLayoutManager(activity,3));
        adapter = new ViewAttachments_adapter(activity, attachmentArrayList, new AttachmentOnClick() {
            @Override
            public void onClickZoomImg(int position, addAttachmentsModel model) {

                Intent intent = new Intent(activity,ZoomImage.class);
                intent.putExtra("imagename",model.getUrl());
                startActivity(intent);
            }
        });

        recyclerview.setAdapter(adapter);
    }
}
