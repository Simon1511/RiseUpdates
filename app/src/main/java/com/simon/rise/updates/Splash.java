package com.simon.rise.updates;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class Splash extends Activity {

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final int SPLASH_DISPLAY_LENGTH = 2000;

        setContentView(R.layout.splash);
        TextView tv = findViewById(R.id.splashText);
        tv.setText("v" + BuildConfig.VERSION_NAME);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(() -> {
            /* Create an Intent that will start the Menu-Activity. */
            Intent mainIntent = new Intent(Splash.this, MainActivity.class);
            Splash.this.startActivity(mainIntent);
            Splash.this.finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}