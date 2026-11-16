package net.kdt.pojavlaunch.customcontrols.mouse;

import android.os.Handler;

public abstract class ValidatorGesture implements Runnable {
    private final Handler mHandler;
    private boolean mGestureActive;

    public ValidatorGesture(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public final boolean submit() {
        if (mGestureActive) return false;
        mHandler.postDelayed(this, getGestureDelay());
        mGestureActive = true;
        return true;
    }

    public final void cancel(boolean isSwitching) {
        if (!mGestureActive) return;
        mHandler.removeCallbacks(this);
        onGestureCancelled(isSwitching);
        mGestureActive = false;
    }

    @Override
    public final void run() {
        if (checkAndTrigger()) return;
        mGestureActive = false;
        onGestureCancelled(false);
    }

    protected abstract int getGestureDelay();

    public abstract boolean checkAndTrigger();

    public abstract void onGestureCancelled(boolean isSwitching);
}

