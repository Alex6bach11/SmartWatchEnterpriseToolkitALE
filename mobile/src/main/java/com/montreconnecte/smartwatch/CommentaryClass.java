package com.montreconnecte.smartwatch;

/**
 * Created by baptiste on 06/06/16.
 */
public class CommentaryClass {

    /**
     * Permet d'envoyer une image à la montre
     *//*
    protected void sendImage(String url, int position) {
        //télécharge l'image
        Bitmap bitmap = getBitmapFromURL(url);
        if (bitmap != null) {
            Asset asset = createAssetFromBitmap(bitmap);

            //créé un emplacement mémoire "image/[url_image]"
            final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/image/" + position);

            //ajoute la date de mise à jour, important pour que les données soient mises à jour
            putDataMapRequest.getDataMap().putString("timestamp", new Date().toString());

            //ajoute l'image à la requête
            putDataMapRequest.getDataMap().putAsset("image", asset);

            //envoie la donnée à la montre
            if (mApiClient.isConnected())
                Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }
    }

    /**
     * Les bitmap transférés depuis les DataApi doivent être empaquetées en Asset
     *//*
    public static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    /**
     * Récupère une bitmap à partir d'une URL
     *//*
    public static Bitmap getBitmapFromURL(String src) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(src).openConnection();
            connection.setDoInput(true);
            connection.connect();
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (Exception e) {
            // Log exception
            return null;
        }
    }*/

            /*if (path.equals("bonjour")) {

            //Utilise Retrofit pour réaliser un appel REST
            AndroidService androidService = new RestAdapter.Builder()
                    .setEndpoint(AndroidService.ENDPOINT)
                    .build().create(AndroidService.class);

            //Récupère et deserialise le contenu de mon fichier JSON en objet Element
            androidService.getElements(new Callback<List<Element>>() {
                @Override
                public void success(List<Element> elements, Response response) {
                    envoyerListElements(elements);
                }

                @Override
                public void failure(RetrofitError error) {
                }
            });

        }*/


    /**
     * Envoie la liste d'éléments à la montre
     * Envoie de même les images
     * @param elements
     *//*
    private void envoyerListElements(final List<Element> elements) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Envoie des elements et leurs images
                sendElements(elements);
            }
        }).start();
    }*/

    /**
     * Permet d'envoyer une liste d'elements
     *//*
    protected void sendElements(final List<Element> elements) {

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/elements/");

        ArrayList<DataMap> elementsDataMap = new ArrayList<>();

        //envoie chaque élémént 1 par 1
        for (int position = 0; position < elements.size(); ++position) {

            DataMap elementDataMap = new DataMap();
            Element element = elements.get(position);

            //créé un emplacement mémoire "element/[position]"

            //ajoute la date de mi[jase à jour
            elementDataMap.putString("timestamp", new Date().toString());

            //ajoute l'element champ par champ
            elementDataMap.putString("titre", element.getTitre
            elementDataMap.putString("description", element.getDescription());
            elementDataMap.putString("url", element.getUrl());

            //ajoute cette datamap à notre arrayList
            elementsDataMap.add(elementDataMap);

        }

        //place la liste dans la datamap envoyée à la wear
        putDataMapRequest.getDataMap().putDataMapArrayList("/list/",elementsDataMap);

        //envoie la liste à la montre
        if (mApiClient.isConnected())
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());

        //puis envoie les images dans un second temps
        for(int position = 0; position < elements.size(); ++position){
            //charge l'image associée pour l'envoyer en bluetooth
            sendImage(elements.get(position).getUrl(), position);
        }
    }*/


    /********************** PARTIE MONTRE **********************/

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
}
