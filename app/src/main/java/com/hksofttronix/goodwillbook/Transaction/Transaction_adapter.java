package com.hksofttronix.goodwillbook.Transaction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;

import java.util.ArrayList;


public class Transaction_adapter extends RecyclerView.Adapter<Transaction_adapter.RecyclerViewHolders> {

    String TAG = this.getClass().getSimpleName();
    private ArrayList<transactionModel> arrayList;
    private Context context;
    TransactionOnClick onclick;
    Globalclass globalclass;

    public Transaction_adapter(Context context, ArrayList<transactionModel> arrayList, TransactionOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalclass = Globalclass.getInstance(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final transactionModel model = arrayList.get(pos);

        holder.datetime.setText(globalclass.formatDateTime_DBToUser(model.getDebitdatetime()));
        holder.amount.setText("₹ "+String.valueOf(model.getAmount()));
        if(globalclass.getuserid().equalsIgnoreCase(String.valueOf(model.getDebituserid())))
        {
            holder.amount.setTextColor(context.getResources().getColor(R.color.mred));
        }
        else
        {
            holder.amount.setTextColor(context.getResources().getColor(R.color.mgreen));
        }

        if(model.getIsApproved() == 1)
        {
            holder.ivapproved.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.ivapproved.setVisibility(View.INVISIBLE);
        }

        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onclick.onClick(pos,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder
    {
        MaterialCardView cardview;
        ImageView ivapproved;
        TextView amount,datetime;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardview = itemView.findViewById(R.id.cardview);
            ivapproved = itemView.findViewById(R.id.ivapproved);
            amount = itemView.findViewById(R.id.amount);
            datetime = itemView.findViewById(R.id.datetime);
        }
    }

    public void removeItem(int newPosition) {
        if(arrayList!=null && arrayList.size()>0) {

            arrayList.remove(newPosition);
            notifyItemRemoved(newPosition);
            notifyItemRangeChanged(newPosition, getItemCount());
        }
    }

    public void undoItem(transactionModel model, int position) {
        arrayList.add(position, model);
        notifyItemInserted(position);
    }


}
