package com.simon.rise.updates.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.rise.updates.HTTP.HTTPConnecting;
import com.simon.rise.updates.R;
import com.simon.rise.updates.SystemProperties.SystemProperties;
import com.simon.rise.updates.json.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * A placeholder fragment containing a simple view.
 */
public class Page1 extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    /* Make the View object created in onCreateView
    available to the whole class */
    protected View fragmentView;

    /* Create instances of HTTPConnecting and
    pass a new instance of JSONParser to it */
    private JSONParser parser = new JSONParser();
    private JSONParser parser2 = new JSONParser();
    private JSONParser parser3 = new JSONParser();
    private HTTPConnecting connect = new HTTPConnecting(parser);
    private HTTPConnecting connect2 = new HTTPConnecting(parser2);
    private HTTPConnecting connect3 = new HTTPConnecting(parser3);

    // URLs
    private static final String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";
    private static final String downloadURL = "https://raw.githubusercontent.com/Simon1511/random/master/downloads.json";
    private static final String xdaURL = "https://forum.xda-developers.com/t/kernel-9-0-10-0-aosp-risekernel-for-a5-a7-2017.3988891/";

    private Button dlButton;

    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;

    private SystemProperties props = new SystemProperties();

    private SupportButtons spB;

    private final AlertDialogRunnable alr = new AlertDialogRunnable();

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
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
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

        spB = new SupportButtons(getContext());
        spB.supportButtons(fragmentView, xdaURL);

        parser.getItemList().clear();
        parser2.getItemList().clear();
        parser3.getItemList().clear();

        // Initialize AFH and GDrive download buttons
        dlButton = root.findViewById(R.id.button_dl_page1);

        // Gray out both buttons by default
        dlButton.setEnabled(false);

        getTypeVersion();

        initializeSpinners();
        onClickSpinners();
        onClickButtons();

        checkInstalled();

        alr.updateAlert(parser, parser2, getActivity());

        return root;
    }

    public void getTypeVersion() {
        // Get kernel types from github JSON
        connect.connectURL("kernelType", fragmentView, versionsURL, "");

        // Get kernel versions from github JSON
        connect2.connectURL("riseKernel", fragmentView, versionsURL, "");
    }

    public void onClickButtons() {
        dlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dlURL = "";

                if (spinner3.getSelectedItem().equals("Google Drive")) {
                    dlURL = parser3.getItemList().get(0).toString();
                }
                else
                if (spinner3.getSelectedItem().equals("MEGA") || spinner3.getSelectedItem().equals("OneDrive")) {
                    dlURL = parser2.getItemList().get(1).toString();
                }
                else
                if (spinner3.getSelectedItem().equals("Androidfilehost")) {
                    dlURL = parser3.getItemList().get(2).toString();
                }

                if (dlURL != "") {
                    try {
                        Uri uri = Uri.parse(dlURL);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void initializeSpinners() {
        // Create a dropdown-list for AOSP Q (default), Treble Q and AOSP Pie
        spinner1 = fragmentView.findViewById(R.id.spinner1_page1);

        ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser.getItemList());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter1.add("");

        spinner1.setAdapter(adapter1);

        // Create a dropdown-list for all versions of the kernel
        spinner2 = fragmentView.findViewById(R.id.spinner2_page1);

        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser2.getItemList());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter2.add("");

        spinner2.setAdapter(adapter2);

        // Create a dropdown-list for download mirrors
        spinner3 = fragmentView.findViewById(R.id.spinner3_page1);
    }

    public void onClickSpinners() {
        // Gray out buttons depending on type and version selected in spinners
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setButtons();

                if(spinner1.getSelectedItem().toString().equals("")) {
                    spinner2.setSelection(0);
                    spinner2.setEnabled(false);
                    spinner3.setEnabled(false);
                }
                else
                if(spinner1.getSelectedItem().toString() != parser.getItemList().toString()) {
                    spinner2.setSelection(0);
                    spinner2.setEnabled(true);
                }

                if(spinner1.getSelectedItem().toString().equals("")) {
                    parser3.getItemList().clear();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Gray out buttons depending on type and version selected in spinners
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setButtons();

                if(spinner2.getSelectedItem().equals("")) {
                    spinner3.setSelection(0);
                    spinner3.setEnabled(false);
                }

                // Get download links for whatever is selected in the spinner
                if(spinner2.getSelectedItem().toString() != "") {
                    // We have different download links for v1.3 (Treble) and v1.3 (AOSP)
                    if(spinner1.getSelectedItem().toString().equals("Treble 10.0") && spinner2.getSelectedItem().toString().equals("v1.3")) {
                        connect3.connectURL("v1.3T", fragmentView, downloadURL, "riseKernel");
                    }
                    else
                    if(spinner2.getSelectedItem().toString().equals("v1.2") || spinner2.getSelectedItem().toString().equals("v1.1")
                            || spinner2.getSelectedItem().toString().equals("v1")) {
                        if(props.read("ro.boot.bootloader") != null) {
                            if (props.read("ro.boot.bootloader").contains("A520")) {
                                connect3.connectURL(spinner2.getSelectedItem().toString() + "_a5", fragmentView, downloadURL, "riseKernel");
                            } else if (props.read("ro.boot.bootloader").contains("A720")) {
                                connect3.connectURL(spinner2.getSelectedItem().toString() + "_a7", fragmentView, downloadURL, "riseKernel");
                            } else {
                                dlButton.setEnabled(false);
                            }
                        }
                        else
                        {
                            dlButton.setEnabled(false);
                        }
                    }
                    else
                    {
                        connect3.connectURL(spinner2.getSelectedItem().toString(), fragmentView, downloadURL, "riseKernel");
                    }

                    Runnable run = new Runnable() {

                        @Override
                        public void run() {
                            /* Run this once in an if-clause and then in a while-loop */
                            if(parser3.getItemList().size() <= 1) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        alr.updateAlert(parser2, parser3, getActivity());
                                    }
                                });
                            }

                            while(parser3.getItemList().size() <= 1) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(parser3.getItemList().size() > 1) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ArrayAdapter<CharSequence> dlAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item);
                                        dlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        dlAdapter.add("");
                                        for(int i = 0; i<parser3.getItemList().size(); i++) {
                                            if (parser3.getItemList().get(i).toString().contains("mega")) {
                                                dlAdapter.add("MEGA");
                                            }

                                            if (parser3.getItemList().get(i).toString().contains("google")) {
                                                dlAdapter.add("Google Drive");
                                            }

                                            if (parser3.getItemList().get(i).toString().contains("1drv")) {
                                                dlAdapter.add("OneDrive");
                                            }

                                            if (parser3.getItemList().get(i).toString().contains("androidfilehost")) {
                                                dlAdapter.add("Androidfilehost");
                                            }
                                        }
                                        spinner3.setAdapter(dlAdapter);
                                        spinner3.setEnabled(true);
                                    }
                                });
                            }
                        }
                    };
                    Thread t = new Thread(run);
                    t.start();
                }

                /* Check if the selection changed and if so, clear
                our ArrayList */
                if(spinner2.getSelectedItem().toString() != parser3.getToUpdate()) {
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
            dlButton.setEnabled(false);
        }

        if(spinner3.getSelectedItem() == null || spinner3.getSelectedItem().toString().equals("")) {
            dlButton.setEnabled(false);
        }
        else
        if(spinner3.getSelectedItem().toString() != "") {
            dlButton.setEnabled(true);
        }


        Runnable run = new Runnable() {
            @Override
            public void run() {
                while(parser3.getItemList().size() < 1) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(parser3.getItemList().size() >= 1) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if(props.read("ro.boot.bootloader").contains("A520") || props.read("ro.boot.bootloader").contains("A720")) {
                                /* AOSP 10.0
                                Include everything except for v3 (Pie only)*/
                                if (spinnerItem1.equals("AOSP 10.0")) {
                                    if(spinnerItem2.equals("v3 (Pie)")) {
                                        spinner3.setEnabled(false);
                                    }
                                }

                                /* Treble 10.0
                                Only include v1.3 and newer, except for v3 (Pie only) */
                                if (spinnerItem1.equals("Treble 10.0")) {
                                    if (spinnerItem2.equals("v1") || spinnerItem2.equals("v1.1") || spinnerItem2.equals("v1.2")
                                            || spinnerItem2.equals("v3 (Pie)")) {
                                        spinner3.setEnabled(false);
                                    }
                                }

                                /* OneUI 10.0
                                Only include v1.5 and newer */
                                if (spinnerItem1.equals("OneUI 10.0")) {
                                    if (spinnerItem2.equals("v1") || spinnerItem2.equals("v1.1") || spinnerItem2.equals("v1.2")
                                            || spinnerItem2.equals("v1.3") || spinnerItem2.equals("v1.4") || spinnerItem2.equals("v1.4-1")
                                            || spinnerItem2.equals("v3 (Pie)")) {
                                        spinner3.setEnabled(false);
                                    }
                                }

                                /* AOSP 9.0
                                We only include v3 and 1.4X and newer versions */
                                if (spinnerItem1.equals("AOSP 9.0")) {
                                    if (spinnerItem2.equals("v1") || spinnerItem2.equals("v1.1") || spinnerItem2.equals("v1.2")
                                            || spinnerItem2.equals("v1.3")) {
                                        spinner3.setEnabled(false);
                                    }
                                }
                            }
                            else
                            {
                                Toast.makeText(getActivity(), "Not an A5/A7", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        };

        Thread t = new Thread(run);
        t.start();
    }

    public void checkInstalled() {
        try {
            TextView tv = fragmentView.findViewById(R.id.textView_version_page1);
            Process process = new ProcessBuilder().command("/system/bin/uname", "-r").redirectErrorStream(true).start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String variable = "v";
            String line = bufferedReader.readLine();
            ImageView image = fragmentView.findViewById(R.id.imageView1_page1);

            int lineIndex = line.indexOf(variable);

            if(line.contains(variable)) {
                if(line.contains("riseKernel") || line.contains("ProjectRise")) {
                    if(line.substring(lineIndex).equals("v4")) {
                        tv.setText("v1.4-1");
                    }
                    else
                    {
                        tv.setText(line.substring(lineIndex));
                    }
                    image.setImageResource(R.drawable.ic_hook_icon);
                }
            }
            else
            {
                tv.setText(R.string.notInstalled);
                image.setImageResource(R.drawable.ic_x_icon);
            }
        }
        catch(IOException e)  {
            e.printStackTrace();
        }
    }
}