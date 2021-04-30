package com.simon.rise.updates;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

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

    // Default to daily update search
    private long updateInterval = 86400000;

    private static final String TEXT = "updateInterval";

    private String intervalString;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: " + TAG + " created");
        // Load the update interval from Sharedpreferences
        loadInterval();

        // Update the interval if it was changed in settings
        updateInterval();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                if(updateInterval != -1) {
                    Log.i(TAG, "onCreate: " + TAG + " is running");
                    Log.i(TAG, "run: " + updateInterval + "ms");

                    checkAppUpdate();

                    handler.postDelayed(runnable, updateInterval);
                }
                else
                {
                    // Stop the service if update-interval is set to "Never"
                    stopSelf();
                }
            }
        };

        // Start the service after 15 seconds
        handler.postDelayed(runnable, 15000);
    }

    public void updateInterval() {
        SharedPreferences.OnSharedPreferenceChangeListener listener;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                loadInterval();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public void loadInterval() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        intervalString = prefs.getString(TEXT, "Daily");
        getInterval();
    }

    public void getInterval() {
        if(intervalString.equals("Every 12hrs")) {
            updateInterval = 43200000;
        }
        else
        if(intervalString.equals("Daily")) {
            updateInterval = 86400000;
        }
        else
        if(intervalString.equals("Weekly")) {
            updateInterval = 604800000;
        }
        else
        if(intervalString.equals("Never")) {
            updateInterval = -1;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: Service was stopped!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: Service started!");
        return START_STICKY;
    }

    public void checkAppUpdate() {
        JSONParser parser = new JSONParser();
        HTTPConnecting connect = new HTTPConnecting(parser);

        parser.getItemList().clear();

        Log.i(TAG, "checkAppUpdate: Connect to GitHub");

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
                            if(!parser.getItemList().get(0).equals("v" + BuildConfig.VERSION_NAME)) {
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