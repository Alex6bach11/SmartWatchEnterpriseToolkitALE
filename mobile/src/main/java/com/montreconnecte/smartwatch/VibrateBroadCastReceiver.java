package com.montreconnecte.smartwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by aheil on 29/04/2016.
 */
public class VibrateBroadCastReceiver extends BroadcastReceiver {

    protected GoogleApiClient mApiClient;

    @Override
    public void onReceive(Context context, Intent intent) {



        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mApiClient.connect();

        final AudioManager amanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (amanager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            sendMessage("vib", "vibrator");
            Log.e("LOG envoi", "vib - vibrator");

        } else if (amanager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            sendMessage("vib", "ringing");
            Log.e("LOG envoi", "vib - ringing");
        } else {

            sendMessage("vib", "silent");
            Log.e("LOG envoi", "vib - silent");
        }
        mApiClient.disconnect();
    }

    /**
     * Envoie un message à la montre
     * @param path identifiant du message
     * @param message message à transmettre
     */
    protected void sendMessage(final String path, final String message) {
        //effectué dans un trhead afin de ne pas être bloquant
        new Thread(new Runnable() {
            @Override
            public void run() {
                //envoie le message à tous les noeuds/montres connectées
                final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();

                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, message.getBytes()).await();

                }
            }
        }).start();
    }
}

