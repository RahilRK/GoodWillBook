package com.hksofttronix.goodwillbook.NotificationSettings;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;

public class Settings extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Settings.this;

    Globalclass globalclass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setToolbar();
        globalclass = Globalclass.getInstance(activity);

        PreferenceManager.setDefaultValues(activity,R.xml.notification_settings,false);

        if(findViewById(R.id.fragment_container) != null)
        {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,new NotificationSettingsFrag()).commit();
        }
    }

    void setToolbar()
    {
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }
}
