package com.hksofttronix.goodwillbook.MainActivity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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


public class allBusinessList_adapter extends RecyclerView.Adapter<allBusinessList_adapter.RecyclerViewHolders> {

    String TAG = this.getClass().getSimpleName();
    private ArrayList<addBusinessModel> arrayList;
    private Context context;
    onClick onclick;
    Globalclass globalclass;
    public int lastSelectedPosition = -1;

    public allBusinessList_adapter(Context context, ArrayList<addBusinessModel> arrayList, onClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalclass = Globalclass.getInstance(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.allbusinesslist_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final addBusinessModel businessModel = arrayList.get(pos);

        holder.activehomebusinessname.setChecked(lastSelectedPosition == pos);

        holder.activehomebusinessname.setText(businessModel.getName());
        holder.activehomebusinessname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onclick.onclickEvent(pos,businessModel);
                lastSelectedPosition = pos;
                notifyDataSetChanged();
            }
        });

        if(businessModel.getBusinessacid() == Integer.parseInt(globalclass.getActivebusinessacid()))
        {
            holder.activehomebusinessname.setChecked(true);
        }

        if(businessModel.getLogourl() != null)
        {
            if(!globalclass.checknull(businessModel.getLogourl()).equalsIgnoreCase(""))
            {
                Glide
                        .with(context)
                        .load(globalclass.getImageUrl()+businessModel.getLogourl())
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
                                globalclass.sendLog(Globalclass.GlideException,TAG,globalclass.getImageUrl()+businessModel.getLogourl(),"Glide_onLoadFailed","",error);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {


                                return false;
                            }
                        })
                        .into(holder.activebusinessicon);

            }

        }
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder
    {
        CircleImageView activebusinessicon;
        RadioButton activehomebusinessname;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            activebusinessicon = itemView.findViewById(R.id.activebusinessicon);
            activehomebusinessname = itemView.findViewById(R.id.activehomebusinessname);
        }
    }
}
