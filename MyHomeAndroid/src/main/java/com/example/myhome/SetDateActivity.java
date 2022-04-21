package com.example.myhome;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SetDateActivity extends AppCompatActivity implements TcpCommunication.AsyncResponse {

    String sendData = String.format("command%s", ControlActivity.action); // replace command
    int[] days = new int[7]; // selected days 1 -> selected, 0 -> not selected
    CheckBox[] checkBoxes = new CheckBox[7]; // checkboxes for dates

    int sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_date);

        // initialize checkboxes
        checkBoxes[0] = findViewById(R.id.checkBox1);
        checkBoxes[1] = findViewById(R.id.checkBox2);
        checkBoxes[2] = findViewById(R.id.checkBox3);
        checkBoxes[3] = findViewById(R.id.checkBox4);
        checkBoxes[4] = findViewById(R.id.checkBox5);
        checkBoxes[5] = findViewById(R.id.checkBox6);
        checkBoxes[6] = findViewById(R.id.checkBox7);

        Button button = findViewById(R.id.button);

        // floating button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            for(int i = 0; i < 7; i++) if (days[i] == 1) sendMessage(i);
        });

        // button select all days
        button.setOnClickListener(v -> {
            for(int i = 0; i < 7; i++) {
                checkBoxes[i].setChecked(true);
                days[i] = 1;
            }
        });
    }

    /**
     * @brief finish activity and jump back to Selection
     */
    public void endActivity(){
        startActivity(new Intent(this, SelectActivity.class));
        finish();
    }

    /**
     * @return sum of active days
     */
    private int controlSum(){
        int x = 0;
        for(int i = 0; i < 7; i++) if(days[i] == 1) x++;

        return x;
    }

    /**
     * @brief Sends day and time settings from previous activity
     * @param day selected day
     */
    public void sendMessage(int day) {
        String message = String.format("%s,%s,%s,%s", sendData, SelectActivity.port, day, SetTimeActivity.message); // message format
        TcpCommunication code = new TcpCommunication(this,  this);
        code.execute(message);
    }

    /**
     * @brief Set days based on checkboxes
     * @param v view
     */
    public void setCheckBoxes(View v) {
        for(int i = 0; i < 7; i++){
            if(checkBoxes[i].isChecked()) days[i] = 1;
            else days[i] = 0;
        }
    }

    public void onBackPressed(){
        super.onBackPressed();
        startActivity(new Intent(this, SetTimeActivity.class));
        finish();
    }

    @Override
    public void processFinish(String output) {
        sum++;
        if(sum == controlSum()) endActivity();
    }
}
