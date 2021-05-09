package com.simon.rise.updates.Others;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemProperties {

    private static final String TAG = "SystemProperties";

    public String read(String propName) {
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            Log.i(TAG, "read: Get '" + propName + "'");

            process = new ProcessBuilder().command("/system/bin/getprop", propName).redirectErrorStream(true).start();
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if (line == null){
                line = "";
                Log.e(TAG, "read: '" + propName + "' is empty");
            }
            else
            {
                Log.i(TAG, "read: '" + propName + "' is '" + line + "'");
            }
            return line;
        } catch (Exception e) {
            Log.e(TAG, "read: '" + propName + "' is empty");
            return "";
        } finally{
            if (bufferedReader != null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null){
                process.destroy();
            }
        }
    }
}