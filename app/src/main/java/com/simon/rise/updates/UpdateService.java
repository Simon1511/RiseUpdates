package com.simon.rise.updates;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.simon.rise.updates.HTTP.HTTPConnecting;
import com.simon.rise.updates.json.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UpdateService extends Service {

    private static final String TAG = "UpdateService";

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    // Notifications
    private static final String CH1_ID = "App updates";
    private static final String CH2_ID = "Background Service";
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
        createNotificationChannels();
        serviceNotification();

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
                    checkKernelVersion();

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

    public void serviceNotification() {
        Intent notificationIntent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra("android.provider.extra.APP_PACKAGE", getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, CH2_ID);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context, CH2_ID)
                .setContentTitle("Background Service is running")
                .setContentText("Click here to disable this notification")
                .setSmallIcon(R.drawable.ic_notification_logo)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(2, notification);
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

    public void checkKernelVersion() {
        try {
            JSONParser parser = new JSONParser();
            HTTPConnecting connect = new HTTPConnecting(parser);

            Process process = new ProcessBuilder().command("/system/bin/uname", "-r").redirectErrorStream(true).start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String variable = "v";
            String line = bufferedReader.readLine();
            String version;

            int lineIndex = line.indexOf(variable);

            if(line.contains(variable)) {
                if(line.contains("riseKernel") || line.contains("ProjectRise")) {
                    if(line.substring(lineIndex).equals("v4")) {
                        version = "v4";
                    }
                    else
                    {
                        Log.i(TAG, "checkKernelVersion: riseKernel " + line.substring(lineIndex) + " installed");
                        version = line.substring(lineIndex);
                    }
                    Log.i(TAG, "checkKernelVersion: Checking for update");

                    connect.connectURL("riseKernel", versionsURL, "");

                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            while(parser.getItemList().size() <= 1) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(parser.getItemList().size() > 2) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(version.equals("v4") || version.equals("v3") || parser.getItemList().get(0).equals(version)) {
                                            Log.i(TAG, "checkKernelVersion: No update was found");
                                        }
                                        else
                                        {
                                            Log.i(TAG, "checkKernelVersion: Update found, notifying user");

                                            notificationManager = NotificationManagerCompat.from(context);

                                            Intent intent = new Intent(context, MainActivity.class);

                                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                            Notification notification = new NotificationCompat.Builder(context, CH1_ID)
                                                    .setSmallIcon(R.drawable.ic_notification_logo)
                                                    .setContentTitle("RiseKernel update available")
                                                    .setContentText("RiseKernel " + parser.getItemList().get(0) + " is available!")
                                                    .setPriority(NotificationCompat.PRIORITY_LOW)
                                                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(true)
                                                    .build();

                                            notificationManager.notify(1, notification);
                                        }
                                    }
                                });
                            }
                        }

                    };

                    Thread t = new Thread(run);
                    t.start();
                }
                else
                {
                    Log.e(TAG, "checkKernelVersion: riseKernel is not installed");
                }
            }
            else
            {
                Log.e(TAG, "checkKernelVersion: riseKernel is not installed");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
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

                                notificationManager = NotificationManagerCompat.from(context);

                                Uri uri = Uri.parse("https://github.com/Simon1511/RiseUpdates/releases/tag/" + parser.getItemList().get(0));

                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                                PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                Notification notification = new NotificationCompat.Builder(context, CH1_ID)
                                        .setSmallIcon(R.drawable.ic_notification_logo)
                                        .setContentTitle("App Update available")
                                        .setContentText("RiseUpdates " + parser.getItemList().get(0) + " is available!")
                                        .setPriority(NotificationCompat.PRIORITY_LOW)
                                        .setCategory(NotificationCompat.CATEGORY_SERVICE)
                                        .setContentIntent(pendingIntent)
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
        ch1.setDescription("App update");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(ch1);

        NotificationChannel ch2 = new NotificationChannel(CH2_ID, "Background Service", NotificationManager.IMPORTANCE_DEFAULT);
        ch2.setDescription("Background Service");

        manager.createNotificationChannel(ch2);
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}