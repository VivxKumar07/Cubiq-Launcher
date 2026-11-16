package com.movtery.zalithlauncher.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.path.PathManager;
import com.movtery.zalithlauncher.setting.AllSettings;

import java.io.File;

public final class ZHTools {
    private ZHTools() {
    }

    public synchronized static Drawable customMouse(Context context) {
        File mouseFile = getCustomMouse();
        if (mouseFile != null && mouseFile.exists() && mouseFile.isFile()) {
            Drawable drawable = Drawable.createFromPath(mouseFile.getAbsolutePath());
            if (drawable != null) {
                return drawable;
            }
        }
        return ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_mouse_pointer, context.getTheme());
    }

    public static File getCustomMouse() {
        String customMouse = AllSettings.getCustomMouse().getValue();
        if (customMouse == null || customMouse.isEmpty()) return null;
        return new File(PathManager.DIR_MOUSE_POINTER, customMouse);
    }

    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}

