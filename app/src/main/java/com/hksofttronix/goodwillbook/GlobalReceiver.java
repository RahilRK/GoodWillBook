package com.hksofttronix.goodwillbook;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.hksofttronix.goodwillbook.BusinessInfo.uploadBusinessLogo_Service;
import com.hksofttronix.goodwillbook.ContactList.contactList_Service;
import com.hksofttronix.goodwillbook.Pay.uploadAttachments_Service;
import com.hksofttronix.goodwillbook.Remainder.RemainderModel;
import com.hksofttronix.goodwillbook.Transaction.transaction_Service;
import com.hksofttronix.goodwillbook.Remainder.AlarmReminderContract;
import com.hksofttronix.goodwillbook.Util.sendLog_Service;

import static com.hksofttronix.goodwillbook.Globalclass.MyAlarmRemainderProvider;

public class GlobalReceiver extends BroadcastReceiver
{
    String TAG = this.getClass().getSimpleName();
    Globalclass globalclass;
    Mydatabase mydatabase;
    Context maincontext;

    @Override
    public void onReceive(Context context, Intent intent) {

        globalclass = Globalclass.getInstance(context);
        mydatabase = Mydatabase.getInstance(context);
        maincontext = context;

        if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
        {
            globalclass.log(TAG,"ACTION_BOOT_COMPLETED");
            globalclass.RefreshAll(false);
        }
        else if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Globalclass.sendLog_Service_ACTION))
        {
            globalclass.log(TAG,Globalclass.sendLog_Service_ACTION);
            if(!globalclass.isMyServiceRunning(context, sendLog_Service.class))
            {
                globalclass.startAnyService(context, sendLog_Service.class);
            }
            else
            {
                globalclass.setAlarmFor_sendLog_Service();
            }
        }
        else if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Globalclass.uploadBusinessLogo_Service_ACTION))
        {
            globalclass.log(TAG,Globalclass.uploadBusinessLogo_Service_ACTION);
            if(!globalclass.isMyServiceRunning(context,uploadBusinessLogo_Service.class))
            {
                globalclass.startAnyService(context, uploadBusinessLogo_Service.class);
            }
            else
            {
                globalclass.setAlarmFor_uploadBusinessLogo_Service();
            }
        }
        else if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Globalclass.uploadAttachments_Service_ACTION))
        {
            globalclass.log(TAG,Globalclass.uploadAttachments_Service_ACTION);
            if(!globalclass.isMyServiceRunning(context, uploadAttachments_Service.class))
            {
                globalclass.startAnyService(context, uploadAttachments_Service.class);
            }
            else
            {
                globalclass.setAlarmFor_uploadAttachments_Service();
            }
        }
        else if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Globalclass.contactList_Service_ACTION))
        {
            globalclass.log(TAG,Globalclass.contactList_Service_ACTION);
            if(!globalclass.isMyServiceRunning(context, contactList_Service.class))
            {
                globalclass.startAnyService(context, contactList_Service.class);
            }
            else
            {
                globalclass.setAlarmFor_contactList_Service();
            }
        }
        else if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Globalclass.transaction_Service_ACTION))
        {
            globalclass.log(TAG,Globalclass.transaction_Service_ACTION);
            if(!globalclass.isMyServiceRunning(context, transaction_Service.class))
            {
                globalclass.startAnyService(context, transaction_Service.class);
            }
            else
            {
                globalclass.setAlarmFor_transaction_Service();
            }
        }
        else if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Globalclass.RefreshAll_ACTION))
        {
            globalclass.log(TAG,"RefreshAll_ACTION");
            globalclass.RefreshAll(false);
        }
        else if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase(Globalclass.Remainder_ACTION))
        {
            globalclass.log(TAG,"Remainder_ACTION");
            handleRemainderData(intent);
        }
    }

    void handleRemainderData(Intent intent)
    {
        Uri uri = intent.getData();

        //todo fetch reminder data
        Cursor cursor = maincontext.getContentResolver().query(uri, null, null, null, null);

        try
        {
            if (cursor != null && cursor.moveToFirst())
            {
                int id = cursor.getInt(cursor.getColumnIndex(AlarmReminderContract.AlarmReminderEntry._ID));
                String title = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
                String date = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_DATE);
                String time = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_TIME);
                String mobilenumber = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_MOBNO);
                int amount = mydatabase.getHomeDetail(mobilenumber).get(0).getGive_receive_sum();

                if(amount == 0)
                {
                    globalclass.log(TAG,"Remainder_ACTION data: "+"title: "+title+", date: "+date+", time: "+time+", amountnote: Settled ₹ "+amount);
                }
                else if(amount > 0)
                {
                    globalclass.log(TAG,"Remainder_ACTION data: "+"title: "+title+", date: "+date+", time: "+time+", amountnote: You will receive ₹ "+amount);

                    RemainderModel model = new RemainderModel();
                    model.setId(id);
                    model.setTitle("Amount to be receive remainder");
                    model.setText("₹ "+amount+" to be receive from "+title+", today!");
                    globalclass.showRemainderNotification(maincontext,model);
                }
                else if(amount < 0)
                {
                    String trim_amount = String.valueOf(amount).replace("-","");
                    globalclass.log(TAG,"Remainder_ACTION data: "+"title: "+title+", date: "+date+", time: "+time+", amountnote: You will give ₹ "+trim_amount);

                    RemainderModel model = new RemainderModel();
                    model.setId(id);
                    model.setTitle("Amount to be pay remainder");
                    model.setText("₹ "+trim_amount+" to be pay to "+title+", today!");
                    globalclass.showRemainderNotification(maincontext,model);
                }

                SQLiteDatabase database = mydatabase.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE,0);
                database.update(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, cv, "_id = ?",new String[]{String.valueOf(id)});
//                    mydatabase.deleteData(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME,AlarmReminderContract.AlarmReminderEntry._ID, String.valueOf(id));
            }

        } catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log(TAG,error);
            globalclass.sendLog(MyAlarmRemainderProvider,TAG,"","Remainder_ACTION_Exception","",error);

        }

        if (cursor != null) {
            cursor.close();
        }
    }
}
