package git.artdeell.mojo;
import com.movtery.zalithlauncher.R;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import androidx.appcompat.app.AlertDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.movtery.zalithlauncher.path.PathManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Tools {
    public static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    public static String DIR_DATA = "";
    public static String CTRLMAP_PATH = "";
    public static String CTRLDEF_FILE = "";
    public static DisplayMetrics currentDisplayMetrics = new DisplayMetrics();
    public static final Gson GLOBAL_GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init(Context context) {
        try {
            DIR_DATA = context.getFilesDir().getAbsolutePath();
            File ctrlDir = PathManager.Companion.getDIR_CONTROL_LAYOUTS();
            if (!ctrlDir.exists()) ctrlDir.mkdirs();
            CTRLMAP_PATH = ctrlDir.getAbsolutePath();
            CTRLDEF_FILE = new File(ctrlDir, "mojo_layout.json").getAbsolutePath();
            if (context instanceof Activity) {
                getDisplayMetrics((Activity) context);
            } else if (context.getResources() != null) {
                currentDisplayMetrics = context.getResources().getDisplayMetrics();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.getDisplay().getRealMetrics(displayMetrics);
        } else {
            activity.getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        }
        currentDisplayMetrics = displayMetrics;
        return displayMetrics;
    }

    public static void setInsetsMode(Activity activity, boolean fullscreen, boolean ignoreNotch) {
        if (activity == null || activity.getWindow() == null) return;
        try {
            android.view.View decorView = activity.getWindow().peekDecorView();
            if (decorView == null) {
                decorView = activity.getWindow().getDecorView();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController controller = decorView != null ? decorView.getWindowInsetsController() : null;
                if (controller == null) {
                    controller = activity.getWindow().getInsetsController();
                }
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } else if (decorView != null) {
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static float dpToPx(float dp) {
        if (currentDisplayMetrics == null || currentDisplayMetrics.density == 0) return dp;
        return dp * currentDisplayMetrics.density;
    }

    public static float pxToDp(float px) {
        if (currentDisplayMetrics == null || currentDisplayMetrics.density == 0) return px;
        return px / currentDisplayMetrics.density;
    }

    public static boolean isValidString(String string) {
        return string != null && !string.isEmpty();
    }

    public static String read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] b = new byte[(int) file.length()];
            fis.read(b);
            return new String(b, StandardCharsets.UTF_8);
        }
    }

    public static String read(String path) throws IOException {
        return read(new File(path));
    }

    public static void write(String path, String data) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            MAIN_HANDLER.post(runnable);
        }
    }

    public static void showError(Context ctx, Throwable e, boolean exitIfOk) { showError(ctx, e); }
    public static void showError(Context ctx, Throwable e) {
        showError(ctx, e != null ? e.getMessage() : "Unknown error");
    }

    public static void showError(Context ctx, String msg) {
        runOnUiThread(() -> {
            if (ctx == null) return;
            try {
                new AlertDialog.Builder(ctx)
                    .setTitle("Notice")
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            } catch (Throwable ignored) {}
        });
    }

    public static void checkStorageInteractive(Context ctx) {}
}
