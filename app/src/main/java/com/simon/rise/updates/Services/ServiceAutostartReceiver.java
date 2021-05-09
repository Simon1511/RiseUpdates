package com.simon.rise.updates.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ServiceAutostartReceiver extends BroadcastReceiver {

    private static final String TAG = "ServiceAutostartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent int1 = new Intent(context, UpdateService.class);
            context.startForegroundService(int1);
        Log.i(TAG, "UpdateService started");
    }
}