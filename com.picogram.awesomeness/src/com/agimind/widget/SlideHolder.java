/*
 * Copyright dmitry.zaicew@gmail.com Dmitry Zaitsev
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.agimind.widget;

import java.util.LinkedList;
import java.util.Queue;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

public class SlideHolder extends FrameLayout {

    public static interface OnSlideListener {
        public void onSlideCompleted(boolean opened);
    }

    private class SlideAnimation extends Animation {

        private final float mStart;
        private final float mEnd;

        public SlideAnimation(final float fromX, final float toX, final float speed) {

            this.mStart = fromX;
            this.mEnd = toX;

            this.setInterpolator(new DecelerateInterpolator());

            final float duration = Math.abs(this.mEnd - this.mStart) / speed;
            this.setDuration((long) duration);
        }

        @Override
        protected void applyTransformation(final float interpolatedTime, final Transformation t) {
            super.applyTransformation(interpolatedTime, t);

            final float offset = ((this.mEnd - this.mStart) * interpolatedTime) + this.mStart;
            SlideHolder.this.mOffset = (int) offset;

            SlideHolder.this.postInvalidate();
        }

    }

    public final static int DIRECTION_LEFT = 1;
    public final static int DIRECTION_RIGHT = -1;
    protected final static int MODE_READY = 0;

    protected final static int MODE_SLIDE = 1;
    protected final static int MODE_FINISHED = 2;
    private Bitmap mCachedBitmap;
    private Canvas mCachedCanvas;

    private Paint mCachedPaint;
    private View mMenuView;
    private int mMode = MODE_READY;

    private int mDirection = DIRECTION_LEFT;
    private float mSpeed = 0.6f;
    private int mOffset = 0;

    private int mStartOffset;
    private int mEndOffset;
    private boolean mEnabled = true;
    private boolean mInterceptTouch = true;

    private boolean mAlwaysOpened = false;

    private boolean mDispatchWhenOpened = false;

    private final Queue<Runnable> mWhenReady = new LinkedList<Runnable>();

    private OnSlideListener mListener;

    private byte mFrame = 0;

    private int mHistoricalX = 0;

    private boolean mCloseOnRelease = false;

    private final Animation.AnimationListener mOpenListener = new Animation.AnimationListener() {

        public void onAnimationEnd(final Animation animation) {
            SlideHolder.this.completeOpening();
        }

        public void onAnimationRepeat(final Animation animation) {
        }

        public void onAnimationStart(final Animation animation) {
        }
    };

    private final Animation.AnimationListener mCloseListener = new Animation.AnimationListener() {

        public void onAnimationEnd(final Animation animation) {
            SlideHolder.this.completeClosing();
        }

        public void onAnimationRepeat(final Animation animation) {
        }

        public void onAnimationStart(final Animation animation) {
        }
    };

    public SlideHolder(final Context context) {
        super(context);

        this.initView();
    }

    public SlideHolder(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        this.initView();
    }

    public SlideHolder(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        this.initView();
    }

    public boolean close() {
        if (!this.isOpened() || this.mAlwaysOpened || (this.mMode == MODE_SLIDE)) {
            return false;
        }

        if (!this.isReadyForSlide()) {
            this.mWhenReady.add(new Runnable() {

                public void run() {
                    SlideHolder.this.close();
                }
            });

            return true;
        }

        this.initSlideMode();

        final Animation anim = new SlideAnimation(this.mOffset, this.mEndOffset, this.mSpeed);
        anim.setAnimationListener(this.mCloseListener);
        this.startAnimation(anim);

        this.invalidate();

        return true;
    }

    public boolean closeImmediately() {
        if (!this.isOpened() || this.mAlwaysOpened || (this.mMode == MODE_SLIDE)) {
            return false;
        }

        if (!this.isReadyForSlide()) {
            this.mWhenReady.add(new Runnable() {

                public void run() {
                    SlideHolder.this.closeImmediately();
                }
            });

            return true;
        }

        this.mMenuView.setVisibility(View.GONE);
        this.mMode = MODE_READY;
        this.requestLayout();

        if (this.mListener != null) {
            this.mListener.onSlideCompleted(false);
        }

        return true;
    }

    private void completeClosing() {
        this.mOffset = 0;
        this.requestLayout();

        this.post(new Runnable() {

            public void run() {
                SlideHolder.this.mMode = MODE_READY;
                SlideHolder.this.mMenuView.setVisibility(View.GONE);
            }
        });

        if (this.mListener != null) {
            this.mListener.onSlideCompleted(false);
        }
    }

    private void completeOpening() {
        this.mOffset = this.mDirection * this.mMenuView.getWidth();
        this.requestLayout();

        this.post(new Runnable() {

            public void run() {
                SlideHolder.this.mMode = MODE_FINISHED;
                SlideHolder.this.mMenuView.setVisibility(View.VISIBLE);
            }
        });

        if (this.mListener != null) {
            this.mListener.onSlideCompleted(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void dispatchDraw(final Canvas canvas) {
        try {
            if (this.mMode == MODE_SLIDE) {
                final View main = this.getChildAt(1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    /*
                     * On new versions we redrawing main layout only if it's
                     * marked as dirty
                     */
                    if (main.isDirty()) {
                        this.mCachedCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
                        main.draw(this.mCachedCanvas);
                    }
                } else {
                    /*
                     * On older versions we just redrawing our cache every 5th
                     * frame
                     */
                    if ((++this.mFrame % 5) == 0) {
                        this.mCachedCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
                        main.draw(this.mCachedCanvas);
                    }
                }

                /*
                 * Draw only visible part of menu
                 */

                final View menu = this.getChildAt(0);
                final int scrollX = menu.getScrollX();
                final int scrollY = menu.getScrollY();

                canvas.save();

                if (this.mDirection == DIRECTION_LEFT) {
                    canvas.clipRect(0, 0, this.mOffset, menu.getHeight(), Op.REPLACE);
                } else {
                    final int menuWidth = menu.getWidth();
                    final int menuLeft = menu.getLeft();

                    canvas.clipRect(menuLeft + menuWidth + this.mOffset, 0, menuLeft + menuWidth,
                            menu.getHeight());
                }

                canvas.translate(menu.getLeft(), menu.getTop());
                canvas.translate(-scrollX, -scrollY);

                menu.draw(canvas);

                canvas.restore();

                canvas.drawBitmap(this.mCachedBitmap, this.mOffset, 0, this.mCachedPaint);
            } else {
                if (!this.mAlwaysOpened && (this.mMode == MODE_READY)) {
                    this.mMenuView.setVisibility(View.GONE);
                }

                super.dispatchDraw(canvas);
            }
        } catch (final IndexOutOfBoundsException e) {
            /*
             * Possibility of crashes on some devices (especially on Samsung).
             * Usually, when ListView is empty.
             */
        }
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        if (((!this.mEnabled || !this.mInterceptTouch) && (this.mMode == MODE_READY))
                || this.mAlwaysOpened) {
            return super.dispatchTouchEvent(ev);
        }

        if (this.mMode != MODE_FINISHED) {
            this.onTouchEvent(ev);

            if (this.mMode != MODE_SLIDE) {
                super.dispatchTouchEvent(ev);
            } else {
                final MotionEvent cancelEvent = MotionEvent.obtain(ev);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                super.dispatchTouchEvent(cancelEvent);
                cancelEvent.recycle();
            }

            return true;
        } else {
            final int action = ev.getAction();

            final Rect rect = new Rect();
            final View menu = this.getChildAt(0);
            menu.getHitRect(rect);

            if (!rect.contains((int) ev.getX(), (int) ev.getY())) {
                if ((action == MotionEvent.ACTION_UP) && this.mCloseOnRelease
                        && !this.mDispatchWhenOpened) {
                    this.close();
                    this.mCloseOnRelease = false;
                } else {
                    if ((action == MotionEvent.ACTION_DOWN) && !this.mDispatchWhenOpened) {
                        this.mCloseOnRelease = true;
                    }

                    this.onTouchEvent(ev);
                }

                if (this.mDispatchWhenOpened) {
                    super.dispatchTouchEvent(ev);
                }

                return true;
            } else {
                try {
                    this.onTouchEvent(ev);

                    ev.offsetLocation(-menu.getLeft(), -menu.getTop());
                    menu.dispatchTouchEvent(ev);

                    return true;
                } catch (final IllegalArgumentException e) {
                    /*
                     * Possibility of crashes on some devices (especially on
                     * Samsung).
                     */
                    return super.dispatchTouchEvent(ev);
                }
            }
        }
    }

    private void finishSlide() {
        if ((this.mDirection * this.mEndOffset) > 0) {
            if ((this.mDirection * this.mOffset) > ((this.mDirection * this.mEndOffset) / 2)) {
                if ((this.mDirection * this.mOffset) > (this.mDirection * this.mEndOffset)) {
                    this.mOffset = this.mEndOffset;
                }

                final Animation anim = new SlideAnimation(this.mOffset, this.mEndOffset,
                        this.mSpeed);
                anim.setAnimationListener(this.mOpenListener);
                this.startAnimation(anim);
            } else {
                if ((this.mDirection * this.mOffset) < (this.mDirection * this.mStartOffset)) {
                    this.mOffset = this.mStartOffset;
                }

                final Animation anim = new SlideAnimation(this.mOffset, this.mStartOffset,
                        this.mSpeed);
                anim.setAnimationListener(this.mCloseListener);
                this.startAnimation(anim);
            }
        } else {
            if ((this.mDirection * this.mOffset) < ((this.mDirection * this.mStartOffset) / 2)) {
                if ((this.mDirection * this.mOffset) < (this.mDirection * this.mEndOffset)) {
                    this.mOffset = this.mEndOffset;
                }

                final Animation anim = new SlideAnimation(this.mOffset, this.mEndOffset,
                        this.mSpeed);
                anim.setAnimationListener(this.mCloseListener);
                this.startAnimation(anim);
            } else {
                if ((this.mDirection * this.mOffset) > (this.mDirection * this.mStartOffset)) {
                    this.mOffset = this.mStartOffset;
                }

                final Animation anim = new SlideAnimation(this.mOffset, this.mStartOffset,
                        this.mSpeed);
                anim.setAnimationListener(this.mOpenListener);
                this.startAnimation(anim);
            }
        }
    }

    public int getMenuOffset() {
        return this.mOffset;
    }

    private boolean handleTouchEvent(final MotionEvent ev) {
        if (!this.mEnabled) {
            return false;
        }

        final float x = ev.getX();

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            this.mHistoricalX = (int) x;

            return true;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {

            final float diff = x - this.mHistoricalX;

            if ((((this.mDirection * diff) > 50) && (this.mMode == MODE_READY))
                    || (((this.mDirection * diff) < -50) && (this.mMode == MODE_FINISHED))) {
                this.mHistoricalX = (int) x;

                this.initSlideMode();
            } else if (this.mMode == MODE_SLIDE) {
                this.mOffset += diff;

                this.mHistoricalX = (int) x;

                if (!this.isSlideAllowed()) {
                    this.finishSlide();
                }
            } else {
                return false;
            }
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (this.mMode == MODE_SLIDE) {
                this.finishSlide();
            }

            this.mCloseOnRelease = false;

            return false;
        }

        return this.mMode == MODE_SLIDE;
    }

    private void initSlideMode() {
        this.mCloseOnRelease = false;

        final View v = this.getChildAt(1);

        if (this.mMode == MODE_READY) {
            this.mStartOffset = 0;
            this.mEndOffset = this.mDirection * this.getChildAt(0).getWidth();
        } else {
            this.mStartOffset = this.mDirection * this.getChildAt(0).getWidth();
            this.mEndOffset = 0;
        }

        this.mOffset = this.mStartOffset + 10;

        if ((this.mCachedBitmap == null) || this.mCachedBitmap.isRecycled()
                || (this.mCachedBitmap.getWidth() != v.getWidth())) {
            this.mCachedBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                    Bitmap.Config.ARGB_8888);
            this.mCachedCanvas = new Canvas(this.mCachedBitmap);
        } else {
            this.mCachedCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        }

        v.setVisibility(View.VISIBLE);

        this.mCachedCanvas.translate(-v.getScrollX(), -v.getScrollY());
        v.draw(this.mCachedCanvas);

        this.mMode = MODE_SLIDE;

        this.mMenuView.setVisibility(View.VISIBLE);
    }

    private void initView() {
        this.mCachedPaint = new Paint(
                Paint.ANTI_ALIAS_FLAG
                        | Paint.FILTER_BITMAP_FLAG
                        | Paint.DITHER_FLAG
                );
    }

    public boolean isAllowedInterceptTouch() {
        return this.mInterceptTouch;
    }

    public boolean isDispatchTouchWhenOpened() {
        return this.mDispatchWhenOpened;
    }

    @Override
    public boolean isEnabled() {
        return this.mEnabled;
    }

    public boolean isOpened() {
        return this.mAlwaysOpened || (this.mMode == MODE_FINISHED);
    }

    private boolean isReadyForSlide() {
        return ((this.getWidth() > 0) && (this.getHeight() > 0));
    }

    private boolean isSlideAllowed() {
        return (((this.mDirection * this.mEndOffset) > 0)
                && ((this.mDirection * this.mOffset) < (this.mDirection * this.mEndOffset)) && ((this.mDirection * this.mOffset) >= (this.mDirection * this.mStartOffset)))
                || ((this.mEndOffset == 0)
                        && ((this.mDirection * this.mOffset) > (this.mDirection * this.mEndOffset)) && ((this.mDirection * this.mOffset) <= (this.mDirection * this.mStartOffset)));
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
            final int b) {
        final int parentLeft = 0;
        final int parentTop = 0;
        final int parentRight = r - l;
        final int parentBottom = b - t;

        final View menu = this.getChildAt(0);
        final int menuWidth = menu.getMeasuredWidth();

        if (this.mDirection == DIRECTION_LEFT) {
            menu.layout(parentLeft, parentTop, parentLeft + menuWidth, parentBottom);
        } else {
            menu.layout(parentRight - menuWidth, parentTop, parentRight, parentBottom);
        }

        if (this.mAlwaysOpened) {
            if (this.mDirection == DIRECTION_LEFT) {
                this.mOffset = menuWidth;
            } else {
                this.mOffset = 0;
            }
        } else if (this.mMode == MODE_FINISHED) {
            this.mOffset = this.mDirection * menuWidth;
        } else if (this.mMode == MODE_READY) {
            this.mOffset = 0;
        }

        final View main = this.getChildAt(1);
        main.layout(
                parentLeft + this.mOffset,
                parentTop,
                parentLeft + this.mOffset + main.getMeasuredWidth(),
                parentBottom
                );

        this.invalidate();

        Runnable rn;
        while ((rn = this.mWhenReady.poll()) != null) {
            rn.run();
        }
    }

    @Override
    protected void onMeasure(final int wSp, final int hSp) {
        this.mMenuView = this.getChildAt(0);

        if (this.mAlwaysOpened) {
            final View main = this.getChildAt(1);

            if ((this.mMenuView != null) && (main != null)) {
                this.measureChild(this.mMenuView, wSp, hSp);
                final LayoutParams lp = (LayoutParams) main.getLayoutParams();

                if (this.mDirection == DIRECTION_LEFT) {
                    lp.leftMargin = this.mMenuView.getMeasuredWidth();
                } else {
                    lp.rightMargin = this.mMenuView.getMeasuredWidth();
                }
            }
        }

        super.onMeasure(wSp, hSp);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        final boolean handled = this.handleTouchEvent(ev);

        this.invalidate();

        return handled;
    }

    public boolean open() {
        if (this.isOpened() || this.mAlwaysOpened || (this.mMode == MODE_SLIDE)) {
            return false;
        }

        if (!this.isReadyForSlide()) {
            this.mWhenReady.add(new Runnable() {

                public void run() {
                    SlideHolder.this.open();
                }
            });

            return true;
        }

        this.initSlideMode();

        final Animation anim = new SlideAnimation(this.mOffset, this.mEndOffset, this.mSpeed);
        anim.setAnimationListener(this.mOpenListener);
        this.startAnimation(anim);

        this.invalidate();

        return true;
    }

    public boolean openImmediately() {
        if (this.isOpened() || this.mAlwaysOpened || (this.mMode == MODE_SLIDE)) {
            return false;
        }

        if (!this.isReadyForSlide()) {
            this.mWhenReady.add(new Runnable() {

                public void run() {
                    SlideHolder.this.openImmediately();
                }
            });

            return true;
        }

        this.mMenuView.setVisibility(View.VISIBLE);
        this.mMode = MODE_FINISHED;
        this.requestLayout();

        if (this.mListener != null) {
            this.mListener.onSlideCompleted(true);
        }

        return true;
    }

    /**
     * @param allow - if false, SlideHolder won't react to swiping gestures (but
     *            still will be able to work by manually invoking mathods)
     */
    public void setAllowInterceptTouch(final boolean allow) {
        this.mInterceptTouch = allow;
    }

    /**
     * @param opened - if true, SlideHolder will always be in opened state
     *            (which means that swiping won't work)
     */
    public void setAlwaysOpened(final boolean opened) {
        this.mAlwaysOpened = opened;

        this.requestLayout();
    }

    /**
     * @param direction - direction in which SlideHolder opens. Can be:
     *            DIRECTION_LEFT, DIRECTION_RIGHT
     */
    public void setDirection(final int direction) {
        this.closeImmediately();

        this.mDirection = direction;
    }

    /**
     * @param dispatch - if true, in open state SlideHolder will dispatch touch
     *            events to main layout (in other words - it will be clickable)
     */
    public void setDispatchTouchWhenOpened(final boolean dispatch) {
        this.mDispatchWhenOpened = dispatch;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.mEnabled = enabled;
    }

    public void setOnSlideListener(final OnSlideListener lis) {
        this.mListener = lis;
    }

    public void setSpeed(final float speed) {
        if (speed <= 0) {
            return;
        }
        this.mSpeed = speed;
    }

    public void toggle() {
        if (this.isOpened()) {
            this.close();
        } else {
            this.open();
        }
    }

    public void toggle(final boolean immediately) {
        if (immediately) {
            this.toggleImmediately();
        } else {
            this.toggle();
        }
    }

    public void toggleImmediately() {
        if (this.isOpened()) {
            this.closeImmediately();
        } else {
            this.openImmediately();
        }
    }

}
