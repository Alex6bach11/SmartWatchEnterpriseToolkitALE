package com.montreconnecte.smartwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;




/**
 * Created by baptiste on 14/04/16.
 *
 * Class gérant la partie communication avec l'application de la montre
 */
public class WearService extends WearableListenerService {

    private final static String TAG = WearService.class.getCanonicalName();
    protected GoogleApiClient mApiClient;
    private BroadcastReceiver receiver;
    private IntentFilter filter;
    private String taskListContent;


    private volatile HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;

    // Define how the handler will process messages
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        // Define how to handle any incoming messages here
        @Override
        public void handleMessage(Message message) {
            // ...
            // When needed, stop the service with
            // stopSelf();
        }
    }

    /**
     * Lance les listener nécéssaires à la récéption des message lors du démarrage de l'application
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mApiClient.connect();
        receiver=new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {

                if(!isInitialStickyBroadcast()) {
                    final AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    if (amanager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                        sendMessage("vib", "vibrator");
                    } else if (amanager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                        sendMessage("vib", "ringing");
                    } else if (amanager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                        sendMessage("vib", "silent");
                    }
                }
            }
        };
        filter=new IntentFilter(
                AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(receiver, filter);

        NodeAPITask nodeAPITask = new NodeAPITask();

        nodeAPITask.execute();

    }

    class NodeAPITask extends AsyncTask {
        @Override
        protected Object doInBackground(Object... params) {

            NodeApi.GetLocalNodeResult nodes = Wearable.NodeApi.getLocalNode(mApiClient).await();
            return null;
        }
    }


    /**
     * Déconnecte les APIs et les listener à la fermeture de l'application
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
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
                    Log.e(TAG, "SendMessageTriggered");
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, message.getBytes()).await();

                }
            }
        }).start();
    }


    /**
     * Appellé à la réception d'un message envoyé depuis la montre ou de la mainActivity
     *
     * @param messageEvent message reçu
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);


        //traite le message reçu
        final String path = messageEvent.getPath();
        final byte[] bytes = messageEvent.getData();
        String message = null;
        try {
            message = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if(path.equals("mainActivity")){
            System.out.println("Envoi du message :" + message);

            sendMessage("todoList", message);


        } else { //Si récéption d'un message de la montre

            //Ouvre une connexion vers la montre
            ConnectionResult connectionResult = mApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Failed to connect to GoogleApiClient.");
                return;
            }

            if (path.equals("vib")) {    //Récéption du message "vib"
                Handler mHandler = new Handler(getMainLooper());

                final AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                if (amanager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                    amanager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else if (amanager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                    amanager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                } else if (amanager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                    amanager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }

            } else if (path.equals("getMode")) {  //Récéption du message "getMode"
                Handler mHandler = new Handler(getMainLooper());
                final AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                if (amanager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                    sendMessage("vib", "vibrator");
                } else if (amanager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                    sendMessage("vib", "ringing");
                } else {
                    sendMessage("vib", "silent");
                }

            } else if (path.equals("lien")) {
                this.ouvrirLien(new String(bytes, StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * Lance l'ouverture du navigateur internet par défaut à l'url spécifié en paramètre
     * @param url identifiant du message
     */
   public void ouvrirLien(String url){
        Uri uriUrl = Uri.parse(url);
        Intent lancerNavigateur = new Intent(Intent.ACTION_VIEW, uriUrl);
        lancerNavigateur.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(lancerNavigateur);
    }


}