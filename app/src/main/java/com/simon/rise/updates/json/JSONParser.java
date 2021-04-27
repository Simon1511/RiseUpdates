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

    private String toUpdate2;

    public void parseJSON(String myResponse, String updateObject, String toUpdate, View root) {
        toUpdate2 = toUpdate;

        new Handler(Looper.getMainLooper()).post(() -> {
            JSONArray jArray;
            try {
                jArray = new JSONArray(myResponse);

                for (int i=0; i < jArray.length(); i++)
                {
                    try {
                        JSONObject oneObject = jArray.getJSONObject(i);

                        if(updateObject.equals("riseKernel") || updateObject.equals("rise-q") || updateObject.equals("riseTreble-q")) {
                            JSONObject jObject = oneObject.getJSONObject(updateObject);
                            JSONArray vArray = jObject.getJSONArray(toUpdate);

                            for(int j=0; j<vArray.length(); j++) {
                                /* Add the String we got from the JSON to an
                                Arraylist in case we need it later, e.g. for a spinner. */
                                itemList.add(vArray.getString(j));
                            }
                        }
                        else
                        {
                            JSONArray jsonArray = oneObject.getJSONArray(toUpdate);

                            for(int j=0; j<jsonArray.length(); j++) {
                                /* Add the String we got from the JSON to an
                                Arraylist in case we need it later, e.g. for a spinner. */
                                itemList.add(jsonArray.getString(j));
                            }
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

    public String getToUpdate() {
        return toUpdate2;
    }
}
