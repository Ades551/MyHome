package com.example.myhome;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TimePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SetTimeActivity extends AppCompatActivity {
    
    static String message; // send message (hour, minute, duration)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);

        TimePicker timePicker =  findViewById(R.id.timePicker); // time picker
        timePicker.setIs24HourView(true); // 24h format

        EditText text = findViewById(R.id.editText1); // duration

        // floating button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            int hour, minute, duration;
            hour = timePicker.getHour();
            minute = timePicker.getMinute();
            try {
                duration = Integer.parseInt(text.getText().toString());
            } catch (NumberFormatException e){
                duration = 30;
            }

            message = String.format("%s,%s,%s", hour, minute, duration);

            startActivity(new Intent(this, SetDateActivity.class));
            finish();
        });
    }

    public void onBackPressed(){
        super.onBackPressed();
        if(SelectActivity.port.equals("all")) startActivity(new Intent(this, SelectActivity.class));
        else startActivity(new Intent(this, TimeActivity.class));
        finish();
    }
}
