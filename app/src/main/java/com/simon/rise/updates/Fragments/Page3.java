package com.simon.rise.updates.Fragments;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.rise.updates.Others.HTTPConnecting;
import com.simon.rise.updates.R;
import com.simon.rise.updates.Others.SystemProperties;
import com.simon.rise.updates.Others.JSONParser;
import com.simon.rise.updates.Others.AlertDialogRunnable;
import com.simon.rise.updates.Others.SupportButtons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private final JSONParser parser3 = new JSONParser();
    private final HTTPConnecting connect = new HTTPConnecting(parser);
    private final HTTPConnecting connect2 = new HTTPConnecting(parser2);
    private final HTTPConnecting connect3 = new HTTPConnecting(parser3);

    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;

    private ArrayAdapter<String> adapter2;

    // URLs
    private static final String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";
    private static final String downloadURL = "https://raw.githubusercontent.com/Simon1511/random/master/downloads.json";
    private static final String xdaURL = "https://forum.xda-developers.com/t/treble-aosp-10-0-risetreble-v1-1-for-a5-and-a7-2017.4160213/";

    private Button dlButton;

    private final AlertDialogRunnable alr = new AlertDialogRunnable();

    private TextView tv;

    private SystemProperties props = new SystemProperties();

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

        checkInstalled();

        getVersions();
        initializeSpinners();
        onClickSpinners();

        onClickButtons();

        setSpinnerSelection();

        return root;
    }

    public void getVersions() {
        Log.i(TAG, "getVersions: Connecting to GitHub");
        // Get available types from GitHub JSON
        connect.connectURL("trebleType", versionsURL, "");

        // Get riseTreble versions from Github JSON
        connect2.connectURL("riseTreble", versionsURL, "");
    }

    public void initializeSpinners() {
        Log.i(TAG, "initializeSpinners: Initialize Spinner 1");

        // Create a dropdown-list for types
        spinner1 = fragmentView.findViewById(R.id.spinner1_page3);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser.getItemList());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter1.add("");

        spinner1.setAdapter(adapter1);

        Log.i(TAG, "initializeSpinners: Initialize Spinner 2");

        // Create a dropdown-list for versions
        spinner2 = fragmentView.findViewById(R.id.spinner2_page3);

        adapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser2.getItemList());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter2.add("");

        spinner2.setAdapter(adapter2);

        Log.i(TAG, "initializeSpinners: Initialize Spinner 3");

        // Create a dropdown-list for download mirrors
        spinner3 = fragmentView.findViewById(R.id.spinner3_page3);
    }

    public void onClickSpinners()  {
        TextView mirror = fragmentView.findViewById(R.id.textView_chooseDownload_page3);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner1.getSelectedItem().toString().equals("")) {
                    Log.i(TAG, "onItemSelected Spinner1: Empty");
                    spinner2.setSelection(0);
                    spinner2.setEnabled(false);
                    spinner3.setSelection(0);
                    spinner3.setEnabled(false);
                    dlButton.setEnabled(false);
                }
                else
                {
                    Log.i(TAG, "onItemSelected spinner1: " + spinner1.getSelectedItem().toString());

                    spinner2.setEnabled(true);
                    spinner2.setSelection(0);
                }

                setSpinnerItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner2.getSelectedItem().equals("")) {
                    spinner3.setSelection(0);
                    spinner3.setEnabled(false);
                    mirror.setEnabled(false);
                    dlButton.setEnabled(false);
                }

                if(spinner2.getSelectedItem() != "") {
                    mirror.setEnabled(true);
                    spinner3.setEnabled(true);

                    Log.i(TAG, "onItemSelected spinner2: " + spinner2.getSelectedItem().toString());

                    Log.i(TAG, "onItemSelected spinner2: Connecting to GitHub");

                    // Get riseTreble downloads from Github JSON
                    connect3.connectURL(spinner2.getSelectedItem().toString(), downloadURL, "riseTreble");

                    Runnable run = () -> {
                        /* Run this once in an if-clause and then in a while-loop */
                        if(parser3.getItemList().size() <= 1) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    alr.updateAlert(parser2, parser3, getActivity()));
                        }

                        while(parser3.getItemList().size() <= 1) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if(parser3.getItemList().size() > 1) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                ArrayAdapter<CharSequence> dlAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item);
                                dlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                dlAdapter.add("");
                                for(int i = 0; i<parser3.getItemList().size(); i++) {
                                    if (parser3.getItemList().get(i).contains("mega")) {
                                        Log.i(TAG, "spinner3: Add MEGA as download mirror");
                                        dlAdapter.add("MEGA");
                                    }

                                    if (parser3.getItemList().get(i).contains("google")) {
                                        Log.i(TAG, "spinner3: Add Google Drive as download mirror");
                                        dlAdapter.add("Google Drive");
                                    }

                                    if (parser3.getItemList().get(i).contains("1drv")) {
                                        Log.i(TAG, "spinner3: Add OneDrive as download mirror");
                                        dlAdapter.add("OneDrive");
                                    }

                                    if (parser3.getItemList().get(i).contains("androidfilehost")) {
                                        Log.i(TAG, "spinner3: Add Androidfilehost as download mirror");
                                        dlAdapter.add("Androidfilehost");
                                    }
                                }
                                spinner3.setAdapter(dlAdapter);
                                spinner3.setEnabled(true);
                            });
                        }
                    };
                    Thread t = new Thread(run);
                    t.start();
                }

                if(!spinner2.getSelectedItem().toString().equals(parser3.getToUpdate())) {
                    parser3.getItemList().clear();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!spinner3.getSelectedItem().toString().equals("")) {
                    Log.i(TAG, "onItemSelected spinner3: " + spinner3.getSelectedItem().toString());
                    Log.i(TAG, "setButtons: Enabled");
                    dlButton.setEnabled(true);
                }
                else
                {
                    Log.i(TAG, "onItemSelected spinner3: Empty");
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

            if (spinner3.getSelectedItem().equals("Google Drive")) {
                dlURL = parser3.getItemList().get(0);
            }
            else
            if (spinner3.getSelectedItem().equals("MEGA") || spinner3.getSelectedItem().equals("OneDrive")) {
                dlURL = parser3.getItemList().get(1);
            }
            else
            if (spinner3.getSelectedItem().equals("Androidfilehost")) {
                dlURL = parser3.getItemList().get(2);
            }

            Log.i(TAG, "download mirror: " + spinner3.getSelectedItem().toString());

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

    public void setSpinnerItems() {
        // Create a clone of getSecList()
        List<String> list = new ArrayList<>();
        list.clear();
        list.addAll(parser2.getSecList());
        list.remove("");

        list.remove("Navbar Enabler");

        for (int i=0; i<list.size(); i++) {
            Collections.replaceAll(list, list.get(i), list.get(i).substring(1));
        }

        if(spinner1.getSelectedItem().equals("riseTreble R")) {
            list.remove("1.0");
            list.remove("1.1");
        }

        if(spinner1.getSelectedItem().equals("riseTreble Q")) {
            for(int i=0; i<list.size(); i++) {
                if(Double.parseDouble(list.get(i)) > 1.1) {
                    list.remove(i);
                }
            }
        }

        for (int i=0; i<list.size(); i++) {
            Collections.replaceAll(list, list.get(i), "v" + list.get(i));
        }

        if(spinner1.getSelectedItem().equals("riseTreble Q")) {
            list.add("Navbar Enabler");
        }

        list.add(0, "");

        parser2.getItemList().clear();
        parser2.getItemList().addAll(list);

        // Run this always
        adapter2.notifyDataSetChanged();
    }

    public void setSpinnerSelection() {
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

                if(parser.getItemList().size() >= 2) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(props.read("ro.system.build.version.sdk").equals("29")) {
                                for(int i=0; i<parser.getItemList().size(); i++) {
                                    if(parser.getItemList().get(i).equals("riseTreble Q")) {
                                        spinner1.setSelection(i);
                                    }
                                }
                            }
                            else
                            if(props.read("ro.system.build.version.sdk").equals("30")) {
                                for(int i=0; i<parser.getItemList().size(); i++) {
                                    if(parser.getItemList().get(i).equals("riseTreble R")) {
                                        spinner1.setSelection(i);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        };

        Thread t = new Thread(run);
        t.start();
    }

    @SuppressLint("SetTextI18n")
    public void checkInstalled() {
        try {
            tv = fragmentView.findViewById(R.id.textView_version_page3);
            Process process = new ProcessBuilder().command("/system/bin/cat", "/proc/mounts").redirectErrorStream(true).start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = bufferedReader.lines().collect(Collectors.joining());
            SystemProperties props = new SystemProperties();

            if(line.contains("/dev/block/platform/13540000.dwmmc0/by-name/VENDOR")) {
                // Newer versions will have a "ro.risetreble.version" prop
                if(!props.read("ro.risetreble.version").equals("")) {
                    Log.i(TAG, "checkInstalled: riseTreble-R is installed");
                    tv.setText(props.read("ro.risetreble.version"));
                }
                else
                {
                    Log.i(TAG, "checkInstalled: riseTreble-Q is installed");
                    tv.setText("v1.1");
                }
            }
            else
            {
                Log.e(TAG, "checkInstalled: riseTreble is not installed");
                tv.setText(R.string.notInstalled);
            }

            Runnable run = new Runnable() {
                @Override
                public void run() {
                    while(parser2.getItemList().size() <= 2) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if(parser2.getItemList().size() >= 3) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if(!parser2.getItemList().get(1).contentEquals(tv.getText()) && !tv.getText().equals("N/A")) {
                                    Log.i(TAG, "checkInstalled: riseTreble " + tv.getText() + " installed, but "
                                            + parser2.getItemList().get(1) + " is available");
                                }
                            }
                        });
                    }
                }
            };

            Thread t = new Thread(run);
            t.start();
        }
        catch(IOException e)  {
            e.printStackTrace();
        }
    }
}