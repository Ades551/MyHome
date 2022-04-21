package com.example.myhome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeActivity1 extends AppCompatActivity implements View.OnClickListener, TcpCommunication.AsyncResponse {

    String getData;
    String sendData;
    CardView[] cardViews;
    ImageView[] imageViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home1);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        getData = extras.getString("operation"); // replace operation
        sendData = extras.getString("operation"); // replace operation
        String[] texts = extras.getStringArray("text");
        int[] icon_id = extras.getIntArray("icons");

        cardViews = new CardView[extras.getInt("length")];
        imageViews = new ImageView[cardViews.length];
        TextView[] text = new TextView[cardViews.length];
        ImageView[] icons = new ImageView[cardViews.length];

        GridLayout gridLayout = findViewById(R.id.gridSwitch);
        for(int i = 0; i < gridLayout.getChildCount(); i++) {
            View view = gridLayout.getChildAt(i);
            view.setVisibility(View.GONE);
        }


        // initialize elements
        for(int i = 0; i < cardViews.length; i++) {
            View view = gridLayout.getChildAt(i + 1); // card view
            cardViews[i] = findViewById(view.getId()); // set switch
            cardViews[i].setVisibility(View.VISIBLE);
            cardViews[i].setOnClickListener(this);

            view = cardViews[i].getChildAt(0); // linear layout
            LinearLayout linearLayout = findViewById(view.getId()); // set linear layout to view
            icons[i] = findViewById(linearLayout.getChildAt(0).getId());
            icons[i].setImageResource(icon_id[i]);
            linearLayout = findViewById(linearLayout.getChildAt(1).getId());
            text[i] = findViewById(linearLayout.getChildAt(0).getId()); // set text
            text[i].setText(texts[i]);
            imageViews[i] = findViewById(linearLayout.getChildAt(1).getId()); // set imageview
        }

        // request data
        TcpCommunication code = new TcpCommunication(this,this);
        code.execute(getData);
    }

    // send data
    public void sendStatus(int id, int status){
        String message = String.format("%s,%s,%s", sendData, id, status);
        TcpCommunication code = new TcpCommunication(this,this);
        code.execute(message);
    }

    @Override
    public void onClick(View v) {
        for(int i = 0; i < cardViews.length; i++){
            if(cardViews[i].getId() == v.getId()){
                if(imageViews[i].getVisibility() == View.VISIBLE){
                    imageViews[i].setVisibility(View.INVISIBLE);
                    sendStatus(i, 0);
                } else {
                    imageViews[i].setVisibility(View.VISIBLE);
                    sendStatus(i, 1);
                }
            }
        }
    }

    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void setHome(String string){
        System.out.println(string);
        String[] array = string.split(",", 5);
        for(int i = 0; i < cardViews.length; i++) {
            if(Integer.parseInt(array[i]) == 1) imageViews[i].setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void processFinish(String output) {
        setHome(output);
    }
}
