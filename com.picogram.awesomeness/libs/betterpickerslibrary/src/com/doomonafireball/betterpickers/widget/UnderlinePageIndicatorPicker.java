/*
 * Copyright (C) 2012 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doomonafireball.betterpickers.widget;



import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.doomonafireball.betterpickers.R;
/**
 * Draws a line for each page. The current page line is colored differently than the unselected page lines.
 */
public class UnderlinePageIndicatorPicker extends View implements PageIndicator {

	static class SavedState extends BaseSavedState {

		int currentPage;

		@SuppressWarnings("UnusedDeclaration")
		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		private SavedState(final Parcel in) {
			super(in);
			this.currentPage = in.readInt();
		}

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(final Parcel dest, final int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(this.currentPage);
		}
	}

	private int mColorUnderline;

	private static final int INVALID_POINTER = -1;

	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private ViewPager mViewPager;
	private ViewPager.OnPageChangeListener mListener;
	private int mScrollState;
	private int mCurrentPage;

	private float mPositionOffset;
	private int mTouchSlop;
	private float mLastMotionX = -1;
	private int mActivePointerId = INVALID_POINTER;

	private boolean mIsDragging;
	private PickerLinearLayout mTitleView = null;

	private Paint rectPaint;

	public UnderlinePageIndicatorPicker(final Context context) {
		this(context, null);
	}

	public UnderlinePageIndicatorPicker(final Context context, final AttributeSet attrs) {
		super(context, attrs);


	}

	public UnderlinePageIndicatorPicker(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		this.mColorUnderline = this.getResources().getColor(R.color.dialog_text_color_holo_dark);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BetterPickersDialogFragment, defStyle, 0);
		this.mColorUnderline = a.getColor(R.styleable.BetterPickersDialogFragment_bpKeyboardIndicatorColor, this.mColorUnderline);

		this.rectPaint = new Paint();
		this.rectPaint.setAntiAlias(true);
		this.rectPaint.setStyle(Style.FILL);

		a.recycle();

