package com.simon.rise.updates.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.simon.rise.updates.HttpAndJsonParser;
import com.simon.rise.updates.R;

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
        final TextView textView = root.findViewById(R.id.section_label);

        // Get latest kernel version from github JSON and set it
        HttpAndJsonParser pullAndParse = new HttpAndJsonParser(R.id.textView_latestVersion, "riseKernel", root);

        // Create a dropdown-list
        Spinner spinner1 = root.findViewById(R.id.spinner_page1);
        ArrayAdapter<CharSequence> adapter1 = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_spinner_item, pullAndParse.getItemList());
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /* Show this as initial item in our spinner.
        Otherwise, selected items won't show in spinner's preview. */
        adapter1.add("Select a version");

        spinner1.setAdapter(adapter1);

        return root;
    }
}