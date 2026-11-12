package com.example.txtwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

public class TxtWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "com.example.txtwidget.ACTION_REFRESH";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, manager, id);
        }
        manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            refreshAll(context);
        }
    }

    /** Relit le fichier et redessine tous les widgets posés sur l'écran d'accueil. */
    public static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, TxtWidgetProvider.class));
        if (ids.length == 0) {
            return;
        }
        for (int id : ids) {
            updateWidget(context, manager, id);
        }
        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        // Liste défilante des lignes du fichier
        Intent serviceIntent = new Intent(context, TxtWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_list, serviceIntent);
        views.setEmptyView(R.id.widget_list, R.id.widget_empty);

        // Titre = nom du fichier ; un appui ouvre l'application pour changer de fichier
        String name = Prefs.getName(context);
        views.setTextViewText(R.id.widget_title,
                name != null ? name : context.getString(R.string.widget_title_default));
        PendingIntent open = PendingIntent.getActivity(
                context, 0, new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_title, open);

        // Crayon et appui sur le texte : ouvrent l'éditeur
        Intent editIntent = new Intent(context, EditActivity.class);
        PendingIntent editPending = PendingIntent.getActivity(
                context, 2, editIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_edit, editPending);

        int templateFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            templateFlags |= PendingIntent.FLAG_MUTABLE;
        }
        PendingIntent template = PendingIntent.getActivity(context, 1, editIntent, templateFlags);
        views.setPendingIntentTemplate(R.id.widget_list, template);

        // Bouton d'actualisation
        Intent refresh = new Intent(context, TxtWidgetProvider.class);
        refresh.setAction(ACTION_REFRESH);
        PendingIntent refreshPending = PendingIntent.getBroadcast(
                context, 0, refresh,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPending);

        manager.updateAppWidget(appWidgetId, views);
    }
}
