package com.example.txtwidget;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

/** Fournit au widget la liste des lignes du fichier (ce qui permet le défilement). */
public class TxtWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    private static class Factory implements RemoteViewsFactory {
        private final Context ctx;
        private List<String> lines = new ArrayList<>();

        Factory(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public void onCreate() {
            lines = TxtReader.readLines(ctx);
        }

        @Override
        public void onDataSetChanged() {
            lines = TxtReader.readLines(ctx);
        }

        @Override
        public void onDestroy() {
            lines.clear();
        }

        @Override
        public int getCount() {
            return lines.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(ctx.getPackageName(), R.layout.widget_item);
            if (position >= 0 && position < lines.size()) {
                rv.setTextViewText(R.id.item_text, lines.get(position));
            }
            // Un appui sur une ligne déclenche l'action définie par le widget (ouvrir l'éditeur)
            rv.setOnClickFillInIntent(R.id.item_text, new Intent());
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
