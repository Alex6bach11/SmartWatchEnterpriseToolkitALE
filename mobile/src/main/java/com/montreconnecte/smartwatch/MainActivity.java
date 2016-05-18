package com.montreconnecte.smartwatch;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Sélection des apps");
        setResult(RESULT_CANCELED);

        ListView list_apps = (ListView)findViewById(R.id.list_apps);
        PackageManager manager = getPackageManager();
        List<PackageInfo> packageInfos = manager.getInstalledPackages(0);
        final ArrayList<AppDescription> apps = new ArrayList<>();
        AppDescription app;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        for(PackageInfo p : packageInfos)
        {
            if(manager.getLaunchIntentForPackage(p.packageName) != null)
            {
                app = new AppDescription();
                app.setName(manager.getApplicationLabel(p.applicationInfo).toString());
                app.setIcon(p.applicationInfo.loadIcon(manager));
                app.setPackageName(p.packageName);
                app.setChecked(preferences.getBoolean(p.packageName, false));
                apps.add(app);
            }
        }

        Collections.sort(apps);
        list_apps.setAdapter(new ListAppsAdapter(this, android.R.layout.simple_list_item_1, apps));

        list_apps.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Switch app_switch = (Switch) view.findViewById(R.id.app_switch);
                boolean checked = !app_switch.isChecked();
                app_switch.setChecked(checked);
                apps.get(position).setChecked(checked);
                editor.putBoolean(apps.get(position).getPackageName(), checked);
            }
        });
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

//Code par defaut/présent a la création du projet
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

/*    @Override
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
    }*/


}
