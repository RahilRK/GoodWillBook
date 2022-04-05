package com.hksofttronix.goodwillbook.Profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;

public class Viewprofile extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Viewprofile.this;

    Globalclass globalclass;

    ImageView iv;
    TextView name,mobilenumber,emailid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewprofile);

        init();
        binding();
        setToolbar();
        setText();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
    }

    void binding()
    {
        iv = findViewById(R.id.iv);
        name = findViewById(R.id.name);
        mobilenumber = findViewById(R.id.mobilenumber);
        emailid = findViewById(R.id.emailid);
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
        name.setText(globalclass.checknullAndSet(globalclass.getStringData("username")));
        mobilenumber.setText(globalclass.getStringData("mobilenumber"));
        emailid.setText(globalclass.checknullAndSet(globalclass.getStringData("useremailid")));
        setImageViewDrawable();
    }

    void setImageViewDrawable()
    {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int randomcolor = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.SANS_SERIF).bold()
                .toUpperCase()
                .endConfig()
                .buildRound(globalclass.getStringData("username").substring(0,1), randomcolor);
        iv.setImageDrawable(drawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_updateprofile:

                startActivity(new Intent(getApplicationContext(), Updateprofile.class));

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setText();
    }
}