		final ViewConfiguration configuration = ViewConfiguration.get(context);
		this.mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
	}

	public int getSelectedColor() {
		return this.mPaint.getColor();
	}

	@Override
	public void notifyDataSetChanged() {
		this.invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		final int count = this.mViewPager.getAdapter().getCount();

		if (this.isInEditMode() || (count == 0)) {
			return;
		}

		if (this.mTitleView != null) {
			final View currentTab = this.mTitleView.getViewAt(this.mCurrentPage);
			float lineLeft = currentTab.getLeft();
			float lineRight = currentTab.getRight();

			// if there is an offset, start interpolating left and right
			// coordinates
			// between current and next tab
			if ((this.mPositionOffset > 0f) && (this.mCurrentPage < (count - 1))) {

				final View nextTab = this.mTitleView.getViewAt(this.mCurrentPage + 1);
				final float nextTabLeft = nextTab.getLeft();
				final float nextTabRight = nextTab.getRight();

				lineLeft = ((this.mPositionOffset * nextTabLeft) + ((1f - this.mPositionOffset) * lineLeft));
				lineRight = ((this.mPositionOffset * nextTabRight) + ((1f - this.mPositionOffset) * lineRight));
			}

			canvas.drawRect(lineLeft, this.getPaddingBottom(), lineRight, this.getHeight() - this.getPaddingBottom(), this.mPaint);
		}
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
		this.mCurrentPage = position;
		this.mPositionOffset = positionOffset;
		this.invalidate();

		if (this.mListener != null) {
			this.mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
		}
	}

	@Override
	public void onPageScrollStateChanged(final int state) {
		this.mScrollState = state;

		if (this.mListener != null) {
			this.mListener.onPageScrollStateChanged(state);
		}
	}

	@Override
	public void onPageSelected(final int position) {
		if (this.mScrollState == ViewPager.SCROLL_STATE_IDLE) {
			this.mCurrentPage = position;
			this.mPositionOffset = 0;
			this.invalidate();
		}
		if (this.mListener != null) {
			this.mListener.onPageSelected(position);
		}
	}

	@Override
	public void onRestoreInstanceState(final Parcelable state) {
		final SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		this.mCurrentPage = savedState.currentPage;
		this.requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		final SavedState savedState = new SavedState(superState);
		savedState.currentPage = this.mCurrentPage;
		return savedState;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		if (super.onTouchEvent(ev)) {
			return true;
		}
		if ((this.mViewPager == null) || (this.mViewPager.getAdapter().getCount() == 0)) {
			return false;
		}

		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
				this.mLastMotionX = ev.getX();
				break;

			case MotionEvent.ACTION_MOVE: {
				final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, this.mActivePointerId);
				final float x = MotionEventCompat.getX(ev, activePointerIndex);
				final float deltaX = x - this.mLastMotionX;

				if (!this.mIsDragging) {
					if (Math.abs(deltaX) > this.mTouchSlop) {
						this.mIsDragging = true;
					}
				}

				if (this.mIsDragging) {
					this.mLastMotionX = x;
					if (this.mViewPager.isFakeDragging() || this.mViewPager.beginFakeDrag()) {
						this.mViewPager.fakeDragBy(deltaX);
					}
				}

				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (!this.mIsDragging) {
					final int count = this.mViewPager.getAdapter().getCount();
					final int width = this.getWidth();
					final float halfWidth = width / 2f;
					final float sixthWidth = width / 6f;

					if ((this.mCurrentPage > 0) && (ev.getX() < (halfWidth - sixthWidth))) {
						if (action != MotionEvent.ACTION_CANCEL) {
							this.mViewPager.setCurrentItem(this.mCurrentPage - 1);
						}
						return true;
					} else if ((this.mCurrentPage < (count - 1)) && (ev.getX() > (halfWidth + sixthWidth))) {
						if (action != MotionEvent.ACTION_CANCEL) {
							this.mViewPager.setCurrentItem(this.mCurrentPage + 1);
						}
						return true;
					}
				}

				this.mIsDragging = false;
				this.mActivePointerId = INVALID_POINTER;
				if (this.mViewPager.isFakeDragging()) {
					this.mViewPager.endFakeDrag();
				}
				break;

			case MotionEventCompat.ACTION_POINTER_DOWN: {
				final int index = MotionEventCompat.getActionIndex(ev);
				this.mLastMotionX = MotionEventCompat.getX(ev, index);
				this.mActivePointerId = MotionEventCompat.getPointerId(ev, index);
				break;
			}

			case MotionEventCompat.ACTION_POINTER_UP:
				final int pointerIndex = MotionEventCompat.getActionIndex(ev);
				final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
				if (pointerId == this.mActivePointerId) {
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					this.mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
				}
				this.mLastMotionX = MotionEventCompat.getX(ev, MotionEventCompat.findPointerIndex(ev, this.mActivePointerId));
				break;
		}

		return true;
	}

	@Override
	public void setCurrentItem(final int item) {
		if (this.mViewPager == null) {
			throw new IllegalStateException("ViewPager has not been bound.");
		}
		this.mViewPager.setCurrentItem(item);
		this.mCurrentPage = item;
		this.invalidate();
	}

	@Override
	public void setOnPageChangeListener(final ViewPager.OnPageChangeListener listener) {
		this.mListener = listener;
	}

	public void setSelectedColor(final int selectedColor) {
		this.mPaint.setColor(selectedColor);
		this.invalidate();
	}

	public void setTitleView(final PickerLinearLayout titleView) {
		this.mTitleView = titleView;
		this.invalidate();
	}

	@Override
	public void setViewPager(final ViewPager viewPager) {
		if (this.mViewPager == viewPager) {
			return;
		}
		if (this.mViewPager != null) {
			// Clear us from the old pager.
			this.mViewPager.setOnPageChangeListener(null);
		}
		if (viewPager.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}
		this.mViewPager = viewPager;
		this.mViewPager.setOnPageChangeListener(this);
		this.invalidate();
	}

	@Override
	public void setViewPager(final ViewPager view, final int initialPosition) {
		this.setViewPager(view);
		this.setCurrentItem(initialPosition);
	}
}