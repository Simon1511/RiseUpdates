package com.simon.rise.updates.Fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.simon.rise.updates.Others.HTTPConnecting;
import com.simon.rise.updates.R;
import com.simon.rise.updates.Services.UpdateService;
import com.simon.rise.updates.Others.JSONParser;
import com.simon.rise.updates.Others.AlertDialogRunnable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    private JSONParser parser;
    private HTTPConnecting connect;

    private static final String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";

    private final AlertDialogRunnable alr = new AlertDialogRunnable();

    private String intervalString = "Daily";

    public static final String INTERVAL = "updateInterval";
    public static final String DATE = "datePref";

    private Preference updateButton;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        parser = new JSONParser();
        connect = new HTTPConnecting(parser);

        updateButton = findPreference("updateButton");

        loadDate();

        Preference aboutText = findPreference("about");
        aboutText.setSummary("RiseUpdates by Simon1511.\nAll rights reserved.\nv1.0 built on " + getString(R.string.buildDate));

        updateButton.setOnPreferenceClickListener(preference -> {
            updateButton.setSummary("Last checked: " + getDate());
            saveDate();

            Log.i(TAG, "onPreferenceClick: Connecting to GitHub");
            connect.connectURL("appVersion", versionsURL, "");

            alr.appUpdateAlert(parser, getActivity());

            return true;
        });

        Preference easterEgg = findPreference("about");
        easterEgg.setOnPreferenceClickListener(preference -> {
            Log.i(TAG, "onPreferenceClick: About button");
            Toast.makeText(getActivity(), "( ͡° ͜ʖ ͡°)", Toast.LENGTH_SHORT).show();
            return true;
        });

        Preference updateInterval = findPreference("updateIntervalList");

        loadInterval();

        if(intervalString != null) {
            updateInterval.setSummary(intervalString);
        }

        updateInterval.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.PreferenceAlertDialog);
            AlertDialog alertDialog;

            builder.setTitle("Update check interval");

            builder.setItems(R.array.updateInterval, (dialog, which) -> {
                if(which == 0) {
                    intervalString = "Every 12hrs";
                }
                else
                if(which == 1) {
                    intervalString = "Daily";
                }
                else
                if(which == 2) {
                    intervalString = "Weekly";
                }
                else
                if(which == 3) {
                    intervalString = "Never";
                }
                else
                {
                    intervalString = "None";
                }
                updateInterval.setSummary(intervalString);
                saveInterval();

                // Start the service again if it was stopped before
                if(!isServiceRunning(UpdateService.class)) {
                    getContext().startService(new Intent(getActivity(), UpdateService.class));
                }
            });

            alertDialog = builder.create();

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Back", (dialog, which) ->
                    alertDialog.dismiss());

            alertDialog.show();

            return true;
        });
    }

    public String getDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
    }

    public void saveDate() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DATE, updateButton.getSummary().toString());
        editor.apply();
    }

    public void loadDate() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        updateButton.setSummary(sharedPreferences.getString(DATE, "Never"));
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void saveInterval() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(INTERVAL, intervalString);
        editor.apply();
    }

    public void loadInterval() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        intervalString = sharedPreferences.getString(INTERVAL, "Daily");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}