package com.simon.rise.updates.Others;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONParser {

    private static final String TAG = "JSONParser";

    private List<String> itemList = new ArrayList<>();

    private String toUpdate2;

    public void parseJSON(String myResponse, String updateObject, String toUpdate) {
        toUpdate2 = toUpdate;

        Log.i(TAG, "parseJSON: Trying to get " + toUpdate);

        new Handler(Looper.getMainLooper()).post(() -> {
            JSONArray jArray;
            try {
                Log.d(TAG, "parseJSON: Trying to parse first JSON Array");
                jArray = new JSONArray(myResponse);

                for (int i=0; i < jArray.length(); i++)
                {
                    try {
                        Log.d(TAG, "parseJSON: Trying to parse first JSON Object");
                        JSONObject oneObject = jArray.getJSONObject(i);

                        if(updateObject.equals("riseKernel") || updateObject.equals("rise-q") || updateObject.equals("riseTreble-q")) {
                            Log.i(TAG, "parseJSON: Requesting download links");

                            Log.d(TAG, "parseJSON: Trying to parse second JSON Object");
                            JSONObject jObject = oneObject.getJSONObject(updateObject);

                            Log.d(TAG, "parseJSON: Trying to parse second JSON Array");
                            JSONArray vArray = jObject.getJSONArray(toUpdate);

                            for(int j=0; j<vArray.length(); j++) {
                                /* Add the String we got from the JSON to an
                                Arraylist in case we need it later, e.g. for a spinner. */
                                itemList.add(vArray.getString(j));
                            }
                        }
                        else
                        {
                            Log.i(TAG, "parseJSON: Requesting versions");

                            Log.d(TAG, "parseJSON: Trying to parse second JSON Array");
                            JSONArray jsonArray = oneObject.getJSONArray(toUpdate);

                            for(int j=0; j<jsonArray.length(); j++) {
                                /* Add the String we got from the JSON to an
                                Arraylist in case we need it later, e.g. for a spinner. */
                                itemList.add(jsonArray.getString(j));
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "parseJSON: Error parsing first JSON Object");
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "parseJSON: Error parsing first JSON Array");
                e.printStackTrace();
            }
        });
    }

    public List<String> getItemList() {
        return itemList;
    }

    public String getToUpdate() {
        return toUpdate2;
    }
}
