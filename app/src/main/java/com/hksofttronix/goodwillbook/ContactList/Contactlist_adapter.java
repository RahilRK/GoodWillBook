package com.hksofttronix.goodwillbook.ContactList;

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


public class Contactlist_adapter extends RecyclerView.Adapter<Contactlist_adapter.RecyclerViewHolders> {

    String TAG = this.getClass().getSimpleName();
    private ArrayList<ContactModel> arrayList;
    private Context context;
    ContactOnClick onclick;
    Globalclass globalclass;

    public Contactlist_adapter(Context context, ArrayList<ContactModel> arrayList, ContactOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalclass = Globalclass.getInstance(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contactlist_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final ContactModel contactModel = arrayList.get(pos);

        holder.contactname.setText(contactModel.getContactName());
        holder.contactnumber.setText(contactModel.getViewcontactNumber());

        //todo setImageView drawable
        ColorGenerator generator = ColorGenerator.MATERIAL;
        final int randomcolor = generator.getRandomColor();

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.SANS_SERIF).bold()
                .toUpperCase()
                .endConfig()
                .buildRoundRect(contactModel.getContactName().substring(0,1), randomcolor,10);

        if(contactModel.getIsverified() == 1)
        {
            holder.contactimage.setImageDrawable(context.getResources().getDrawable(R.mipmap.ic_launcher));
        }
        else
        {
            holder.contactimage.setImageDrawable(drawable);
        }

        if(pos == getItemCount()-1) //hide last view
        {
            holder.view.setVisibility(View.GONE);
        }

        holder.contactlist_lo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                contactModel.setRandomcolor(randomcolor);
                onclick.onContactClick(pos,contactModel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder
    {
        RelativeLayout contactlist_lo;
        ImageView contactimage;
        TextView contactname,contactnumber;
        View view;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            contactlist_lo = itemView.findViewById(R.id.contactlist_lo);
            contactimage = itemView.findViewById(R.id.contactimage);
            contactname = itemView.findViewById(R.id.contactname);
            contactnumber = itemView.findViewById(R.id.contactnumber);
            view = itemView.findViewById(R.id.view);
        }
    }
}
