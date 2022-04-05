package com.hksofttronix.goodwillbook;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hksofttronix.goodwillbook.AddBusiness.Addbusiness;
import com.hksofttronix.goodwillbook.MainActivity.MainActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = SplashScreen.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        globalclass = Globalclass.getInstance(SplashScreen.this);
        mydatabase = Mydatabase.getInstance(SplashScreen.this);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                if(!globalclass.isInternetPresent())
                {
                    proceedFurther();
                }
                else
                {
                    if(globalclass.userHadLoggedIn()) {
                        globalclass.checkForPendingUploads();
                    }
                    getFCMDataForEmergencyValues();
                }
            }
        }, 500);

    }

    public void getFCMData()
    {
        try
        {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("StaticValues");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists())
                    {
                        globalclass.log(TAG,"dataSnapshot data: "+dataSnapshot.getValue());
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                        {
                            String getkey = dataSnapshot1.getKey();
                            String getvalue = dataSnapshot1.getValue().toString();
                            globalclass.setRefreshAllStringData(getkey,getvalue);
                        }

                        proceedFurther();
                    }
                    else
                    {
                        globalclass.log(TAG,"dataSnapshot not exist");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    String error = Log.getStackTraceString(databaseError.toException());
                    globalclass.log(TAG,"getFCMData_onCancelled"+error);
                    globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","getFCMData_onCancelled","",error);
                }
            });
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,"getFCMData_Exception"+error);
            globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","getFCMData_Exception","",error);
        }
    }

    void proceedFurther()
    {
        if(globalclass.userHadLoggedIn())
        {
            if(mydatabase.getAllBusinessList().size() == 0)
            {
                if(globalclass.getStringData(Globalclass.lockcode).equalsIgnoreCase(""))
                {
                    startActivity(new Intent(getApplicationContext(), Addbusiness.class));
                }
                else
                {
                    startActivity(new Intent(getApplicationContext(), LockScreen.class).putExtra("goto","Addbusiness"));
                }
            }
            else
            {
                if(globalclass.getStringData(Globalclass.lockcode).equalsIgnoreCase(""))
                {
//                    startActivity(new Intent(getApplicationContext(), BarcodeScanner.class));
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                else
                {
                    startActivity(new Intent(getApplicationContext(), LockScreen.class).putExtra("goto","MainActivity"));
                }
            }
        }
        else
        {
            startActivity(new Intent(getApplicationContext(), Verification.class));
        }

        finish();
    }

    public void getFCMDataForEmergencyValues()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            return;
        }

        final Map<String,String> map = new HashMap<>();
        try
        {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("EmergencyValues");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists())
                    {
                        globalclass.log(TAG,"dataSnapshot data: "+dataSnapshot.getValue());
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                        {
                            String getkey = dataSnapshot1.getKey();
                            String getvalue = dataSnapshot1.getValue().toString();

                            map.put(getkey,getvalue);
                        }

                        if(map.get("show").equalsIgnoreCase("1"))
                        {
                            emergencyDialogue(map.get("message"));
                        }
                        else
                        {
                            getFCMData();
                        }
                    }
                    else
                    {
                        globalclass.log(TAG,"dataSnapshot not exist");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    String error = Log.getStackTraceString(databaseError.toException());
                    globalclass.log(TAG,"getFCMDataForEmergencyValues_onCancelled"+error);
                    globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","getFCMDataForEmergencyValues_onCancelled","",error);
                }
            });
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,"getFCMDataForEmergencyValues_Exception"+error);
            globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","getFCMDataForEmergencyValues_Exception","",error);
        }
    }

    void emergencyDialogue(String message)
    {
        // custom dialog
        final Dialog dialog = new Dialog(activity,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.emergency_dialogue);
        dialog.setCancelable(false);

        ImageView ivclose = dialog.findViewById(R.id.ivclose);
        TextView tvmessage = dialog.findViewById(R.id.tvmessage);
        tvmessage.setText(message);

        ivclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                finishAffinity();
                finish();
            }
        });
        dialog.show();
    }
}
