package com.hksofttronix.goodwillbook.Remainder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hksofttronix.goodwillbook.Globalclass;
import com.hksofttronix.goodwillbook.Mydatabase;
import com.hksofttronix.goodwillbook.R;

import java.util.ArrayList;

import static com.hksofttronix.goodwillbook.Globalclass.MyAlarmRemainderProvider;
import static com.hksofttronix.goodwillbook.Remainder.AlarmReminderContract.AlarmReminderEntry.CONTENT_URI;

public class RemainderList extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = RemainderList.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    RecyclerView recyclerview;
    RelativeLayout nodatafoundlo;

    ArrayList<RemainderModel> arrayList = new ArrayList<>();
    Remainder_adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remainder_list);

        init();
        binding();
        setToolbar();
        getData();
    }

    void init()
    {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
//        volleyApiCall = new VolleyApiCall(activity);
    }

    void binding()
    {
        recyclerview = findViewById(R.id.recyclerview);
        nodatafoundlo = findViewById(R.id.nodatafoundlo);
    }

    void setToolbar()
    {
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getData()
    {
        arrayList = mydatabase.getRemainderList();
        if(!arrayList.isEmpty())
        {
            nodatafoundlo.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);

            adapter = new Remainder_adapter(activity,arrayList);
            recyclerview.setLayoutManager(new LinearLayoutManager(this));
            recyclerview.setAdapter(adapter);
            SwipeDelete();
        }
        else
        {
            recyclerview.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);
        }
    }

    void SwipeDelete()
    {
        final Paint p = new Paint();

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                RemainderModel model = arrayList.get(viewHolder.getAdapterPosition());
                int position = viewHolder.getAdapterPosition();

                if (direction == ItemTouchHelper.LEFT)
                {
                    ConfirmRemoveRemainderdialogue(model,viewHolder,position);
                }
                else if (direction == ItemTouchHelper.RIGHT)
                {
                    Intent intent = new Intent(activity, AddRemainder.class);
                    intent.putExtra("mobilenumber",model.getMobilenumber());
                    startActivity(intent);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        p.setColor(Color.parseColor("#9E9E9E"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);
//                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_edit);
                        icon = globalclass.drawableToBitmap(getResources().getColor(R.color.mwhite),getResources().getDrawable(R.drawable.ic_edit));
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    } else if(dX < 0){
                        p.setColor(Color.parseColor("#F44336"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
//                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete);
                        icon = globalclass.drawableToBitmap(getResources().getColor(R.color.mwhite),getResources().getDrawable(R.drawable.ic_delete));
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest,p);
                    }
                }


            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerview);
    }

    void ConfirmRemoveRemainderdialogue(final RemainderModel model, final RecyclerView.ViewHolder viewHolder, final int position)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure remove ?")
                .setCancelable(false)
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                        Uri removedUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(model.getId()));
                        int rowsDeleted = getContentResolver().delete(removedUri, null, null);
                        globalclass.cancelRemainderAlarm(getApplicationContext(), Globalclass.Remainder_ACTION,Globalclass.Remainder_requestID, removedUri);

                        if (rowsDeleted == 0)
                        {
                            // fail or error occured
                            String error = "Unable to delete remainder";
                            globalclass.log(TAG,error);
                            globalclass.sendLog(MyAlarmRemainderProvider,TAG,"","ConfirmRemoveRemainderdialogue","",error);
                            globalclass.toast_long("Unable to remove remainder, due to internal error, please try again later!");
                            onBackPressed();
                        }
                        else
                        {
                            //success
                            globalclass.log(TAG,"Deleted Remainder Uri: "+removedUri);
                            adapter.removeItem(position);
                            globalclass.snackit(activity,"Remiander has been removed successfully!");
                            show_hide();
                        }
                    }
                })
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        //todo refresh the adapter to prevent hiding the item from UI
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }).show();
    }

    void ShowSnakeBarToUndo(final RemainderModel model, final int position)
    {
        Snackbar snackbar = Snackbar
                .make(activity.findViewById(android.R.id.content), "Removed successfully", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //todo undo deleted item
                adapter.undoItem(model, position);
            }
        });

        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);

                show_hide();
            }
        });

        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    void show_hide()
    {
        if(!arrayList.isEmpty())
        {
            nodatafoundlo.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);
        }
        else
        {
            recyclerview.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }
}
