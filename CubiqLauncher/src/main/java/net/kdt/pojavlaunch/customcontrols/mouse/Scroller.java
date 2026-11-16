package net.kdt.pojavlaunch.customcontrols.mouse;

import org.lwjgl.glfw.CallbackBridge;

public class Scroller {
    private float mScrollOvershootH, mScrollOvershootV;
    private final float mScrollThreshold;

    public Scroller(float mScrollThreshold) {
        this.mScrollThreshold = mScrollThreshold;
    }

    public void performScroll(float dx, float dy) {
        float hScroll = (dx / mScrollThreshold) + mScrollOvershootH;
        float vScroll = (dy / mScrollThreshold) + mScrollOvershootV;
        int hScrollRound = (int) hScroll, vScrollRound = (int) vScroll;
        if (hScrollRound != 0 || vScrollRound != 0) CallbackBridge.sendScroll(hScroll, vScroll);
        mScrollOvershootH = hScroll - hScrollRound;
        mScrollOvershootV = vScroll - vScrollRound;
    }

    public void performScroll(float[] vector) {
        performScroll(vector[0], vector[1]);
    }

    public void resetScrollOvershoot() {
        mScrollOvershootH = mScrollOvershootV = 0f;
    }
}

