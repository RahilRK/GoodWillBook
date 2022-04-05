package com.hksofttronix.goodwillbook;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hksofttronix.goodwillbook.BusinessInfo.uploadBusinessLogo_Service;
import com.hksofttronix.goodwillbook.ContactList.ContactModel;
import com.hksofttronix.goodwillbook.ContactList.Contactlist;
import com.hksofttronix.goodwillbook.ContactList.contactList_Service;
import com.hksofttronix.goodwillbook.Feedback.uploadFeedbackAttachments_Service;
import com.hksofttronix.goodwillbook.Pay.uploadAttachments_Service;
import com.hksofttronix.goodwillbook.Remainder.RemainderModel;
import com.hksofttronix.goodwillbook.Transaction.TransactionDetail;
import com.hksofttronix.goodwillbook.Transaction.transaction_Service;
import com.hksofttronix.goodwillbook.Util.appLogModel;
import com.hksofttronix.goodwillbook.Util.sendLog_Service;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import id.zelory.compressor.Compressor;

import static com.hksofttronix.goodwillbook.NotificationSettings.NotificationSettingsFrag.AMOUNT_APPROVED_KEY;
import static com.hksofttronix.goodwillbook.NotificationSettings.NotificationSettingsFrag.AMOUNT_RECEIVED_KEY;
import static com.hksofttronix.goodwillbook.NotificationSettings.NotificationSettingsFrag.TRANSACTION_ATTACHMENT_DELETED_KEY;
import static com.hksofttronix.goodwillbook.NotificationSettings.NotificationSettingsFrag.TRANSACTION_UPDATED_KEY;
import static com.hksofttronix.goodwillbook.Remainder.AlarmReminderContract.AlarmReminderEntry.CONTENT_URI_RefreshAll;

public class Globalclass {

    String TAG = this.getClass().getSimpleName();
    private static volatile Globalclass globalClass;
    private Context context;

    SharedPreferences preferences,refreshallPref;
    SharedPreferences.Editor editor,refreshallEditor;
    private static final String GOOD_WILL_BOOK_Pref ="GoodWillBook";
    private static final String RefreshAll_Pref ="RefreshAll";

    //todo sendlog related public variables
    public static final String SuccessApiCall = "SuccessApiCall";
    public static final String ErrorApiCall = "ErrorApiCall";
    public static final String TryCatchException = "TryCatchException";
    public static final String ErrorInSendingFcmNotification = "ErrorInSendingFcmNotification";
    public static final String GlideException = "GlideException";
    public static final String MyAlarmRemainderProvider = "MyAlarmRemainderProvider";
    public static final String RefreshAllRemainder = "RefreshAllRemainder";
    public static final String GeneratePdf = "GeneratePdf";
    public static final String Screenshot = "Screenshot";

    //todo other public variables
    public static final String CompressedImagespath = "/GoodWill/.CompressedImages/";
    public static final String Pdfpath = "/GoodWill/Pdf/";
    public static final String Screenshotspath = "/GoodWill/Screenshots/";
    public static final String timeStamp = new SimpleDateFormat("dd_MMM_yyyy_HH_mm_ss").format(new Date());
    public static final String openGoogleMap = "http://maps.google.com/maps?q=loc:";
    public static final String lockcode = "lockcode";
    public static final String defaultstartdate = "2000-01-01";


    public static final String NotificationReceiver = "NotificationReceiver";
    public static final String ContactSyncCompleteReceiver = "ContactSyncCompleteReceiver";

    //todo Notif Channel
    public static final String GoodWill_Notifi_Channel_ID = "500";
    public static final String GoodWill_Notifi_Channel_Name = "Important notification";

    public static final String uploadBusinessLogo_Service_Notifi_Channel_ID = "501";
    public static final String uploadBusinessLogo_Service_Notifi_Channel_Name = "Upload Bussiness Logo notification";

    public static final String contactList_Service_Notifi_Channel_ID = "502";
    public static final String contactList_Service_Notifi_Channel_Name = "Sync contact notification";

    public static final String uploadAttachments_Service_Notifi_Channel_ID = "503";
    public static final String uploadAttachments_Service_Notifi_Channel_Name = "Attachments notification";

    public static final String transaction_Service_Notifi_Channel_ID = "504";
    public static final String transaction_Service_Notifi_Channel_Name = "Refresh transaction notification";

    public static final String Fcm_Notifi_Channel_ID = "505";
    public static final String Fcm_Notifi_Channel_Name = "Transaction notification";

    public static final String sendLog_Channel_ID = "506";
    public static final String sendLog_Channel_Name = "Error log notification";

    public static final String uploadFeedbackImageViaEmails_Service_Notifi_Channel_ID = "507";
    public static final String uploadFeedbackImageViaEmails_Service_Notifi_Channel_Name = "Send feedback notification";

    public static final String Fcm_Notifi_SilentChannel_ID = "508";
    public static final String Fcm_Notifi_SilentChannel_Name = "Transaction Silent notification";

    //todo Broadcast Receiver ACTIONS and alarmMin
    public static final String uploadBusinessLogo_Service_ACTION = "uploadBusinessLogo_Service";
    public static final int uploadBusinessLogo_Service_requestID = 101;
    public static final int uploadBusinessLogo_Service_alarmMin = 5;

    public static final String uploadAttachments_Service_ACTION = "uploadAttachments_Service";
    public static final int uploadAttachments_Service_requestID = 102;
    public static final int uploadAttachments_Service_alarmMin = 5;

    public static final String transaction_Service_ACTION = "transaction_Service";
    public static final int transaction_Service_requestID = 103;
    public static final int transaction_Service_alarmMin = 5;

