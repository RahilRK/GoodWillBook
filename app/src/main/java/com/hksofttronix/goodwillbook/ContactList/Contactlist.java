package com.hksofttronix.goodwillbook.ContactList;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Transaction.Transaction;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class Contactlist extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = Contactlist.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;

    ArrayList<ContactModel> arrayList = new ArrayList();
    RecyclerView recyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);

        init();
        binding();
        setToolbar();
        getData();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        recyclerview = findViewById(R.id.recyclerview);
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

    void getData()
    {
        showprogress();

        arrayList.clear();
        arrayList = mydatabase.getAllContact();

        if(!arrayList.isEmpty())
        {
            globalclass.log(TAG,"Total "+arrayList.size()+" contacts are available!");
            setAdapter();
//            refreshMenu(activity);
        }
        else
        {
            globalclass.snackit(activity,"No data found");
        }

        hideprogress();
    }

    void searchData(String newText)
    {
        arrayList.clear();
        arrayList = mydatabase.searchContact(newText);

        if(!arrayList.isEmpty())
        {
            globalclass.log(TAG,"Total "+arrayList.size()+" contacts are available!");
            setAdapter();
        }
        else
        {
            globalclass.snackit(activity,"No data found");
        }
    }

    void setAdapter()
    {
        Contactlist_adapter contactAdapter = new Contactlist_adapter(activity, arrayList, new ContactOnClick() {
            @Override
            public void onContactClick(int position, ContactModel contactModel) {

                globalclass.log(TAG,
                        "userid: "+contactModel.getUserid()+
                                ", contactname: "+contactModel.getContactName()+
                                ", contactnumber: "+contactModel.getContactNumber()+
                                ", isverified: "+contactModel.getIsverified()+
                                ", goodwilluser: "+contactModel.getGoodwilluser());

                Intent intent = new Intent(activity, Transaction.class);
                intent.putExtra("mobilenumber",contactModel.getContactNumber());
                startActivity(intent);
            }
        });
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(contactAdapter);
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//
//        globalclass.log(TAG,"onPrepareOptionsMenu");
//
//        MenuItem menu_searchcontact = menu.findItem(R.id.menu_searchcontact);
//        if(!arrayList.isEmpty())
//        {
//            menu_searchcontact.setVisible(true);
//        }
//        else
//        {
//            menu_searchcontact.setVisible(false);
//        }
//
//        return super.onPrepareOptionsMenu(menu);
//    }

//    public static void refreshMenu(Activity activity)
//    {
//        activity.invalidateOptionsMenu();
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contactlist, menu);

        MenuItem menu_searchcontact = menu.findItem(R.id.menu_searchcontact);
        SearchView searchView = (SearchView) menu_searchcontact.getActionView();
        searchView.setQueryHint("by name");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchData(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(newText.length() > 0)
                {
                   searchData(newText);
                }
                else
                {
                    getData();
                }

                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_refresh:

                requestContactPermission();

                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    void requestContactPermission()
    {
        if(!globalclass.isInternetPresent())
        {
            globalclass.toast_short(getResources().getString(R.string.noInternetConnection));
            return;
        }

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response)
                    {
                        globalclass.log(TAG,"Contact permission Granted");
                        showprogress();
                        globalclass.startAnyService(activity,contactList_Service.class);
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response)
                    {
                        globalclass.log(TAG,"Contact permission Denied");
                        globalclass.snackit(activity,"Contact permission required to sync contacts");
                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token)
                    {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {

                        globalclass.log(TAG+"_requestContactPermission",error.toString());
                        globalclass.sendLog(Globalclass.TryCatchException,TAG,"","requestContactPermission","",error.toString());
                    }
                }).check();
    }

    public BroadcastReceiver ContactSyncCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(TAG,"ContactSyncCompleteReceiver: onReceive");
                getData();
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(TAG,"ContactSyncCompleteReceiver: onReceive\n"+error);
                globalclass.sendLog(Globalclass.TryCatchException,TAG,"","ContactSyncCompleteReceiver: onReceive","",error);
            }
        }
    };

    void showprogress()
    {
        RelativeLayout progresslo = findViewById(R.id.progresslo);

        if (progresslo.getVisibility() != View.VISIBLE) {
            progresslo.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, //disable all ContactOnClick/onTouch
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    void hideprogress()
    {
        RelativeLayout progresslo = findViewById(R.id.progresslo);
        if (progresslo.getVisibility() == View.VISIBLE) {
            progresslo.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //enable all ContactOnClick/onTouch
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                ContactSyncCompleteReceiver, new IntentFilter(Globalclass.ContactSyncCompleteReceiver));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(ContactSyncCompleteReceiver);
    }
}
