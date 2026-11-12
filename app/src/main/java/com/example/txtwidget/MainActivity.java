package com.example.txtwidget;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int REQ_PICK = 1;

    private TextView currentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentFile = findViewById(R.id.current_file);
        findViewById(R.id.btn_pick).setOnClickListener(v -> pickFile());
        findViewById(R.id.btn_refresh).setOnClickListener(v -> TxtWidgetProvider.refreshAll(this));

        updateLabel();
        buildSwatches();
    }

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQ_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_PICK || resultCode != RESULT_OK || data == null) {
            return;
        }
        Uri uri = data.getData();
        if (uri == null) {
            return;
        }
        try {
            // Garde l'accès (lecture et écriture) après le redémarrage du téléphone
            getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException e) {
            try {
                // Fichier en lecture seule : on garde au moins la lecture
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Certains fournisseurs de fichiers n'autorisent pas la permission durable
            }
        }
        Prefs.save(this, uri.toString(), queryName(uri));
        TxtWidgetProvider.refreshAll(this);
        updateLabel();
    }

    private String queryName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (i >= 0) {
                    String name = c.getString(i);
                    if (name != null) {
                        return name;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : uri.toString();
    }

    // Couleurs claires, pour que le texte sombre reste lisible
    private static final int[] COLORS = {
            0xFFFFF59D, // jaune
            0xFFFFFFFF, // blanc
            0xFFBBDEFB, // bleu clair
            0xFFC8E6C9, // vert clair
            0xFFF8BBD0, // rose
            0xFFFFE0B2, // orange clair
            0xFFE0E0E0, // gris clair
            0xFFE1BEE7  // lavande
    };

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    /** Dessine les pastilles de couleur ; un appui change la couleur du widget. */
    private void buildSwatches() {
        GridLayout grid = findViewById(R.id.color_grid);
        grid.removeAllViews();
        int current = Prefs.getColor(this);
        for (int color : COLORS) {
            boolean selected = color == current;
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(color);
            shape.setStroke(dp(selected ? 4 : 1), selected ? 0xFF3F51B5 : 0xFF888888);

            View swatch = new View(this);
            swatch.setBackground(shape);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = dp(48);
            lp.height = dp(48);
            lp.setMargins(dp(6), dp(6), dp(6), dp(6));
            swatch.setLayoutParams(lp);
            swatch.setOnClickListener(v -> {
                Prefs.saveColor(this, color);
                TxtWidgetProvider.refreshAll(this);
                buildSwatches();
            });
            grid.addView(swatch);
        }
    }

    private void updateLabel() {
        String name = Prefs.getName(this);
        currentFile.setText(name != null
                ? getString(R.string.current_file, name)
                : getString(R.string.no_file));
    }
}
