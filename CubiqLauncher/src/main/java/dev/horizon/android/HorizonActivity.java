package dev.horizon.android;

import android.app.Instrumentation;
import android.app.NativeActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.movtery.zalithlauncher.ui.control.gyroscope.Zl1GyroControl;
import com.movtery.zalithlauncher.setting.AllSettings;

import java.io.File;

/**
 * Native host activity for Horizon (Minecraft Rust Edition).
 * Runs in isolated :horizon process.
 * Handles SAF document callbacks, ZL1 Gyroscope, and seamless direct Minecraft Rust launch without exposing launcher UI.
 */
public final class HorizonActivity extends NativeActivity {
    static {
        try {
            File filesDir = new File("/data/user/0/com.cubiq.app/files");
            if (!filesDir.exists()) {
                filesDir = new File("/data/data/com.cubiq.app/files");
            }
            if (filesDir.exists()) {
                String path = filesDir.getAbsolutePath();
                try {
                    com.movtery.zalithlauncher.bridge.ZLBridge.chdir(path);
                } catch (Throwable ignored) {}
                android.system.Os.setenv("HOME", path, true);
                android.system.Os.setenv("HORIZON_HOME", path, true);
                android.system.Os.setenv("XDG_DATA_HOME", path, true);
            }
        } catch (Throwable ignored) {}
        System.loadLibrary("horizon_android");
    }

    private native void nativeDocumentResult(int request, int result, String uri);

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mDirectLaunchRequested = false;
    private boolean mTriggerDispatched = false;
    private FrameLayout mCoverOverlay = null;
    private Zl1GyroControl mZl1GyroControl = null;
    private volatile boolean mIsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            File filesDir = getFilesDir();
            if (filesDir != null) {
                String path = filesDir.getAbsolutePath();
                try {
                    com.movtery.zalithlauncher.bridge.ZLBridge.chdir(path);
                } catch (Throwable ignored) {}
                android.system.Os.setenv("HOME", path, true);
                android.system.Os.setenv("HORIZON_HOME", path, true);
                android.system.Os.setenv("XDG_DATA_HOME", path, true);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setTitle("Minecraft Rust Edition");

        // Clear any stale android-error.txt before launching
        try {
            File errFile = new File(getFilesDir(), "android-error.txt");
            if (errFile.exists()) {
                errFile.delete();
            }
        } catch (Exception ignored) {}

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("EXTRA_DIRECT_LAUNCH", false)) {
            mDirectLaunchRequested = true;
        }

        // 1. Setup seamless full-screen splash cover to eliminate Horizon Launcher UI entirely
        setupSeamlessCover();

