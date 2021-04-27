package com.simon.rise.updates.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.simon.rise.updates.R;
import com.simon.rise.updates.json.JSONParser;

public class AlertDialogRunnable {

    public void updateAlert(JSONParser parser, JSONParser parser2, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog alertDialog;

        builder.setTitle("Fetching data...");
        builder.setCancelable(false);
        builder.setView(R.layout.progressbar);

        alertDialog = builder.create();

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Exit app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        alertDialog.show();

        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);

        ProgressBar pBar = alertDialog.findViewById(R.id.progressBar);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Sleep as long as the itemList contains no items
                while(parser.getItemList().size() <= 1 || parser2.getItemList().size() <= 1) {
                    try {
                        Thread.sleep(1000);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                /* Check if the user is connected to the internet and
                                 if not, show an exit button instead of the progressbar */
                                if(!isConnected(context)) {
                                    alertDialog.setTitle("NO INTERNET CONNECTION");
                                    pBar.setVisibility(View.GONE);
                                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(parser.getItemList().size() > 2 || parser2.getItemList().size() > 2) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        };

        Thread t = new Thread(runnable);
        t.start();
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
