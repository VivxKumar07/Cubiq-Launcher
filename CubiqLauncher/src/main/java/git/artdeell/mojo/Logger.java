package git.artdeell.mojo;

import android.util.Log;

public class Logger {
    private static final String TAG = "MojoLogger";
    public static void appendToLog(String text) {
        Log.i(TAG, text);
    }
    public static void begin(String logFilePath) {
        Log.i(TAG, "Begin log at: " + logFilePath);
    }
    public interface eventLogListener {
        void onEventLogged(String text);
    }
    public static void setLogListener(eventLogListener logListener) {}
}
