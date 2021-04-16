package com.simon.rise.updates.json;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONParser {

    private List<String> itemList = new ArrayList<>();
    private TextView text;

    public void parseJSON(int textId, String myResponse, String toUpdate, View root) {

        text = root.findViewById(textId);

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

    public List getItemList() {
        return itemList;
    }
}
