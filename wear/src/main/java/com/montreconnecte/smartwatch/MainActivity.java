package com.montreconnecte.smartwatch;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe principale gérant l'affichage sur la montre et les déclenchements d'interfaec
 */
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener, WearableListView.ClickListener{

private List<ListViewItem> viewItemList = new ArrayList<>();
    protected GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /**
     * gère la liste des liens en favoris sur la montre et leurs affichage
     * @param v vue d'affichage
     */
    public void afficheFavori(View v) {
        setContentView(R.layout.main_list_activity);

        WearableListView wearableListView = (WearableListView) findViewById(R.id.wearable_list_view);

        viewItemList.add(new ListViewItem("Google", "http://www.google.com" ));
        viewItemList.add(new ListViewItem("Eurosport", "http://www.eurosport.fr"));
        viewItemList.add(new ListViewItem("Racing club de Strasbourg", "http://www.rcstrasbourgalsace.fr"));
        viewItemList.add(new ListViewItem("Sig", "http://www.sigstrasbourg.fr"));

        wearableListView.setAdapter(new ListViewAdapter(this, viewItemList));
        wearableListView.setClickListener(this);

    }
    /**
     * A l'ouverture, connecte la montre au Google API Client / donc au vibrator
     */
    @Override
    protected void onStart() {
        super.onStart();
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();
    }

    /**
     * Permet de gérer les suspensions de connexions
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
    }

    /**
     * Permet de gérer les échecs de connexions
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * Appellé lors de la connexions de la montre au mobile, au démarrage des deux applications
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mApiClient, this);

        //Envoie le premier message pour obtenir le mode de sonnerie courant
        sendMessage("getMode", "");

        //Démarre un thread qui sera chargé de maintenir à jour l'affichage du mode de sonnerie du mobile sur la montre
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        synchronized (this) {
                            wait(3000); // Boucle de 3s
                        }
                    } catch (InterruptedException ex) {
                    }

                    sendMessage("getMode", "");
                    Log.e("LOG RUN", "RUN");

                }
            }
        };

        thread.start();
        
    }

    /**
     * Gère l'arret de connexions entre les deux appareils
     */
    @Override
    protected void onStop() {
        if (null != mApiClient && mApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mApiClient, this);
            mApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Fonction déclenchée lors de la récéption d'un message en provenance du mobile
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        final String path = messageEvent.getPath();
        final String message = new String(messageEvent.getData());

        Log.e("TEST","LOG LOG : TEST TEST "+message+" path = "+path);


        if(path.equals("vib")){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageButton b = (ImageButton) findViewById(R.id.imageButton2);
                    if (b != null) {
                        //Mise à jour de l'icone du bouton
                        if (message.equals("silent")) {
                            b.setImageResource(R.mipmap.mute);
                        } else if (message.equals("vibrator")) {
                            b.setImageResource(R.mipmap.smartphone);
                        } else if (message.equals("ringing")) {
                            b.setImageResource(R.mipmap.ring);
                        }
                    }
                }
            });
        }
    }

    /**
     * Gère l'envoi de message depuis la montre vers le mobile
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

    /**
     * Déclenche l'envoi d'un message provoquant le changement de mode de sonnerie au téléphone lors du click sur le bouton de changement de mode de sonnerie
     * @param view
     */
    public void buttonClick(View view){
        sendMessage("vib", "SWITCH");
    }

    /**
     * Déclenche l'envoi d'un message d'ouverture de lien sur le navigateur au mobile
     * @param viewHolder
     */
    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Toast.makeText(this, "Open " + viewItemList.get(viewHolder.getLayoutPosition()).getText(), Toast.LENGTH_SHORT).show();
        sendMessage("lien", viewItemList.get(viewHolder.getLayoutPosition()).getUrl());
        setContentView(R.layout.activity_main);
    }

    /**
     * Fonction mystère, à renseigner par quelqu'un sachant quelle est cette chose
     */
    @Override
    public void onTopEmptyRegionClick() {

    }

}