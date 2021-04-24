package com.simon.rise.updates.ui.main;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.simon.rise.updates.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}