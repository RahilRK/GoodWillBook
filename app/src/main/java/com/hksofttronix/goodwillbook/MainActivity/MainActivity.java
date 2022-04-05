package com.hksofttronix.goodwillbook.MainActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Home.HomeFrag.HomeFrag;
import com.hksofttronix.goodwillbook.Home.NotificationFrag.NotificationFrag;
import com.hksofttronix.goodwillbook.Home.OthersFrag;
import com.hksofttronix.goodwillbook.Home.TabAdp;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = MainActivity.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    BadgeDrawable badgecount;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.ic_home,
            R.drawable.ic_notification,
            R.drawable.ic_user
    };
    int tabpos = 0;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        binding();
        setTAB();
        onclick();
        requestAllPermissions();
    }

    void binding()
    {
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);
    }

    void init()
    {
        globalclass  = Globalclass.getInstance(activity);
        mydatabase  = Mydatabase.getInstance(activity);
    }

    void onclick()
    {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                tabpos = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    void setupViewPager(ViewPager viewPager) {
        TabAdp adapter = new TabAdp(getSupportFragmentManager());
        adapter.addFrag(new HomeFrag(), "Home");
        adapter.addFrag(new NotificationFrag(), "Notification");
        adapter.addFrag(new OthersFrag(), "Others");
        viewPager.setAdapter(adapter);
    }

    void setTAB()
    {
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    public void setBudgetCount()
    {
        badgecount = tabLayout.getTabAt(1).getOrCreateBadge();
        int unreadcount = mydatabase.getUnreadNotificationCount();
        globalclass.log(TAG,"BudgetCount: "+unreadcount);
        if(unreadcount > 0)
        {
            badgecount.setVisible(true);
            badgecount.setNumber(unreadcount);
        }
        else if(unreadcount == 0)
        {
            badgecount.setVisible(false);
        }
    }

    public BroadcastReceiver NotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(TAG,"NotificationReceiver: onReceive");
                setBudgetCount();
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(TAG,"NotificationReceiver: onReceive\n"+error);
                globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","NotificationReceiver: onReceive","",error);
            }
        }
    };

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
                        }

                        if (report.getDeniedPermissionResponses().size() > 0) {
                            globalclass.log(TAG,"Some permissions are Denied by user!");
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

    @Override
    protected void onResume() {
        super.onResume();

        globalclass.checkAutomaticTimeAndTimeZone(activity);
        setBudgetCount();
        globalclass.appUpdateList(activity);
        LocalBroadcastManager.getInstance(activity).registerReceiver(
                NotificationReceiver, new IntentFilter(Globalclass.NotificationReceiver));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(NotificationReceiver);
    }

    @Override
    public void onBackPressed() {

        if(tabpos == 0)
        {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            globalclass.toast_short("Press back again to exit!");

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
        else
        {
            viewPager.setCurrentItem(0);
        }
    }
}
