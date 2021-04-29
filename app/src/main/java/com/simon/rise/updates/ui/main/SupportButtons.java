package com.simon.rise.updates.ui.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.simon.rise.updates.R;

public class SupportButtons {

    private static final String TAG = "SupportButtons";

    private final Context context;

    public SupportButtons(Context context) {
        this.context = context;
    }

    public void supportButtons(View fragmentView, String xdaURL) {
        ImageButton xda = fragmentView.findViewById(R.id.imageButton_xda_page1);
        ImageButton tg = fragmentView.findViewById(R.id.imageButton_telegram_page1);
        ImageButton gh = fragmentView.findViewById(R.id.imageButton_github_page1);
        xda.setOnClickListener(v -> {
            try {
                Log.i(TAG, "onClick: Redirect to XDA thread");

                Uri uri = Uri.parse(xdaURL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
            catch(IndexOutOfBoundsException e) {
                Log.e(TAG, "onClick: Error opening XDA URL");
                e.printStackTrace();
            }
        });

        tg.setOnClickListener(v -> {
            try {
                Log.i(TAG, "onClick: Redirect to Telegram group");

                Uri uri = Uri.parse("https://t.me/joinchat/E8KG_kwFn5tmOTdh");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
            catch(IndexOutOfBoundsException e) {
                Log.e(TAG, "onClick: Error opening Telegram URL");
                e.printStackTrace();
            }
        });

        gh.setOnClickListener(v -> {
            try {
                Log.i(TAG, "onClick: Redirect to Simon1511's GitHub profile");

                Uri uri = Uri.parse("https://github.com/Simon1511");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
            }
            catch(IndexOutOfBoundsException e) {
                Log.e(TAG, "onClick: Error opening GitHub URL");
                e.printStackTrace();
            }
        });
    }
}
