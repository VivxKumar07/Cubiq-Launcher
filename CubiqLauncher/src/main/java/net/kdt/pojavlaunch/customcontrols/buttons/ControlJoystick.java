package net.kdt.pojavlaunch.customcontrols.buttons;

import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NONE;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_NORTH_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH_EAST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_SOUTH_WEST;
import static net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick.DIRECTION_WEST;

import android.annotation.SuppressLint;
import android.view.View;

import net.kdt.pojavlaunch.LwjglGlfwKeycode;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.ControlJoystickData;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.gamepad.GamepadJoystick;
import net.kdt.pojavlaunch.customcontrols.handleview.EditControlPopup;

import org.lwjgl.glfw.CallbackBridge;

import io.github.controlwear.virtual.joystick.android.JoystickView;

@SuppressLint("ViewConstructor")
public class ControlJoystick extends JoystickView implements ControlInterface {
    public final static int DIRECTION_FORWARD_LOCK = 8;
    // Directions keycode
    private final int[] mDirectionForwardLock = new int[]{LwjglGlfwKeycode.GLFW_KEY_LEFT_CONTROL};
    private final int[] mDirectionForward = new int[]{LwjglGlfwKeycode.GLFW_KEY_W};
    private final int[] mDirectionRight = new int[]{LwjglGlfwKeycode.GLFW_KEY_D};
    private final int[] mDirectionBackward = new int[]{LwjglGlfwKeycode.GLFW_KEY_S};
    private final int[] mDirectionLeft = new int[]{LwjglGlfwKeycode.GLFW_KEY_A};
    private ControlJoystickData mControlData;
    private int mLastDirectionInt = GamepadJoystick.DIRECTION_NONE;
    private int mCurrentDirectionInt = GamepadJoystick.DIRECTION_NONE;
    public ControlJoystick(ControlLayout parent, ControlJoystickData data) {
        super(parent.getContext());
        init(data, parent);
    }

    private static void sendInput(int[] keys, boolean isDown) {
        for (int key : keys) {
            CallbackBridge.sendKeyPress(key, CallbackBridge.getCurrentMods(), isDown);
        }
    }

    private void init(ControlJoystickData data, ControlLayout layout) {
        mControlData = data;
        setProperties(preProcessProperties(data, layout));
        setDeadzone(35);
        setFixedCenter(data.absolute);
        setAutoReCenterButton(true);

        injectBehaviors();

        setOnMoveListener(new OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                mLastDirectionInt = mCurrentDirectionInt;
                mCurrentDirectionInt = getDirectionInt(angle, strength);

                if (mLastDirectionInt != mCurrentDirectionInt) {
                    sendDirectionalKeycode(mLastDirectionInt, false);
                    sendDirectionalKeycode(mCurrentDirectionInt, true);
                }
            }

            @Override
            public void onForwardLock(boolean isLocked) {
                sendInput(mDirectionForwardLock, isLocked);
            }
        });
    }

    @Override
    public View getControlView() {
        return this;
    }

    @Override
    public ControlData getProperties() {
        return mControlData;
    }

    @Override
    public void setProperties(ControlData properties, boolean changePos) {
        mControlData = (ControlJoystickData) properties;
        mControlData.isHideable = true;
        ControlInterface.super.setProperties(properties, changePos);
        postDelayed(() -> {
            setForwardLockDistance(mControlData.forwardLock ? (int) Tools.dpToPx(60) : 0);
            setFixedCenter(mControlData.absolute);
        }, 10);
    }

    private int mActivePointerId = android.view.MotionEvent.INVALID_POINTER_ID;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();

        switch (action) {
            case android.view.MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                return super.onTouchEvent(event);
            case android.view.MotionEvent.ACTION_POINTER_DOWN:
                if (mActivePointerId == android.view.MotionEvent.INVALID_POINTER_ID) {
                    mActivePointerId = event.getPointerId(actionIndex);
                }
                return true;
            case android.view.MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerId(actionIndex) == mActivePointerId) {
                    mActivePointerId = android.view.MotionEvent.INVALID_POINTER_ID;
                    return super.onTouchEvent(event);
                }
                return true;
            case android.view.MotionEvent.ACTION_UP:
            case android.view.MotionEvent.ACTION_CANCEL:
                mActivePointerId = android.view.MotionEvent.INVALID_POINTER_ID;
                return super.onTouchEvent(event);
            case android.view.MotionEvent.ACTION_MOVE:
                if (mActivePointerId != android.view.MotionEvent.INVALID_POINTER_ID) {
                    int pointerIndex = event.findPointerIndex(mActivePointerId);
                    if (pointerIndex != -1) {
                        return super.onTouchEvent(event);
                    }
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public void removeButton() {
        getControlLayoutParent().getLayout().mJoystickDataList.remove(mControlData);
        getControlLayoutParent().removeView(this);
    }

    @Override
    public void cloneButton() {
        ControlJoystickData data = new ControlJoystickData(mControlData);
        getControlLayoutParent().addJoystickButton(data);
    }


    @Override
    public void setBackground() {
        setBorderWidth((int) Tools.dpToPx(getProperties().strokeWidth * (getControlLayoutParent().getLayoutScale()/100f)));
        setBorderColor(getProperties().strokeColor);
        setBackgroundColor(getProperties().bgColor);
    }

    @Override
    public void sendKeyPresses(boolean isDown) {/*STUB since non swipeable*/ }

    @Override
    public void loadEditValues(EditControlPopup editControlPopup) {
        editControlPopup.loadJoystickValues(mControlData);
    }

    private int getDirectionInt(int angle, int intensity) {
        if (intensity == 0) return DIRECTION_NONE;
        return (int) (((angle + 22.5) / 45) % 8);
    }

    private void sendDirectionalKeycode(int direction, boolean isDown) {
        switch (direction) {
            case DIRECTION_NORTH:
                sendInput(mDirectionForward, isDown);
                break;
            case DIRECTION_NORTH_EAST:
                sendInput(mDirectionForward, isDown);
                sendInput(mDirectionRight, isDown);
                break;
            case DIRECTION_EAST:
                sendInput(mDirectionRight, isDown);
                break;
            case DIRECTION_SOUTH_EAST:
                sendInput(mDirectionRight, isDown);
                sendInput(mDirectionBackward, isDown);
                break;
            case DIRECTION_SOUTH:
                sendInput(mDirectionBackward, isDown);
                break;
            case DIRECTION_SOUTH_WEST:
                sendInput(mDirectionBackward, isDown);
                sendInput(mDirectionLeft, isDown);
                break;
            case DIRECTION_WEST:
                sendInput(mDirectionLeft, isDown);
                break;
            case DIRECTION_NORTH_WEST:
                sendInput(mDirectionForward, isDown);
                sendInput(mDirectionLeft, isDown);
                break;
            case DIRECTION_FORWARD_LOCK:
                sendInput(mDirectionForwardLock, isDown);
                break;
        }
    }

}
