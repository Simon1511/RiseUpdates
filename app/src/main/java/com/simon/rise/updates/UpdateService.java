package com.simon.rise.updates;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.simon.rise.updates.HTTP.HTTPConnecting;
import com.simon.rise.updates.json.JSONParser;

public class UpdateService extends Service {

    private static final String TAG = "UpdateService";

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    // Notifications
    private static final String CH1_ID = "App updates";
    private NotificationManagerCompat notificationManager;

    // URLs
    private static final String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                checkAppUpdate();
                // Check for updates once per day
                handler.postDelayed(runnable, 86400000);
            }
        };

        // Start the service after 15 seconds
        handler.postDelayed(runnable, 15000);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    public void checkAppUpdate() {
        JSONParser parser = new JSONParser();
        HTTPConnecting connect = new HTTPConnecting(parser);

        parser.getItemList().clear();

        connect.connectURL("appVersion", versionsURL, "");

        Runnable run = new Runnable() {
            @Override
            public void run() {
                while(parser.getItemList().size() < 1) {
                    try {
                        Thread.sleep(500);
                        if(!isConnected()) {
                            Log.e(TAG, "checkUpdate could not connect to GitHub");
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(parser.getItemList().size() >= 1) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(!parser.getItemList().get(0).equals("ve" + BuildConfig.VERSION_NAME)) {
                                Log.i(TAG, "checkUpdate: App update found, notifying user");

                                createNotificationChannels();

                                notificationManager = NotificationManagerCompat.from(context);

                                Notification notification = new NotificationCompat.Builder(context, CH1_ID)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle("App Update available")
                                        .setContentText("RiseUpdates " + parser.getItemList().get(0) + " is available!")
                                        .setPriority(NotificationCompat.PRIORITY_LOW)
                                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                                        .build();
                                notificationManager.notify(1, notification);
                            }
                            else
                            {
                                Log.i(TAG, "checkUpdate: Newest app version already installed");
                            }
                        }
                    });
                }
            }
        };

        Thread t = new Thread(run);
        t.start();
    }

    public void createNotificationChannels() {
        NotificationChannel ch1 = new NotificationChannel(CH1_ID, "App Updates", NotificationManager.IMPORTANCE_LOW);
        ch1.setDescription("App update notification");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(ch1);
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}