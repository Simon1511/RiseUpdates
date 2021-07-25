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

/**
 * A placeholder fragment containing a simple view.
 */
public class Page1 extends Fragment {

    private static final String TAG = "Page1";

    private static final String ARG_SECTION_NUMBER = "section_number";

    /* Make the View object created in onCreateView
    available to the whole class */
    protected View fragmentView;

    /* Create instances of HTTPConnecting and
    pass a new instance of JSONParser to it */
    private final JSONParser parser = new JSONParser();
    private final JSONParser parser2 = new JSONParser();
    private final JSONParser parser3 = new JSONParser();
    private final HTTPConnecting connect = new HTTPConnecting(parser);
    private final HTTPConnecting connect2 = new HTTPConnecting(parser2);
    private final HTTPConnecting connect3 = new HTTPConnecting(parser3);

    // URLs
    private static final String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";
    private static final String downloadURL = "https://raw.githubusercontent.com/Simon1511/random/master/downloads.json";
    private static final String xdaURL = "https://forum.xda-developers.com/t/kernel-9-0-10-0-aosp-risekernel-for-a5-a7-2017.3988891/";

    private Button dlButton;

    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;

    private final SystemProperties props = new SystemProperties();

    private final AlertDialogRunnable alr = new AlertDialogRunnable();

    private ArrayAdapter<String> adapter2;

    private int linuxVer;

    private TextView version;
    private TextView mirror;

    public static Page1 newInstance(int index) {
        Page1 fragment = new Page1();
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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_page1, container, false);

        this.fragmentView = root;

        SupportButtons spB = new SupportButtons(getContext());
        spB.supportButtons(fragmentView, xdaURL);

        parser.getItemList().clear();
        parser2.getItemList().clear();
        parser3.getItemList().clear();

        // Initialize download button
        dlButton = root.findViewById(R.id.button_dl_page1);

        // Gray out download button by default
        dlButton.setEnabled(false);

        checkKernelVersion();

        getTypeVersion();

        initializeSpinners();

        version = fragmentView.findViewById(R.id.textView_chooseVersion_page1);
        mirror = fragmentView.findViewById(R.id.textView_chooseDownload_page1);

        spinner2.setEnabled(false);
        spinner3.setEnabled(false);
        mirror.setEnabled(false);
        version.setEnabled(false);

        onClickSpinners();
        onClickButtons();

        checkInstalled();

        setSpinnerSelection();

        if(props.read("ro.boot.bootloader").contains("A520") || props.read("ro.boot.bootloader").contains("A720")) {
            alr.updateAlert(parser, parser2, getActivity());
        }

