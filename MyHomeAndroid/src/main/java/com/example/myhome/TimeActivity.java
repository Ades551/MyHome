package com.example.myhome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;


public class TimeActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener, TcpCommunication.AsyncResponse {

    String removeData = String.format("command%s", ControlActivity.action); // server side config file
    String getData = String.format("command%s", ControlActivity.action); // config file

    MyRecyclerViewAdapter adapter;
    ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            startActivity(new Intent(this, SetTimeActivity.class));
            finish();
        });

        list = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.DatesTimes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        // send request for date
        TcpCommunication code = new TcpCommunication(this, this);
        code.execute(String.format("%s,%s", getData, SelectActivity.port));


        // for swipe removing
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                alertDialog(position);
            }

            /* ********* NO CLUE ********** */

            /**
             * Should draw red line when swiping
             */
            @Override
            public void onChildDraw (@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Get RecyclerView item from the ViewHolder
                    View itemView = viewHolder.itemView;

                    //Drawable icon = ContextCompat.getDrawable(context, R.drawable.my_icon);

                    Paint p = new Paint();
                    p.setARGB(200, 200, 0, 0);

                    if (dX > 0) {

                        // Draw Rect with varying right side, equal to displacement dX
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                (float) itemView.getBottom(), p);

                    } else {

                        // Draw Rect with varying left side, equal to the item's right side plus negative displacement dX
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                (float) itemView.getRight(), (float) itemView.getBottom(), p);

                    }

                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }
    /* ******* NO CLUE ********** */

    @Override
    public void onItemClick(View view, int position) {
        alertDialog(position); // delete dialog
    }

    public void onBackPressed(){
        // return to previous activity
        super.onBackPressed();
        startActivity(new Intent(this, SelectActivity.class));
        finish();
    }

    /**
     * @brief dialog for removing
     * @param position
     */
    private void alertDialog(int position){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Odstrániť");
        dialog.setMessage("Naozaj si želáte odstrániť toto nastavenie?");
        dialog.setCancelable(false);
        // if removed
        dialog.setPositiveButton("Áno", (dialog1, which) -> {
            TcpCommunication code = new TcpCommunication(this, this);
            System.out.println(position);
            code.execute(String.format("%s,%s,%s", removeData, SelectActivity.port, position));
            list.remove(position); // remove from list
            adapter.notifyDataSetChanged(); // update list

        });
        // if not removed
        dialog.setNegativeButton("Nie", (dialog12, which) -> {
            dialog12.dismiss();
            adapter.notifyDataSetChanged();
        });
        dialog.show();
    }

    /**
     * @brief Set Text for listview in TimeActivity
     * @param text
     */
    public void getTime(String text){
        System.out.println(text);
        if(text != null){
            String[] time = text.split(";", 10);
            for (String s : time) if (!s.isEmpty()) list.add(s);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processFinish(String output) {
        getTime(output);
    }
}