    public static final String contactList_Service_ACTION = "contactList_Service";
    public static final int contactList_Service_requestID = 104;
    public static final int contactList_Service_alarmMin = 5;

    public static final String RefreshAll_ACTION = "RefreshAll";
    public static final int RefreshAll_requestID = 105;

    public static final String Remainder_ACTION = "AddRemainder";
    public static final int Remainder_requestID = 106;

    public static final String sendLog_Service_ACTION = "sendLog_Service";
    public static final int sendLog_Service_requestID = 107;
    public static final int sendLog_Service_alarmMin = 5;

    public static final String uploadFeedbackAttachments_Service_ACTION = "uploadFeedbackAttachments_Service";
    public static final int uploadFeedbackAttachments_Service_requestID = 108;
    public static final int uploadFeedbackAttachments_Service_alarmMin = 5;

    public Globalclass(Context context) {
        this.context = context;
        preferences=context.getSharedPreferences(GOOD_WILL_BOOK_Pref,0);
        refreshallPref=context.getSharedPreferences(RefreshAll_Pref,0);
        editor=preferences.edit();
        refreshallEditor=refreshallPref.edit();
    }

    public static Globalclass getInstance(Context context) {

        if (globalClass == null) { //Check for the first time

            synchronized (Globalclass.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (globalClass == null) globalClass = new Globalclass(context);
            }
        }

        return globalClass;
    }

    //todo GoodWillBook Pref
    // todo PrefStringData
    public void setStringData(String setkey,String setvalue)
    {
        editor.putString(setkey,setvalue).commit();
    }

    public String getStringData(String getkey)
    {
        return preferences.getString(getkey,"");
    }
    public String getStringDataWithD(String getkey, String defValue)
    {
        return preferences.getString(getkey,defValue);
    }

    //todo PrefIntData
    public void setIntData(String setkey,int setvalue)
    {
        editor.putInt(setkey,setvalue).commit();
    }

    public int getIntData(String getkey)
    {
        return preferences.getInt(getkey, 0);
    }

    //todo PrefBooleanData
    public void setBooleanData(String setkey,Boolean setvalue)
    {
        editor.putBoolean(setkey,setvalue).commit();
    }

    public boolean getBooleanData(String getkey)
    {
        return preferences.getBoolean(getkey, false);

    }

    //todo RefreshAll Pref
    // todo PrefStringData
    public void setRefreshAllStringData(String setkey,String setvalue)
    {
        refreshallEditor.putString(setkey,setvalue).commit();
    }

    public String getRefreshAllStringData(String getkey)
    {
        return refreshallPref.getString(getkey,"");
    }
    public String getRefreshAllStringDataWithD(String getkey, String defValue)
    {
        return refreshallPref.getString(getkey,defValue);
    }

    //todo PrefIntData
    public void setRefreshAllIntData(String setkey,int setvalue)
    {
        refreshallEditor.putInt(setkey,setvalue).commit();
    }

    public int getRefreshAllIntData(String getkey)
    {
        return refreshallPref.getInt(getkey, 0);
    }

    //todo PrefBooleanData
    public void setRefreshAllBooleanData(String setkey,Boolean setvalue)
    {
        refreshallEditor.putBoolean(setkey,setvalue).commit();
    }

    public boolean getRefreshAllBooleanData(String getkey)
    {
        return refreshallPref.getBoolean(getkey, false);

    }

    //todo userPref data related function
    public boolean userHadLoggedIn()
    {
        if(getIntData("userid") != 0)
        {
            return true;
        }

        return false;
    }

    public String getuserid()
    {
        return String.valueOf(getIntData("userid"));
    }

    public String getmobilenumber()
    {
        return String.valueOf(getStringData("mobilenumber"));
    }

    public String getActivebusinessacid()
    {
        return String.valueOf(getIntData("businessacid"));
    }

    //todo clearAllPref
    public void clearPref()
    {
        SharedPreferences settings = context.getSharedPreferences(GOOD_WILL_BOOK_Pref, Context.MODE_PRIVATE);
        settings.edit().clear().apply();

        //todo to clear notification settings pref
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }

    //todo other functions
    public String getUrl()
    {
        if(BuildConfig.DEBUG)
        {
            if(getRefreshAllStringDataWithD("useglobalurl","0").equalsIgnoreCase("1"))
            {
                return getRefreshAllStringDataWithD("globalurl","");
            }
            else
            {
                return getRefreshAllStringDataWithD("localurl","http://192.168.43.94:8080/ZeroCredit/index.php/Globalclass/");
            }
        }
        else
        {
            return getRefreshAllStringDataWithD("globalurl","");
        }
    }

    public String getImageUrl()
    {
        if(BuildConfig.DEBUG)
        {
            if(getRefreshAllStringDataWithD("useglobalurl","0").equalsIgnoreCase("1"))
            {
                return getRefreshAllStringDataWithD("globalimageurl","");
            }
            else
            {
                return getRefreshAllStringDataWithD("localimageurl","http://192.168.43.94:8080/ZeroCredit/assets/images/Attachments/");
            }
        }
        else
        {
            return getRefreshAllStringDataWithD("globalimageurl","");
        }
    }

    public void log(String tag,String msg)
    {
        if(BuildConfig.DEBUG)
        {
            Log.e(tag,msg);
        }
        else
        {
            if(checknull(getRefreshAllStringDataWithD("showLogs","0")).equalsIgnoreCase("1"))
            {
                Log.e(tag,msg);
            }
        }

    }

