package com.simon.rise.updates.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.simon.rise.updates.R;
import com.simon.rise.updates.SystemProperties.SystemProperties;
import com.simon.rise.updates.json.JSONParser;

public class AlertDialogRunnable {

    private static final String TAG = "AlertDialogRunnable";

    private final SystemProperties props = new SystemProperties();

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

        Log.i(TAG, "updateAlert: Fetch data from GitHub");

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
                                    Log.i(TAG, "updateAlert: No internet connection found, forcing user to exit");
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
                            Log.i(TAG, "updateAlert: Data fetched successfully");
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        };

        Thread t = new Thread(runnable);
        t.start();
    }

    public void deviceAlert(Context context) {
        if(props.read("ro.boot.bootloader").contains("A520") || props.read("ro.boot.bootloader").contains("A720")) {
            Log.i(TAG, "deviceAlert: This device is A5/A7");
        }
        else
        {
            Log.e(TAG, "deviceAlert: Unsupported device detected, forcing user to exit");

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            AlertDialog alertDialog;

            builder.setTitle("Unsupported device detected");
            builder.setMessage("Required device: a5y17lte/a7y17lte\n\nDetected device: " + props.read("ro.product.model"));
            builder.setCancelable(false);

            alertDialog = builder.create();

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Exit app", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);
                }
            });

            alertDialog.show();
        }
    }

    public boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
