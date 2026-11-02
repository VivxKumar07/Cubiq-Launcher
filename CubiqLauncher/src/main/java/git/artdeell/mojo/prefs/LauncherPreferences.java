package git.artdeell.mojo.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import git.artdeell.mojo.Tools;

public class LauncherPreferences {
    public static SharedPreferences DEFAULT_PREF;
    public static float PREF_BUTTONSIZE = 100f;
    public static float PREF_MOUSESCALE = 1f;
    public static float PREF_MOUSESPEED = 1f;
    public static int PREF_LONGPRESS_TRIGGER = 300;
    public static String PREF_DEFAULTCTRL_PATH = "";
    public static boolean PREF_DISABLE_SWAP_HAND = false;
    public static float PREF_SCALE_FACTOR = 1f;
    public static boolean PREF_ENABLE_GYRO = false;
    public static float PREF_GYRO_SENSITIVITY = 1f;
    public static int PREF_GYRO_SAMPLE_RATE = 16;
    public static boolean PREF_GYRO_SMOOTHING = true;
    public static boolean PREF_GYRO_INVERT_X = false;
    public static boolean PREF_GYRO_INVERT_Y = false;
    public static boolean PREF_BUTTON_ALL_CAPS = true;
    public static float PREF_DEADZONE_SCALE = 1f;
    public static short PREF_BUTTON_TRANSPARENCY = 100;
    public static boolean PREF_IGNORE_NOTCH = false;

    public static void loadPreferences(Context ctx) {
        if (DEFAULT_PREF == null && ctx != null) {
            DEFAULT_PREF = ctx.getSharedPreferences("launcher_preferences", Context.MODE_PRIVATE);
        }
        if (DEFAULT_PREF != null) {
            PREF_BUTTONSIZE = DEFAULT_PREF.getInt("buttonscale", 100);
            PREF_BUTTON_ALL_CAPS = DEFAULT_PREF.getBoolean("buttonAllCaps", true);
            PREF_BUTTON_TRANSPARENCY = (short) DEFAULT_PREF.getInt("buttonTransparency", 100);
        }
        if (PREF_DEFAULTCTRL_PATH == null || PREF_DEFAULTCTRL_PATH.isEmpty()) {
            PREF_DEFAULTCTRL_PATH = Tools.CTRLDEF_FILE;
        }
    }
}
