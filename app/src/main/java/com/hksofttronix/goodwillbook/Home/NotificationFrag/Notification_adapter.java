package com.hksofttronix.goodwillbook.Home.NotificationFrag;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseBooleanArray;
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
import com.hksofttronix.goodwillbook.VolleyUtil.common.NotificationModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class Notification_adapter extends RecyclerView.Adapter<Notification_adapter.RecyclerViewHolders> {

    String TAG = this.getClass().getSimpleName();
    private ArrayList<NotificationModel> arrayList;
    private Context context;
    NotificationOnClick onclick;
    Globalclass globalclass;
    SparseBooleanArray sparseBooleanArray;

    public Notification_adapter(Context context, ArrayList<NotificationModel> arrayList, NotificationOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalclass = Globalclass.getInstance(context);
        sparseBooleanArray = new SparseBooleanArray();
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notificationlist_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, final int position) {

        final int pos = holder.getAdapterPosition();
        final NotificationModel model = arrayList.get(pos);

        holder.text.setText(model.getText());
        holder.senddatetime.setText(globalclass.formatDateTime_DBToUser(model.getSenddatetime()));

        //todo setImageView drawable
        ColorGenerator generator = ColorGenerator.MATERIAL;
        final int randomcolor = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.SANS_SERIF).bold()
                .toUpperCase()
                .endConfig()
                .buildRound(getTextDrawable_text(model), randomcolor);

        if(model.getisSelected())
        {
            holder.circleiv.setBackground(context.getResources().getDrawable(R.drawable.ic_done));
        }
        else
        {
            holder.circleiv.setBackground(drawable);
        }

        if(model.getHasread() == 0)
        {
            holder.readiv.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.readiv.setVisibility(View.INVISIBLE);
        }

        holder.detail_lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onclick.viewDetail(position,model);
            }
        });

        holder.detail_lo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                onclick.onLongPress(position,model);
                return false;
            }
        });

        if(pos == getItemCount()-1) //hide last view
        {
            holder.view.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder
    {
        RelativeLayout detail_lo;
        CircleImageView circleiv;
        ImageView readiv;
        TextView text, senddatetime;
        View view;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            detail_lo = itemView.findViewById(R.id.detail_lo);
            circleiv = itemView.findViewById(R.id.circleiv);
            readiv = itemView.findViewById(R.id.readiv);
            text = itemView.findViewById(R.id.text);
            senddatetime = itemView.findViewById(R.id.senddatetime);
            view = itemView.findViewById(R.id.view);
        }
    }

    String getTextDrawable_text(NotificationModel model)
    {
        String text = "N";
        if(model.getType().equalsIgnoreCase("Amount received"))
        {
            text = "R";
        }
        else if(model.getType().equalsIgnoreCase("Amount approved"))
        {
            text = "A";
        }
        else if(model.getType().equalsIgnoreCase("Transaction update"))
        {
            text = "U";
        }
        else if(model.getType().equalsIgnoreCase("deleteAttachment"))
        {
            text = "D";
        }

        return text;
    }

    public void removeItem(int newPosition) {
        if(arrayList!=null && arrayList.size()>0) {

            arrayList.remove(newPosition);
            notifyItemRemoved(newPosition);
            notifyItemRangeChanged(newPosition, getItemCount());
        }
    }

    public void AddandRemoveInSparseBooleanArray(int pos)
    {
        if(sparseBooleanArray.get(pos,false))
        {
            sparseBooleanArray.delete(pos);
        }
        else
        {
            sparseBooleanArray.put(pos,true);
        }

        notifyItemChanged(pos);
    }

    public void SelectAll()
    {
        clearSelections();
        for(int i=0;i<arrayList.size();i++)
        {
            AddandRemoveInSparseBooleanArray(i);
            arrayList.get(i).setisSelected(true);
        }
    }

    public void UnSelectAll()
    {
        clearSelections();
        for(int i=0;i<arrayList.size();i++)
        {
            arrayList.get(i).setisSelected(false);
        }
    }

    public void clearSelections() {
        sparseBooleanArray.clear();
        notifyDataSetChanged();
    }

    public int getSparseBooleanArraySize()
    {
        return sparseBooleanArray.size();
    }
}
