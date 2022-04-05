package com.hksofttronix.goodwillbook.ViewAttachments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Pay.addAttachmentsModel;
import com.hksofttronix.goodwillbook.R;

import java.util.ArrayList;

public class ViewAttachments_adapter extends RecyclerView.Adapter<ViewAttachments_adapter.ViewHolder> {

    String TAG = this.getClass().getSimpleName();
    Context context;
    private ArrayList<addAttachmentsModel> arrayList;
    AttachmentOnClick onClick;
    Globalclass globalclass;

    public ViewAttachments_adapter(Context context, ArrayList<addAttachmentsModel> arrayList, AttachmentOnClick onClick) {
        this.context = context;
        this.arrayList = arrayList;
        this.onClick = onClick;
        globalclass = Globalclass.getInstance(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewAttachments_adapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attachphotos_rvitem, null);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        final int pos = viewHolder.getAdapterPosition();
        final addAttachmentsModel model = arrayList.get(pos);

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClick.onClickZoomImg(position,model);
            }
        });
        viewHolder.delete.setVisibility(View.GONE);
        if(!globalclass.checknull(model.getUrl()).equalsIgnoreCase(""))
        {
            Glide
                    .with(context)
                    .load(globalclass.getImageUrl()+model.getUrl())
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
                            globalclass.sendLog(Globalclass.GlideException,TAG,globalclass.getImageUrl()+arrayList.get(position).getUrl(),"Glide_onLoadFailed","",error);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(viewHolder.imageView);
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public ImageView delete;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            imageView = (ImageView) itemLayoutView.findViewById(R.id.imageView);
            delete = (ImageView) itemLayoutView.findViewById(R.id.delete);
        }
    }


    // Return the size of your arrayList (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
