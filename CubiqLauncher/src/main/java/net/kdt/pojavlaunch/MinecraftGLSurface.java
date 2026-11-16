package net.kdt.pojavlaunch;

import static org.lwjgl.glfw.CallbackBridge.sendMouseButton;
import static org.lwjgl.glfw.CallbackBridge.windowHeight;
import static org.lwjgl.glfw.CallbackBridge.windowWidth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.movtery.zalithlauncher.bridge.ZLBridgeStates;
import com.movtery.zalithlauncher.event.single.RefreshHotbarEvent;
import com.movtery.zalithlauncher.game.sdl.SdlBridge;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.setting.AllStaticSettings;
import com.movtery.zalithlauncher.utils.logging.Logger;

import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.gamepad.DefaultDataProvider;
import net.kdt.pojavlaunch.customcontrols.gamepad.Gamepad;
import net.kdt.pojavlaunch.customcontrols.keyboard.TouchCharInput;
import net.kdt.pojavlaunch.customcontrols.mouse.AbstractTouchpad;
import net.kdt.pojavlaunch.customcontrols.mouse.AndroidPointerCapture;
import net.kdt.pojavlaunch.customcontrols.mouse.InGUIEventProcessor;
import net.kdt.pojavlaunch.customcontrols.mouse.InGameEventProcessor;
import net.kdt.pojavlaunch.customcontrols.mouse.TouchEventProcessor;

import org.greenrobot.eventbus.EventBus;
import org.libsdl.app.SDLActivity;
import org.libsdl.app.SDLSurface;
import org.lwjgl.glfw.CallbackBridge;

import java.util.Locale;

import fr.spse.gamepad_remapper.RemapperManager;
import fr.spse.gamepad_remapper.RemapperView;

/**
 * Class dealing with showing minecraft surface and taking inputs to dispatch them to minecraft
 */
public class MinecraftGLSurface extends View implements GrabListener {
    /* Gamepad object for gamepad inputs, instantiated on need */
    private Gamepad mGamepad = null;
    /* The RemapperView.Builder object allows you to set which buttons to remap */
    private final RemapperManager mInputManager = new RemapperManager(getContext(), new RemapperView.Builder(null)
            .remapA(true)
            .remapB(true)
            .remapX(true)
            .remapY(true)
            .remapLeftJoystick(true)
            .remapRightJoystick(true)
            .remapStart(true)
            .remapSelect(true)
            .remapLeftShoulder(true)
            .remapRightShoulder(true)
            .remapLeftTrigger(true)
            .remapRightTrigger(true)
            .remapDpad(true));

    /* Sensitivity, adjusted according to screen size */
    private final double mSensitivityFactor;

    /* View holding the surface, either a SurfaceView or a TextureView */
    private View mSurface;

    private final InGameEventProcessor mIngameProcessor;
    private final InGUIEventProcessor mInGUIProcessor = new InGUIEventProcessor();
    private TouchEventProcessor mCurrentTouchProcessor = mInGUIProcessor;
    private AndroidPointerCapture mPointerCapture;
    private TouchCharInput mTouchCharInput;
    private boolean mLastGrabState = false;

    private OnRenderingStartedListener mOnRenderingStartedListener = null;
    private boolean mIsRenderingStarted = false;

    public MinecraftGLSurface(Context context) {
        this(context, null);
    }

    public MinecraftGLSurface(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setFocusable(true);
        Activity activity = unwrapActivity(context);
        int heightPixels = activity != null
                ? Tools.getDisplayMetrics(activity).heightPixels
                : (Tools.currentDisplayMetrics != null ? Tools.currentDisplayMetrics.heightPixels : 1080);
        mSensitivityFactor = (1.4 * (1080f / Math.max(1, heightPixels)));
        mIngameProcessor = new InGameEventProcessor(mSensitivityFactor);
    }

