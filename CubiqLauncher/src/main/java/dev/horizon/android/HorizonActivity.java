package dev.horizon.android;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.movtery.zalithlauncher.R;

/**
 * Native host activity for Horizon (Minecraft Rust Edition).
 * Runs in isolated :horizon process.
 * Handles SAF document callbacks and direct Minecraft Rust launch without exposing launcher UI.
 */
public final class HorizonActivity extends NativeActivity {
    static {
        System.loadLibrary("horizon_android");
    }

    private native void nativeDocumentResult(int request, int result, String uri);

    private FrameLayout mSplashOverlay;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mDirectLaunchRequested = false;
    private boolean mTriggerDispatched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("EXTRA_DIRECT_LAUNCH", false)) {
            mDirectLaunchRequested = true;
            createSplashOverlay();
        }
    }

    private void createSplashOverlay() {
        mSplashOverlay = new FrameLayout(this);
        mSplashOverlay.setBackgroundColor(Color.parseColor("#121318"));
        mSplashOverlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        container.setLayoutParams(params);

        // Horizon Circular Logo
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_horizon_logo);
        int logoSize = (int) (64 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(logoSize, logoSize);
        logoParams.bottomMargin = (int) (18 * getResources().getDisplayMetrics().density);
        container.addView(logo, logoParams);

        // Title
        TextView title = new TextView(this);
        title.setText("Minecraft Rust Edition");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = (int) (6 * getResources().getDisplayMetrics().density);
        container.addView(title, titleParams);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Powered by Rust · Starting Engine...");
        subtitle.setTextColor(Color.parseColor("#9CA3AF"));
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subParams.bottomMargin = (int) (20 * getResources().getDisplayMetrics().density);
        container.addView(subtitle, subParams);

        // Indeterminate Progress Indicator
        ProgressBar progress = new ProgressBar(this);
        int progSize = (int) (32 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams progParams = new LinearLayout.LayoutParams(progSize, progSize);
        container.addView(progress, progParams);

        mSplashOverlay.addView(container);

        // Attach overlay to window decor view
        getWindow().addContentView(mSplashOverlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus && mDirectLaunchRequested && !mTriggerDispatched) {
            mTriggerDispatched = true;

            // Wait 350ms for native window and surface event loop to ready up, then simulate Play trigger
            mHandler.postDelayed(() -> {
                dispatchDirectPlayTrigger();
            }, 350);

            // Wait 1400ms for game engine to take over native window, then smoothly fade out splash
            mHandler.postDelayed(() -> {
                if (mSplashOverlay != null) {
                    mSplashOverlay.animate()
                            .alpha(0f)
                            .setDuration(400)
                            .withEndAction(() -> {
                                if (mSplashOverlay != null && mSplashOverlay.getParent() != null) {
                                    ((ViewGroup) mSplashOverlay.getParent()).removeView(mSplashOverlay);
                                    mSplashOverlay = null;
                                }
                            })
                            .start();
                }
            }, 1400);
        }
    }

    private void dispatchDirectPlayTrigger() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        // Down in lower-left area where Play button is located: [32, h - 100, 320, 60]
        float clickX = 192.0f * dm.density;
        float clickY = dm.heightPixels - (70.0f * dm.density);

        long downTime = SystemClock.uptimeMillis();

        // 1. Dispatch touch event down & up on Play button
        MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, clickX, clickY, 0);
        dispatchTouchEvent(down);
        down.recycle();

        long upTime = downTime + 40;
        MotionEvent up = MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, clickX, clickY, 0);
        dispatchTouchEvent(up);
        up.recycle();

        // 2. Also dispatch Gamepad A (Button::South) & Enter keys
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BUTTON_A));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BUTTON_A));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != 4101) return;
        nativeDocumentResult(request, result,
                data == null || data.getData() == null ? null : data.getData().toString());
    }
}
