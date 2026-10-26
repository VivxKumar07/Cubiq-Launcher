package net.kdt.pojavlaunch;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class Tools {
    public static final Gson GLOBAL_GSON = new GsonBuilder().setPrettyPrinting().create();
    public static DisplayMetrics currentDisplayMetrics;
    static {
        try {
            currentDisplayMetrics = Resources.getSystem().getDisplayMetrics();
        } catch (Throwable ignored) {}
    }
    public static boolean customMouse = false;

    public static float dpToPx(float dp) {
        if (currentDisplayMetrics != null) {
            return dp * currentDisplayMetrics.density;
        }
        return dp * 2.0f;
    }

    public static float pxToDp(float px) {
        if (currentDisplayMetrics != null && currentDisplayMetrics.density != 0) {
            return px / currentDisplayMetrics.density;
        }
        return px / 2.0f;
    }

    public static boolean isValidString(String s) {
        return s != null && !s.isEmpty();
    }

    public static String read(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
        }
        return sb.toString();
    }

    public static String read(String path) throws IOException {
        return read(new File(path));
    }

    public static String read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return read(fis);
        }
    }

    public static void write(String path, String content) throws IOException {
        write(new File(path), content);
    }

    public static void write(File file, String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }

    public static void showError(Context ctx, Throwable e) {
        showError(ctx, e, false);
    }

    public static void showError(Context ctx, Throwable e, boolean exitIfOk) {
        Log.e("Tools", "Error", e);
        if (ctx != null) {
            Toast.makeText(ctx, e.getMessage() != null ? e.getMessage() : e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void showError(Context ctx, int resId, Throwable e) {
        Log.e("Tools", "Error res: " + resId, e);
        if (ctx != null) {
            Toast.makeText(ctx, ctx.getString(resId) + (e != null ? ": " + e.getMessage() : ""), Toast.LENGTH_LONG).show();
        }
    }

    public static void showError(Context ctx, String msg, Throwable e) {
        Log.e("Tools", msg, e);
        if (ctx != null) {
            Toast.makeText(ctx, msg + (e != null ? ": " + e.getMessage() : ""), Toast.LENGTH_LONG).show();
        }
    }
}