    public void toast_short(String text)
    {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public void toast_long(String text)
    {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public void snackit(Activity activity, String text)
    {
        try {
            Snackbar.make(activity.findViewById(android.R.id.content),
                    "" + text, Snackbar.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void showDialogue(Activity activity, String title, String message)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
//                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
                .show();

    }

    public void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean isInternetPresent()
    {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    public String checknull(String value)
    {
        if(value == null)
        {
            return "";
        }

        String rvalue=value;
        if(TextUtils.isEmpty(value.toString().trim()) || value.toString().trim().equals("null")) {
            rvalue="";
        }
        return rvalue;
    }

    public String checknullAndSet(String value)
    {
        if(value == null)
        {
            return "";
        }

        String rvalue=value;
        if(TextUtils.isEmpty(value.toString().trim()) || value.toString().trim().equals("null")) {
            rvalue="No available";
        }
        return rvalue;
    }

    public static String to_md5(String pass) {
        String password = null;
        MessageDigest mdEnc;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(pass.getBytes(), 0, pass.length());
            pass = new BigInteger(1, mdEnc.digest()).toString(16);
            while (pass.length() < 32) {
                pass = "0" + pass;
            }
            password = pass;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return password;
    }

    public String getDeviceInfo()
    {
        JSONObject device_info = new JSONObject();
        try
        {
            device_info.put("Device Name",""+ Build.MANUFACTURER);
            device_info.put("Device Model",""+Build.MODEL);
            device_info.put("Android Version",""+ Build.VERSION.RELEASE);
            device_info.put("App Version",""+BuildConfig.VERSION_NAME);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return device_info.toString();
    }

    public String sendLog(String Dir, String Name, String url, String Errorlocation, String params, String data)
    {
        String Logdata = "";
        try
        {
            JSONObject sendLog = new JSONObject();
            JSONObject deviceobj;
//        JSONObject paramsobj;
//        JSONObject dataobj;

            try
            {
                if(Errorlocation.equalsIgnoreCase("Volley_POST_onResponse"))
                {
                    deviceobj = new JSONObject(getDeviceInfo());
//                    JSONObject paramsobj = new JSONObject(params);
                    JSONObject dataobj = new JSONObject(data);

                    sendLog.put("deviceInfo",deviceobj);
                    sendLog.put("params",params);
                    sendLog.put("data",dataobj);
                    sendLog.put("url",url);
                    sendLog.put("location",Errorlocation);
                    sendLog.put("Dir",Dir);
                    sendLog.put("Name",Name);
                    sendLog.put("LogDatetime",getCurrentDatetime());
                }
                else
                {
                    deviceobj = new JSONObject(getDeviceInfo());

                    sendLog.put("deviceInfo",deviceobj);
                    sendLog.put("params",params);
                    sendLog.put("data",data);
                    sendLog.put("url",url);
                    sendLog.put("location",Errorlocation);
                    sendLog.put("Dir",Dir);
                    sendLog.put("Name",Name);
                }

                Logdata = sendLog.toString();

                appLogModel model = new appLogModel();
                model.setDir(Dir);
                model.setName(Name);
                model.setData(Logdata);
                new Mydatabase(context).insertLog_inDB(model);

                log("sendLog",Logdata);

            } catch (JSONException e) {

                String error = Log.getStackTraceString(e);
                sendLog(Globalclass.TryCatchException,TAG,"","sendLogJSONException","",error);
                log("sendLogException",error);

                Logdata = "";
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            sendLog(Globalclass.TryCatchException,TAG,"","sendLogException","",error);
            log("sendLogException",error);

            Logdata = "";
        }

        return Logdata;
    }

    public void openGoogleMap(AppCompatActivity activity, String location)
    {
        String uri = openGoogleMap+location;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        activity.startActivity(intent);
    }

    public void makeCall(Context mycontext,String mobileNumber)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel: " + mobileNumber));
        mycontext.startActivity(intent);
    }

    boolean isTimeAutomatic(Context context) {

        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
    }

    boolean isTimeZoneAutomatic(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0) == 1;
    }

    public void checkAutomaticTimeAndTimeZone(final Context context)
    {
        if(!isTimeAutomatic(context) || !isTimeZoneAutomatic(context))
        {
            new MaterialAlertDialogBuilder(context, R.style.RoundShapeTheme)
                    .setTitle("Automatic Date Time and Time zone")
                    .setMessage("You will have to turn on 'Automatic date and time and time zone' to continue!")
                    .setCancelable(false)
                    .setPositiveButton("Enable now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            context.startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                        }
                    })
                    .show();
        }
    }

    public void openPdf(Context context,String filepath)
    {
        Uri path;
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                path = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(filepath));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }
            else
            {
                path = Uri.fromFile(new File(filepath));

                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setDataAndType(path,"application/pdf");

                Intent chooser = Intent.createChooser(browserIntent, "Open pdf");
                chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // optional

                context.startActivity(chooser);
            }
        }
        catch (ActivityNotFoundException e)
        {
            String error = Log.getStackTraceString(e);
            log(TAG,"openPdfActivityNotFoundException: "+error);
            toast_short(context.getResources().getString(R.string.open_pdf_no_app_found));
            sendLog(Globalclass.GeneratePdf,TAG,"","openPdfActivityNotFoundException","",error);
        }
    }

    public String getPdfpath(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+
                Globalclass.Pdfpath);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        String mImageName="pdf_"+ timeStamp +".pdf";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile.toString();
    }

    //todo staticValue Functions
    int CompressedQuality()
    {
        return Integer.parseInt(getRefreshAllStringDataWithD("compressedQuality","10"));
    }

