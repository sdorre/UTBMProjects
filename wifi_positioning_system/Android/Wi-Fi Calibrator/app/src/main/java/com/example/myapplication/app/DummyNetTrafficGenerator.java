package com.example.myapplication.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.io.IOException;


public class DummyNetTrafficGenerator {

    private Process mProcess;
    private Context mContext;
    private boolean mRunning = false;

    public DummyNetTrafficGenerator(Context context) {
        mContext = context;
    }

    public int start() {
        if(mRunning) return -1;
        try {
            run();
            mRunning = true;
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    public int stop() {
        if(mProcess != null) {
            mProcess.destroy();
            mRunning = false;
            return 0;
        }
        return -1;
    }

    private void run() throws IOException {
        if(Build.VERSION.SDK_INT <= 16) {
            mProcess = Runtime.getRuntime().exec("/system/bin/ping " + getTarget());
        } else {
            mProcess = new ProcessBuilder()
                    .command("/system/bin/ping", getTarget())
                    .redirectErrorStream(true)
                    .start();
        }
    }

    public boolean isRunning() {
        return mRunning;
    }

    private String getTarget() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        return settings.getString("preference_server_hostname", "0.0.0.0");
    }
}
