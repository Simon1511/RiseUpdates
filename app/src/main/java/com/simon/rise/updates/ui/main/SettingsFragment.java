package com.simon.rise.updates.ui.main;

import com.simon.rise.updates.BuildConfig;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.simon.rise.updates.HTTP.HTTPConnecting;
import com.simon.rise.updates.R;
import com.simon.rise.updates.json.JSONParser;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = "SettingsFragment";

    private JSONParser parser;
    private HTTPConnecting connect;

    private static final String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        parser = new JSONParser();
        connect = new HTTPConnecting(parser);

        Runnable runnable = () -> {
            // Sleep as long as the itemList contains no items
            while(parser.getItemList().size() < 1) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(parser.getItemList().size() >= 1) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if(parser.getItemList().get(0).equals("v" + BuildConfig.VERSION_NAME)) {
                        Log.i(TAG, "checkUpdate: No update available");
                        Toast.makeText(getActivity(), "No update available", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Log.i(TAG, "checkUpdate: Update found, opening webbrowser");

                        // Need to complete this later with "release" URL
                        Uri uri = Uri.parse("https://github.com/Simon1511/RiseUpdates");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
            }
        };

        Preference updateButton = findPreference("updateButton");
        updateButton.setOnPreferenceClickListener(preference -> {
            Log.i(TAG, "onPreferenceClick: Connecting to GitHub");
            connect.connectURL("appVersion", versionsURL, "");

            /* Wait for our itemList to contain an item before
            telling the user that an update exists */
            Thread t = new Thread(runnable);
            t.start();

            return true;
        });

        Preference easterEgg = findPreference("about");
        easterEgg.setOnPreferenceClickListener(preference -> {
            Log.i(TAG, "onPreferenceClick: About button");
            Toast.makeText(getActivity(), "( ͡° ͜ʖ ͡°)", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}