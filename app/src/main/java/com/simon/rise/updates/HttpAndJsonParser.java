package com.simon.rise.updates;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpAndJsonParser {

    private TextView text;
    private List<String> itemList = new ArrayList<>();

    public HttpAndJsonParser(int textId, String toUpdate, View root) {

        OkHttpClient client = new OkHttpClient();

        String url = "https://raw.githubusercontent.com/Simon1511/random/master/versions.json";
        text = root.findViewById(textId);

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string();

                    new Handler(Looper.getMainLooper()).post(() -> {
                        JSONArray jArray;
                        try {
                            jArray = new JSONArray(myResponse);

                            for (int i=0; i < jArray.length(); i++)
                            {
                                try {
                                    JSONObject oneObject = jArray.getJSONObject(i);
                                    JSONArray jsonArray = oneObject.getJSONArray(toUpdate);

                                    for(int j=0; j<jsonArray.length(); j++) {
                                        /* Add the String we got from the JSON to an
                                        Arraylist in case we need it later, e.g. for a spinner. */
                                        itemList.add(jsonArray.getString(j));
                                        // Show the newest version in our textView
                                        text.setText(jsonArray.getString(0));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    public List getItemList() {
        return itemList;
    }

    public void setItemList(String jsonString) {
        itemList.add(jsonString);
    }
}
