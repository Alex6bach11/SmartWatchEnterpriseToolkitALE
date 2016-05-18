package com.montreconnecte.smartwatch;

import android.graphics.drawable.Drawable;


public class AppDescription implements Comparable<AppDescription>
{
    private String name;
    private String packageName;
    private Drawable icon;
    private boolean checked;

    public AppDescription(String name, String packageName, Drawable icon, boolean checked)
    {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.checked = checked;
    }

    public AppDescription()
    {
        this.name = null;
        this.packageName = null;
        this.icon = null;
        this.checked = false;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPackageName(String packageName)
    {
        this.packageName = packageName;
    }

    public void setIcon(Drawable icon)
    {
        this.icon = icon;
    }

    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    public String getName()
    {
        return name;
    }

    public Drawable getIcon()
    {
        return icon;
    }

    public boolean isChecked()
    {
        return checked;
    }

    @Override
    public int compareTo(AppDescription another)
    {
        return name.compareTo(another.name);
    }

    public String getPackageName()
    {
        return packageName;
    }
}
