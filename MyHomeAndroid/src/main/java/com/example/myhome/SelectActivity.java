package com.example.myhome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SelectActivity extends AppCompatActivity implements View.OnClickListener {

    static String port; // selected port "1" - "6" or "all" see server config
    CardView[] cards = new CardView[ControlActivity.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        String cardText = null;

        switch (ControlActivity.action){
            case ("garden"):
                cardText = "Okruh";
                break;
            case ("pool"):
                cardText = "Plug";
                break;
            default:
                System.exit(0);
        }

        TextView[] text = new TextView[ControlActivity.length];

        // set to invisible
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for(int i = 0; i < gridLayout.getChildCount(); i++){
            View view = gridLayout.getChildAt(i);
            view.setVisibility(View.INVISIBLE);
        }

        // initialize elements
        for(int i = 0; i < ControlActivity.length; i++){
            cards[i] = findViewById(gridLayout.getChildAt(i).getId());
            cards[i].setVisibility(View.VISIBLE);
            LinearLayout linearLayout = findViewById(cards[i].getChildAt(0).getId());
            text[i] = findViewById(linearLayout.getChildAt(0).getId());
        }

        text[0].setText("Inteligentné nastavenie");
        for(int i = 1; i < ControlActivity.length; i++)
            text[i].setText(String.format("%s %s", cardText, i));

        // set listener
        for(int i = 0; i < ControlActivity.length; i++) cards[i].setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        for(int i = 0; i < ControlActivity.length; i++){
            if(cards[i].getId() == v.getId()) {
                if (i == 0) port = "all";
                else port = String.valueOf(i);
            }
        }

        if(port.equals("all")) startActivity(new Intent(this, SetTimeActivity.class));
        else startActivity(new Intent(this, TimeActivity.class));
        finish();
    }

    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(this, ControlActivity.class));
        finish();
    }
}