package com.simon.rise.updates.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.simon.rise.updates.SystemProperties.SystemProperties;
import com.simon.rise.updates.json.JSONParser;

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
    private String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";
    private String downloadURL = "https://raw.githubusercontent.com/Simon1511/random/master/downloads.json";

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

        return root;
    }

    public void getTypeVersion() {
        // Get latest kernel version from github JSON and set it
        connect.connectURL("riseKernel", fragmentView, versionsURL);

        // Get kernel types from github JSON
        connect2.connectURL("kernelType", fragmentView, versionsURL);
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
        // Create a dropdown-list for all versions of the kernel
        spinner1 = fragmentView.findViewById(R.id.spinner1_page1);

        ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser.getItemList());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter1.add("");

        spinner1.setAdapter(adapter1);

        // Create a dropdown-list for AOSP Q (default), Treble Q and AOSP Pie
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
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setButtons();

                if(spinner2.getSelectedItem().toString().equals("")) {
                    spinner1.setSelection(0);
                    spinner1.setEnabled(false);
                }
                else
                if(spinner2.getSelectedItem().toString() != parser2.getItemList().toString()) {
                    spinner1.setSelection(0);
                    spinner1.setEnabled(true);
                }

                if(spinner2.getSelectedItem().toString().equals("")) {
                    parser3.getItemList().clear();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Gray out buttons depending on type and version selected in spinners
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setButtons();

                SystemProperties props = new SystemProperties();

                // Get download links for whatever is selected in the spinner
                if(spinner1.getSelectedItem().toString() != "") {
                    // We have different download links for v1.3 (Treble) and v1.3 (AOSP)
                    if(spinner2.getSelectedItem().toString().equals("Treble 10.0") && spinner1.getSelectedItem().toString().equals("v1.3")) {
                        connect3.connectURL("v1.3T", fragmentView, downloadURL);
                    }
                    else
                    if(spinner1.getSelectedItem().toString().equals("v1.2") || spinner1.getSelectedItem().toString().equals("v1.1")
                            || spinner1.getSelectedItem().toString().equals("v1")) {
                        if(props.read("ro.boot.bootloader") != null) {
                            if (props.read("ro.boot.bootloader").contains("A520")) {
                                connect3.connectURL(spinner1.getSelectedItem().toString() + "_a5", fragmentView, downloadURL);
                            } else if (props.read("ro.boot.bootloader").contains("A720")) {
                                connect3.connectURL(spinner1.getSelectedItem().toString() + "_a7", fragmentView, downloadURL);
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
                        connect3.connectURL(spinner1.getSelectedItem().toString(), fragmentView, downloadURL);
                    }
                }

                /* Check if the selection changed and if so, clear
                our ArrayList */
                if(spinner1.getSelectedItem().toString() != parser3.getToUpdate()) {
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

        /* AOSP 10.0
        Include everything except for v3 (Pie only)*/
        if(spinnerItem2.equals("AOSP 10.0")) {
            if(spinnerItem1.equals("v1.2") || spinnerItem1.equals("v1.1") || spinnerItem1.equals("v1")) {
                afh.setEnabled(true);
                gdrive.setEnabled(false);
            }
            else
            {
                afh.setEnabled(true);
                gdrive.setEnabled(true);
            }

            if(spinnerItem1.equals("v3 (Pie)") || spinnerItem1.equals("")) {
                afh.setEnabled(false);
                gdrive.setEnabled(false);
            }
        }

        /* Treble 10.0
        Only include v1.3 and newer, except for v3 (Pie only) */
        if(spinnerItem2.equals("Treble 10.0")) {
            afh.setEnabled(true);
            gdrive.setEnabled(true);

            if(spinnerItem1.equals("v1") || spinnerItem1.equals("v1.1") || spinnerItem1.equals("v1.2")
                    || spinnerItem1.equals("v3 (Pie)") || spinnerItem1.equals("")) {
                afh.setEnabled(false);
                gdrive.setEnabled(false);
            }
        }

        /* OneUI 10.0
        Only include v1.5 and newer */
        if(spinnerItem2.equals("OneUI 10.0")) {
            afh.setEnabled(true);
            gdrive.setEnabled(true);

            if(spinnerItem1.equals("v1") || spinnerItem1.equals("v1.1") || spinnerItem1.equals("v1.2")
                    || spinnerItem1.equals("v1.3")|| spinnerItem1.equals("v1.4") || spinnerItem1.equals("v1.4-1")
                    || spinnerItem1.equals("v3 (Pie)") || spinnerItem1.equals("")) {
                afh.setEnabled(false);
                gdrive.setEnabled(false);
            }
        }

        /* AOSP 9.0
        We only include v3 and 1.4X and newer versions */
        if(spinnerItem2.equals("AOSP 9.0")) {
            afh.setEnabled(true);
            gdrive.setEnabled(true);

            if(spinnerItem1.equals("v1") || spinnerItem1.equals("v1.1") || spinnerItem1.equals("v1.2")
                    || spinnerItem1.equals("v1.3") || spinnerItem1.equals("")) {
                afh.setEnabled(false);
                gdrive.setEnabled(false);
            }
        }

    }
}