    @Nullable
    private static Activity unwrapActivity(Context context) {
        Context c = context;
        while (c instanceof ContextWrapper) {
            if (c instanceof Activity) return (Activity) c;
            c = ((ContextWrapper) c).getBaseContext();
        }
        return null;
    }

    public void setTouchCharInput(@Nullable TouchCharInput touchCharInput) {
        this.mTouchCharInput = touchCharInput;
    }

    private void setUpPointerCapture(AbstractTouchpad touchpad) {
        if (mPointerCapture != null) mPointerCapture.detach();
        mPointerCapture = new AndroidPointerCapture(touchpad, this);
    }

    /**
     * Initialize the render view (SurfaceView or TextureView) using the host Activity's
     * existing surface lifecycle callbacks while keeping ZL1 touch/pointer/gamepad dispatch.
     */
    public void start(
            boolean useSurfaceView,
            AbstractTouchpad touchpad,
            @Nullable SurfaceHolder.Callback surfaceCallback,
            @Nullable TextureView.SurfaceTextureListener textureListener,
            @Nullable View.OnApplyWindowInsetsListener insetsListener,
            @Nullable RenderViewCreatedListener renderViewCreatedListener
    ) {
        setUpPointerCapture(touchpad);
        mInGUIProcessor.setAbstractTouchpad(touchpad);
        CallbackBridge.addGrabListener(this);
        onGrabState(CallbackBridge.isGrabbing());

        if (useSurfaceView) {
            SurfaceView surfaceView = new SurfaceView(getContext());
            surfaceView.getHolder().setFormat(PixelFormat.RGBX_8888);
            if (surfaceCallback != null) {
                surfaceView.getHolder().addCallback(surfaceCallback);
            }
            mSurface = surfaceView;
        } else {
            TextureView textureView = new TextureView(getContext());
            textureView.setOpaque(true);
            textureView.setAlpha(1.0f);
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    if (textureListener != null) {
                        textureListener.onSurfaceTextureAvailable(surface, width, height);
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                    if (textureListener != null) {
                        textureListener.onSurfaceTextureSizeChanged(surface, width, height);
                    }
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return textureListener == null || textureListener.onSurfaceTextureDestroyed(surface);
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                    if (textureListener != null) {
                        textureListener.onSurfaceTextureUpdated(surface);
                    }
                    if (!mIsRenderingStarted) {
                        mIsRenderingStarted = true;
                        if (mOnRenderingStartedListener != null) mOnRenderingStartedListener.isStarted();
                    }
                }
            });
            mSurface = textureView;
        }

        if (insetsListener != null) {
            mSurface.setOnApplyWindowInsetsListener(insetsListener);
        }
        if (renderViewCreatedListener != null) {
            renderViewCreatedListener.onRenderViewCreated(mSurface, useSurfaceView);
        }
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).addView(mSurface, 0);
        }
    }

    @Nullable
    public View getRenderSurfaceView() {
        return mSurface;
    }

    @Override
    @SuppressWarnings("accessibility")
    public boolean onTouchEvent(MotionEvent e) {
        if (getParent() instanceof ControlLayout && ((ControlLayout) getParent()).getModifiable()) {
            return false;
        }

        for (int i = 0; i < e.getPointerCount(); i++) {
            int toolType = e.getToolType(i);
            if (toolType == MotionEvent.TOOL_TYPE_MOUSE) {
                if (mPointerCapture != null) {
                    mPointerCapture.handleAutomaticCapture();
                    return true;
                }
            } else if (toolType != MotionEvent.TOOL_TYPE_STYLUS) continue;

            if (CallbackBridge.isGrabbing()) return false;
            CallbackBridge.sendCursorPos(e.getX(i) * AllStaticSettings.scaleFactor, e.getY(i) * AllStaticSettings.scaleFactor);
            return true;
        }
        if (mIngameProcessor == null || mInGUIProcessor == null) return true;
        updateGrabState(CallbackBridge.isGrabbing());
        return mCurrentTouchProcessor.processTouchEvent(e);
    }

    private void createGamepad(View contextView, InputDevice inputDevice) {
        mGamepad = new Gamepad(contextView, inputDevice, DefaultDataProvider.INSTANCE, true);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        int mouseCursorIndex = -1;

        if (Gamepad.isGamepadEvent(event)) {
            if (mGamepad == null) createGamepad(this, event.getDevice());
            mInputManager.handleMotionEventInput(getContext(), event, mGamepad);
            return true;
        }

        for (int i = 0; i < event.getPointerCount(); i++) {
            if (event.getToolType(i) != MotionEvent.TOOL_TYPE_MOUSE && event.getToolType(i) != MotionEvent.TOOL_TYPE_STYLUS) continue;
            mouseCursorIndex = i;
            break;
        }
        if (mouseCursorIndex == -1) return false;

        updateGrabState(CallbackBridge.isGrabbing());

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_HOVER_MOVE:
                CallbackBridge.mouseX = (event.getX(mouseCursorIndex) * AllStaticSettings.scaleFactor);
                CallbackBridge.mouseY = (event.getY(mouseCursorIndex) * AllStaticSettings.scaleFactor);
                CallbackBridge.sendCursorPos(CallbackBridge.mouseX, CallbackBridge.mouseY);
                return true;
            case MotionEvent.ACTION_SCROLL:
                CallbackBridge.sendScroll(event.getAxisValue(MotionEvent.AXIS_HSCROLL), event.getAxisValue(MotionEvent.AXIS_VSCROLL));
                return true;
            case MotionEvent.ACTION_BUTTON_PRESS:
                return sendMouseButtonUnconverted(event.getActionButton(), true);
            case MotionEvent.ACTION_BUTTON_RELEASE:
                return sendMouseButtonUnconverted(event.getActionButton(), false);
            default:
                return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mCurrentTouchProcessor != null) {
            mCurrentTouchProcessor.dispatchTouchEvent(event, this);
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean processKeyEvent(KeyEvent event) {
        int eventKeycode = event.getKeyCode();
        if (eventKeycode == KeyEvent.KEYCODE_UNKNOWN) return true;
        if (eventKeycode == KeyEvent.KEYCODE_VOLUME_DOWN) return false;
        if (eventKeycode == KeyEvent.KEYCODE_VOLUME_UP) return false;
        if (event.getRepeatCount() != 0) return true;
        int action = event.getAction();
        if (action == KeyEvent.ACTION_MULTIPLE) return true;
        if (action == KeyEvent.ACTION_UP && (event.getFlags() & KeyEvent.FLAG_CANCELED) != 0) return true;

        if ((event.getFlags() & KeyEvent.FLAG_SOFT_KEYBOARD) == KeyEvent.FLAG_SOFT_KEYBOARD) {
            if (eventKeycode == KeyEvent.KEYCODE_ENTER) return true;
            if (mTouchCharInput != null) {
                mTouchCharInput.dispatchKeyEvent(event);
            }
            return true;
        }

        if (event.getDevice() != null
                && ((event.getSource() & InputDevice.SOURCE_MOUSE_RELATIVE) == InputDevice.SOURCE_MOUSE_RELATIVE
                || (event.getSource() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE)) {
            if (eventKeycode == KeyEvent.KEYCODE_BACK) {
                sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT, event.getAction() == KeyEvent.ACTION_DOWN);
                return true;
            }
        }

        if (Gamepad.isGamepadEvent(event)) {
            if (mGamepad == null) createGamepad(this, event.getDevice());
            mInputManager.handleKeyEventInput(getContext(), event, mGamepad);
            return true;
        }

        int index = EfficientAndroidLWJGLKeycode.getIndexByKey(eventKeycode);
        if (EfficientAndroidLWJGLKeycode.containsIndex(index)) {
            EfficientAndroidLWJGLKeycode.execKey(event, index);
            return true;
        }

        return (event.getFlags() & KeyEvent.FLAG_FALLBACK) == KeyEvent.FLAG_FALLBACK;
    }

    public static boolean sendMouseButtonUnconverted(int button, boolean status) {
        int glfwButton = -256;
        switch (button) {
            case MotionEvent.BUTTON_PRIMARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT;
                break;
            case MotionEvent.BUTTON_TERTIARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_MIDDLE;
                break;
            case MotionEvent.BUTTON_SECONDARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT;
                break;
        }
        if (glfwButton == -256) return false;
        sendMouseButton(glfwButton, status);
        return true;
    }

    /** Called when the size needs to be updated during the surface lifecycle */
    public void refreshSize() {
        if (Tools.currentDisplayMetrics == null) return;
        int baseW = Math.max(Tools.currentDisplayMetrics.widthPixels, Tools.currentDisplayMetrics.heightPixels);
        int baseH = Math.min(Tools.currentDisplayMetrics.widthPixels, Tools.currentDisplayMetrics.heightPixels);
        int newWidth = Tools.getDisplayFriendlyRes(baseW, AllStaticSettings.scaleFactor);
        int newHeight = Tools.getDisplayFriendlyRes(baseH, AllStaticSettings.scaleFactor);
        if (newHeight < 1 || newWidth < 1) {
            Logger.INSTANCE.error("MGLSurface", String.format(Locale.getDefault(), "Impossible resolution : %dx%d", newWidth, newHeight), null);
            return;
        }
        windowWidth = newWidth;
        windowHeight = newHeight;
        if (mSurface == null) {
            Logger.INSTANCE.warning("MGLSurface", "Attempt to refresh size on null surface", null);
            return;
        }
        if (mSurface instanceof SurfaceView) {
            SurfaceView view = (SurfaceView) mSurface;
            view.post(() -> view.getHolder().setFixedSize(windowWidth, windowHeight));
        } else if (mSurface instanceof TextureView) {
            TextureView view = (TextureView) mSurface;
            if (view.getSurfaceTexture() != null) {
                view.getSurfaceTexture().setDefaultBufferSize(windowWidth, windowHeight);
            }
        }

        ZLBridgeStates.INSTANCE.onWindowChange();
        CallbackBridge.sendUpdateWindowSize(windowWidth, windowHeight);
        if (SdlBridge.getSdlEnabled()) {
            SDLSurface sdlSurface = SDLActivity.getSDLSurface();
            if (sdlSurface != null) {
                sdlSurface.surfaceChanged();
                sdlSurface.nativeResize(windowWidth, windowHeight);
            }
        }
        EventBus.getDefault().post(new RefreshHotbarEvent());
    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        post(() -> updateGrabState(isGrabbing));
    }

    private TouchEventProcessor pickEventProcessor(boolean isGrabbing) {
        return isGrabbing ? mIngameProcessor : mInGUIProcessor;
    }

    private void updateGrabState(boolean isGrabbing) {
        if (mLastGrabState != isGrabbing) {
            mCurrentTouchProcessor.cancelPendingActions();
            mCurrentTouchProcessor = pickEventProcessor(isGrabbing);
            mLastGrabState = isGrabbing;
        }
    }

    public void destroy() {
        CallbackBridge.removeGrabListener(this);
        if (mPointerCapture != null) {
            mPointerCapture.detach();
            mPointerCapture = null;
        }
        if (mGamepad != null) {
            mGamepad.destroy();
            mGamepad = null;
        }
    }

    public interface RenderViewCreatedListener {
        void onRenderViewCreated(View renderView, boolean isSurfaceView);
    }

    public interface OnRenderingStartedListener {
        void isStarted();
    }

    public void setOnRenderingStartedListener(OnRenderingStartedListener listener) {
        mOnRenderingStartedListener = listener;
    }
}

