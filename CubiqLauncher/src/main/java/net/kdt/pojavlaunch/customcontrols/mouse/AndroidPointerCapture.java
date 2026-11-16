package net.kdt.pojavlaunch.customcontrols.mouse;

import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.movtery.zalithlauncher.setting.AllStaticSettings;

import net.kdt.pojavlaunch.MinecraftGLSurface;
import net.kdt.pojavlaunch.Tools;

import org.lwjgl.glfw.CallbackBridge;

public class AndroidPointerCapture implements ViewTreeObserver.OnWindowFocusChangeListener, View.OnCapturedPointerListener {
    private static final float TOUCHPAD_SCROLL_THRESHOLD = 1;
    private final AbstractTouchpad mTouchpad;
    private final View mHostView;
    private final float mMousePrescale = Tools.dpToPx(1);
    private final PointerTracker mPointerTracker = new PointerTracker();
    private final Scroller mScroller = new Scroller(TOUCHPAD_SCROLL_THRESHOLD);
    private final float[] mVector = mPointerTracker.getMotionVector();

    private int mInputDeviceIdentifier;
    private boolean mDeviceSupportsRelativeAxis;

    public AndroidPointerCapture(AbstractTouchpad touchpad, View hostView) {
        this.mTouchpad = touchpad;
        this.mHostView = hostView;
        hostView.setOnCapturedPointerListener(this);
        hostView.getViewTreeObserver().addOnWindowFocusChangeListener(this);
    }

    private void enableTouchpadIfNecessary() {
        if (!mTouchpad.getDisplayState()) mTouchpad.enable(true);
    }

    public void handleAutomaticCapture() {
        if (!mHostView.hasWindowFocus()) {
            mHostView.requestFocus();
        } else {
            mHostView.requestPointerCapture();
        }
    }

    @Override
    public boolean onCapturedPointer(View view, MotionEvent event) {
        checkSameDevice(event.getDevice());

        if ((event.getSource() & InputDevice.SOURCE_CLASS_TRACKBALL) != 0) {
            if (mDeviceSupportsRelativeAxis) {
                mVector[0] = event.getAxisValue(MotionEvent.AXIS_RELATIVE_X);
                mVector[1] = event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y);
            } else {
                mVector[0] = event.getX();
                mVector[1] = event.getY();
            }
        } else {
            mPointerTracker.trackEvent(event);
        }

        if (!CallbackBridge.isGrabbing()) {
            enableTouchpadIfNecessary();
            mVector[0] *= mMousePrescale;
            mVector[1] *= mMousePrescale;
            if (event.getPointerCount() < 2) {
                mTouchpad.applyMotionVector(mVector);
                mScroller.resetScrollOvershoot();
            } else {
                mScroller.performScroll(mVector);
            }
        } else {
            CallbackBridge.sendCursorDelta(
                    mVector[0] * AllStaticSettings.scaleFactor,
                    mVector[1] * AllStaticSettings.scaleFactor
            );
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_BUTTON_PRESS:
                return MinecraftGLSurface.sendMouseButtonUnconverted(event.getActionButton(), true);
            case MotionEvent.ACTION_BUTTON_RELEASE:
                return MinecraftGLSurface.sendMouseButtonUnconverted(event.getActionButton(), false);
            case MotionEvent.ACTION_SCROLL:
                CallbackBridge.sendScroll(
                        event.getAxisValue(MotionEvent.AXIS_HSCROLL),
                        event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                );
                return true;
            case MotionEvent.ACTION_UP:
                mPointerTracker.cancelTracking();
                return true;
            default:
                return false;
        }
    }

    private void checkSameDevice(InputDevice inputDevice) {
        if (inputDevice == null) return;
        int newIdentifier = inputDevice.getId();
        if (mInputDeviceIdentifier != newIdentifier) {
            reinitializeDeviceSpecificProperties(inputDevice);
            mInputDeviceIdentifier = newIdentifier;
        }
    }

    private void reinitializeDeviceSpecificProperties(InputDevice inputDevice) {
        mPointerTracker.cancelTracking();
        boolean relativeXSupported = inputDevice.getMotionRange(MotionEvent.AXIS_RELATIVE_X) != null;
        boolean relativeYSupported = inputDevice.getMotionRange(MotionEvent.AXIS_RELATIVE_Y) != null;
        mDeviceSupportsRelativeAxis = relativeXSupported && relativeYSupported;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) mHostView.requestPointerCapture();
    }

    public void detach() {
        mHostView.setOnCapturedPointerListener(null);
        mHostView.getViewTreeObserver().removeOnWindowFocusChangeListener(this);
    }
}

