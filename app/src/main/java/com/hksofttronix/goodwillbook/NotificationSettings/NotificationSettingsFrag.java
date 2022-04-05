package com.hksofttronix.goodwillbook.NotificationSettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;

public class NotificationSettingsFrag extends PreferenceFragmentCompat
{
    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    public static final String AMOUNT_RECEIVED_KEY = "amountreceivedkey";
    public static final String AMOUNT_APPROVED_KEY = "amountapprovedkey";
    public static final String TRANSACTION_UPDATED_KEY = "transactionupdatedkey";
    public static final String TRANSACTION_ATTACHMENT_DELETED_KEY = "transactionattachmentdeletedkey";
    public static final String AMOUNT_RECEIVED_SOUND_KEY = "amountreceivedsoundkey";

    SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    SwitchPreferenceCompat SC_amountreceived, SC_amountapproved, SC_transactionupdated, SC_transactionattachmentupdated;
    Preference PRF_amountreceivedsoundkey;

    String chosenRingtone = "";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof AppCompatActivity)
        {
            activity  = (AppCompatActivity) context;
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.notification_settings);
        globalclass = Globalclass.getInstance(activity);

        PRF_amountreceivedsoundkey = findPreference(AMOUNT_RECEIVED_SOUND_KEY);
        SC_amountreceived =  findPreference(AMOUNT_RECEIVED_KEY);
        SC_amountapproved =  findPreference(AMOUNT_APPROVED_KEY);
        SC_transactionupdated =  findPreference(TRANSACTION_UPDATED_KEY);
        SC_transactionattachmentupdated =  findPreference(TRANSACTION_ATTACHMENT_DELETED_KEY);
        SC_transactionattachmentupdated =  findPreference(TRANSACTION_ATTACHMENT_DELETED_KEY);

        setText();

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                if (key.equals(AMOUNT_RECEIVED_KEY))
                {
                    boolean result = sharedPreferences.getBoolean(AMOUNT_RECEIVED_KEY,false);
                    if(result)
                    {
                        SC_amountreceived.setTitle("Notification sound is ON");
                    }
                    else
                    {
                        SC_amountreceived.setTitle("Notification sound is OFF");
                    }
                }
                else  if (key.equals(AMOUNT_APPROVED_KEY))
                {
                    boolean result = sharedPreferences.getBoolean(AMOUNT_APPROVED_KEY,false);
                    if(result)
                    {
                        SC_amountapproved.setTitle("Notification sound is ON");
                    }
                    else
                    {
                        SC_amountapproved.setTitle("Notification sound is OFF");
                    }
                }
                else  if (key.equals(TRANSACTION_UPDATED_KEY))
                {
                    boolean result = sharedPreferences.getBoolean(TRANSACTION_UPDATED_KEY,false);
                    if(result)
                    {
                        SC_transactionupdated.setTitle("Notification sound is ON");
                    }
                    else
                    {
                        SC_transactionupdated.setTitle("Notification sound is OFF");
                    }
                }
                else  if (key.equals(TRANSACTION_ATTACHMENT_DELETED_KEY))
                {
                    boolean result = sharedPreferences.getBoolean(TRANSACTION_ATTACHMENT_DELETED_KEY,false);
                    if(result)
                    {
                        SC_transactionattachmentupdated.setTitle("Notification sound is ON");
                    }
                    else
                    {
                        SC_transactionattachmentupdated.setTitle("Notification sound is OFF");
                    }
                }
            }
        };

        PRF_amountreceivedsoundkey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select sound");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                startActivityForResult(intent, 5);

                return false;
            }
        });
    }

    void setText()
    {
        if(globalclass.getamountreceivedkey_value())
        {
            SC_amountreceived.setTitle("Notification sound is ON");
        }
        else
        {
            SC_amountreceived.setTitle("Notification sound is OFF");
        }

        if(globalclass.getamountapprovedkey_value())
        {
            SC_amountapproved.setTitle("Notification sound is ON");
        }
        else
        {
            SC_amountapproved.setTitle("Notification sound is OFF");
        }

        if(globalclass.gettransactionupdatedkey_value())
        {
            SC_transactionupdated.setTitle("Notification sound is ON");
        }
        else
        {
            SC_transactionupdated.setTitle("Notification sound is OFF");
        }

        if(globalclass.gettransactionattachmentdeletedkey_value())
        {
            SC_transactionattachmentupdated.setTitle("Notification sound is ON");
        }
        else
        {
            SC_transactionattachmentupdated.setTitle("Notification sound is OFF");
        }

        if(globalclass.checknull(globalclass.getStringData("chosenRingtone")).equalsIgnoreCase(""))
        {
            Uri defaulturi = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(activity, defaulturi);
            String ringtonetitle = ringtone.getTitle(activity);
            PRF_amountreceivedsoundkey.setTitle(ringtonetitle);
        }
        else
        {
            PRF_amountreceivedsoundkey.setTitle(globalclass.getStringData("ringtonetitle"));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 5)
        {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (uri != null)
            {
                chosenRingtone = uri.toString();
                globalclass.log(TAG,"chosenRingtone: "+chosenRingtone);
                globalclass.setStringData("chosenRingtone",chosenRingtone);
                Ringtone ringtone = RingtoneManager.getRingtone(activity, uri);
                String ringtonetitle = ringtone.getTitle(activity);
                PRF_amountreceivedsoundkey.setTitle(ringtonetitle);
                globalclass.setStringData("ringtonetitle",ringtonetitle);
            }
            else
            {
                chosenRingtone = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
