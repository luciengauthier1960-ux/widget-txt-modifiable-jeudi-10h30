package com.example.txtwidget;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Petit éditeur : s'ouvre quand on touche le widget, et enregistre dans le fichier choisi. */
public class EditActivity extends Activity {

    private EditText editText;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        editText = findViewById(R.id.edit_text);

        String uriStr = Prefs.getUri(this);
        if (uriStr == null) {
            Toast.makeText(this, R.string.no_file, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        uri = Uri.parse(uriStr);

        if (savedInstanceState == null) {
            try {
                editText.setText(TxtReader.readText(this, true));
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.read_error, String.valueOf(e.getMessage())),
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    private void save() {
        // Le fichier est réécrit en UTF-8
        byte[] data = editText.getText().toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
            if (out == null) {
                throw new IOException("?");
            }
            out.write(data);
            out.flush();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.save_error, String.valueOf(e.getMessage())),
                    Toast.LENGTH_LONG).show();
            return;
        }
        TxtWidgetProvider.refreshAll(this);
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
