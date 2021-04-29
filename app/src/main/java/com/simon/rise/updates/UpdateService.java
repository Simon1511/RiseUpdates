package com.simon.rise.updates;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class UpdateService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    // Notification channels
    private static final String CH1_ID = "App updates";

    private NotificationManagerCompat notificationManager;

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
                checkUpdate();
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
        return super.onStartCommand(intent, flags, startId);
    }

    public void checkUpdate() {
        createNotificationChannels();

        notificationManager = NotificationManagerCompat.from(this);

        Notification notification = new NotificationCompat.Builder(this, CH1_ID)
                .setSmallIcon(R.drawable.avatar)
                .setContentTitle("App Update available")
                .setContentText("PLACEHOLDER")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
        notificationManager.notify(1, notification);
    }

    public void createNotificationChannels() {
        NotificationChannel ch1 = new NotificationChannel(CH1_ID, "App Updates", NotificationManager.IMPORTANCE_LOW);
        ch1.setDescription("App update notification");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(ch1);
    }
}