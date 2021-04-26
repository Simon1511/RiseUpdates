package com.simon.rise.updates.ui.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;

import com.simon.rise.updates.R;

public class SupportButtons {

    private final Context context;

    public SupportButtons(Context context) {
        this.context = context;
    }

    public void supportButtons(View fragmentView, String xdaURL) {
        ImageButton xda = fragmentView.findViewById(R.id.imageButton_xda_page1);
        ImageButton tg = fragmentView.findViewById(R.id.imageButton_telegram_page1);
        ImageButton gh = fragmentView.findViewById(R.id.imageButton_github_page1);
        xda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(xdaURL);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }
                catch(IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });

        tg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse("https://t.me/joinchat/E8KG_kwFn5tmOTdh");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }
                catch(IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });

        gh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse("https://github.com/Simon1511");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }
                catch(IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
