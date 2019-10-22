package com.example.autoplayad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.autoplayad.activity.LaunchActivity;
import com.example.autoplayad.activity.MainActivity;


public class BootReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)){
            Intent launchActivityIntent = new Intent(context, LaunchActivity.class);
            launchActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchActivityIntent);
        }
    }
}
