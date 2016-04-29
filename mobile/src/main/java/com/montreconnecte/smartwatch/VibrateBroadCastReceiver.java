package com.montreconnecte.smartwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by aheil on 29/04/2016.
 */
public class VibrateBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

/*
        final AudioManager amanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (amanager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            WearService.m_instance.sendMessage("vib", "vibrator");
            Log.e("LOG envoi", "vib - vibrator");

        } else if (amanager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            WearService.m_instance.sendMessage("vib", "ringing");
            Log.e("LOG envoi", "vib - ringing");
        } else {

            WearService.m_instance.sendMessage("vib", "silent");
            Log.e("LOG envoi", "vib - silent");
        }
*/
    }
}

