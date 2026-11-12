package com.example.txtwidget;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Lit le fichier texte choisi. */
final class TxtReader {
    private static final int MAX_BYTES = 2 * 1024 * 1024; // 2 Mo
    private static final int MAX_LINES = 5000;

    private TxtReader() {}

    /**
     * Lit tout le fichier choisi.
     *
     * @param failIfTooBig si vrai, lève une erreur au lieu de tronquer un fichier trop gros
     *                     (indispensable avant une modification, pour ne rien perdre).
     */
    static String readText(Context ctx, boolean failIfTooBig) throws IOException {
        String uriStr = Prefs.getUri(ctx);
        if (uriStr == null) {
            throw new IOException(ctx.getString(R.string.no_file));
        }
        try (InputStream in = ctx.getContentResolver().openInputStream(Uri.parse(uriStr))) {
            if (in == null) {
                throw new IOException("?");
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            int total = 0;
            while ((n = in.read(buf)) > 0) {
                if (total + n > MAX_BYTES) {
                    if (failIfTooBig) {
                        throw new IOException(ctx.getString(R.string.edit_too_big));
                    }
                    bos.write(buf, 0, MAX_BYTES - total);
                    break;
                }
                bos.write(buf, 0, n);
                total += n;
            }
            byte[] data = bos.toByteArray();

            String text;
            try {
                // UTF-8 strict d'abord ; si ce n'est pas de l'UTF-8, on retombe sur Windows-1252
                text = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(data))
                        .toString();
            } catch (CharacterCodingException e) {
                text = new String(data, Charset.forName("windows-1252"));
            }
            if (text.startsWith("﻿")) {
                text = text.substring(1);
            }
            return text;
        }
    }

    /** Lignes à afficher dans le widget. */
    static List<String> readLines(Context ctx) {
        List<String> out = new ArrayList<>();
        if (Prefs.getUri(ctx) == null) {
            return out;
        }
        try {
            String text = readText(ctx, false);
            String[] parts = text.split("\r\n|\r|\n", -1);
            for (String line : parts) {
                if (out.size() >= MAX_LINES) {
                    break;
                }
                // Une ligne vide doit garder sa hauteur dans le widget
                out.add(line.isEmpty() ? " " : line);
            }
        } catch (Exception e) {
            out.clear();
            out.add(ctx.getString(R.string.read_error, String.valueOf(e.getMessage())));
        }
        return out;
    }
}
