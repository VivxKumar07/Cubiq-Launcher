package git.artdeell.mojo.input.handleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.movtery.zalithlauncher.R;

public class DrawerPullButton extends View {
    public DrawerPullButton(Context context) {
        super(context);
        init();
    }

    public DrawerPullButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private final Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private VectorDrawableCompat mDrawable;
    public boolean autoTranslate = true;

    private void init() {
        setClickable(true);
        setFocusable(true);
        mDrawable = VectorDrawableCompat.create(getContext().getResources(), R.drawable.ic_sharp_settings_24, null);
        if (mDrawable != null) {
            mDrawable.setTint(Color.WHITE);
        }
        setAlpha(0.92f);
        mBackgroundPaint.setColor(Color.argb(238, 26, 27, 33));
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mBorderPaint.setColor(Color.argb(60, 255, 255, 255));
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setStrokeWidth(2f);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float pl = getPaddingLeft();
        float pr = getPaddingRight();
        float pb = getPaddingBottom();
        float w = getWidth();
        float h = getHeight();

        // Authentic half-circle tab hanging from the top edge
        canvas.drawArc(pl, -h + pb, w - pr, h - pb, 0, 180, true, mBackgroundPaint);
        canvas.drawArc(pl, -h + pb, w - pr, h - pb, 0, 180, false, mBorderPaint);

        if (mDrawable != null) {
            int iconSize = (int) (Math.min(w, h) * 0.72f);
            int left = (int) ((w - iconSize) / 2f);
            int top = (int) ((h - iconSize) / 3f);
            mDrawable.setBounds(left, top, left + iconSize, top + iconSize);
            mDrawable.draw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (autoTranslate && getParent() instanceof View) {
            int parentWidth = ((View) getParent()).getWidth();
            setTranslationX((int) (parentWidth * 0.25));
        }
    }
}
