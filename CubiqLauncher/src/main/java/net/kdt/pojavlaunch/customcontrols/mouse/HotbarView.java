package net.kdt.pojavlaunch.customcontrols.mouse;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.zalithlauncher.event.single.MCOptionChangeEvent;
import com.movtery.zalithlauncher.event.single.RefreshHotbarEvent;
import com.movtery.zalithlauncher.event.value.HotbarChangeEvent;
import com.movtery.zalithlauncher.game.launch.MCOptions;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.setting.AllStaticSettings;
import com.movtery.zalithlauncher.ui.subassembly.hotbar.HotbarType;
import com.movtery.zalithlauncher.ui.subassembly.hotbar.HotbarUtils;

import net.kdt.pojavlaunch.GrabListener;
import net.kdt.pojavlaunch.LwjglGlfwKeycode;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.MathUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.lwjgl.glfw.CallbackBridge;

public class HotbarView extends View implements View.OnLayoutChangeListener, Runnable {
    private final TapDetector mDoubleTapDetector = new TapDetector(2, TapDetector.DETECTION_METHOD_DOWN);
    private View mParentView;
    private static final int[] HOTBAR_KEYS = {
            LwjglGlfwKeycode.GLFW_KEY_1, LwjglGlfwKeycode.GLFW_KEY_2, LwjglGlfwKeycode.GLFW_KEY_3,
            LwjglGlfwKeycode.GLFW_KEY_4, LwjglGlfwKeycode.GLFW_KEY_5, LwjglGlfwKeycode.GLFW_KEY_6,
            LwjglGlfwKeycode.GLFW_KEY_7, LwjglGlfwKeycode.GLFW_KEY_8, LwjglGlfwKeycode.GLFW_KEY_9
    };
    private final DropGesture mDropGesture = new DropGesture(new Handler(Looper.getMainLooper()));
    private final GrabListener mGrabListener = isGrabbing -> {
        mLastIndex = -1;
        mDropGesture.cancel();
    };

    private int mWidth;
    private int mLastIndex = -1;
    private int mGuiScale;

    private final AnimPlayer adjustAnimPlayer = new AnimPlayer();

    public HotbarView(Context context) {
        super(context);
        init();
    }

    public HotbarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HotbarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressWarnings("unused")
    public HotbarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setAlpha(0);
        setBackgroundColor(Color.parseColor("#80E64242"));
        adjustAnimPlayer.duration(800);
        adjustAnimPlayer.apply(new AnimPlayer.Entry(this, Animations.FadeOut));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
        ViewParent parent = getParent();
        if (parent == null) return;
        if (parent instanceof View) {
            mParentView = (View) parent;
            mParentView.addOnLayoutChangeListener(this);
        }
        mGuiScale = MCOptions.getMcScale();
        post(this);
        CallbackBridge.addGrabListener(mGrabListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        CallbackBridge.removeGrabListener(mGrabListener);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void event(RefreshHotbarEvent event) {
        post(this);
    }

    @Subscribe
    public void event(MCOptionChangeEvent event) {
        mGuiScale = MCOptions.getMcScale();
        post(this);
    }

    @Subscribe
    public void event(HotbarChangeEvent event) {
        manualReset(event.getWidth(), event.getHeight(), true);
    }

    private ViewGroup.MarginLayoutParams getMarginLayoutParams() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams))
            throw new RuntimeException("Incorrect LayoutParams type, expected ViewGroup.MarginLayoutParams");
        return (ViewGroup.MarginLayoutParams) layoutParams;
    }

    private void adaptiveReset() {
        ViewGroup.MarginLayoutParams marginLayoutParams = getMarginLayoutParams();
        int height;
        marginLayoutParams.width = mWidth = mcScale(180);
        marginLayoutParams.height = height = mcScale(20);
        marginLayoutParams.leftMargin = (CallbackBridge.physicalWidth / 2) - (mWidth / 2);
        marginLayoutParams.topMargin = CallbackBridge.physicalHeight - height;
        setLayoutParams(marginLayoutParams);
    }

    private void manualReset(int width, int height, boolean playAnim) {
        int dmW = Tools.currentDisplayMetrics != null ? Tools.currentDisplayMetrics.widthPixels : CallbackBridge.physicalWidth;
        int dmH = Tools.currentDisplayMetrics != null ? Tools.currentDisplayMetrics.heightPixels : CallbackBridge.physicalHeight;
        int resolvedW = width > 0 ? width : Math.max(40, dmW / 3);
        int resolvedH = height > 0 ? height : Math.max(20, dmH / 4);

        ViewGroup.MarginLayoutParams marginLayoutParams = getMarginLayoutParams();
        marginLayoutParams.width = mWidth = resolvedW;
        marginLayoutParams.height = resolvedH;
        marginLayoutParams.leftMargin = dmW / 2 - resolvedW / 2;
        marginLayoutParams.topMargin = dmH - resolvedH;
        setLayoutParams(marginLayoutParams);
        if (playAnim) adjustAnimPlayer.start();
    }

    @SuppressWarnings("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!CallbackBridge.isGrabbing()) return false;
        boolean hasDoubleTapped = mDoubleTapDetector.onTouchEvent(event);

        int actionMasked = event.getActionMasked();
        if (isLastEventInGesture(actionMasked)) mDropGesture.cancel();
        else mDropGesture.submit();
        float x = event.getX();
        if (x < 0 || x >= mWidth) {
            mDropGesture.cancel();
            return true;
        }
        int hotbarIndex = (int) MathUtils.map(x, 0, mWidth, 0, HOTBAR_KEYS.length);
        if (hotbarIndex < 0) hotbarIndex = 0;
        if (hotbarIndex >= HOTBAR_KEYS.length) hotbarIndex = HOTBAR_KEYS.length - 1;
        if (hotbarIndex == mLastIndex) {
            if (hasDoubleTapped && !AllStaticSettings.disableDoubleTap) CallbackBridge.sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_F);
            return true;
        }
        mLastIndex = hotbarIndex;
        int hotbarKey = HOTBAR_KEYS[hotbarIndex];
        CallbackBridge.sendKeyPress(hotbarKey);
        mDropGesture.cancel();
        if (!isLastEventInGesture(actionMasked)) mDropGesture.submit();
        return true;
    }

    private boolean isLastEventInGesture(int actionMasked) {
        return actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL;
    }

    private int mcScale(int input) {
        return (int) ((mGuiScale * input) / Math.max(0.1f, AllStaticSettings.scaleFactor));
    }

    @Override
    public void run() {
        if (getParent() == null) return;

        HotbarType hotbarType = HotbarUtils.getCurrentType();
        if (hotbarType == HotbarType.AUTO) {
            adaptiveReset();
        } else {
            manualReset(
                    AllSettings.getZl1HotbarWidth().getValue(),
                    AllSettings.getZl1HotbarHeight().getValue(),
                    false
            );
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v.equals(mParentView) && (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom)) {
            post(this);
        }
    }
}

