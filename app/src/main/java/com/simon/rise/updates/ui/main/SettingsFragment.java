package com.simon.rise.updates.ui.main;

import com.simon.rise.updates.BuildConfig;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

    private JSONParser parser;
    private HTTPConnecting connect;

    private View fragmentView;

    private static final String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        parser = new JSONParser();
        connect = new HTTPConnecting(parser);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Sleep as long as the itemList contains no items
                while(parser.getItemList().size() < 1) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(parser.getItemList().size() >= 1) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {

                        @Override
                        public void run() {
                            if(parser.getItemList().get(0).toString().equals("v" + BuildConfig.VERSION_NAME)) {
                                Toast.makeText(getActivity(), "No update available", Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                // Need to complete this later with "release" URL
                                Uri uri = Uri.parse("https://github.com/Simon1511/RiseUpdates");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        };

        Preference updateButton = findPreference("updateButton");
        updateButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                connect.connectURL("appVersion", fragmentView, versionsURL, "");

                /* Wait for our itemList to contain an item before
                telling the user that an update exists */
                Thread t = new Thread(runnable);
                t.start();

                return true;
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page1, container, false);

        this.fragmentView = view;

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}