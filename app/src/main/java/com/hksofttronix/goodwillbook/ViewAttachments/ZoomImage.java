package com.hksofttronix.goodwillbook.ViewAttachments;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;
//import com.jsibbold.zoomage.ZoomageView;

public class ZoomImage extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = ZoomImage.this;

    Globalclass globalclass;

//    ZoomageView myZoomageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        globalclass = Globalclass.getInstance(activity);
//        myZoomageView = findViewById(R.id.myZoomageView);

        setToolbar();
//        loadImage();
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

//    void loadImage()
//    {
//        if(!globalclass.isInternetPresent())
//        {
//            globalclass.snackit(activity,getResources().getString(R.string.noInternetConnection));
//        }
//
//        showprogress();
//
//        Glide
//                .with(activity)
//                .load(globalclass.getImageUrl()+getIntent().getStringExtra("imagename"))
//                .apply(new RequestOptions()
//                        .placeholder(R.mipmap.ic_launcher)
//                        .error(R.mipmap.ic_launcher)
//                        .fitCenter())
//                .addListener(new RequestListener<Drawable>() {
//
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//
//                        hideprogress();
//                        globalclass.toast_short(activity.getResources().getString(R.string.Glide_onLoadFailed));
//                        globalclass.log(TAG, "Glide_onLoadFailed");
//                        String error = Log.getStackTraceString(e);
//                        globalclass.sendLog(Globalclass.GlideException,TAG,globalclass.getImageUrl()+getIntent().getStringExtra("imagename"),"Glide_onLoadFailed","",error);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//
//                        hideprogress();
//                        return false;
//                    }
//                })
//                .into(myZoomageView);
//
//    }

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
