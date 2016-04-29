package com.montreconnecte.smartwatch;

/**
 * Created by baptiste on 14/04/16.
 */
public class Element {

    public String getUrl() { return url; }

    public String getTitre() { return titre; }

    public String getDescription() { return description; }



    private String titre;
    private String description;
    private String url;

    public Element(String titre, String description, String url) {
        this.titre = titre;
        this.url = url;
        this.description = description;
    }

}
