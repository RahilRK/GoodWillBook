package com.hksofttronix.goodwillbook.Remainder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.material.button.MaterialButton;
import com.hksofttronix.goodwillbook.ContactList.ContactModel;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.hksofttronix.goodwillbook.Globalclass.MyAlarmRemainderProvider;
import static com.hksofttronix.goodwillbook.Remainder.AlarmReminderContract.AlarmReminderEntry.CONTENT_URI;

public class AddRemainder extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = AddRemainder.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    ImageView iv;
    LinearLayout choosedate,choosetime;
    TextView name,tvdate,tvtime;
    MaterialButton setremainderbt;

    String currentdatetime;
    String dbDate,dbTime;
    int mYear, mMonth, mDay, mHour, mMinute;
    Calendar alarmcalendar = null;
    Calendar remaindercalender = null;

    ArrayList<ContactModel> contactDetailarrayList = new ArrayList<>();
    ArrayList<RemainderModel> remainderDetailarrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addremainder);

        init();
        binding();
        onClick();
        setToolbar();
        getIntentData();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
//        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        iv = findViewById(R.id.iv);
        name = findViewById(R.id.name);
        choosedate = findViewById(R.id.choosedate);
        choosetime = findViewById(R.id.choosetime);
        tvdate = findViewById(R.id.tvdate);
        tvtime = findViewById(R.id.tvtime);
        setremainderbt = findViewById(R.id.setremainderbt);
    }

    void onClick()
    {
        choosedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OpenDatePicker();
            }
        });

        choosetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tvdate.getText().length() == 0)
                {
                    globalclass.snackit(activity,"Choose remainder date!");
                }
                else
                {
                    OpenTimePicker();
                }
            }
        });

        setremainderbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(tvdate.getText().length() == 0)
                {
                    globalclass.snackit(activity,"Choose remainder date!");
                }
                else if(tvtime.getText().length() == 0)
                {
                    globalclass.snackit(activity,"Choose remainder time!");
                }
                else
                {
                    if(mydatabase.checkRemainderAlreadySet(contactDetailarrayList.get(0).getContactNumber()) == 0)
                    {
                        saveRemainder();
                    }
                    else
                    {
                        updateRemainder();
                    }
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getIntentData()
    {
        contactDetailarrayList = mydatabase.getContactDetail(getIntent().getStringExtra("mobilenumber"));;
        if(!contactDetailarrayList.isEmpty())
        {
            name.setText(contactDetailarrayList.get(0).getContactName());
            setImageViewDrawable();
            setText();
        }
        else
        {
            globalclass.toast_long("No contact details found for "+contactDetailarrayList.get(0).getContactNumber());
            onBackPressed();
        }
    }

    void setImageViewDrawable()
    {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        final int randomcolor = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.SANS_SERIF).bold()
                .toUpperCase()
                .endConfig()
                .buildRoundRect(contactDetailarrayList.get(0).getContactName().substring(0,1), randomcolor,10);

        if(contactDetailarrayList.get(0).getIsverified() == 1)
        {
            iv.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }
        else
        {
            iv.setImageDrawable(drawable);
        }
    }

    void setText()
    {
        if(mydatabase.checkRemainderAlreadySet(contactDetailarrayList.get(0).getContactNumber()) == 1)
        {
            remainderDetailarrayList = mydatabase.getRemainderDetail(contactDetailarrayList.get(0).getContactNumber());
            if(remainderDetailarrayList.get(0).getActive() == 1)
            {
                tvdate.setText(globalclass.formatDateTime(remainderDetailarrayList.get(0).getDate(),"yyyy-MM-dd","dd MMM yyyy"));
                tvtime.setText(globalclass.formatDateTime(remainderDetailarrayList.get(0).getTime(),"HH:mm:ss","hh:mm:ss a"));
                dbDate = remainderDetailarrayList.get(0).getDate();
                dbTime = remainderDetailarrayList.get(0).getTime();

                final Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(globalclass.datetoMiliSecond(remainderDetailarrayList.get(0).getDate()));

                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH);
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
            }
        }
    }

    void OpenDatePicker()
    {
        final Calendar todaycalendar = Calendar.getInstance();
        mYear = todaycalendar.get(Calendar.YEAR);
        mMonth = todaycalendar.get(Calendar.MONTH);
        mDay = todaycalendar.get(Calendar.DAY_OF_MONTH);

        final Calendar calendar = Calendar.getInstance();
        if(mydatabase.checkRemainderAlreadySet(contactDetailarrayList.get(0).getContactNumber()) == 1)
        {
            if(remainderDetailarrayList.get(0).getActive() == 1)
            {
                calendar.setTimeInMillis(globalclass.datetoMiliSecond(remainderDetailarrayList.get(0).getDate()));
            }
        }

        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        alarmcalendar = Calendar.getInstance();
                        alarmcalendar.set(year, monthOfYear, dayOfMonth);
                        alarmcalendar.set(Calendar.HOUR_OF_DAY,10);
                        alarmcalendar.set(Calendar.MINUTE,10);
                        alarmcalendar.set(Calendar.SECOND,10);

                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;

                        SimpleDateFormat userformat = new SimpleDateFormat("dd MMM yyyy");
                        SimpleDateFormat dbformat = new SimpleDateFormat("yyyy-MM-dd");
                        String userDate = userformat.format(alarmcalendar.getTime());
                        dbDate = dbformat.format(alarmcalendar.getTime());
                        tvdate.setText(userDate);

                    }
                }, mYear, mMonth, mDay);
        todaycalendar.add(Calendar.DATE, 1);
        datePickerDialog.getDatePicker().setMinDate(todaycalendar.getTimeInMillis());
        datePickerDialog.show();
    }

    void OpenTimePicker()
    {
        final Calendar calendar = Calendar.getInstance();
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);

        if(mydatabase.checkRemainderAlreadySet(contactDetailarrayList.get(0).getContactNumber()) == 1)
        {
            if(remainderDetailarrayList.get(0).getActive() == 1)
            {
                mHour = Integer.parseInt(globalclass.splitTimeInDBFormat(remainderDetailarrayList.get(0).getTime()).get(0));
                mMinute = Integer.parseInt(globalclass.splitTimeInDBFormat(remainderDetailarrayList.get(0).getTime()).get(1));
            }
        }

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute)
                    {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(0,0,0,hourOfDay,minute,0);

                        mHour = hourOfDay;
                        mMinute = minute;

                        SimpleDateFormat userformat = new SimpleDateFormat("hh:mm:ss a");
                        SimpleDateFormat dbformat = new SimpleDateFormat("HH:mm:ss");
                        String userDate = userformat.format(calendar.getTime());
                        dbTime = dbformat.format(calendar.getTime());
                        tvtime.setText(userDate);

                        currentdatetime = dbDate+" "+dbTime;
                    }
                }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    void saveRemainder()
    {
        try
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,contactDetailarrayList.get(0).getContactName());
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_MOBNO,contactDetailarrayList.get(0).getContactNumber());
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE,dbDate);
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME,dbTime);

            remaindercalender = Calendar.getInstance();
            remaindercalender.set(Calendar.MONTH,mMonth);
            remaindercalender.set(Calendar.YEAR,mYear);
            remaindercalender.set(Calendar.DAY_OF_MONTH,mDay);
            remaindercalender.set(Calendar.HOUR_OF_DAY,mHour);
            remaindercalender.set(Calendar.MINUTE,mMinute);
            remaindercalender.set(Calendar.SECOND,0);

            long selectedTimestamp =  remaindercalender.getTimeInMillis();
            Uri newUri = getContentResolver().insert(CONTENT_URI, contentValues);
            if(newUri == null)
            {
                globalclass.toast_long("Unable to create remainder, please try again later!");
                onBackPressed();
            }
            else
            {
                globalclass.log(TAG,"Added Remainder Uri: "+newUri);
                globalclass.toast_short("Remainder has been set successfully!");
                globalclass.setRemainderAlarm(selectedTimestamp, newUri);
                onBackPressed();
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,error);
            globalclass.sendLog(MyAlarmRemainderProvider,TAG,"","saveRemainder_Exception","",error);

            globalclass.toast_long("Unable to create remainder, please try again later!");
            onBackPressed();
        }
    }

    void updateRemainder()
    {
        try
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,contactDetailarrayList.get(0).getContactName());
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_MOBNO,contactDetailarrayList.get(0).getContactNumber());
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE,dbDate);
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME,dbTime);
            contentValues.put(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE,1);

            remaindercalender = Calendar.getInstance();
            remaindercalender.set(Calendar.MONTH,mMonth);
            remaindercalender.set(Calendar.YEAR,mYear);
            remaindercalender.set(Calendar.DAY_OF_MONTH,mDay);
            remaindercalender.set(Calendar.HOUR_OF_DAY,mHour);
            remaindercalender.set(Calendar.MINUTE,mMinute);
            remaindercalender.set(Calendar.SECOND,0);

            long selectedTimestamp =  remaindercalender.getTimeInMillis();
            Uri updateUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(remainderDetailarrayList.get(0).getId()));
            int rowsAffected = getContentResolver().update(updateUri, contentValues,null,null);
            if(rowsAffected == 0)
            {
                globalclass.toast_long("Unable to update remainder, please try again later!");
                onBackPressed();
            }
            else
            {
                globalclass.log(TAG,"Updated Remainder Uri: "+updateUri);
                globalclass.log(TAG,"Updated rowsAffected: "+rowsAffected);
                globalclass.toast_short("Remainder has been updated successfully!");
                globalclass.setRemainderAlarm(selectedTimestamp, updateUri);
                onBackPressed();
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,error);
            globalclass.sendLog(MyAlarmRemainderProvider,TAG,"","updateRemainder_Exception","",error);

            globalclass.toast_long("Unable to update remainder, please try again later!");
            onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        globalclass.checkAutomaticTimeAndTimeZone(activity);
    }
}
