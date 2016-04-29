package com.montreconnecte.smartwatch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener, WearableListView.ClickListener{

private List<ListViewItem> viewItemList = new ArrayList<>();
    protected GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

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

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * Appellé à la réception d'un message envoyé depuis le vibrator
     */
    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mApiClient, this);
        //Wearable.DataApi.addListener(mApiClient, this);

        //envoie le premier message
        sendMessage("getMode", "");

    }

    @Override
    protected void onStop() {
        if (null != mApiClient && mApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mApiClient, this);
            //Wearable.DataApi.removeListener(mApiClient, this);
            mApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        //traite le message reçu
        final String path = messageEvent.getPath();
        final String message = new String(messageEvent.getData());

        Log.e("TEST","LOG LOG : TEST TEST "+message+" path = "+path);


        if(path.equals("vib")){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(MainActivity.this, "vib ", Toast.LENGTH_SHORT).show();
                    ImageButton b = (ImageButton) findViewById(R.id.imageButton2);
                    if (b != null) {
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
        /*if(path.equals("bonjour")){

            //récupère le contenu du message
            final String message = new String(messageEvent.getData());


            //penser à effectuer les actions graphiques dans le UIThread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //nous affichons ici dans notre viewpager
                    elementList = new ArrayList<>();
                    elementList.add(new Element("Message reçu", message, "#F44336"));
                    pager.setAdapter(new ElementGridPagerAdapter(elementList, getFragmentManager()));
                }
            });
        }*/
    }

    /**
     * Envoie un message à au vibrator
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

    public void buttonClick(View view){
        sendMessage("vib", "SWITCH");
    }

    /*@Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {
            //on attend ici des assets dont le path commence par /image/
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith("/image/")) {

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset profileAsset = dataMapItem.getDataMap().getAsset("image");
                Bitmap bitmap = loadBitmapFromAsset(profileAsset);
                // On peux maintenant utiliser notre bitmap
            }
        }
    }*/

    /*@Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        //appellé lorsqu'une donnée à été mise à jour, nous utiliserons une autre méthode

        for (DataEvent event : dataEvents) {
            //on attend les "elements"
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith("/elements/")) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                List<DataMap> elementsDataMap = dataMapItem.getDataMap().getDataMapArrayList("/list/");

                if (elementList == null || elementList.isEmpty()) {
                    elementList = new ArrayList<>();

                    for (DataMap dataMap : elementsDataMap) {
                        elementList.add(getElement(dataMap));
                    }

                    //charge les images puis affiche le main screen
                    preloadImages(elementList.size());
                }

            }
        }
    }*/

    /*public Element getElement(DataMap elementDataMap) {
        return new Element(
                elementDataMap.getString("titre"),
                elementDataMap.getString("description"),
                elementDataMap.getString("url"));
    }*/

    /**
     * Précharge les images dans un cache Lru (en mémoire, pas sur le disque)
     * Afin d'être accessibles depuis l'adapter
     * Puis affiche le viewpager une fois terminé
     *
     * param sized nombre d'images à charger
     *//*
    public void preloadImages(final int size) {
        //initialise le cache
        DrawableCache.init(size);

        //dans le UIThread pour avoir accès aux toasts
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        Toast.makeText(MainActivity.this, "Chargement des images", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        //charge les images 1 par 1 et les place dans un LruCache
                        for (int i = 0; i < size; ++i) {
                            Bitmap bitmap = getBitmap(i);
                            Drawable drawable = null;
                            if (bitmap != null)
                                drawable = new BitmapDrawable(MainActivity.this.getResources(), bitmap);
                            else
                                drawable = new ColorDrawable(Color.BLUE);
                            DrawableCache.getInstance().put(i, drawable);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        //affiche le viewpager
                        //startMainScreen();
                    }
                }.execute();
            }
        });
    }*/
    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Toast.makeText(this, "Open " + viewItemList.get(viewHolder.getLayoutPosition()).getText(), Toast.LENGTH_SHORT).show();
        sendMessage("lien", viewItemList.get(viewHolder.getLayoutPosition()).getUrl());
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

}