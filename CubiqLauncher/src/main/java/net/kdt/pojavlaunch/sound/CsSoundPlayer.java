package net.kdt.pojavlaunch.sound;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import com.movtery.zalithlauncher.R;

/**
 * CS Launcher sound system.
 *
 * <p>One shared {@link SoundPool} for short UI sounds (welcome chime, future
 * interaction cues). Sounds live in {@code res/raw/}; the welcome popup calls
 * {@link #playWelcome(Context)} and this class decides whether a file exists —
 * the sound is dropped in later, and until it lands the call is a silent no-op
 * with a single log line. No crashes, no missing-resource errors, ever.
 *
 * <p>Rules kept from the codebase: no work on the game path, everything
 * wrapped so a broken OEM audio stack cannot take the UI down, and the pool
 * is built lazily on first use instead of at app start.
 */
public final class CsSoundPlayer {
    private static final String TAG = "CsSound";

    /** Raw resource name the welcome popup plays. Drop the file at res/raw/cs_plus_welcome.* */
    private static final String WELCOME_SOUND_NAME = "cs_plus_welcome";

    private static SoundPool sPool;
    private static Integer sWelcomeId;

    private CsSoundPlayer() { /* no instances */ }

    /** Play the CS Launcher Plus welcome chime, if the sound file is bundled. */
    public static void playWelcome(Context ctx) {
        try {
            int id = welcomeId(ctx);
            if (id <= 0) return;
            SoundPool pool = pool();
            if (pool == null) return;
            pool.play(id, 0.9f, 0.9f, 1, 0, 1f);
        } catch (Throwable t) {
            Log.i(TAG, "welcome sound unavailable (" + t.getClass().getSimpleName() + ")");
        }
    }

    /** Registers the welcome sound once; resolves to 0 when the file is not bundled yet. */
    private static synchronized int welcomeId(Context ctx) {
        if (sWelcomeId != null) return sWelcomeId;
        sWelcomeId = 0;
        try {
            Resources res = ctx.getResources();
            int raw = res.getIdentifier(WELCOME_SOUND_NAME, "raw", ctx.getPackageName());
            if (raw == 0) {
                Log.i(TAG, "welcome sound not bundled yet — add res/raw/" + WELCOME_SOUND_NAME);
                return 0;
            }
            SoundPool pool = pool();
            if (pool != null) sWelcomeId = pool.load(ctx, raw, 1);
        } catch (Throwable ignored) {
            sWelcomeId = 0;
        }
        return sWelcomeId;
    }

    private static synchronized SoundPool pool() {
        if (sPool != null) return sPool;
        try {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            sPool = new SoundPool.Builder()
                    .setMaxStreams(2)
                    .setAudioAttributes(attrs)
                    .build();
        } catch (Throwable t) {
            Log.w(TAG, "SoundPool unavailable", t);
            return null;
        }
        return sPool;
    }

    /** Release the pool (e.g. low memory). Safe to call any time. */
    public static synchronized void release() {
        if (sPool != null) {
            try { sPool.release(); } catch (Throwable ignored) {}
            sPool = null;
        }
        sWelcomeId = null;
    }
}
