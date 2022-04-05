package com.hksofttronix.goodwillbook.Home.HomeFrag;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.R;

import java.util.ArrayList;


public class HomeFrag_adapter extends RecyclerView.Adapter<HomeFrag_adapter.RecyclerViewHolders> {

    String TAG = this.getClass().getSimpleName();
    private ArrayList<HomeFragModel> arrayList;
    private Context context;
    HomeFragOnClick onclick;
    Globalclass globalclass;

    public HomeFrag_adapter(Context context, ArrayList<HomeFragModel> arrayList, HomeFragOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalclass = Globalclass.getInstance(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.homelist_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, final int position) {

        final int pos = holder.getAdapterPosition();
        final HomeFragModel model = arrayList.get(pos);

        holder.contactname.setText(model.getContactname());

        //todo setImageView drawable
        ColorGenerator generator = ColorGenerator.MATERIAL;
        final int randomcolor = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.SANS_SERIF).bold()
                .toUpperCase()
                .endConfig()
                .buildRoundRect(model.getContactname().substring(0,1), randomcolor,10);

        if(model.getIsverified() == 1)
        {
            holder.contactimage.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_launcher));
        }
        else
        {
            holder.contactimage.setImageDrawable(drawable);
        }

        //todo setText give receive
        int sum = model.getGive_receive_sum();
        if(sum > 0)
        {
            holder.tvgivereceive.setText(context.getResources().getString(R.string.will_receive));
            holder.tvgivereceiveamount.setText("₹ "+String.valueOf(sum));
            holder.tvgivereceiveamount.setTextColor(context.getResources().getColor(R.color.mgreen));
        }
        else if(sum < 0)
        {
            holder.tvgivereceive.setText(context.getResources().getString(R.string.to_give));
            holder.tvgivereceiveamount.setText("₹ "+String.valueOf(sum).replace("-",""));
            holder.tvgivereceiveamount.setTextColor(context.getResources().getColor(R.color.mred));
        }
        else if(sum == 0)
        {
            holder.tvgivereceive.setText(context.getResources().getString(R.string.settled));
            holder.tvgivereceiveamount.setText("₹ "+String.valueOf(sum));
            holder.tvgivereceiveamount.setTextColor(context.getResources().getColor(R.color.mblack));
        }

        if(pos == getItemCount()-1) //hide last view
        {
            holder.view.setVisibility(View.GONE);
        }

        holder.detail_lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onclick.viewTransactions(position,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder
    {
        RelativeLayout detail_lo;
        ImageView contactimage;
        TextView contactname,tvgivereceive,tvgivereceiveamount;
        View view;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            detail_lo = itemView.findViewById(R.id.detail_lo);
            contactimage = itemView.findViewById(R.id.contactimage);
            contactname = itemView.findViewById(R.id.contactname);
            tvgivereceive = itemView.findViewById(R.id.tvgivereceive);
            tvgivereceiveamount = itemView.findViewById(R.id.tvgivereceiveamount);
            view = itemView.findViewById(R.id.view);
        }
    }
}
