package com.hksofttronix.goodwillbook.Remainder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;
import com.hksofttronix.goodwillbook.Transaction.TransactionOnClick;
import com.hksofttronix.goodwillbook.Transaction.transactionModel;

import java.util.ArrayList;


public class Remainder_adapter extends RecyclerView.Adapter<Remainder_adapter.RecyclerViewHolders> {

    String TAG = this.getClass().getSimpleName();
    private ArrayList<RemainderModel> arrayList;
    private Context context;
    Globalclass globalclass;

    public Remainder_adapter(Context context, ArrayList<RemainderModel> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
        globalclass = Globalclass.getInstance(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.remainderlist_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final RemainderModel model = arrayList.get(pos);

        holder.title.setText(model.getTitle());
        holder.datetime.setText(globalclass.formatDateTime_DBToUser(model.getDate()+" "+model.getTime()));

        if(model.getActive() == 0)
        {
            holder.ivactive.setVisibility(View.GONE);
        }
        else
        {
            holder.ivactive.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder
    {
        MaterialCardView cardview;
        TextView title,datetime;
        ImageView ivactive;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardview = itemView.findViewById(R.id.cardview);
            title = itemView.findViewById(R.id.title);
            datetime = itemView.findViewById(R.id.datetime);
            ivactive = itemView.findViewById(R.id.ivactive);
        }
    }

    public void removeItem(int newPosition) {
        if(arrayList!=null && arrayList.size()>0) {

            arrayList.remove(newPosition);
            notifyItemRemoved(newPosition);
            notifyItemRangeChanged(newPosition, getItemCount());
        }
    }

    public void undoItem(RemainderModel model, int position) {
        arrayList.add(position, model);
        notifyItemInserted(position);
    }

}
