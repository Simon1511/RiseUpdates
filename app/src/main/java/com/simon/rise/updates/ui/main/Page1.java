package com.simon.rise.updates.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

    private Button afh;
    private Button gdrive;

    private Spinner spinner1;
    private Spinner spinner2;

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

        parser.getItemList().clear();
        parser2.getItemList().clear();
        parser3.getItemList().clear();

        // Initialize AFH and GDrive download buttons
        gdrive = root.findViewById(R.id.button_gdrive);
        afh = root.findViewById(R.id.button_AFH);

        // Gray out both buttons by default
        afh.setEnabled(false);
        gdrive.setEnabled(false);

        getTypeVersion();

        initializeSpinners();
        onClickSpinners();
        onClickButtons();

        checkInstalled();

        updateAlert();

        return root;
    }

    public void updateAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog alertDialog;

        builder.setTitle("Fetching data...");
        builder.setCancelable(false);
        builder.setView(R.layout.progressbar);

        alertDialog = builder.create();

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Exit app", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        alertDialog.show();

        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);

        ProgressBar pBar = alertDialog.findViewById(R.id.progressBar);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Sleep as long as the itemList contains no items
                while(parser.getItemList().size() <= 1 && parser2.getItemList().size() <= 1) {
                    try {
                        Thread.sleep(1000);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                /* Check if the user is connected to the internet and
                                 if not, show an exit button instead of the progressbar */
                                if(!isConnected()) {
                                    alertDialog.setTitle("NO INTERNET CONNECTION");
                                    pBar.setVisibility(View.GONE);
                                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }

                if(parser.getItemList().size() > 2 || parser2.getItemList().size() > 2) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            alertDialog.dismiss();
                        }
                    });
                }
            }
        };

        Thread t = new Thread(runnable);
        t.start();
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void getTypeVersion() {
        // Get kernel types from github JSON
        connect.connectURL("kernelType", fragmentView, versionsURL);

        // Get kernel versions from github JSON
        connect2.connectURL("riseKernel", fragmentView, versionsURL);
    }

    public void onClickButtons() {
        afh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(parser3.getItemList().get(0).toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                catch(IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });

        gdrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(parser3.getItemList().get(1).toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                catch(IndexOutOfBoundsException e) {
                    e.printStackTrace();
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

                SystemProperties props = new SystemProperties();

                // Get download links for whatever is selected in the spinner
                if(spinner2.getSelectedItem().toString() != "") {
                    // We have different download links for v1.3 (Treble) and v1.3 (AOSP)
                    if(spinner1.getSelectedItem().toString().equals("Treble 10.0") && spinner2.getSelectedItem().toString().equals("v1.3")) {
                        connect3.connectURL("v1.3T", fragmentView, downloadURL);
                    }
                    else
                    if(spinner2.getSelectedItem().toString().equals("v1.2") || spinner2.getSelectedItem().toString().equals("v1.1")
                            || spinner2.getSelectedItem().toString().equals("v1")) {
                        if(props.read("ro.boot.bootloader") != null) {
                            if (props.read("ro.boot.bootloader").contains("A520")) {
                                connect3.connectURL(spinner2.getSelectedItem().toString() + "_a5", fragmentView, downloadURL);
                            } else if (props.read("ro.boot.bootloader").contains("A720")) {
                                connect3.connectURL(spinner2.getSelectedItem().toString() + "_a7", fragmentView, downloadURL);
                            } else {
                                afh.setEnabled(false);
                                gdrive.setEnabled(false);
                            }
                        }
                        else
                        {
                            afh.setEnabled(false);
                            gdrive.setEnabled(false);
                        }
                    }
                    else
                    {
                        connect3.connectURL(spinner2.getSelectedItem().toString(), fragmentView, downloadURL);
                    }
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
    }

    public void setButtons() {
        String spinnerItem1 = spinner1.getSelectedItem().toString();
        String spinnerItem2 = spinner2.getSelectedItem().toString();

        if(spinnerItem1.equals("") || spinnerItem2.equals("")) {
            afh.setEnabled(false);
            gdrive.setEnabled(false);
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
                            /* AOSP 10.0
                            Include everything except for v3 (Pie only)*/
                            if(spinnerItem1.equals("AOSP 10.0")) {
                                if(spinnerItem2.equals("v1.2") || spinnerItem2.equals("v1.1") || spinnerItem2.equals("v1")) {
                                    afh.setEnabled(true);
                                    gdrive.setEnabled(false);
                                }
                                else
                                {
                                    afh.setEnabled(true);
                                    gdrive.setEnabled(true);
                                }

                                if(spinnerItem2.equals("v3 (Pie)") || spinnerItem2.equals("")) {
                                    afh.setEnabled(false);
                                    gdrive.setEnabled(false);
                                }
                            }

                            /* Treble 10.0
                            Only include v1.3 and newer, except for v3 (Pie only) */
                            if(spinnerItem1.equals("Treble 10.0")) {
                                afh.setEnabled(true);
                                gdrive.setEnabled(true);

                                if(spinnerItem2.equals("v1") || spinnerItem2.equals("v1.1") || spinnerItem2.equals("v1.2")
                                        || spinnerItem2.equals("v3 (Pie)") || spinnerItem2.equals("")) {
                                    afh.setEnabled(false);
                                    gdrive.setEnabled(false);
                                }
                            }

                            /* OneUI 10.0
                            Only include v1.5 and newer */
                            if(spinnerItem1.equals("OneUI 10.0")) {
                                afh.setEnabled(true);
                                gdrive.setEnabled(true);

                                if(spinnerItem2.equals("v1") || spinnerItem2.equals("v1.1") || spinnerItem2.equals("v1.2")
                                        || spinnerItem2.equals("v1.3")|| spinnerItem2.equals("v1.4") || spinnerItem2.equals("v1.4-1")
                                        || spinnerItem2.equals("v3 (Pie)") || spinnerItem2.equals("")) {
                                    afh.setEnabled(false);
                                    gdrive.setEnabled(false);
                                }
                            }

                            /* AOSP 9.0
                               We only include v3 and 1.4X and newer versions */
                            if(spinnerItem1.equals("AOSP 9.0")) {
                                afh.setEnabled(true);
                                gdrive.setEnabled(true);

                                if(spinnerItem2.equals("v1") || spinnerItem2.equals("v1.1") || spinnerItem2.equals("v1.2")
                                        || spinnerItem2.equals("v1.3") || spinnerItem2.equals("")) {
                                    afh.setEnabled(false);
                                    gdrive.setEnabled(false);
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

    public void checkInstalled() {
        try {
            TextView tv = fragmentView.findViewById(R.id.textView_kernelVersion);
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
                tv.setText(R.string.kernelNotInstalled);
                image.setImageResource(R.drawable.ic_x_icon);
            }
        }
        catch(IOException e)  {
            e.printStackTrace();
        }
    }
}