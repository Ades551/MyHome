package com.example.myhome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    CardView[] cardViews = new CardView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        // set every card to invisible
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        for(int i = 0; i < gridLayout.getChildCount(); i++) {
            View view = gridLayout.getChildAt(i);
            view.setVisibility(View.INVISIBLE);
        }

        // cards
        cardViews[0] = findViewById(R.id.listLayout1);
        cardViews[1] = findViewById(R.id.listLayout2);
        cardViews[2] = findViewById(R.id.listLayout3);

        // initialize text
        TextView[] text = new TextView[cardViews.length];
        text[0] = findViewById(R.id.listText1);
        text[1] = findViewById(R.id.listText2);
        text[2] = findViewById(R.id.listText3);

        text[0].setText("Pracovňa");
        text[1].setText("Vonkajšie osvetlenie");

        for(int i = 0; i < cardViews.length; i++){
            cardViews[i].setVisibility(View.VISIBLE); // set card to visible
            text[i].setVisibility(View.VISIBLE); // set text visible
            cardViews[i].setOnClickListener(this); // listener
        }
    }

    @Override
    public void onClick(View v) {
        for(int i = 0; i < cardViews.length; i++){
            if(cardViews[i].getId() == v.getId()){
                Bundle extras = new Bundle();
                if(i == 0){
                    String[] text = {"Lampa", "Plug 1", "Plug 2", "Plug 3"};
                    int[] icons = {R.drawable.lamp, R.drawable.plug, R.drawable.plug, R.drawable.plug};
                    extras.putString("operation", "command"); // replace operation and command
                    extras.putString("operation", "command"); // replace operation and command
                    extras.putStringArray("text", text);
                    extras.putInt("length", text.length);
                    extras.putIntArray("icons", icons);
                }
                else if(i == 1) {
                    String[] text = {"Lampa"};
                    int[] icons = {R.drawable.lamp};
                    extras.putString("operation", "command"); // replace operation and command
                    extras.putString("operation", "command"); // replace operation and command
                    extras.putStringArray("text", text);
                    extras.putInt("length", text.length);
                    extras.putIntArray("icons", icons);
                } else {
                    break;
                }
                startActivity(new Intent(this, HomeActivity1.class).putExtras(extras));
                finish();
            }
        }
    }

    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}