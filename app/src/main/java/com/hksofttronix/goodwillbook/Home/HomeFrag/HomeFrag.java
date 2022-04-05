package com.hksofttronix.goodwillbook.Home.HomeFrag;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hksofttronix.goodwillbook.AddBusiness.addBusinessModel;
import com.hksofttronix.goodwillbook.ContactList.Contactlist;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.MainActivity.MainActivity;
import com.hksofttronix.goodwillbook.MainActivity.allBusinessList_adapter;
import com.hksofttronix.goodwillbook.MainActivity.onClick;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Transaction.Transaction;
import com.hksofttronix.goodwillbook.VolleyUtil.common.VolleyApiCall;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;
import com.wangjie.rapidfloatingactionbutton.util.RFABShape;
import com.wangjie.rapidfloatingactionbutton.util.RFABTextUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFrag extends Fragment implements RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener
{
    public HomeFrag() {
        // Required empty public constructor
    }

    String TAG = this.getClass().getSimpleName();

    Globalclass globalclass;
    Mydatabase mydatabase;
    VolleyApiCall volleyApiCall;
    AppCompatActivity activity;

    View myview;

    ArrayList<HomeFragModel> homearrayList = new ArrayList();

    ArrayList<addBusinessModel> activeBusinessAC_arrayList = new ArrayList<>();
    ArrayList<addBusinessModel> arrayList = new ArrayList<>();
    allBusinessList_adapter adapter;

    Toolbar toolbar;
    RelativeLayout activebusinesslo;
    CircleImageView activebusinessicon;
    TextView activehomebusinessname;

    LinearLayout mainlo;
    RelativeLayout nodatafoundlo;
    TextView tvremovefilter;
    RecyclerView recyclerview;
    TextView tvamountreceive,tvamountgive;

    List<RFACLabelItem> fabitems = new ArrayList<>();
    RapidFloatingActionLayout fabmenuLayout;
    RapidFloatingActionButton fabmenuButton;
    RapidFloatingActionHelper fabmenuHelper;

    MenuItem menu_searchcontact;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof AppCompatActivity)
        {
            activity  = (AppCompatActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myview = view;
        init();
        binding();
        onClick();
        setToolbar();
    }

    void init()
    {
        globalclass  = Globalclass.getInstance(activity);
        mydatabase  = Mydatabase.getInstance(activity);
        volleyApiCall = new VolleyApiCall(activity);

        globalclass.setStringData("homefilteroption","");
    }

    void binding()
    {
        toolbar =  myview.findViewById(R.id.toolbar);
        activebusinesslo = myview.findViewById(R.id.activebusinesslo);
        activebusinessicon = myview.findViewById(R.id.activebusinessicon);
        activehomebusinessname = myview.findViewById(R.id.activehomebusinessname);

        mainlo = myview.findViewById(R.id.mainlo);
        nodatafoundlo = myview.findViewById(R.id.nodatafoundlo);
        tvremovefilter = myview.findViewById(R.id.tvremovefilter);
        recyclerview = myview.findViewById(R.id.recyclerview);
        tvamountreceive = myview.findViewById(R.id.tvamountreceive);
        tvamountgive = myview.findViewById(R.id.tvamountgive);

        fabmenuLayout = myview.findViewById(R.id.fabmenuLayout);
        fabmenuButton = myview.findViewById(R.id.fabmenuButton);
    }

    void onClick()
    {
        activebusinesslo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAllBusinessList_BS();
            }
        });

        tvremovefilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.setStringData("homefilteroption","");
                getData("","");
                tvremovefilter.setVisibility(View.GONE);
            }
        });
    }

    void setText()
    {
        activeBusinessAC_arrayList = mydatabase.getBusinessInfo(Integer.parseInt(globalclass.getActivebusinessacid()));
        if(!activeBusinessAC_arrayList.isEmpty())
        {
            activehomebusinessname.setText(activeBusinessAC_arrayList.get(0).getName());

            if(activeBusinessAC_arrayList.get(0).getLogourl() == null)
            {
                activebusinessicon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher_round));
            }
            else
            {
                if(!globalclass.checknull(activeBusinessAC_arrayList.get(0).getLogourl()).equalsIgnoreCase(""))
                {
                    setActivebusinessicon(globalclass.getImageUrl()+activeBusinessAC_arrayList.get(0).getLogourl());
                }
                else
                {
                    activebusinessicon.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher_round));
                }
            }
        }
    }

    void setToolbar()
    {
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
    }

    void setFab_menu()
    {
        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(activity);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        fabitems.clear();
        fabitems = new ArrayList<>();

        if(!homearrayList.isEmpty())
        {
            fabitems.add(new RFACLabelItem<Integer>()
                    .setLabel(activity.getResources().getString(R.string.sort_or_filter))
                    .setDrawable(getResources().getDrawable(R.drawable.ic_filter_white))
                    .setIconNormalColor(0xffd84315)
                    .setIconPressedColor(0xffbf360c)
                    .setLabelColor(Color.WHITE)
                    .setLabelSizeSp(14)
                    .setLabelBackgroundDrawable(RFABShape.generateCornerShapeDrawable(0xaa000000, RFABTextUtil.dip2px(activity, 4)))
                    .setWrapper(1)
            );
        }
        fabitems.add(new RFACLabelItem<Integer>()
                .setLabel(activity.getResources().getString(R.string.contact_list))
                .setDrawable(getResources().getDrawable(R.drawable.ic_contactlist_white))
                .setIconNormalColor(0xff056f00)
                .setIconPressedColor(0xff0d5302)
                .setLabelColor(Color.WHITE)
                .setLabelSizeSp(14)
                .setLabelBackgroundDrawable(RFABShape.generateCornerShapeDrawable(0xaa000000, RFABTextUtil.dip2px(activity, 4)))
                .setWrapper(1)
        );

        rfaContent
                .setItems(fabitems)
                .setIconShadowRadius(RFABTextUtil.dip2px(activity, 5))
                .setIconShadowColor(0xff888888)
                .setIconShadowDy(RFABTextUtil.dip2px(activity, 5))
        ;

        fabmenuHelper = new RapidFloatingActionHelper(
                activity,
                fabmenuLayout,
                fabmenuButton,
                rfaContent
        ).build();
    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem item) {

        onFabMenuClick(item);
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem item) {

        onFabMenuClick(item);
    }

    void onFabMenuClick(RFACLabelItem item)
    {
        if(item.getLabel().equalsIgnoreCase(activity.getResources().getString(R.string.sort_or_filter)))
        {
            showHomeFilter_BS();
        }
        else if(item.getLabel().equalsIgnoreCase(activity.getResources().getString(R.string.contact_list)))
        {
            startActivity(new Intent(activity, Contactlist.class));
        }

        fabmenuHelper.toggleContent();
    }

    @SuppressLint("ClickableViewAccessibility")
    void showHomeFilter_BS()
    {
        final Dialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.homefilter_bs);

        final RadioGroup radiogroupfilter = dialog.findViewById(R.id.radiogroupfilter);
        RadioButton radiobtreceivables = dialog.findViewById(R.id.radiobtreceivables);
        RadioButton radiobtpayables = dialog.findViewById(R.id.radiobtpayables);
        RadioButton radiobtsettled = dialog.findViewById(R.id.radiobtsettled);

        if(!globalclass.checknull(globalclass.getStringData("homefilteroption")).equalsIgnoreCase(""))
        {
            if(globalclass.getStringData("homefilteroption").equalsIgnoreCase("radiobtreceivables"))
            {
                radiogroupfilter.check(radiogroupfilter.getChildAt(0).getId());
            }
            else if(globalclass.getStringData("homefilteroption").equalsIgnoreCase("radiobtpayables"))
            {
                radiogroupfilter.check(radiogroupfilter.getChildAt(1).getId());
            }
            else if(globalclass.getStringData("homefilteroption").equalsIgnoreCase("radiobtsettled"))
            {
                radiogroupfilter.check(radiogroupfilter.getChildAt(2).getId());
            }
        }

        radiobtreceivables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.setStringData("homefilteroption","radiobtreceivables");
                getData("",">");
                tvremovefilter.setVisibility(View.VISIBLE);
                dialog.dismiss();

            }
        });

        radiobtpayables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.setStringData("homefilteroption","radiobtpayables");
                getData("","<");
                tvremovefilter.setVisibility(View.VISIBLE);
                dialog.dismiss();


            }
        });

        radiobtsettled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                globalclass.setStringData("homefilteroption","radiobtsettled");
                getData("","==");
                tvremovefilter.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    void showAllBusinessList_BS()
    {
        final Dialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(R.layout.allbusinesslist_bs);

        // set the custom dialog components - text, image and button
        RecyclerView recyclerview =  dialog.findViewById(R.id.recyclerview);
        arrayList.clear();
        arrayList = mydatabase.getAllBusinessList();
        adapter = new allBusinessList_adapter(activity, arrayList, new onClick() {
            @Override
            public void onclickEvent(int position, addBusinessModel businessModel) {

                globalclass.setIntData("businessacid",businessModel.getBusinessacid());
                setText();
                getData("","");
                setFab_menu();
                dialog.dismiss();
            }
        });

        recyclerview.setLayoutManager(new LinearLayoutManager(activity));
        recyclerview.setAdapter(adapter);

        dialog.show();
    }

    void setActivebusinessicon(final String getactivebusinessicon)
    {
        Glide
                .with(activity)
                .load(getactivebusinessicon)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .fitCenter())
                .addListener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                        globalclass.toast_short(getResources().getString(R.string.Glide_onLoadFailed));
                        globalclass.log(TAG, "Glide_onLoadFailed");
                        String error = Log.getStackTraceString(e);
                        globalclass.sendLog(Globalclass.GlideException,TAG,globalclass.getImageUrl()+getactivebusinessicon,"Glide_onLoadFailed","",error);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {


                        return false;
                    }
                })
                .into(activebusinessicon);

    }

    void getData(String text, String sort_filter)
    {
        showprogress();

        mydatabase.addDataToHomeMaster(globalclass.getActivebusinessacid());
        homearrayList.clear();
        homearrayList = mydatabase.getHomeList(text,sort_filter);

        if(!homearrayList.isEmpty())
        {
            nodatafoundlo.setVisibility(View.GONE);
            mainlo.setVisibility(View.VISIBLE);
            if(menu_searchcontact !=null)
            {
                menu_searchcontact.setVisible(true);
            }

            setAdapter();
            int receiveSum = mydatabase.getHomeSumBusinessWise(globalclass.getActivebusinessacid(),">");
            int giveSum = mydatabase.getHomeSumBusinessWise(globalclass.getActivebusinessacid(),"<");
            globalclass.log(TAG,"receiveSum: "+receiveSum+" & giveSum: "+giveSum);

            tvamountreceive.setText("₹ "+String.valueOf(receiveSum));
            tvamountgive.setText("₹ "+String.valueOf(giveSum).replace("-",""));
        }
        else
        {
            mainlo.setVisibility(View.GONE);
            if(menu_searchcontact !=null)
            {
                menu_searchcontact.setVisible(false);
            }

            nodatafoundlo.setVisibility(View.VISIBLE);
        }

        hideprogress();
    }

    void setAdapter()
    {
        HomeFrag_adapter adapter = new HomeFrag_adapter(activity, homearrayList, new HomeFragOnClick() {
            @Override
            public void viewTransactions(int position, HomeFragModel model) {

                Intent intent = new Intent(activity, Transaction.class);
                intent.putExtra("mobilenumber",model.getMobilenumber());
                startActivity(intent);
            }
        });
        recyclerview.setLayoutManager(new LinearLayoutManager(activity));
        recyclerview.setAdapter(adapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home, menu);

        menu_searchcontact = menu.findItem(R.id.menu_searchcontact);
        if(!homearrayList.isEmpty())
        {
            menu_searchcontact.setVisible(true);
            SearchView searchView = (SearchView) menu_searchcontact.getActionView();
            searchView.setQueryHint("by name");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

//                globalclass.toast_short("onQueryTextSubmit");
                    getData(query,"");
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    globalclass.setStringData("homefilteroption","");
                    tvremovefilter.setVisibility(View.GONE);

//                globalclass.toast_short("onQueryTextChange");
                    if(newText.length() > 0)
                    {
                        getData(newText,"");
                    }
                    else
                    {
                        getData("","");
                    }

                    return false;
                }
            });
        }
        else
        {
            menu_searchcontact.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId())
        {
            case R.id.menu_refresh:

                if(!globalclass.isInternetPresent())
                {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return true;
                }

                globalclass.RefreshAll(true);
                getFCMDataForEmergencyValues();

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void getFCMDataForEmergencyValues()
    {
        if(!globalclass.isInternetPresent())
        {
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
                ((MainActivity)getActivity()).finishAffinity();
                ((MainActivity)getActivity()).finish();
            }
        });
        dialog.show();
    }

    void showprogress()
    {
        RelativeLayout progresslo = myview.findViewById(R.id.progresslo);

        if (progresslo.getVisibility() != View.VISIBLE) {
            progresslo.setVisibility(View.VISIBLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, //disable all ContactOnClick/onTouch
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    void hideprogress()
    {
        RelativeLayout progresslo = myview.findViewById(R.id.progresslo);
        if (progresslo.getVisibility() == View.VISIBLE) {
            progresslo.setVisibility(View.GONE);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE); //enable all ContactOnClick/onTouch
        }
    }

    public BroadcastReceiver NotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try
            {
                globalclass.log(TAG,"NotificationReceiver: onReceive");


                getData("","");
                globalclass.setStringData("homefilteroption","");
                tvremovefilter.setVisibility(View.GONE);
                setFab_menu();
            }
            catch (Exception e)
            {
                String error = Log.getStackTraceString(e);
                globalclass.log(TAG,"NotificationReceiver: onReceive\n"+error);
                globalclass.sendLog(Globalclass.ErrorInSendingFcmNotification,TAG,"","NotificationReceiver: onReceive","",error);
            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();

        setText();

        if(globalclass.checknull(globalclass.getStringData("homefilteroption")).equalsIgnoreCase(""))
        {
            getData("","");
        }

        setFab_menu();
        LocalBroadcastManager.getInstance(activity).registerReceiver(
                NotificationReceiver, new IntentFilter(Globalclass.NotificationReceiver));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(NotificationReceiver);
    }
}
