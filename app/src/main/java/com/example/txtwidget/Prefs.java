package com.example.txtwidget;

import android.content.Context;
import android.content.SharedPreferences;

/** Mémorise le fichier choisi (adresse et nom d'affichage) et la couleur du widget. */
final class Prefs {
    private static final String FILE = "txtwidget";
    private static final String KEY_URI = "uri";
    private static final String KEY_NAME = "name";
    private static final String KEY_COLOR = "color";

    /** Jaune clair par défaut. */
    static final int DEFAULT_COLOR = 0xFFFFF59D;

    private Prefs() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static void save(Context c, String uri, String name) {
        prefs(c).edit().putString(KEY_URI, uri).putString(KEY_NAME, name).apply();
    }

    /** Adresse du fichier choisi, ou null si aucun. */
    static String getUri(Context c) {
        return prefs(c).getString(KEY_URI, null);
    }

    /** Nom du fichier choisi, ou null si aucun. */
    static String getName(Context c) {
        return prefs(c).getString(KEY_NAME, null);
    }

    static void saveColor(Context c, int color) {
        prefs(c).edit().putInt(KEY_COLOR, color).apply();
    }

    /** Couleur de fond du widget (ARGB). */
    static int getColor(Context c) {
        return prefs(c).getInt(KEY_COLOR, DEFAULT_COLOR);
    }
}
