package com.hksofttronix.goodwillbook.Pay;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hksofttronix.goodwillbook.AddBusiness.addBusinessModel;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BusinessSpinner_adapter extends BaseAdapter {

    String TAG = this.getClass().getSimpleName();
    Context context;
    ArrayList<addBusinessModel> arrayList;
    LayoutInflater inflter;

    Globalclass globalclass;

    public BusinessSpinner_adapter(Context applicationContext, ArrayList<addBusinessModel> arrayList) {
        this.context = applicationContext;
        this.arrayList = arrayList;
        inflter = (LayoutInflater.from(applicationContext));
        globalclass = Globalclass.getInstance(context);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        view = inflter.inflate(R.layout.businessspinner_layout, null);

        CircleImageView businessicon = view.findViewById(R.id.businessicon);
        TextView businessname = view.findViewById(R.id.businessname);

        businessname.setText(arrayList.get(position).getName());
        if(arrayList.get(position).getLogourl() != null)
        {
            if(!globalclass.checknull(arrayList.get(position).getLogourl()).equalsIgnoreCase(""))
            {
                Glide
                        .with(context)
                        .load(globalclass.getImageUrl()+arrayList.get(position).getLogourl())
                        .apply(new RequestOptions()
                                .placeholder(R.mipmap.ic_launcher)
                                .error(R.mipmap.ic_launcher)
                                .fitCenter())
                        .addListener(new RequestListener<Drawable>() {

                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                globalclass.toast_short(context.getResources().getString(R.string.Glide_onLoadFailed));
                                globalclass.log(TAG, "Glide_onLoadFailed");
                                String error = Log.getStackTraceString(e);
                                globalclass.sendLog(Globalclass.GlideException,TAG,globalclass.getImageUrl()+arrayList.get(position).getLogourl(),"Glide_onLoadFailed","",error);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(businessicon);

            }

        }


        return view;
    }
}
