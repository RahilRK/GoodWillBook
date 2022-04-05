package com.hksofttronix.goodwillbook.Pay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hksofttronix.goodwillbook.R;

import java.util.ArrayList;

public class AttachPhotos_adapter extends RecyclerView.Adapter<AttachPhotos_adapter.ViewHolder> {

    Context context;
    private ArrayList<String> itemsData;
    AttachPhotosOnClick onClick;

    public AttachPhotos_adapter(Context context,ArrayList<String> itemsData,AttachPhotosOnClick onClick) {
        this.context = context;
        this.itemsData = itemsData;
        this.onClick = onClick;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AttachPhotos_adapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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

        Glide.with(context)
                .load(itemsData.get(position))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.image_placeholder).centerCrop()
                        .fitCenter())
                .into(viewHolder.imageView);

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onDelete(position);
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

    void deleteItem(int position)
    {
        itemsData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, itemsData.size());
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return itemsData.size();
    }
}
