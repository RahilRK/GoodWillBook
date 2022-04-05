package com.hksofttronix.goodwillbook.EditPay;

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

public class EditAttachPhotos_adapter extends RecyclerView.Adapter<EditAttachPhotos_adapter.ViewHolder> {

    String TAG = this.getClass().getSimpleName();
    Context context;
    private ArrayList<addAttachmentsModel> arrayList;
    EditAttachPhotosOnClick onClick;
    Globalclass globalclass;

    public EditAttachPhotos_adapter(Context context, ArrayList<addAttachmentsModel> arrayList, EditAttachPhotosOnClick onClick) {
        this.context = context;
        this.arrayList = arrayList;
        this.onClick = onClick;
        globalclass = Globalclass.getInstance(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public EditAttachPhotos_adapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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

        if(model.getChoosefromStorage())
        {
            Glide.with(context)
                    .load(model.getAttachmentfilepath())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.image_placeholder).centerCrop()
                            .fitCenter())
                    .into(viewHolder.imageView);
        }
        else
        {
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

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onDelete(position,model);
            }
        });
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

    void removeItem(int position)
    {
        arrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void addItem(addAttachmentsModel model) {
        arrayList.add(getItemCount(), model);
        notifyItemInserted(getItemCount());
    }


    // Return the size of your arrayList (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
