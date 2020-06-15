package com.mds.eventifind;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class EventiFind extends Application {
    private SharedPreferences sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        boolean darkMode = sharedPref.getBoolean("dark", false);
        if(darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
