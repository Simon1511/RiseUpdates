package com.simon.rise.updates.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.rise.updates.HTTP.HTTPConnecting;
import com.simon.rise.updates.R;
import com.simon.rise.updates.json.JSONParser;

/**
 * A placeholder fragment containing a simple view.
 */
public class Page3 extends Fragment {

    public static final String TAG = "Page3";

    private static final String ARG_SECTION_NUMBER = "section_number";

    private View fragmentView;

    /* Create instances of HTTPConnecting and
    pass a new instance of JSONParser to it */
    private final JSONParser parser = new JSONParser();
    private final JSONParser parser2 = new JSONParser();
    private final HTTPConnecting connect = new HTTPConnecting(parser);
    private final HTTPConnecting connect2 = new HTTPConnecting(parser2);

    private Spinner spinner1;
    private Spinner spinner2;

    // URLs
    private static final String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";
    private static final String downloadURL = "https://raw.githubusercontent.com/Simon1511/random/master/downloads.json";
    private static final String xdaURL = "https://forum.xda-developers.com/t/treble-aosp-10-0-risetreble-v1-1-for-a5-and-a7-2017.4160213/";

    private Button dlButton;

    private final AlertDialogRunnable alr = new AlertDialogRunnable();

    public static Page3 newInstance(int index) {
        Page3 fragment = new Page3();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PageViewModel pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_page3, container, false);

        this.fragmentView = root;

        SupportButtons spB = new SupportButtons(getActivity());
        spB.supportButtons(fragmentView, xdaURL);

        dlButton = fragmentView.findViewById(R.id.button_dl_page3);
        dlButton.setEnabled(false);

        getVersions();
        initializeSpinners();
        onClickSpinners();

        onClickButtons();

        return root;
    }

    public void getVersions() {
        Log.i(TAG, "getVersions: Connecting to GitHub");

        // Get riseTreble versions from Github JSON
        connect.connectURL("riseTreble-q", versionsURL, "");
    }

    public void initializeSpinners() {
        Log.i(TAG, "initializeSpinners: Initialize Spinner 1");

        // Create a dropdown-list for versions
        spinner1 = fragmentView.findViewById(R.id.spinner1_page3);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser.getItemList());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter1.add("");

        spinner1.setAdapter(adapter1);

        Log.i(TAG, "initializeSpinners: Initialize Spinner 2");

        // Create a dropdown-list for download mirrors
        spinner2 = fragmentView.findViewById(R.id.spinner2_page3);
    }

    public void onClickSpinners()  {
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner1.getSelectedItem().equals("")) {
                    spinner2.setSelection(0);
                    spinner2.setEnabled(false);
                }

                if(spinner1.getSelectedItem() != "") {
                    Log.i(TAG, "onItemSelected Spinner1: " + spinner1.getSelectedItem().toString());

                    Log.i(TAG, "onItemSelected Spinner1: Connecting to GitHub");

                    // Get riseTreble downloads from Github JSON
                    connect2.connectURL(spinner1.getSelectedItem().toString(), downloadURL, "riseTreble-q");
                    Runnable run = () -> {
                        /* Run this once in an if-clause and then in a while-loop */
                        if(parser2.getItemList().size() <= 1) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    alr.updateAlert(parser, parser2, getActivity()));
                        }

                        while(parser2.getItemList().size() <= 1) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if(parser2.getItemList().size() > 1) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                ArrayAdapter<CharSequence> dlAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item);
                                dlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dlAdapter.add("");
                                for(int i = 0; i<parser2.getItemList().size(); i++) {
                                    if (parser2.getItemList().get(i).contains("mega")) {
                                        Log.i(TAG, "Spinner2: Add MEGA as download mirror");
                                        dlAdapter.add("MEGA");
                                    }

                                    if (parser2.getItemList().get(i).contains("google")) {
                                        Log.i(TAG, "Spinner2: Add Google Drive as download mirror");
                                        dlAdapter.add("Google Drive");
                                    }

                                    if (parser2.getItemList().get(i).contains("1drv")) {
                                        Log.i(TAG, "Spinner2: Add OneDrive as download mirror");
                                        dlAdapter.add("OneDrive");
                                    }

                                    if (parser2.getItemList().get(i).contains("androidfilehost")) {
                                        Log.i(TAG, "Spinner2: Add Androidfilehost as download mirror");
                                        dlAdapter.add("Androidfilehost");
                                    }
                                }
                                spinner2.setAdapter(dlAdapter);
                                spinner2.setEnabled(true);
                            });
                        }
                    };
                    Thread t = new Thread(run);
                    t.start();
                }

                if(!spinner1.getSelectedItem().toString().equals(parser2.getToUpdate())) {
                    parser2.getItemList().clear();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!spinner2.getSelectedItem().toString().equals("")) {
                    Log.i(TAG, "onItemSelected Spinner2: " + spinner2.getSelectedItem().toString());
                    Log.i(TAG, "setButtons: Enabled");
                    dlButton.setEnabled(true);
                }
                else
                {
                    Log.i(TAG, "onItemSelected Spinner2: Empty");
                    Log.i(TAG, "setButtons: Disabled");
                    dlButton.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void onClickButtons() {
        dlButton.setOnClickListener(v -> {
            String dlURL = "";

            if (spinner2.getSelectedItem().equals("Google Drive")) {
                dlURL = parser2.getItemList().get(0);
            }
            else
            if (spinner2.getSelectedItem().equals("MEGA") || spinner2.getSelectedItem().equals("OneDrive")) {
                dlURL = parser2.getItemList().get(1);
            }
            else
            if (spinner2.getSelectedItem().equals("Androidfilehost")) {
                dlURL = parser2.getItemList().get(2);
            }

            Log.i(TAG, "download mirror: " + spinner2.getSelectedItem().toString());

            if (!dlURL.equals("")) {
                try {
                    Uri uri = Uri.parse(dlURL);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    Log.i(TAG, "onClick: Redirect to webbrowser");
                    startActivity(intent);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "onClick: Error opening download URL");
                    e.printStackTrace();
                }
            }
        });
    }
}