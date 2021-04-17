package com.simon.rise.updates.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

        // Get latest kernel version from github JSON and set it
        connect.connectURL("riseKernel", root);

        // Get kernel types from github JSON
        connect2.connectURL("kernelType", root);

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

        return root;
    }
}