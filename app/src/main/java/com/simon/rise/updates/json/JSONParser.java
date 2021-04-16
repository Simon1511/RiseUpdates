package com.simon.rise.updates.json;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONParser {

    private List<String> itemList = new ArrayList<>();

    public void parseJSON(String myResponse, String toUpdate, View root) {

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
