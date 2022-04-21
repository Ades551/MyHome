package com.example.myhome;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize TextView
        TextView[] textViews = new TextView[3];

        // CardView text
        textViews[0] = findViewById(R.id.text1);
        textViews[1] = findViewById(R.id.text2);
        textViews[2] = findViewById(R.id.text3);

        // set text
        textViews[0].setText("Zavlažovanie");
        textViews[1].setText("Domácnosť");
        textViews[2].setText("Bazén");

        if(connection()) alertDialog(); // show alert window if not connected

    }

    /**
     * @brief checks if internet connection is available
     * @return connection status
     */
    private boolean connection(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo(); // get connection info

        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    /**
     * @brief alert dialog for connection error
     */
    private void alertDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this); // dialog builder
        dialog.setMessage("Vaše zariadenie nie je pripojené k internetu!");
        dialog.setTitle("Internetové pripojenie");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Skúsiť znova!", (dialog1, which) -> {
            if(connection()) alertDialog();
        });
        dialog.setNegativeButton("Odísť", (dialog12, which) -> {
            finish();
            System.exit(0);
        });

        dialog.show();
    }

    /**
     * @brief MainActivity on click function for CardView
     * @param view
     */
    public void cardViewClick(View view){
        switch (view.getId()){
            case (R.id.cardView1):  // first card clicked
                startActivity(new Intent(this, ControlActivity.class).putExtra("action", "garden"));
                finish();
                break;
            case (R.id.cardView2):  // second card clicked
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                break;
            case (R.id.cardView3):  // third card clicked
                startActivity(new Intent(this, ControlActivity.class).putExtra("action", "pool"));
                finish();
                break;
            default:  // exit app
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                finish();
                System.exit(0);
        }
    }
}