        return root;
    }

    public void getTypeVersion() {
        Log.i(TAG, "getTypeVersion: Connecting to GitHub");

        // Get kernel types from github JSON
        connect.connectURL("kernelType", versionsURL, "");

        // Get kernel versions from github JSON
        connect2.connectURL("riseKernel", versionsURL, "");
    }

    public void onClickButtons() {
        dlButton.setOnClickListener(v -> {
            String dlURL = "";

            if (spinner3.getSelectedItem().equals("Google Drive")) {
                dlURL = parser3.getItemList().get(0);
            }
            else
            if (spinner3.getSelectedItem().equals("MEGA") || spinner3.getSelectedItem().equals("OneDrive")) {
                dlURL = parser2.getItemList().get(1);
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

    public void initializeSpinners() {
        Log.i(TAG, "initializeSpinners: Initialize Spinner 1");

        // Create a dropdown-list for AOSP Q, Treble Q and AOSP Pie
        spinner1 = fragmentView.findViewById(R.id.spinner1_page1);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser.getItemList());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter1.add("");

        spinner1.setAdapter(adapter1);

        Log.i(TAG, "initializeSpinners: Initialize Spinner 2");

        // Create a dropdown-list for all versions of the kernel
        spinner2 = fragmentView.findViewById(R.id.spinner2_page1);

        adapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser2.getItemList());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter2.add("");

        spinner2.setAdapter(adapter2);

        Log.i(TAG, "initializeSpinners: Initialize Spinner 3");
        // Create a dropdown-list for download mirrors
        spinner3 = fragmentView.findViewById(R.id.spinner3_page1);
    }

    public void onClickSpinners() {
        // Make spinners invisible depending on selection
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setButtons();

                if(spinner1.getSelectedItem().toString().equals("")) {
                    Log.i(TAG, "onItemSelected Spinner1: Empty");
                    spinner2.setSelection(0);
                    spinner2.setEnabled(false);
                    spinner3.setEnabled(false);
                    mirror.setEnabled(false);
                    version.setEnabled(false);
                    dlButton.setEnabled(false);

                    parser3.getItemList().clear();
                }
                else
                {
                    Log.i(TAG, "onItemSelected Spinner1: " + spinner1.getSelectedItem().toString());
                    spinner2.setSelection(0);
                    spinner2.setEnabled(true);
                    version.setEnabled(true);
                }

                setSpinnerItems();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Disable spinners depending on selection
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setButtons();

                if(spinner2.getSelectedItem().equals("")) {
                    Log.i(TAG, "onItemSelected Spinner2: Empty");
                    spinner3.setSelection(0);
                    spinner3.setEnabled(false);
                    mirror.setEnabled(false);
                    dlButton.setEnabled(false);
                }

                // Get download links for whatever is selected in the spinner
                if(!spinner2.getSelectedItem().toString().equals("")) {
                    spinner3.setEnabled(true);
                    mirror.setEnabled(true);

                    Log.i(TAG, "onItemSelected: " + spinner2.getSelectedItem().toString());

                    // We have different download links for v1.3 (Treble) and v1.3 (AOSP)
                    if(spinner1.getSelectedItem().toString().equals("Treble 10.0") && spinner2.getSelectedItem().toString().equals("v1.3")) {
                        Log.i(TAG, "onItemSelected Spinner2: Connecting to GitHub");
                        connect3.connectURL("v1.3T", downloadURL, "riseKernel");
                    }
                    else
                    if(spinner2.getSelectedItem().toString().equals("v1.2") || spinner2.getSelectedItem().toString().equals("v1.1")
                            || spinner2.getSelectedItem().toString().equals("v1")) {
                        if(!props.read("ro.boot.bootloader").equals("")) {
                            if (props.read("ro.boot.bootloader").contains("A520")) {
                                Log.i(TAG, "onItemSelected Spinner2: Connecting to GitHub");
                                connect3.connectURL(spinner2.getSelectedItem().toString() + "_a5", downloadURL, "riseKernel");
                            } else if (props.read("ro.boot.bootloader").contains("A720")) {
                                Log.i(TAG, "onItemSelected Spinner2: Connecting to GitHub");
                                connect3.connectURL(spinner2.getSelectedItem().toString() + "_a7", downloadURL, "riseKernel");
                            } else {
                                alr.deviceAlert(getActivity());
                            }
                        }
                        else
                        {
                            alr.deviceAlert(getActivity());
                        }
                    }
                    else
                    {
                        Log.i(TAG, "onItemSelected Spinner2: Connecting to GitHub");
                        connect3.connectURL(spinner2.getSelectedItem().toString(), downloadURL, "riseKernel");
                    }

                    Runnable run = () -> {
                        /* Run this once in an if-clause and then use
                         the while-loop to wait for it */
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
                                        Log.i(TAG, "Spinner3: Add MEGA as download mirror");
                                        dlAdapter.add("MEGA");
                                    }

                                    if (parser3.getItemList().get(i).contains("google")) {
                                        Log.i(TAG, "Spinner3: Add Google Drive as download mirror");
                                        dlAdapter.add("Google Drive");
                                    }

                                    if (parser3.getItemList().get(i).contains("1drv")) {
                                        Log.i(TAG, "Spinner3: Add OneDrive as download mirror");
                                        dlAdapter.add("OneDrive");
                                    }

                                    if (parser3.getItemList().get(i).contains("androidfilehost")) {
                                        Log.i(TAG, "Spinner3: Add Androidfilehost as download mirror");
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

                /* Check if the selection changed and if so, clear
                our ArrayList */
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
                if(spinner3.getSelectedItem().toString().equals("")) {
                    Log.i(TAG, "onItemSelected Spinner3: Empty");
                }
                else
                {
                    Log.i(TAG, "onItemSelected Spinner3: " + spinner3.getSelectedItem().toString());
                }
                setButtons();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void setButtons() {
        String spinnerItem1 = spinner1.getSelectedItem().toString();
        String spinnerItem2 = spinner2.getSelectedItem().toString();

        if(spinnerItem1.equals("") || spinnerItem2.equals("")) {
            Log.i(TAG, "setButtons: Disabled");
            dlButton.setEnabled(false);
        }

        if(spinner3.getSelectedItem() == null || spinner3.getSelectedItem().toString().equals("")) {
            Log.i(TAG, "setButtons: Disabled");
            dlButton.setEnabled(false);
        }
        else
        if(!spinner3.getSelectedItem().toString().equals("")) {
            Log.i(TAG, "setButtons: Enabled");
            dlButton.setEnabled(true);
        }
    }

    public void checkKernelVersion() {
        try {
            Process process = new ProcessBuilder().command("/system/bin/uname", "-r").redirectErrorStream(true).start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if(line.contains("3.18.140")) {
                linuxVer = 140;
            }
            else
            if(line.contains("3.18.91")) {
                linuxVer = 91;
            }
            else
            {
                linuxVer = 14;
                alr.kernelVersionAlert(linuxVer, getContext());
            }
            Log.i(TAG, "checkKernelVersion: Device is running a kernel with Linux 3.18." + linuxVer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSpinnerItems() {
        Log.i(TAG, "setSpinnerItems: Setup Spinner2 selections");

        // Create a clone of getSecList()
        List<String> list = new ArrayList<>();
        list.clear();
        list.addAll(parser2.getSecList());
        list.remove("");

        for (int i=0; i<list.size(); i++) {
            /* "v1.4-1" and "v3 (Pie)" can't be converted to double, so
             just replace them with "v1.41" and "v3" */
            if(list.get(i).equals("v1.4-1")) {
                Collections.replaceAll(list, "v1.4-1", "v1.41");
            }
            if(list.get(i).equals("v3 (Pie)")) {
                Collections.replaceAll(list, "v3 (Pie)", "v3");
            }

            Collections.replaceAll(list, list.get(i), list.get(i).substring(1));
        }

        if(!spinner1.getSelectedItem().toString().equals("AOSP 9.0")) {
            list.remove("3");
        }

        if(spinner1.getSelectedItem().toString().equals("AOSP 10.0")) {
            for (int i=0; i<list.size(); i++) {
                if (linuxVer == 14 && Double.parseDouble(list.get(i)) >= 1.5) {
                    list.remove(i);
                }
            }
        }

        if(spinner1.getSelectedItem().toString().equals("Treble 10.0")) {
            for (int i=list.size()-1; i>0; i--) {
                if (Double.parseDouble(list.get(i)) <= 1.2) {
                    list.remove(i);
                }
            }
        }

        // Support for R was added in v1.6, so remove v1.5 and older
        if(spinner1.getSelectedItem().toString().equals("AOSP 11.0") || spinner1.getSelectedItem().toString().equals("Treble 11.0")) {
            for (int i=list.size()-1; i>0; i--) {
                if (Double.parseDouble(list.get(i)) <= 1.5) {
                    list.remove(i);
                }
            }
        }

        if(spinner1.getSelectedItem().toString().equals("OneUI 10.0")) {
            for (int i=list.size()-1; i>0; i--) {
                if (Double.parseDouble(list.get(i)) <= 1.41) {
                    list.remove(i);
                }
            }
        }

        if(spinner1.getSelectedItem().toString().equals("AOSP 9.0")) {
            /* No new versions will support this, therefore this solution is more
             future proof than the old one */
            list.clear();

            list.add("1.41");
            list.add("1.4");
            list.add("3");
        }

        for (int i=0; i<list.size(); i++) {
            Collections.replaceAll(list, "1.41", "1.4-1");
            Collections.replaceAll(list, "3", "3 (Pie)");
            Collections.replaceAll(list, list.get(i), "v" + list.get(i));
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
                            if(props.read("ro.build.version.release").equals("11")) {
                                if(props.read("ro.treble.enabled").equals("false")) {
                                    Log.i(TAG, "setSpinnerSelection: Device runs AOSP 11.0");
                                    for (int i = 0; i < parser.getItemList().size(); i++) {
                                        if(spinner1.getItemAtPosition(i).equals("AOSP 11.0")) {
                                            spinner1.setSelection(i);
                                        }
                                    }
                                }
                                else
                                {
                                    Log.i(TAG, "setSpinnerSelection: Device runs Treble 11.0");
                                    for (int i = 0; i < parser.getItemList().size(); i++) {
                                        if(spinner1.getItemAtPosition(i).equals("Treble 11.0")) {
                                            spinner1.setSelection(i);
                                        }
                                    }
                                }
                            }
                            else
                            if(props.read("ro.build.version.release").equals("10")) {
                                if(props.read("ro.treble.enabled").equals("false")) {
                                    Log.i(TAG, "setSpinnerSelection: Device runs AOSP 10.0");
                                    for (int i = 0; i < parser.getItemList().size(); i++) {
                                        if(spinner1.getItemAtPosition(i).equals("AOSP 10.0")) {
                                            spinner1.setSelection(i);
                                        }
                                    }
                                }
                                else
                                if(props.read("ro.treble.enabled").equals("true")) {
                                    if(props.read("ro.build.display.id").contains("Rise-Q")) {
                                        Log.i(TAG, "setSpinnerSelection: Device runs OneUI 10.0");
                                        for (int i = 0; i < parser.getItemList().size(); i++) {
                                            if(spinner1.getItemAtPosition(i).equals("OneUI 10.0")) {
                                                spinner1.setSelection(i);
                                            }
                                        }
                                    }
                                    else
                                    {
                                        Log.i(TAG, "setSpinnerSelection: Device runs Treble 10.0");
                                        for (int i = 0; i < parser.getItemList().size(); i++) {
                                            if(spinner1.getItemAtPosition(i).equals("Treble 10.0")) {
                                                spinner1.setSelection(i);
                                            }
                                        }
                                    }
                                }
                            }
                            else
                            if(props.read("ro.build.version.release").equals("9")) {
                                Log.d(TAG, "setSpinnerSelection: Device runs AOSP 9.0");
                                for (int i = 0; i < parser.getItemList().size(); i++) {
                                    if(spinner1.getItemAtPosition(i).equals("AOSP 9.0")) {
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
            TextView tv = fragmentView.findViewById(R.id.textView_version_page1);
            Process process = new ProcessBuilder().command("/system/bin/uname", "-r").redirectErrorStream(true).start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String variable = "v";
            String line = bufferedReader.readLine();

            int lineIndex = line.indexOf(variable);

            if(line.contains(variable)) {
                if(line.contains("riseKernel") || line.contains("ProjectRise")) {
                    if(line.substring(lineIndex).equals("v4")) {
                        Log.i(TAG, "checkInstalled: riseKernel v1.4-1 installed");
                        tv.setText("v1.4-1");
                    }
                    else
                    {
                        Log.i(TAG, "checkInstalled: riseKernel " + line.substring(lineIndex) + " installed");
                        tv.setText(line.substring(lineIndex));
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

                            if(parser2.getItemList().size() >= 2) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!parser2.getItemList().get(1).equals(line.substring(lineIndex))) {
                                            Log.i(TAG, "checkInstalled: riseKernel " + line.substring(lineIndex) + " installed, but "
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
                else
                {
                    Log.e(TAG, "checkInstalled: riseKernel is not installed");
                    tv.setText(R.string.notInstalled);
                }
            }
            else
            {
                Log.e(TAG, "checkInstalled: riseKernel is not installed");
                tv.setText(R.string.notInstalled);
            }
        }
        catch(IOException e)  {
            Log.e(TAG, "checkInstalled: Error reading riseKernel version");
            e.printStackTrace();
        }
    }
}
