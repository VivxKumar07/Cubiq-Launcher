package net.kdt.pojavlaunch.customcontrols.mouse;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.setting.AllStaticSettings;
import com.movtery.zalithlauncher.utils.ZHTools;
import com.movtery.zalithlauncher.utils.image.Dimension;

import net.kdt.pojavlaunch.GrabListener;

import org.lwjgl.glfw.CallbackBridge;

public class Touchpad extends View implements GrabListener, AbstractTouchpad {
    private boolean mDisplayState;
    private Drawable mMousePointerDrawable;
    private float mMouseX, mMouseY;

    public Touchpad(@NonNull Context context) {
        this(context, null);
    }

    public Touchpad(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void _enable() {
        setVisibility(VISIBLE);
        int w = currentDisplayMetrics != null ? currentDisplayMetrics.widthPixels : getWidth();
        int h = currentDisplayMetrics != null ? currentDisplayMetrics.heightPixels : getHeight();
        placeMouseAt(w / 2f, h / 2f);
    }

    private void _disable() {
        setVisibility(GONE);
    }

    public boolean switchState() {
        mDisplayState = !mDisplayState;
        if (!CallbackBridge.isGrabbing()) {
            if (mDisplayState) _enable();
            else _disable();
        }
        return mDisplayState;
    }

    public void placeMouseAt(float x, float y) {
        mMouseX = x;
        mMouseY = y;
        updateMousePosition();
    }

    private void sendMousePosition() {
        CallbackBridge.sendCursorPos((mMouseX * AllStaticSettings.scaleFactor), (mMouseY * AllStaticSettings.scaleFactor));
    }

    private void updateMousePosition() {
        sendMousePosition();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMousePointerDrawable == null) return;
        canvas.save();
        canvas.translate(mMouseX, mMouseY);
        mMousePointerDrawable.draw(canvas);
        canvas.restore();
    }

    private void init() {
        updateMouseDrawable();
        setFocusable(false);
        setDefaultFocusHighlightEnabled(false);
        disable();
        mDisplayState = false;
    }

    public void updateMouseScale() {
        if (mMousePointerDrawable == null) return;
        Dimension mousescale = Dimension.resizeWithRatio(
                mMousePointerDrawable.getIntrinsicWidth(),
                mMousePointerDrawable.getIntrinsicHeight(),
                AllSettings.getMouseScale().getValue()
        );
        mMousePointerDrawable.setBounds(0, 0, Math.max(8, (int) (mousescale.width * 0.5)), Math.max(8, (int) (mousescale.height * 0.5)));
    }

    public void updateMouseDrawable() {
        mMousePointerDrawable = ZHTools.customMouse(getContext());
        updateMouseScale();
    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        post(() -> updateGrabState(isGrabbing));
    }

    private void updateGrabState(boolean isGrabbing) {
        if (!isGrabbing) {
            if (mDisplayState && getVisibility() != VISIBLE) _enable();
            if (!mDisplayState && getVisibility() == VISIBLE) _disable();
        } else {
            if (getVisibility() != View.GONE) _disable();
        }
    }

    @Override
    public boolean getDisplayState() {
        return mDisplayState;
    }

    @Override
    public void applyMotionVector(float x, float y) {
        int maxW = currentDisplayMetrics != null ? currentDisplayMetrics.widthPixels : Math.max(1, getWidth());
        int maxH = currentDisplayMetrics != null ? currentDisplayMetrics.heightPixels : Math.max(1, getHeight());
        mMouseX = Math.max(0, Math.min(maxW, mMouseX + x * (AllSettings.getMouseSpeed().getValue() / 100f)));
        mMouseY = Math.max(0, Math.min(maxH, mMouseY + y * (AllSettings.getMouseSpeed().getValue() / 100f)));
        updateMousePosition();
    }

    @Override
    public void enable(boolean supposed) {
        if (mDisplayState) return;
        mDisplayState = true;
        if (supposed && CallbackBridge.isGrabbing()) return;
        _enable();
    }

    @Override
    public void disable() {
        if (!mDisplayState) return;
        mDisplayState = false;
        _disable();
    }
}