        // 2. Initialize authentic ZL1 Gyroscope Mechanism for Rust Edition
        try {
            mZl1GyroControl = new Zl1GyroControl(this);
            mZl1GyroControl.setOnDeltaListener((deltaX, deltaY) -> {
                if (mIsActive) {
                    injectGyroMotion(deltaX, deltaY);
                }
                return kotlin.Unit.INSTANCE;
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void setupSeamlessCover() {
        if (!mDirectLaunchRequested) return;

        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        mCoverOverlay = new FrameLayout(this);
        mCoverOverlay.setBackgroundColor(Color.parseColor("#101014"));
        mCoverOverlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout centerBox = new LinearLayout(this);
        centerBox.setOrientation(LinearLayout.VERTICAL);
        centerBox.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams boxParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        centerBox.setLayoutParams(boxParams);

        TextView titleText = new TextView(this);
        titleText.setText("Minecraft Rust Edition");
        titleText.setTextColor(Color.WHITE);
        titleText.setTextSize(20);
        titleText.setTypeface(Typeface.DEFAULT_BOLD);
        titleText.setGravity(Gravity.CENTER);

        TextView subText = new TextView(this);
        subText.setText("Loading Minecraft Rust 26.3...");
        subText.setTextColor(Color.parseColor("#55FF55"));
        subText.setTextSize(13);
        subText.setPadding(0, 16, 0, 24);
        subText.setGravity(Gravity.CENTER);

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);

        centerBox.addView(titleText);
        centerBox.addView(subText);
        centerBox.addView(spinner);
        mCoverOverlay.addView(centerBox);

        decor.addView(mCoverOverlay);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsActive = true;
        if (mZl1GyroControl != null && AllSettings.INSTANCE.getGyroscopeControl().getValue()) {
            mZl1GyroControl.enable(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsActive = false;
        if (mZl1GyroControl != null) {
            mZl1GyroControl.disable();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && mDirectLaunchRequested && !mTriggerDispatched) {
            mTriggerDispatched = true;

            // Trigger direct play through Instrumentation worker thread
            new Thread(() -> {
                int[] delays = {100, 250, 450, 750, 1200, 1800};
                for (int delay : delays) {
                    SystemClock.sleep(delay);
                    dispatchInstrumentationPlay();
                }

                // Smoothly dismiss cover overlay after launch trigger has fired
                mHandler.postDelayed(() -> {
                    if (mCoverOverlay != null && mCoverOverlay.getParent() != null) {
                        mCoverOverlay.animate()
                                .alpha(0f)
                                .setDuration(350)
                                .withEndAction(() -> {
                                    ViewGroup parent = (ViewGroup) mCoverOverlay.getParent();
                                    if (parent != null) {
                                        parent.removeView(mCoverOverlay);
                                    }
                                });
                    }
                }, 1000);
            }).start();
        }
    }

    private void dispatchInstrumentationPlay() {
        try {
            View decorView = getWindow().getDecorView();
            int width = decorView.getWidth();
            int height = decorView.getHeight();
            if (width <= 0) width = getResources().getDisplayMetrics().widthPixels;
            if (height <= 0) height = getResources().getDisplayMetrics().heightPixels;

            // Horizon Android 0.7.6 Play button: situated at 11% from left, 90% from top
            float clickX = width * 0.11f;
            float clickY = height * 0.90f;

            long downTime = SystemClock.uptimeMillis();
            MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, clickX, clickY, 0);
            down.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);
            MotionEvent up = MotionEvent.obtain(downTime, downTime + 30, MotionEvent.ACTION_UP, clickX, clickY, 0);
            up.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);

            Instrumentation inst = new Instrumentation();
            inst.sendPointerSync(down);
            inst.sendPointerSync(up);
            down.recycle();
            up.recycle();

            // Also send Keycodes to trigger start
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BUTTON_A);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        } catch (Throwable t) {
            // Fallback to View dispatch if instrumentation is restricted
            mHandler.post(this::dispatchViewPlayFallback);
        }
    }

    private void dispatchViewPlayFallback() {
        try {
            View decorView = getWindow().getDecorView();
            int width = decorView.getWidth();
            int height = decorView.getHeight();
            if (width <= 0) width = getResources().getDisplayMetrics().widthPixels;
            if (height <= 0) height = getResources().getDisplayMetrics().heightPixels;

            float clickX = width * 0.11f;
            float clickY = height * 0.90f;
            long downTime = SystemClock.uptimeMillis();

            MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, clickX, clickY, 0);
            decorView.dispatchTouchEvent(down);
            down.recycle();

            MotionEvent up = MotionEvent.obtain(downTime, downTime + 30, MotionEvent.ACTION_UP, clickX, clickY, 0);
            decorView.dispatchTouchEvent(up);
            up.recycle();

            decorView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_A));
            decorView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BUTTON_A));
            decorView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            decorView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
        } catch (Throwable ignored) {}
    }

    private void injectGyroMotion(float dx, float dy) {
        try {
            View decorView = getWindow().getDecorView();
            int width = decorView.getWidth();
            int height = decorView.getHeight();
            if (width <= 0) width = getResources().getDisplayMetrics().widthPixels;
            if (height <= 0) height = getResources().getDisplayMetrics().heightPixels;

            // Inject touch camera swipe on the right half of the screen
            float startX = width * 0.75f;
            float startY = height * 0.50f;
            float endX = startX + dx;
            float endY = startY + dy;

            long time = SystemClock.uptimeMillis();
            MotionEvent move = MotionEvent.obtain(time, time, MotionEvent.ACTION_MOVE, endX, endY, 0);
            move.setSource(android.view.InputDevice.SOURCE_TOUCHSCREEN);
            decorView.dispatchGenericMotionEvent(move);
            decorView.dispatchTouchEvent(move);
            move.recycle();
        } catch (Throwable ignored) {}
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != 4101) return;
        nativeDocumentResult(request, result,
                data == null || data.getData() == null ? null : data.getData().toString());
    }
}
