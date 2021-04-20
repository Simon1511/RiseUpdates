package com.simon.rise.updates.SystemProperties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemProperties {

    private static String GETPROP_EXECUTABLE_PATH = "/system/bin/getprop";

    public String read(String propName) {
        Process process = null;
        BufferedReader bufferedReader = null;

        try {
            process = new ProcessBuilder().command(GETPROP_EXECUTABLE_PATH, propName).redirectErrorStream(true).start();
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = bufferedReader.readLine();
            if (line == null){
                line = "";
            }
            return line;
        } catch (Exception e) {
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