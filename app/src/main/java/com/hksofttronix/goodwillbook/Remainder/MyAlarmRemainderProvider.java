package com.hksofttronix.goodwillbook.Remainder;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;

public class MyAlarmRemainderProvider extends ContentProvider {

    String TAG = this.getClass().getSimpleName();
    Globalclass globalclass;

    private static final int REMINDER = 100;

    private static final int REMINDER_ID = 101;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        sUriMatcher.addURI(AlarmReminderContract.CONTENT_AUTHORITY, AlarmReminderContract.PATH_VEHICLE, REMINDER);
        sUriMatcher.addURI(AlarmReminderContract.CONTENT_AUTHORITY, AlarmReminderContract.PATH_VEHICLE_RefreshAll, REMINDER);

        sUriMatcher.addURI(AlarmReminderContract.CONTENT_AUTHORITY, AlarmReminderContract.PATH_VEHICLE + "/#", REMINDER_ID);
        sUriMatcher.addURI(AlarmReminderContract.CONTENT_AUTHORITY, AlarmReminderContract.PATH_VEHICLE_RefreshAll + "/#", REMINDER_ID);
    }

    Mydatabase mydatabase;

    @Override
    public boolean onCreate() {

        globalclass = Globalclass.getInstance(getContext());
        mydatabase = Mydatabase.getInstance(getContext());
        globalclass.log(TAG,"onCreate");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mydatabase.getReadableDatabase();
        Cursor cursor = null;

        try
        {
            int match = sUriMatcher.match(uri);
            if(match == REMINDER)
            {
                cursor = database.query(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

            }
            else if(match == REMINDER_ID)
            {
                selection = AlarmReminderContract.AlarmReminderEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
            }
            else
            {
                String error = "Cannot query unknown URI " + uri;
                globalclass.log("Cursor_query_Exception",error);
                globalclass.sendLog(TAG,TAG,"","Cursor_query_Exception","",error);
            }
        }
        catch (Exception e)
        {
            String error = Log.getStackTraceString(e);
            globalclass.log("Cursor_query_Exception",error);
            globalclass.sendLog(TAG,TAG,"","Cursor_query_Exception","",error);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);
        if(match == REMINDER)
        {
            return AlarmReminderContract.AlarmReminderEntry.CONTENT_LIST_TYPE;
        }
        else if(match == REMINDER_ID)
        {
            return AlarmReminderContract.AlarmReminderEntry.CONTENT_ITEM_TYPE;
        }
        else
        {
            String error = "Unknown URI " + uri + " with match " + match;
            globalclass.log(TAG,error);
            globalclass.sendLog(TAG,TAG,"","getType_Exception","",error);
            return "Unknown URI " + uri + " with match " + match;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        if(match == REMINDER)
        {
            return insertReminderInDB(uri,values);
        }
        else
        {
            String error = "Insertion is not supported for " + uri;
            globalclass.log(TAG,error);
            globalclass.sendLog(TAG,TAG,"","insert_Exception","",error);
            return uri;
        }
    }

    private Uri insertReminderInDB(Uri uri, ContentValues values) {

        long id = 0;

        if(values != null)
        {
            SQLiteDatabase database = mydatabase.getWritableDatabase();

            id = database.insert(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, null, values);

            if (id == -1) {

                String error = "Failed to insertReminderInDB row for " + uri;
                globalclass.log(TAG,error);
                globalclass.sendLog(TAG,TAG,"","insertReminderInDB_Exception","",error);

                return null;
            }

            getContext().getContentResolver().notifyChange(uri, null);
        }
        else
        {
            return ContentUris.withAppendedId(uri, 1);
        }


        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mydatabase.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        if(match == REMINDER)
        {
            rowsDeleted = database.delete(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, selection, selectionArgs);
        }
        else if(match == REMINDER_ID)
        {
            selection = AlarmReminderContract.AlarmReminderEntry._ID + "=?";
            selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
            rowsDeleted = database.delete(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, selection, selectionArgs);
        }
        else
        {
            String error = "Deletion is not supported for " + uri;
            globalclass.log(TAG,error);
            globalclass.sendLog(TAG,TAG,"","delete_Exception","",error);

            return -1;

        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        if(match == REMINDER)
        {
            return updateReminder(uri, values, selection, selectionArgs);
        }
        else if(match == REMINDER_ID)
        {
            selection = AlarmReminderContract.AlarmReminderEntry._ID + "=?";
            selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
            return updateReminder(uri, values, selection, selectionArgs);
        }
        else
        {
            String error = "update is not supported for " + uri;
            globalclass.log(TAG,error);
            globalclass.sendLog(TAG,TAG,"","update_Exception","",error);

            return -1;
        }
    }

    private int updateReminder(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mydatabase.getWritableDatabase();

        int rowsUpdated = database.update(AlarmReminderContract.AlarmReminderEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