    public int getAttachmentMaxImages()
    {
        return Integer.parseInt(getRefreshAllStringDataWithD("maxAttachments","3"));
    }

    public String getContactfilter_regex()
    {
        return getRefreshAllStringDataWithD("contactfilter_regex","[- + '' ( ) * #]");
    }

    public int getMinimumtransactionMonth()
    {
        return Integer.parseInt(getRefreshAllStringDataWithD("minimumTransactionMonths","-3"));
    }

    public String getPancard_Pattern()
    {
        return getRefreshAllStringDataWithD("pancardPattern:","[A-Z]{5}[0-9]{4}[A-Z]{1}");
    }

    public String getMobileNoRegex()
    {
        return getRefreshAllStringDataWithD("mobilenumberRegex:","[5-9]{1}[0-9]{9}");
    }

    //todo icon related functions
    public Drawable setIconColor(String iconcolor, int icon)
    {
        Drawable drawable = ContextCompat.getDrawable(context,icon);
        drawable.setColorFilter(Color.parseColor(iconcolor), PorterDuff.Mode.SRC_ATOP);

        return drawable;
    }

    public Bitmap drawableToBitmap (int iconcolor,Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.setColorFilter(iconcolor, PorterDuff.Mode.SRC_ATOP);
        drawable.draw(canvas);

        return bitmap;
    }

