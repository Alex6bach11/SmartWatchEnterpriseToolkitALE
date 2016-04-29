package com.montreconnecte.smartwatch;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by kango_000 on 27/04/2016.
 */
public class ListViewItem {

    private int imageRes;
    private String text;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String url;
    public ListViewItem(String text, String url) {
        this.text = text;
        this.url = url;
    }


}
