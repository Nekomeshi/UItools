package com.nekomeshi312.uitools;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class AbsVerticalSeekBar extends VerticalProgressBar{
    private Drawable mThumb;
    private int mThumbOffset;
    
    /**
     * On touch, this offset plus the scaled value from the position of the
     * touch will form the progress value. Usually 0.
     */
    float mTouchProgressOffset;

    /**
     * Whether this is user seekable.
     */
    boolean mIsUserSeekable = true;
  
    /**
     * On key presses (right or left), the amount to increment/decrement the
     * progress.
     */
    private int mKeyProgressIncrement = 1;

    private static final int NO_ALPHA = 0xFF;
    private float mDisabledAlpha;

    public AbsVerticalSeekBar(Context context) {
    	super(context);
    }

    public AbsVerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsVerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SeekBar, defStyle, 0);
        Drawable thumb = a.getDrawable(R.styleable.SeekBar_android_thumb);
        setThumb(thumb); // will guess mThumbOffset if thumb != null...
        // ...but allow layout to override this
        int thumbOffset =
                a.getDimensionPixelOffset(R.styleable.SeekBar_android_thumbOffset, getThumbOffset());
        setThumbOffset(thumbOffset);
        a.recycle();

        a = context.obtainStyledAttributes(attrs,
                R.styleable.Theme, 0, 0);
        mDisabledAlpha = a.getFloat(R.styleable.Theme_android_disabledAlpha, 0.5f);
        a.recycle();
    }

    /**
     * Sets the thumb that will be drawn at the end of the progress meter within the SeekBar.
     * <p>
     * If the thumb is a valid drawable (i.e. not null), half its width will be
     * used as the new thumb offset (@see #setThumbOffset(int)).
     *
     * @param thumb Drawable representing the thumb
     */
    public void setThumb(Drawable thumb) {
        if (thumb != null) {
            thumb.setCallback(this);

            // Assuming the thumb drawable is symmetric, set the thumb offset
            // such that the thumb will hang halfway off either edge of the
            // progress bar.
            mThumbOffset = (int)thumb.getIntrinsicHeight() / 2;
        }
        mThumb = thumb;
        invalidate();
    }

    /**
     * @see #setThumbOffset(int)
     */
    public int getThumbOffset() {
        return mThumbOffset;
    }

    /**
     * Sets the thumb offset that allows the thumb to extend out of the range of
     * the track.
     *
     * @param thumbOffset The offset amount in pixels.
     */
    public void setThumbOffset(int thumbOffset) {
        mThumbOffset = thumbOffset;
        invalidate();
    }

    /**
     * Sets the amount of progress changed via the arrow keys.
     *
     * @param increment The amount to increment or decrement when the user
     *            presses the arrow keys.
     */
    public void setKeyProgressIncrement(int increment) {
        mKeyProgressIncrement = increment < 0 ? -increment : increment;
    }

    /**
     * Returns the amount of progress changed via the arrow keys.
     * <p>
     * By default, this will be a value that is derived from the max progress.
     *
     * @return The amount to increment or decrement when the user presses the
     *         arrow keys. This will be positive.
     */
    public int getKeyProgressIncrement() {
        return mKeyProgressIncrement;
    }
    
    @Override
    public synchronized void setMax(int max) {
        super.setMax(max);

        if ((mKeyProgressIncrement == 0) || (getMax() / mKeyProgressIncrement > 20)) {
            // It will take the user too long to change this via keys, change it
            // to something more reasonable
            setKeyProgressIncrement(Math.max(1, Math.round((float) getMax() / 20)));
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mThumb || super.verifyDrawable(who);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null) {
            progressDrawable.setAlpha(isEnabled() ? NO_ALPHA : (int) (NO_ALPHA * mDisabledAlpha));
        }

        if (mThumb != null && mThumb.isStateful()) {
            int[] state = getDrawableState();
            mThumb.setState(state);
        }
    }
   
    @Override
    void onProgressRefresh(float scale, boolean fromUser) {
        Drawable thumb = mThumb;
        if (thumb != null) {
            setThumbPos(getHeight(), thumb, scale, Integer.MIN_VALUE);
            /*
             * Since we draw translated, the drawable's bounds that it signals
             * for invalidation won't be the actual bounds we want invalidated,
             * so just invalidate this whole view.
             */
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Drawable d = getCurrentDrawable();
        Drawable thumb = mThumb;
        int thumbWidth = thumb == null ? 0 : thumb.getIntrinsicWidth();
        // The max height does not incorporate padding, whereas the height
        // parameter does
        int trackWidth = Math.min(mMaxWidth, w - mPaddingRight - mPaddingLeft);
        int max = getMax();
        float scale = max > 0 ? (float) getProgress() / (float) max : 0;

        if (thumbWidth > trackWidth) {
            int gapForCenteringTrack = (thumbWidth - trackWidth) / 2;
            if (thumb != null) {
                setThumbPos(h, thumb, scale, gapForCenteringTrack * -1);
            }
            if (d != null) {
                // Canvas will be translated by the padding, so 0,0 is where we start drawing
                d.setBounds(gapForCenteringTrack, 0,
                        w - mPaddingRight - mPaddingLeft - gapForCenteringTrack,
                        h - mPaddingBottom - mPaddingTop);
            }
        } else {
            if (d != null) {
                // Canvas will be translated by the padding, so 0,0 is where we start drawing
                d.setBounds(0, 0, w - mPaddingRight - mPaddingLeft, h - mPaddingBottom - mPaddingTop);
            }
            int gap = (trackWidth - thumbWidth) / 2;
            if (thumb != null) {
                setThumbPos(h, thumb, scale, gap);
            }
        }
    }

    /**
     * @param gap If set to {@link Integer#MIN_VALUE}, this will be ignored and
     */
    private void setThumbPos(int h, Drawable thumb, float scale, int gap) {
        int available = h - mPaddingTop - mPaddingBottom;
        int thumbWidth = thumb.getIntrinsicWidth();
        int thumbHeight = thumb.getIntrinsicHeight();
        available -= thumbHeight;

        // The extra space for the thumb to move on the track
        available += mThumbOffset * 2;
        int thumbPos = (int) ((1-scale) * available);
        int leftBound, rightBound;
        if (gap == Integer.MIN_VALUE) {
        	Rect oldBounds = thumb.getBounds();
            leftBound = oldBounds.left;
            rightBound = oldBounds.right;
        } else {
            leftBound = gap;
            rightBound = gap + thumbWidth;
        }

        // Canvas will be translated, so 0,0 is where we start drawing
        thumb.setBounds(leftBound, thumbPos, rightBound, thumbPos + thumbHeight);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mThumb != null) {
            canvas.save();
            // Translate the padding. For the x, we need to allow the thumb to
            // draw in its extra space
            canvas.translate(mPaddingLeft, mPaddingTop - mThumbOffset);
            mThumb.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getCurrentDrawable();

        int thumbWidth = mThumb == null ? 0 : mThumb.getIntrinsicWidth();
        int dw = 0;
        int dh = 0;
        if (d != null) {
            dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
            dw = Math.max(thumbWidth, dh);
            dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
        }
        dw += mPaddingLeft + mPaddingRight;
        dh += mPaddingTop + mPaddingBottom;

        setMeasuredDimension(resolveSize(dw, widthMeasureSpec),
                resolveSize(dh, heightMeasureSpec));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsUserSeekable || !isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                onStartTrackingTouch();
                trackTouchEvent(event);
                break;

            case MotionEvent.ACTION_MOVE:
                trackTouchEvent(event);
                attemptClaimDrag();
                break;

            case MotionEvent.ACTION_UP:
                trackTouchEvent(event);
                onStopTrackingTouch();
                setPressed(false);
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                onStopTrackingTouch();
                setPressed(false);
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private void trackTouchEvent(MotionEvent event) {
        final int height = getHeight();
        final int available = height - mPaddingTop - mPaddingBottom;
        int y = height - (int)event.getY();
        float scale;
        float progress = 0;
        if (y < mPaddingBottom) {
            scale = 0.0f;
        } else if (y > height - mPaddingTop) {
            scale = 1.0f;
        } else {
            scale = (float)(y - mPaddingBottom) / (float)available;
            progress = mTouchProgressOffset;
        }

        final int max = getMax();
        progress += scale * max;

        setProgress((int) progress, true);
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any
     * ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (mParent != null) {
            mParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    void onStopTrackingTouch() {
    }

    /**
     * Called when the user changes the seekbar's progress by using a key event.
     */
    void onKeyChange() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int progress = getProgress();

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (progress <= 0) break;
                setProgress(progress - mKeyProgressIncrement, true);
                onKeyChange();
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                if (progress >= getMax()) break;
                setProgress(progress + mKeyProgressIncrement, true);
                onKeyChange();
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}
