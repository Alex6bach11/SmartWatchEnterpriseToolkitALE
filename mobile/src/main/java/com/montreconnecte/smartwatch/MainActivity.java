package com.montreconnecte.smartwatch;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.api.services.tasks.model.Tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    GoogleAccountCredential mCredential;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Tasks API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { TasksScopes.TASKS_READONLY };

    private MakeRequestTask makeRequestTask;
    public String taskListContent;
    public PingTodoListTask pingTodoListTask;
    public PrintTask printTask;
    private List<TaskList> tasklists = null;
    public TaskList taskList = null;
    private GoogleApiClient mGoogleApiClient; //Sert à communiquer avec le wearService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button button = (Button) findViewById(R.id.selectTodoList);
        button.setEnabled(false);
        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();
            System.out.println("créé : mGoogleApiClient");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            System.out.println("Connecté : mGoogleApiClient");
        }
    }

    class SendWearServiceMessage extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendWearServiceMessage(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            SendMessageTask messageTask = new SendMessageTask(path,message);

            messageTask.execute();
        }
    }



    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    public void selectAccountG(View v){

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        chooseGoogleAccount();

    }

    public void selectTodoList(View v){


        if(mCredential != null) {

            if (! isGooglePlayServicesAvailable()) {
                acquireGooglePlayServices();
            }
            else{
                if (isDeviceOnline()) {

                    System.out.println("Lancement de Choose task list");
                    taskList = null;
                    chooseAndSendTaskList();

                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void test(View v)
    {
        ouvrirLien("test");
    }

    public void ouvrirLien(String url){
        Uri uriUrl = Uri.parse("http://www.google.com"/*url*/);
        Intent lancerNavigateur = new Intent(Intent.ACTION_VIEW, uriUrl);
        //lancerNavigateur.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(lancerNavigateur);
    }

    private void chooseGoogleAccount() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        }
        chooseAccount();
    }

    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {


            startActivityForResult(
                   mCredential.newChooseAccountIntent(),
                   REQUEST_ACCOUNT_PICKER);



        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }


    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    System.out.println(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                }                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);

                        makeRequestTask = new MakeRequestTask(mCredential);

                        try {
                            makeRequestTask.execute();
                        } catch(Exception exception){
                            System.out.println("Erreur lors de l'éxécution de l'asynctask ");
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    //Créer une autre asyncTask pour récupérer les items
    private class PrintTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.tasks.Tasks mService = null;

        public PrintTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.tasks.Tasks.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Tasks API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Tasks API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {

            printList();
            return null;
        }

        private void printList(){

            while(taskList==null) {}
            Tasks tasks;
            String res="";
            try {

                tasks = mService.tasks().list(taskList.getId()).execute();

                for (Task task : tasks.getItems()) {
                    res+=task.getTitle()+";";
                }
                new SendWearServiceMessage("mainActivity",res).run();

                System.out.println("TaskListContent set à :" + res);
                taskListContent = res;

            } catch (IOException e) {
                System.out.println("IOException");
            }

        }

    }

    private class PingTodoListTask extends AsyncTask<Void, Void, Void> {

        public PingTodoListTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            while(taskList != null){
                try{
                    Thread.sleep(5000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                System.out.println("contenu envoyé : "+ taskListContent);

                new SendWearServiceMessage("mainActivity",taskListContent).run();

            }

            return null;
        }

    }



    private class SendMessageTask extends AsyncTask<Void, Void, List<String>> {
        private String path;
        private String message;

        public SendMessageTask(String path, String message) {
            this.path = path;
            this.message = message;
        }


        @Override
        protected List<String> doInBackground(Void... params) {

            NodeApi.GetLocalNodeResult nodes = Wearable.NodeApi.getLocalNode(mGoogleApiClient).await();
            Node node = nodes.getNode();
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes()).await();
            return null;
        }

    }


    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.tasks.Tasks mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.tasks.Tasks.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Tasks API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Tasks API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {

            try {
                return getTaskLists();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the first 10 task lists.
         * @return List of Strings describing task lists, or an empty list if
         *         there are no task lists found.
         * @throws IOException
         */
        private List<String> getTaskLists() throws IOException {
            // List up to 10 task lists.

            TaskLists result = mService.tasklists().list()
                    .setMaxResults(Long.valueOf(10))
                    .execute();
            tasklists = result.getItems();
            System.out.println("tasklists renseigné");

            return null;
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(List<String> output) {
            Button button = (Button) findViewById(R.id.selectTodoList);
            button.setEnabled(true);
        }

        @Override
        protected void onCancelled() {
            //mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    System.out.println("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                System.out.println("Request cancelled.");
            }
        }
    }

    private void chooseAndSendTaskList(){
        AlertDialog.Builder builder;

        System.out.println("chooseTaskList");
        if(tasklists != null) {
            System.out.println("tasklists != null");

            CharSequence choices[] = new CharSequence[tasklists.size()];

            int i = 0;
            for (TaskList taskList : tasklists) {
                choices[i] = taskList.getTitle();
                i++;
            }

            builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choisissez une liste de tâches");
            builder.setItems(choices, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on colors[which]
                    taskList = tasklists.get(which);

                }
            });
            builder.show();

            printTask = new PrintTask(mCredential);

            printTask.execute();

            pingTodoListTask = new PingTodoListTask();

            pingTodoListTask.execute();
        }
    }
}
