package com.montreconnecte.smartwatch;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Map;


public class ListWidgetProvider implements RemoteViewsService.RemoteViewsFactory
{
    final String TAG = "ListWidgetProvider";
    private ArrayList<AppDescription> apps = new ArrayList();
    private Context context = null;
    private int appWidgetId;

    public ListWidgetProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }

    private void populateListItem() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, ?> keys = preferences.getAll();
        PackageManager manager = context.getPackageManager();
        PackageInfo packageInfo;
        AppDescription app;

        for(Map.Entry<String, ?> entry : keys.entrySet())
        {
            try
            {
                if(entry.getValue() instanceof Boolean && (Boolean)entry.getValue())
                {
                    packageInfo = manager.getPackageInfo(entry.getKey(), 0);
                    app = new AppDescription();
                    app.setName(manager.getApplicationLabel(packageInfo.applicationInfo).toString());
                    app.setIcon(packageInfo.applicationInfo.loadIcon(manager));
                    app.setPackageName(packageInfo.packageName);
                    app.setChecked(preferences.getBoolean(packageInfo.packageName, false));
                    apps.add(app);
                }
            }
            catch(Exception e)
            {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onCreate()
    {
    }

    @Override
    public void onDataSetChanged()
    {

    }

    @Override
    public void onDestroy()
    {

    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.list_row);
        AppDescription app = apps.get(position);
        remoteView.setTextViewText(R.id.app_name, app.getName());
        remoteView.setImageViewBitmap(R.id.app_icon, ((BitmapDrawable)app.getIcon()).getBitmap());

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView()
    {
        return null;
    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }
}