    //todo Notif Channel
    public void createuploadBusinessLogo_Service_Notifi_Channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    uploadBusinessLogo_Service_Notifi_Channel_ID,
                    uploadBusinessLogo_Service_Notifi_Channel_Name,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void createcontactList_Service_Notifi_Channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    contactList_Service_Notifi_Channel_ID,
                    contactList_Service_Notifi_Channel_Name,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void createGoodWill_Notifi_Channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    GoodWill_Notifi_Channel_ID,
                    GoodWill_Notifi_Channel_Name,
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void createuploadAttachment_Service_Notifi_Channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    uploadAttachments_Service_Notifi_Channel_ID,
                    uploadAttachments_Service_Notifi_Channel_Name,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void createtransaction_Service_Notifi_Channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    transaction_Service_Notifi_Channel_ID,
                    transaction_Service_Notifi_Channel_Name,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void createFcmNotifi_Channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Fcm_Notifi_Channel_ID,
                    Fcm_Notifi_Channel_Name,
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void createFcmNotifi_SilentChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Fcm_Notifi_SilentChannel_ID,
                    Fcm_Notifi_SilentChannel_Name,
                    NotificationManager.IMPORTANCE_LOW
            );

            serviceChannel.setSound(null,null);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void createsendLog_Notifi_Channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    sendLog_Channel_ID,
                    sendLog_Channel_Name,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public void createuploadFeedbackAttachments_Notifi_Channel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    uploadFeedbackImageViaEmails_Service_Notifi_Channel_ID,
                    uploadFeedbackImageViaEmails_Service_Notifi_Channel_Name,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    //todo Notifications
    public void showUploadSuccessNotif(Context context, int notif_id, String ContextTitle, String ContentText, Intent customIntent)
    {
        Intent myIntent;
        if(customIntent == null)
        {
            myIntent = new Intent(context, SplashScreen.class);
        }
        else
        {
            myIntent = customIntent;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = new NotificationCompat.Builder(context, GoodWill_Notifi_Channel_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(ContextTitle)
                .setContentText(ContentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(getAppNotifSound())
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.BLUE, 3000, 3000)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(notif_id,notification);
    }

    public void showUploadFailedNotif(Context context, int notif_id, String ContextTitle, String ContentText)
    {
        Intent myIntent = new Intent(context, SplashScreen.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = new NotificationCompat.Builder(context, GoodWill_Notifi_Channel_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(ContextTitle)
                .setContentText(ContentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(getAppNotifSound())
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED, 3000, 3000)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(notif_id,notification);
    }

    //todo upload image related functions
    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }


    public String CompressFILE(Context context, String filePath)
    {
        String compressPath=filePath;

        try
        {
            File compressedFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+
                    CompressedImagespath);

            if(!compressedFolder.exists())
            {
                compressedFolder.mkdirs();
            }

            File compressedImage = null;

            if(isImageFile(filePath))
            {
                try {
                    compressedImage = new Compressor(context)
                            .setMaxWidth(640)
                            .setMaxHeight(480)
                            .setQuality(CompressedQuality())
                            .setCompressFormat(Bitmap.CompressFormat.PNG)
                            .setDestinationDirectoryPath(compressedFolder.getAbsolutePath())
                            .compressToFile(new File(filePath));

                    compressPath=compressedImage.getAbsolutePath();

                } catch (IOException e) {
                    e.printStackTrace();

                    compressPath=filePath;
                    String error = Log.getStackTraceString(e);
                    log(TAG+"_CompressFILEIOException",error);
                    sendLog(Globalclass.TryCatchException,TAG,"","CompressFILEIOException","",error);
                }
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            log(TAG+"_CompressFILEException",error);
            sendLog(Globalclass.TryCatchException,TAG,"","CompressFILEException","",error);
        }

        log("CompressFILE",compressPath);
        return compressPath;
    }

    public String EncodeBase64(File imageFile)
    {
        FileInputStream fis = null;
        String base64ImageSend = "";

        try {
            fis = new FileInputStream(imageFile);
            byte[] byteArray = new byte[0];
            try {
                byteArray = inputStreamToByteArray(fis);
            } catch (IOException e) {
                log("Base64_1 IOException",e.getMessage());
                e.printStackTrace();
            }
            base64ImageSend = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log("Base64_2 IOException",e.getMessage());
        }
        return base64ImageSend;
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        if(inputStream==null) {
            return null;
        }
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    //todo BG service related functions
    public void startAnyService(Context context,Class aClass)
    {
        if(!isMyServiceRunning(context,aClass))
        {
            Intent intent = new Intent(context, aClass);
            ContextCompat.startForegroundService(context,intent);
        }
    }

    public void stopAnyService(Context context,Class aClass)
    {
        Intent intent = new Intent(context, aClass);
        context.stopService(intent);
    }

    public boolean isMyServiceRunning(Context context,Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void checkForPendingUploads()
    {
        if(new Mydatabase(context).getAllPendingUploadBusinessLogo() > 0)
        {
            startAnyService(context, uploadBusinessLogo_Service.class);
        }

        if(new Mydatabase(context).getAttachmentList() > 0)
        {
            startAnyService(context, uploadAttachments_Service.class);
        }

        if(new Mydatabase(context).getFeedbackCount() > 0)
        {
            startAnyService(context, uploadFeedbackAttachments_Service.class);
        }
    }

    public void RefreshAll(boolean show_notification)
    {
        if(!isInternetPresent() || !userHadLoggedIn())
        {
            log(TAG,context.getResources().getString(R.string.noInternetConnection));
            updateRemainderForRefreshAll(Calendar.HOUR,1);
            return;
        }
        updateRemainderForRefreshAll(Calendar.DATE,1);

        if(new Mydatabase(context).getAppLogList() > 0)
        {
            startAnyService(context, sendLog_Service.class);
        }
        else
        {
            setAlarmFor_sendLog_Service();
        }

//        startAnyService(context, contactList_Service.class);
//        startAnyService(context, transaction_Service.class);

        Intent transaction_ServiceIntent = new Intent(context, transaction_Service.class);
        transaction_ServiceIntent.putExtra("show_notification",show_notification);
        ContextCompat.startForegroundService(context,transaction_ServiceIntent);

        Intent contactList_ServiceIntent = new Intent(context, contactList_Service.class);
        contactList_ServiceIntent.putExtra("show_notification",show_notification);
        ContextCompat.startForegroundService(context,contactList_ServiceIntent);

        checkForPendingUploads();

        getFCMData();

        log(TAG,"RefreshAll done");
    }

    //todo Alarm related functions
    public void setAlarm(Context context, int time, String ACTION, int requestID)
    {
        cancelAlarm(context,ACTION,requestID);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = getCalendar();

//        if(BuildConfig.DEBUG)
//        {
//            calendar.add(Calendar.SECOND,10);
//        }
//        else
//        {
//            calendar.add(Calendar.MINUTE,time);
//        }

        calendar.add(Calendar.MINUTE,time);


        Intent alarmIntent = new Intent(context, GlobalReceiver.class);
        alarmIntent.setAction(ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestID, alarmIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        log(TAG+"_setAlarm", "Of "+ACTION+" ON - "+ getLongToDatetime(calendar.getTimeInMillis(),"dd-MM-yyyy hh:mm:ss aa"));
    }

    public void cancelAlarm(Context context, String ACTION, int requestID)
    {
        Intent intent = new Intent(context, GlobalReceiver.class);
        intent.setAction(ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void setAlarmFor_uploadBusinessLogo_Service()
    {
        setAlarm(context, uploadBusinessLogo_Service_alarmMin,uploadBusinessLogo_Service_ACTION,uploadBusinessLogo_Service_requestID);
    }

    public void setAlarmFor_uploadAttachments_Service()
    {
        setAlarm(context, uploadAttachments_Service_alarmMin,uploadAttachments_Service_ACTION,uploadAttachments_Service_requestID);
    }

    public void setAlarmFor_transaction_Service()
    {
        setAlarm(context, transaction_Service_alarmMin,transaction_Service_ACTION,transaction_Service_requestID);
    }

    public void setAlarmFor_contactList_Service()
    {
        setAlarm(context, contactList_Service_alarmMin,contactList_Service_ACTION,contactList_Service_requestID);
    }

    public void setAlarmFor_sendLog_Service()
    {
        setAlarm(context, sendLog_Service_alarmMin,sendLog_Service_ACTION,sendLog_Service_requestID);
    }

    public void setAlarmFor_uploadFeedbackImageViaEmails_Service()
    {
        setAlarm(context, uploadFeedbackAttachments_Service_alarmMin,uploadFeedbackAttachments_Service_ACTION,uploadFeedbackAttachments_Service_requestID);
    }

    public void setRemainderAlarm(long time, Uri mCurrentReminderUri)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, GlobalReceiver.class);
        intent.setData(mCurrentReminderUri);
        intent.setAction(Globalclass.Remainder_ACTION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Globalclass.Remainder_requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= 23)
        {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
        else if (Build.VERSION.SDK_INT >= 19)
        {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
        else
        {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }

        log(TAG+"_setCollection Alarm", "Of "+Globalclass.Remainder_ACTION +" ON - "+ getLongToDatetime(time,"dd-MM-yyyy hh:mm:ss aa"));
    }

    public void cancelRemainderAlarm(Context context, String ACTION, int requestID, Uri cancelRemainderuri)
    {
        Intent intent = new Intent(context, GlobalReceiver.class);
        intent.setData(cancelRemainderuri);
        intent.setAction(ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void setRefreshAllRemainderAlarm(long time, Uri mCurrentReminderUri)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, GlobalReceiver.class);
        intent.setData(mCurrentReminderUri);
        intent.setAction(Globalclass.RefreshAll_ACTION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Globalclass.RefreshAll_requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, 86400000L, pendingIntent); //repeat in every 24 hr

        if (Build.VERSION.SDK_INT >= 23)
        {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
        else if (Build.VERSION.SDK_INT >= 19)
        {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
        else
        {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }


        log(TAG+"_setRefreshAll Alarm", "Of "+Globalclass.RefreshAll_ACTION +" ON - "+ getLongToDatetime(time,"dd-MM-yyyy hh:mm:ss aa"));
        sendLog(RefreshAllRemainder,TAG,"","setRefreshAll Alarm of userid = "+getuserid(),"","setRefreshAll Alarm Of "+Globalclass.RefreshAll_ACTION +" ON - "+ getLongToDatetime(time,"dd-MM-yyyy hh:mm:ss aa"));
    }

    public void saveRemainderForRefreshAll()
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(getMilliSecond());

            calendar.add(Calendar.HOUR, 24);

            long selectedTimestamp =  calendar.getTimeInMillis();

            Uri newUri = context.getContentResolver().insert(CONTENT_URI_RefreshAll, null);
            if(newUri == null)
            {
                sendLog(RefreshAllRemainder,TAG,"","saveRemainderForRefreshAll","","newUri is null");
            }
            else
            {
                log(TAG,"Added RefreshAll Remainder Uri: "+newUri);
                setRefreshAllRemainderAlarm(selectedTimestamp, newUri);
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            log(TAG,error);
            sendLog(RefreshAllRemainder,TAG,"","saveRemainderForRefreshAll","",error);
        }
    }

    public void cancelRefreshAllRemainderAlarm(Context context, String ACTION, int requestID, Uri cancelRemainderuri)
    {
        Uri cancelUri = Uri.parse(cancelRemainderuri+"/1");
        Intent intent = new Intent(context, GlobalReceiver.class);
        intent.setData(cancelUri);
        intent.setAction(ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                requestID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        log(TAG,"cancelRefreshAllRemainderAlarm Uri: "+cancelUri.toString());
    }

    void updateRemainderForRefreshAll(int type,int qty)
    {
        try
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(getMilliSecond());

            calendar.add(type, qty);

            long selectedTimestamp =  calendar.getTimeInMillis();

            Uri newUri = context.getContentResolver().insert(CONTENT_URI_RefreshAll, null);
            if(newUri == null)
            {
                sendLog(RefreshAllRemainder,TAG,"","updateRemainderForRefreshAll","","newUri is null");
            }
            else
            {
                log(TAG,"Updated RefreshAll Remainder Uri: "+newUri);
                setRefreshAllRemainderAlarm(selectedTimestamp, newUri);
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            log(TAG,error);
            sendLog(RefreshAllRemainder,TAG,"","updateRemainderForRefreshAll","",error);
        }
    }

    //todo Datetime related functions
    public static Calendar getCalendar()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return calendar;
    }

    public String getLongToDatetime(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public String getCurrentDate()
    {
        Date date = getCalendar().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = simpleDateFormat.format(date);
        return formattedDate;
    }

    public String getCurrentTime()
    {
        Date date = getCalendar().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        String formattedDate = simpleDateFormat.format(date);
        return formattedDate;
    }

    public String getCurrentSeconds()
    {
        Date date = getCalendar().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ss");
        String formattedDate = simpleDateFormat.format(date);
        return formattedDate;
    }

    public String getCurrentDatetime()
    {
        String currentdate = getCurrentDate();
        String currenttime = getCurrentTime();

        return currentdate+" "+currenttime;
    }

    public String formatDateTime(String datetime,String inputformat,String output) {
        String inputPattern = inputformat; //db format
        String outputPattern = output; //user format
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(datetime);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public String formatDateTime_DBToUser(String datetime) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss"; //db format
        String outputPattern = "dd MMM yyyy hh:mm:ss a"; //user format
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(datetime);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public String formatDateTime_UserToDB(String datetime) {
        String inputPattern = "dd MMM yyyy hh:mm:ss a";
        String outputPattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(datetime);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public long getMilliSecond()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
        return calendar.getTimeInMillis();
    }

    public long datetoMiliSecond(String date)
    {
        //String date_ = date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            Date mDate = sdf.parse(date);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Date in milli :: " + timeInMilliseconds);
            return timeInMilliseconds;
        }
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;
    }

    public ArrayList<String> splitTimeInDBFormat(String time)
    {
        ArrayList<String> arrayList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        arrayList.add(String.valueOf(c.get(Calendar.HOUR_OF_DAY)));
        arrayList.add(String.valueOf(c.get(Calendar.MINUTE)));
        return arrayList;
    }

    public ArrayList<String> splitDateAndTimeInUserFormat(String debitdatetime)
    {
        ArrayList<String> arrayList = new ArrayList<>();

        String dateValue = debitdatetime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        try {
            date = sdf.parse(dateValue);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss a");
        String getdate = dateFormatter.format(date);
        String gettime = timeFormatter.format(date);

        arrayList.add(getdate);
        arrayList.add(gettime);
        return arrayList;
    }

    public ArrayList<String> splitDateAndTimeInDBFormat(String debitdatetime)
    {
        ArrayList<String> arrayList = new ArrayList<>();

        String dateValue = debitdatetime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        try {
            date = sdf.parse(dateValue);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
        String getdate = dateFormatter.format(date);
        String gettime = timeFormatter.format(date);

        arrayList.add(getdate);
        arrayList.add(gettime);
        return arrayList;
    }

    //todo find sum related function
    public int getCustomerSum(String contactNumber)
    {
        int debitamount = new Mydatabase(context).getTotalDebitAmount(contactNumber);
        int creditamount = new Mydatabase(context).getTotalCreditAmount(contactNumber);
        return debitamount - creditamount;
    }

    //todo call LocalReceiver functions
    public void callNotificationReceiver()
    {
        Intent intent = new Intent(Globalclass.NotificationReceiver);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void callContactSyncCompleteReceiver()
    {
        Intent intent = new Intent(Globalclass.ContactSyncCompleteReceiver);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    //todo FCM Notification related functions
    public void subscribeToTopic(final String topic)
    {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (!task.isSuccessful()) {
                            log(TAG,"subscribeToTopic: "+topic);
                            String error = task.getException().toString();
                            log(TAG,"subscribeToTopicException: "+error);
                            sendLog(Globalclass.TryCatchException,TAG,"","subscribeToTopic_Exception",topic,error);
                        }
                        else
                        {
                            log(TAG,"subscribeToTopic: "+topic);
                        }
                    }
                });
    }

    public void unsubscribeToTopic(final String topic)
    {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (!task.isSuccessful()) {
                            log(TAG,"unsubscribeToTopic: "+topic);
                            String error = task.getException().toString();
                            log(TAG,"unsubscribeToTopicException: "+error);
                            sendLog(Globalclass.TryCatchException,TAG,"","unsubscribeToTopic_Exception",topic,error);
                        }
                        else
                        {
                            log(TAG,"unsubscribeToTopic: "+topic);
                        }
                    }
                });
    }

    public boolean getamountreceivedkey_value()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(AMOUNT_RECEIVED_KEY,true);
    }

    public boolean getamountapprovedkey_value()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(AMOUNT_APPROVED_KEY,true);
    }

    public boolean gettransactionupdatedkey_value()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(TRANSACTION_UPDATED_KEY,true);
    }

    public boolean gettransactionattachmentdeletedkey_value()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(TRANSACTION_ATTACHMENT_DELETED_KEY,true);
    }

    public Uri getAppNotifSound()
    {
        Uri defaulturi = null;
        if(checknull(getStringData("chosenRingtone")).equalsIgnoreCase(""))
        {
            defaulturi = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        else
        {
            defaulturi = Uri.parse(getStringDataWithD("chosenRingtone", String.valueOf(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))));
            if(defaulturi == null)
            {
                defaulturi = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        return defaulturi;
    }

    public void showFcmNotif_ForAmountReceivedOrApproved(Context context, JSONObject jsonObject)
    {
        NotificationManagerCompat notificationManager;
        NotificationCompat.Builder notification = null;

        try
        {
            try
            {
                Intent myIntent = new Intent(context, TransactionDetail.class);
                myIntent.putExtra("transactionid",jsonObject.getString("transactionid"));
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher);

                notificationManager = NotificationManagerCompat.from(context);

                notification = new NotificationCompat.Builder(context, Fcm_Notifi_Channel_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(jsonObject.getString("title"))
                        .setContentText(jsonObject.getString("text"))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setSound(getAppNotifSound())
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setLights(Color.BLUE, 3000, 3000)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(jsonObject.getString("text"))
                                .setBigContentTitle(jsonObject.getString("title"))
                                .setSummaryText(jsonObject.getString("text")));

                if(jsonObject.getString("type").equalsIgnoreCase("Amount received"))
                {
                    if(getamountreceivedkey_value())
                    {
                    }
                    else 
                    {
                        notification = new NotificationCompat.Builder(context, Fcm_Notifi_SilentChannel_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setLargeIcon(largeIcon)
                                .setContentTitle(jsonObject.getString("title"))
                                .setContentText(jsonObject.getString("text"))
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                                .setLights(Color.BLUE, 3000, 3000)
                                .setAutoCancel(true)
                                .setContentIntent(pendingIntent)
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(jsonObject.getString("text"))
                                        .setBigContentTitle(jsonObject.getString("title"))
                                        .setSummaryText(jsonObject.getString("text")));

                    }
                }

                notificationManager.notify(Integer.parseInt(jsonObject.getString("notif_id")),notification.build());

            } catch (JSONException e) {
                e.printStackTrace();
                String error = Log.getStackTraceString(e);
                log("showFcmNotif_ForAmountReceivedOrApproved_JSONException",error);
                sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","showFcmNotif_ForAmountReceivedOrApproved_JSONException",jsonObject.toString(),error);
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            log("showFcmNotif_ForAmountReceivedOrApproved_Exception",error);
            sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","showFcmNotif_ForAmountReceivedOrApproved_Exception",jsonObject.toString(),error);

        }
    }

    public void showFcmSilentNotif_ForAmountReceivedOrApproved(Context context, JSONObject jsonObject)
    {
        NotificationManagerCompat notificationManager;
        NotificationCompat.Builder notification = null;

        try
        {
            try
            {
                Intent myIntent = new Intent(context, TransactionDetail.class);
                myIntent.putExtra("transactionid",jsonObject.getString("transactionid"));
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher);

                notificationManager = NotificationManagerCompat.from(context);

                notification = new NotificationCompat.Builder(context, Fcm_Notifi_SilentChannel_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(largeIcon)
                        .setContentTitle(jsonObject.getString("title"))
                        .setContentText(jsonObject.getString("text"))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setLights(Color.BLUE, 3000, 3000)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(jsonObject.getString("text"))
                                .setBigContentTitle(jsonObject.getString("title"))
                                .setSummaryText(jsonObject.getString("text")));

                notificationManager.notify(Integer.parseInt(jsonObject.getString("notif_id")),notification.build());

            } catch (JSONException e) {
                e.printStackTrace();
                String error = Log.getStackTraceString(e);
                log("showFcmNotif_ForAmountReceivedOrApproved_JSONException",error);
                sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","showFcmNotif_ForAmountReceivedOrApproved_JSONException",jsonObject.toString(),error);
            }

        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            log("showFcmNotif_ForAmountReceivedOrApproved_Exception",error);
            sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","showFcmNotif_ForAmountReceivedOrApproved_Exception",jsonObject.toString(),error);

        }
    }

    public void showRemainderNotification(Context context, RemainderModel model)
    {
        try
        {
            Intent myIntent = new Intent(context, SplashScreen.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, GoodWill_Notifi_Channel_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(model.getTitle())
                    .setContentText(model.getText())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setSound(getAppNotifSound())
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setLights(Color.BLUE, 3000, 3000)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(model.getText())
                            .setBigContentTitle(model.getTitle())
                            .setSummaryText(model.getText()));

            notificationManager.notify(model.getId(),notification.build());
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            log("showRemainderNotification_Exception",error);
            sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","showRemainderNotification_Exception","text: "+model.getText(),error);
        }
    }

    public void showNewUserNotification(Context context, ContactModel model)
    {
        try
        {
            String title = "New contact found";
            String text = model.getContactName()+" is now on "+context.getResources().getString(R.string.application_name)+"!";

            Intent myIntent = new Intent(context, Contactlist.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, GoodWill_Notifi_Channel_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setSound(getAppNotifSound())
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setLights(Color.BLUE, 3000, 3000)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(text)
                            .setBigContentTitle(title)
                            .setSummaryText(text));

            notificationManager.notify(model.getId(),notification.build());
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            log("showNewUserNotification_Exception",error);
            sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","showNewUserNotification_Exception","userid: "+globalClass.getuserid(),error);
        }
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
                        log(TAG,"dataSnapshot data: "+dataSnapshot.getValue());
                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                        {
                            String getkey = dataSnapshot1.getKey();
                            String getvalue = dataSnapshot1.getValue().toString();
                            setRefreshAllStringData(getkey,getvalue);
                        }
                    }
                    else
                    {
                        log(TAG,"dataSnapshot not exist");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    String error = Log.getStackTraceString(databaseError.toException());
                    log(TAG,"getFCMData_onCancelled"+error);
                    sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","getFCMData_onCancelled","",error);
                }
            });
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            log(TAG,"getFCMData_Exception"+error);
            sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","getFCMData_Exception","",error);
        }
    }

    //todo for AppUpdate
    public void appUpdateList(final Context context)
    {
        if(!isInternetPresent())
        {
            return;
        }

        final String url = getUrl()+"appUpdateList";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", getuserid());
        params.put("versionCode", String.valueOf(BuildConfig.VERSION_CODE));

        new VolleyApiCall(context).Volley_POST(TAG,params,url, new VolleyApiCall.VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {

                log("appUpdateList_RES",result);

                if(!result.equalsIgnoreCase(context.getResources().getString(R.string.onError)))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.getString(context.getResources().getString(R.string.status));
                        String message = jsonObject.getString(context.getResources().getString(R.string.message));

                        if(status.equalsIgnoreCase(context.getResources().getString(R.string.success)))
                        {
                            JSONArray data_jsonArray = jsonObject.getJSONArray("data");
                            for(int i=0;i<data_jsonArray.length();i++)
                            {
                                JSONObject data_object = data_jsonArray.getJSONObject(i);
                                int versionCode = data_object.getInt("versionCode");
                                String versionName = data_object.getString("versionName");
                                String shortdescription = data_object.getString("shortdescription");
                                int forceupdate = data_object.getInt("forceupdate");

                                if(versionCode == BuildConfig.VERSION_CODE && forceupdate == 1)
                                {
                                    log(TAG,"Update is required!");
                                    appUpdateDialogue(context);
                                }
                            }
                        }
                        else
                        {
                            if(message.contains("Error"))
                            {
                                toast_long(context.getResources().getString(R.string.errorInFunction_string));
                                sendLog(Globalclass.ErrorApiCall,TAG,url,"appUpdateList_RES_ErrorInFunction",params.toString(),message);
                            }
                            else if(!message.equalsIgnoreCase("No data found"))
                            {
                                sendLog(Globalclass.ErrorApiCall,TAG,url,"appUpdateList_RES_ErrorInFunction",params.toString(),message);
                            }
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();

                        String error = Log.getStackTraceString(e);
                        log("appUpdateList_JSONException",error);
                        toast_long(context.getResources().getString(R.string.JSONException_string));
                        sendLog(Globalclass.ErrorApiCall,TAG,url,"appUpdateList_JSONException",params.toString(),error);
                    }
                }
            }
        });
    }

    void appUpdateDialogue(Context context)
    {
        new MaterialAlertDialogBuilder(context, R.style.RoundShapeTheme)
                .setTitle("Update required")
                .setMessage("Please update the app to proceed further!")
                .setCancelable(false)
                .setPositiveButton("Update now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .show();
    }

    //todo take Screenshot
    public Bitmap takeScreenShot(View view)
    {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public File storeScreenShot(Bitmap bm)
    {
        File getScreenshotspath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+
                Globalclass.Screenshotspath);
        File dir = new File(getScreenshotspath.getAbsolutePath());
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(getScreenshotspath.getAbsolutePath(), "screenshot_"+timeStamp+".png");
        try
        {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
