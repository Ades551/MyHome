package com.example.myhome;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;

public class TcpCommunication extends AsyncTask<String, Void, String> {
    private final WeakReference<Context> activityWeakReference;
    private final WeakReference<AsyncResponse> callback;

    private Dialog dialog;
    private String message;

    // check server config file
    final int port = 1234;
    final String address = "addr"; // replace

    String received = "command"; // replace

    public interface AsyncResponse {
        void processFinish(String output);
    }

    TcpCommunication(Context context, AsyncResponse callback){
        activityWeakReference = new WeakReference<>(context);
        this.callback = new WeakReference<>(callback);
    }

    /**
     * @brief pre execution (loading)
     */
    protected void onPreExecute(){
        super.onPreExecute();
        Context context = activityWeakReference.get();
        if(context == null){
            return;
        }

        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_layout);
        this.dialog.show();

    }

    /**
     * @brief while loading socket communication
     * @param strings string to send
     * @return String or null
     */
    protected String doInBackground(String... strings) {
        message = strings[0]; // sending message
        String message_received = null; // received message

        //System.out.println(message);

        try {
            Socket socket = new Socket(address, port); // establish connection
            socket.setSoTimeout(10000); // timeout 10sec
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
            output.write(message);
            output.flush();

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream())); // input reader

            // receive message only when ...
            if(message.contains(received)){
                message_received = input.readLine();
            }

            // close connection
            output.close();
            input.close();
            socket.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        //System.out.println(message_received);
        return message_received;
    }

    /**
     * @brief post execution (received data)
     * @param text received text
     */
    protected void onPostExecute(String text){
        super.onPostExecute(text);

        Context context = activityWeakReference.get();
        if(context == null){
            return;
        }

        // kill loading screen
        if(dialog.isShowing()) dialog.dismiss();


        // if data were received or data should be send
        if(text != null || message.contains("command")){ // replace command
            final AsyncResponse call = callback.get();
            if(call != null){
                call.processFinish(text);
            }
        }
    }

}
