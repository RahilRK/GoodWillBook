package com.hksofttronix.goodwillbook.VolleyUtil.common;

import android.os.Build;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.hksofttronix.goodwillbook.Globalclass;

/**
 * Created by iit_amiyo on 10/10/17.
 */

public class MyApplication extends MultiDexApplication
{

    public static final String TAG = MyApplication.class.getSimpleName();

    private RequestQueue mRequestQueue;
    private static MyApplication mInstance;
    Globalclass globalclass;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mInstance = this;
        globalclass = Globalclass.getInstance(MyApplication.this);

        //todo creating all Channels
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            globalclass.createGoodWill_Notifi_Channel();
            globalclass.createuploadBusinessLogo_Service_Notifi_Channel();
            globalclass.createcontactList_Service_Notifi_Channel();
            globalclass.createuploadAttachment_Service_Notifi_Channel();
            globalclass.createtransaction_Service_Notifi_Channel();
            globalclass.createFcmNotifi_Channel();
            globalclass.createFcmNotifi_SilentChannel();
            globalclass.createsendLog_Notifi_Channel();
            globalclass.createuploadFeedbackAttachments_Notifi_Channel();
        }
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}