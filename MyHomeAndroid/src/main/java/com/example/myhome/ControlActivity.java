package com.example.myhome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;

public class ControlActivity extends AppCompatActivity implements View.OnClickListener, TcpCommunication.AsyncResponse {

    static int length;
    static String action;

    int[] states; // = new int[7];
    CardView[] switches; // = new CardView[states.length];
    ImageView[] imageViews; // = new ImageView[states.length];

    String getData;
    String sendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        String intentExtra = getIntent().getStringExtra("action");
        if(intentExtra != null) action = intentExtra;

        //System.out.println(action);

        getData = String.format("command%s", action);
        sendData = String.format("command%s", action);

        String auto, cards;
        auto = cards = null;
        int icon = 0;

        switch (action){
            case ("garden"):
                length = 7;
                auto = "Automatická závlaha";
                cards = "Okruh";
                icon = R.drawable.grass;
                break;
            case ("pool"):
                auto = "Automatické čistenie";
                cards = "Plug";
                length = 3;
                icon = R.drawable.plug;
                break;
            default:
                System.exit(0);
                break;
        }

        states = new int[length];
        switches = new CardView[length];
        imageViews = new ImageView[length];
        TextView[] text = new TextView[length];
        ImageView[] icons = new ImageView[length];

        // set each card invisible
        GridLayout gridLayout = findViewById(R.id.gridSwitch);
        for(int i = 0; i < gridLayout.getChildCount(); i++){
            View view = gridLayout.getChildAt(i);
            view.setVisibility(View.GONE);
        }

        // initialize elements
        int index;
        for(int i = 0; i < length; i++) {
            View view = gridLayout.getChildAt(i); // card view
            switches[i] = findViewById(view.getId()); // set switch
            switches[i].setVisibility(View.VISIBLE);
            switches[i].setOnClickListener(this);


            view = switches[i].getChildAt(0); // linear layout
            LinearLayout linearLayout = findViewById(view.getId()); // set linear layout to view

            if(i == 0) index = 1;
            else {
                icons[i] = findViewById(linearLayout.getChildAt(0).getId());
                icons[i].setImageResource(icon);
                linearLayout = findViewById(linearLayout.getChildAt(1).getId());
                index = 0;
            }

            text[i] = findViewById(linearLayout.getChildAt(index).getId()); // set text
            imageViews[i] = findViewById(linearLayout.getChildAt(index + 1).getId()); // set imageview
        }

        // set text
        text[0].setText(auto);
        for(int i = 1; i < length; i++) text[i].setText(String.format("%s %s", cards, i));

        TcpCommunication tcpCommunication = new TcpCommunication(this, this);
        tcpCommunication.execute(getData);
    }

    /**
     * @brief option bar
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * @brief option bar selection
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.timing){
            startActivity(new Intent(this, SelectActivity.class));
            finish();
        }
        return true;
    }

    /**
     * @brief send data to server
     */
    public void sendStatus(){
        // for each switch send data
        for(int i = 0; i < states.length; i++) {
            String index = String.valueOf(i);
            String state_index = String.valueOf(states[i]);
            TcpCommunication code = new TcpCommunication(this, this);
            code.execute(String.format("%s,%s,%s", sendData, index, state_index));
        }
    }

    /**
     * @brief to check how many switches are enabled
     * @return sum of all states
     */
    private int controlSum(){
        int sum = 0;
        for (int state : states) sum += state;

        return sum;
    }

    /**
     * @brief reset all states
     */
    public void resetSwitches(){
        Arrays.fill(states, 0); // fills array with 0
        displayStates();
    }

    /**
     * @brief displays switch states
     */
    public void displayStates(){
        for(int i = 0; i < states.length; i++){
            if(states[i] == 1) imageViews[i].setVisibility(View.VISIBLE);
            else imageViews[i].setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        // list all switches
        for(int i = 0; i < states.length; i++){
            // find clicked one
            if(v.getId() == switches[i].getId()){
                if((switches[i].getId() != switches[0].getId()) && (states[0] == 1)){
                    Toast.makeText(this, "Automatický režim je zapnutý", Toast.LENGTH_SHORT).show();
                } else {
                    if(action.equals("pool")) {
                        if((switches[i].getId() == switches[0].getId()) && states[0] == 1){
                            resetSwitches();
                        } else {
                            if(i == 0) resetSwitches();
                            if (states[i] == 1) states[i] = 0;
                            else states[i] = 1;
                        }
                        sendStatus();
                        break;
                    } else {
                        switch (controlSum()) {
                            case (0): // switch can be enabled
                                states[i] = 1;
                                sendStatus();
                                break;
                            case (1): // switch can be replaced with another
                                if (imageViews[i].getVisibility() == View.VISIBLE) {
                                    resetSwitches();
                                } else {
                                    resetSwitches();
                                    states[i] = 1;
                                }
                                sendStatus();
                                break;
                            case (2): // auto mode is enabled (can turn off only auto mode)
                                if (switches[0].getId() == v.getId()) {
                                    resetSwitches();
                                    sendStatus();
                                }
                                break;
                            default:
                                Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                                finish();
                                System.exit(0);
                                break;
                        }
                    }
                }
            }
        }
        displayStates();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * @brief sets active cards (switches)
     * @param string
     */
    private void setControlSwitches(String string){
        String[] array = string.split(",", states.length);
        for(int i = 0; i < states.length; i++)
            states[i] = Integer.parseInt(array[i]);

        displayStates();
    }

    @Override
    public void processFinish(String output) { setControlSwitches(output); }
}