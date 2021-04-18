package com.simon.rise.updates.ui.main;

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
import com.simon.rise.updates.json.JSONParser;

/**
 * A placeholder fragment containing a simple view.
 */
public class Page1 extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

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
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_page1, container, false);

        /* Create an instance of HTTPConnecting and
        pass a new instance of JSONParser to it */
        JSONParser parser = new JSONParser();
        JSONParser parser2 = new JSONParser();
        HTTPConnecting connect = new HTTPConnecting(parser);
        HTTPConnecting connect2 = new HTTPConnecting(parser2);

        String versionsURL = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";

        // Initialize AFH and GDrive download buttons
        Button gdrive = root.findViewById(R.id.button_gdrive);
        Button afh = root.findViewById(R.id.button_AFH);

        // Gray out both buttons by default
        afh.setEnabled(false);
        gdrive.setEnabled(false);

        // Get latest kernel version from github JSON and set it
        connect.connectURL("riseKernel", root, versionsURL);

        // Get kernel types from github JSON
        connect2.connectURL("kernelType", root, versionsURL);

        // Create a dropdown-list
        Spinner spinner1 = root.findViewById(R.id.spinner1_page1);
        ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<>(root.getContext(), R.layout.spinner_item, parser.getItemList());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter1.add("");

        spinner1.setAdapter(adapter1);

        // Show selection between AOSP Q (default), Treble Q and AOSP Pie
        Spinner spinner2 = root.findViewById(R.id.spinner2_page1);
        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<>(root.getContext(), R.layout.spinner_item, parser2.getItemList());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter2.add("");

        spinner2.setAdapter(adapter2);

        // Gray out buttons depending on type and version selected in spinners
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setButtons(spinner1, spinner2, afh, gdrive);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Gray out buttons depending on type and version selected in spinners
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setButtons(spinner1, spinner2, afh, gdrive);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        afh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        gdrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return root;
    }

    public void setButtons(Spinner spinner1, Spinner spinner2, Button afh, Button gdrive) {
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