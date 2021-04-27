package com.simon.rise.updates.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

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
public class Page2 extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

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

    private SystemProperties props = new SystemProperties();

    public static Page2 newInstance(int index) {
        Page2 fragment = new Page2();
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
        View root = inflater.inflate(R.layout.fragment_page2, container, false);

        this.fragmentView = root;

        getVersions();
        initializeSpinners();
        onClickSpinners();

        checkInstalled();

        return root;
    }

    public void getVersions() {
        // Get Rise-Q versions from Github JSON
        connect.connectURL("rise-q", fragmentView, versionsURL, "");
    }

    public void initializeSpinners() {
        // Create a dropdown-list for versions
        spinner1 = fragmentView.findViewById(R.id.spinner1_page2);

        ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser.getItemList());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter1.add("");

        spinner1.setAdapter(adapter1);

        // Create a dropdown-list for download mirrors
        spinner2 = fragmentView.findViewById(R.id.spinner2_page2);

        ArrayAdapter<CharSequence> adapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item, parser2.getItemList());
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter2.add("");

        spinner2.setAdapter(adapter2);
    }

    public void onClickSpinners()  {
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinner1.getSelectedItem().toString().equals("")) {
                    spinner2.setSelection(0);
                    spinner2.setEnabled(false);
                }
                else
                if(spinner1.getSelectedItem().toString() != parser.getItemList().toString()) {
                    spinner2.setSelection(0);
                    spinner2.setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void checkInstalled() {
        TextView tv = fragmentView.findViewById(R.id.textView_version_page2);

        String variable = "v";
        String line = props.read("ro.build.display.id");
        ImageView image = fragmentView.findViewById(R.id.imageView1_page2);

        int lineIndex = line.indexOf(variable);

        String str = line.substring(lineIndex);

        if(line.contains(variable)) {
            if(line.contains("Rise-Q")) {
                if(line.contains("v1 ")) {
                    tv.setText(str.substring(0, 2));
                }
                else
                {
                    tv.setText(str.substring(0, 4));
                }
                image.setImageResource(R.drawable.ic_hook_icon);
            }
            else
            {
                tv.setText(R.string.notInstalled);
                image.setImageResource(R.drawable.ic_x_icon);
            }
        }
    }
}