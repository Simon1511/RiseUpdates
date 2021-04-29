package com.simon.rise.updates.HTTP;

import android.util.Log;
import com.simon.rise.updates.json.JSONParser;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPConnecting {

    private static final String TAG = "HTTPConnecting";

    // We use the same instance of JSONParser in 3 classes
    private final JSONParser parser;

    /* Get an instance of JSONParser as a parameter and create new objects of it
    in classes where the constructor is called */
    public HTTPConnecting(JSONParser parser) {
        this.parser = parser;
    }

    public void connectURL(String toUpdate, String url, String updateObject) {

        Log.i(TAG, "connectURL: Create HTTP request");

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "onFailure: HTTP request failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i(TAG, "onResponse: HTTP request successful");
                    String myResponse = response.body().string();
                    parser.parseJSON(myResponse, updateObject, toUpdate);
                }
            }
        });
    }
}
