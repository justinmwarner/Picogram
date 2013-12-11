/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import java.util.LinkedList;
import java.util.List;

import kankan.wheel.R;
import kankan.wheel.widget.adapters.WheelViewAdapter;

/**
 * Numeric wheel view.
 * 
 * @author Yuri Kanivets
 */
public class WheelView extends View {

    /** Top and bottom shadows colors */
    private static final int[] SHADOWS_COLORS = new int[] {
            0xFF111111,
            0x00AAAAAA, 0x00AAAAAA
    };

    /** Top and bottom items offset (to hide that) */
    private static final int ITEM_OFFSET_PERCENT = 10;

    /** Left and right padding value */
    private static final int PADDING = 10;

    /** Default count of visible items */
    private static final int DEF_VISIBLE_ITEMS = 5;

    // Wheel Values
    private int currentItem = 0;

    // Count of visible items
    private int visibleItems = DEF_VISIBLE_ITEMS;

    // Item height
    private int itemHeight = 0;

    // Center Line
    private Drawable centerDrawable;

    // Shadows drawables
    private GradientDrawable topShadow;
    private GradientDrawable bottomShadow;

    // Scrolling
    private WheelScroller scroller;
    private boolean isScrollingPerformed;
    private int scrollingOffset;

    // Cyclic
    boolean isCyclic = false;

    // Items layout
    private LinearLayout itemsLayout;

    // The number of first item in layout
    private int firstItem;

    // View adapter
    private WheelViewAdapter viewAdapter;

    // Recycle
    private final WheelRecycle recycle = new WheelRecycle(this);

    // Listeners
    private final List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private final List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
    private final List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();

    // Scrolling listener
    WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
        @Override
        public void onFinished() {
            if (WheelView.this.isScrollingPerformed) {
                WheelView.this.notifyScrollingListenersAboutEnd();
                WheelView.this.isScrollingPerformed = false;
            }

            WheelView.this.scrollingOffset = 0;
            WheelView.this.invalidate();
        }

        @Override
        public void onJustify() {
            if (Math.abs(WheelView.this.scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                WheelView.this.scroller.scroll(WheelView.this.scrollingOffset, 0);
            }
        }

        @Override
        public void onScroll(final int distance) {
            WheelView.this.doScroll(distance);

            final int height = WheelView.this.getHeight();
            if (WheelView.this.scrollingOffset > height) {
                WheelView.this.scrollingOffset = height;
                WheelView.this.scroller.stopScrolling();
            } else if (WheelView.this.scrollingOffset < -height) {
                WheelView.this.scrollingOffset = -height;
                WheelView.this.scroller.stopScrolling();
            }
        }

        @Override
        public void onStarted() {
            WheelView.this.isScrollingPerformed = true;
            WheelView.this.notifyScrollingListenersAboutStart();
        }
    };

    // Adapter listener
    private final DataSetObserver dataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            WheelView.this.invalidateWheel(false);
        }

        @Override
        public void onInvalidated() {
            WheelView.this.invalidateWheel(true);
        }
    };

    /**
     * Constructor
     */
    public WheelView(final Context context) {
        super(context);
        this.initData(context);
    }

    /**
     * Constructor
     */
    public WheelView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.initData(context);
    }

    /**
     * Constructor
     */
    public WheelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.initData(context);
    }

    /**
     * Adds wheel changing listener
     * 
     * @param listener the listener
     */
    public void addChangingListener(final OnWheelChangedListener listener) {
        this.changingListeners.add(listener);
    }

    /**
     * Adds wheel clicking listener
     * 
     * @param listener the listener
     */
    public void addClickingListener(final OnWheelClickedListener listener) {
        this.clickingListeners.add(listener);
    }

    /**
     * Adds wheel scrolling listener
     * 
     * @param listener the listener
     */
    public void addScrollingListener(final OnWheelScrollListener listener) {
        this.scrollingListeners.add(listener);
    }

    /**
     * Adds view for item to items layout
     * 
     * @param index the item index
     * @param first the flag indicates if view should be first
     * @return true if corresponding item exists and is added
     */
    private boolean addViewItem(final int index, final boolean first) {
        final View view = this.getItemView(index);
        if (view != null) {
            if (first) {
                this.itemsLayout.addView(view, 0);
            } else {
                this.itemsLayout.addView(view);
            }

            return true;
        }

        return false;
    }

    /**
     * Builds view for measuring
     */
    private void buildViewForMeasuring() {
        // clear all items
        if (this.itemsLayout != null) {
            this.recycle.recycleItems(this.itemsLayout, this.firstItem, new ItemsRange());
        } else {
            this.createItemsLayout();
        }

        // add views
        final int addItems = this.visibleItems / 2;
        for (int i = this.currentItem + addItems; i >= (this.currentItem - addItems); i--) {
            if (this.addViewItem(i, true)) {
                this.firstItem = i;
            }
        }
    }

    /**
     * Calculates control width and creates text layouts
     * 
     * @param widthSize the input layout width
     * @param mode the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(final int widthSize, final int mode) {
        this.initResourcesIfNecessary();

        // TODO: make it static
        this.itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        this.itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = this.itemsLayout.getMeasuredWidth();

        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width += 2 * PADDING;

            // Check against our minimum width
            width = Math.max(width, this.getSuggestedMinimumWidth());

            if ((mode == MeasureSpec.AT_MOST) && (widthSize < width)) {
                width = widthSize;
            }
        }

        this.itemsLayout.measure(
                MeasureSpec.makeMeasureSpec(width - (2 * PADDING), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        return width;
    }

    /**
     * Creates item layouts if necessary
     */
    private void createItemsLayout() {
        if (this.itemsLayout == null) {
            this.itemsLayout = new LinearLayout(this.getContext());
            this.itemsLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }

    /**
     * Scrolls the wheel
     * 
     * @param delta the scrolling value
     */
    private void doScroll(final int delta) {
        this.scrollingOffset += delta;

        final int itemHeight = this.getItemHeight();
        int count = this.scrollingOffset / itemHeight;

        int pos = this.currentItem - count;
        final int itemCount = this.viewAdapter.getItemsCount();

        int fixPos = this.scrollingOffset % itemHeight;
        if (Math.abs(fixPos) <= (itemHeight / 2)) {
            fixPos = 0;
        }
        if (this.isCyclic && (itemCount > 0)) {
            if (fixPos > 0) {
                pos--;
                count++;
            } else if (fixPos < 0) {
                pos++;
                count--;
            }
            // fix position by rotating
            while (pos < 0) {
                pos += itemCount;
            }
            pos %= itemCount;
        } else {
            //
            if (pos < 0) {
                count = this.currentItem;
                pos = 0;
            } else if (pos >= itemCount) {
                count = (this.currentItem - itemCount) + 1;
                pos = itemCount - 1;
            } else if ((pos > 0) && (fixPos > 0)) {
                pos--;
                count++;
            } else if ((pos < (itemCount - 1)) && (fixPos < 0)) {
                pos++;
                count--;
            }
        }

        final int offset = this.scrollingOffset;
        if (pos != this.currentItem) {
            this.setCurrentItem(pos, false);
        } else {
            this.invalidate();
        }

        // update offset
        this.scrollingOffset = offset - (count * itemHeight);
        if (this.scrollingOffset > this.getHeight()) {
            this.scrollingOffset = (this.scrollingOffset % this.getHeight()) + this.getHeight();
        }
    }

    /**
     * Draws rect for current value
     * 
     * @param canvas the canvas for drawing
     */
    private void drawCenterRect(final Canvas canvas) {
        final int center = this.getHeight() / 2;
        final int offset = (int) ((this.getItemHeight() / 2) * 1.2);
        this.centerDrawable.setBounds(0, center - offset, this.getWidth(), center + offset);
        this.centerDrawable.draw(canvas);
    }

    /**
     * Draws items
     * 
     * @param canvas the canvas for drawing
     */
    private void drawItems(final Canvas canvas) {
        canvas.save();

        final int top = ((this.currentItem - this.firstItem) * this.getItemHeight())
                + ((this.getItemHeight() - this.getHeight()) / 2);
        canvas.translate(PADDING, -top + this.scrollingOffset);

        this.itemsLayout.draw(canvas);

        canvas.restore();
    }

    /**
     * Draws shadows on top and bottom of control
     * 
     * @param canvas the canvas for drawing
     */
    private void drawShadows(final Canvas canvas) {
        final int height = (int) (1.5 * this.getItemHeight());
        this.topShadow.setBounds(0, 0, this.getWidth(), height);
        // topShadow.draw(canvas);

        this.bottomShadow
                .setBounds(0, this.getHeight() - height, this.getWidth(), this.getHeight());
        // bottomShadow.draw(canvas);
    }

    /**
     * Gets current value
     * 
     * @return the current value
     */
    public int getCurrentItem() {
        return this.currentItem;
    }

    /**
     * Calculates desired height for layout
     * 
     * @param layout the source layout
     * @return the desired layout height
     */
    private int getDesiredHeight(final LinearLayout layout) {
        if ((layout != null) && (layout.getChildAt(0) != null)) {
            this.itemHeight = layout.getChildAt(0).getMeasuredHeight();
        }

        final int desired = (this.itemHeight * this.visibleItems)
                - ((this.itemHeight * ITEM_OFFSET_PERCENT) / 50);

        return Math.max(desired, this.getSuggestedMinimumHeight());
    }

    /**
     * Returns height of wheel item
     * 
     * @return the item height
     */
    private int getItemHeight() {
        if (this.itemHeight != 0) {
            return this.itemHeight;
        }

        if ((this.itemsLayout != null) && (this.itemsLayout.getChildAt(0) != null)) {
            this.itemHeight = this.itemsLayout.getChildAt(0).getHeight();
            return this.itemHeight;
        }

        return this.getHeight() / this.visibleItems;
    }

    /**
     * Calculates range for wheel items
     * 
     * @return the items range
     */
    private ItemsRange getItemsRange() {
        if (this.getItemHeight() == 0) {
            return null;
        }

        int first = this.currentItem;
        int count = 1;

        while ((count * this.getItemHeight()) < this.getHeight()) {
            first--;
            count += 2; // top + bottom items
        }

        if (this.scrollingOffset != 0) {
            if (this.scrollingOffset > 0) {
                first--;
            }
            count++;

            // process empty items above the first or below the second
            final int emptyItems = this.scrollingOffset / this.getItemHeight();
            first -= emptyItems;
            count += Math.asin(emptyItems);
        }
        return new ItemsRange(first, count);
    }

    /**
     * Returns view for specified item
     * 
     * @param index the item index
     * @return item view or empty view if index is out of bounds
     */
    private View getItemView(int index) {
        if ((this.viewAdapter == null) || (this.viewAdapter.getItemsCount() == 0)) {
            return null;
        }
        final int count = this.viewAdapter.getItemsCount();
        if (!this.isValidItemIndex(index)) {
            return this.viewAdapter.getEmptyItem(this.recycle.getEmptyItem(), this.itemsLayout);
        } else {
            while (index < 0) {
                index = count + index;
            }
        }

        index %= count;
        return this.viewAdapter.getItem(index, this.recycle.getItem(), this.itemsLayout);
    }

    /**
     * Gets view adapter
     * 
     * @return the view adapter
     */
    public WheelViewAdapter getViewAdapter() {
        return this.viewAdapter;
    }

    /**
     * Gets count of visible items
     * 
     * @return the count of visible items
     */
    public int getVisibleItems() {
        return this.visibleItems;
    }

    /**
     * Initializes class data
     * 
     * @param context the context
     */
    private void initData(final Context context) {
        this.scroller = new WheelScroller(this.getContext(), this.scrollingListener);
    }

    /**
     * Initializes resources
     */
    private void initResourcesIfNecessary() {
        if (this.centerDrawable == null) {
            this.centerDrawable = this.getContext().getResources()
                    .getDrawable(R.drawable.wheel_val);
        }

        if (this.topShadow == null) {
            this.topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }

        if (this.bottomShadow == null) {
            this.bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }

        this.setBackgroundResource(R.drawable.wheel_bg);
    }

    /**
     * Invalidates wheel
     * 
     * @param clearCaches if true then cached views will be clear
     */
    public void invalidateWheel(final boolean clearCaches) {
        if (clearCaches) {
            this.recycle.clearAll();
            if (this.itemsLayout != null) {
                this.itemsLayout.removeAllViews();
            }
            this.scrollingOffset = 0;
        } else if (this.itemsLayout != null) {
            // cache all items
            this.recycle.recycleItems(this.itemsLayout, this.firstItem, new ItemsRange());
        }

        this.invalidate();
    }

    /**
     * Tests if wheel is cyclic. That means before the 1st item there is shown
     * the last one
     * 
     * @return true if wheel is cyclic
     */
    public boolean isCyclic() {
        return this.isCyclic;
    }

    /**
     * Checks whether intem index is valid
     * 
     * @param index the item index
     * @return true if item index is not out of bounds or the wheel is cyclic
     */
    private boolean isValidItemIndex(final int index) {
        return (this.viewAdapter != null) && (this.viewAdapter.getItemsCount() > 0) &&
                (this.isCyclic || ((index >= 0) && (index < this.viewAdapter.getItemsCount())));
    }

    /**
     * Sets layouts width and height
     * 
     * @param width the layout width
     * @param height the layout height
     */
    private void layout(final int width, final int height) {
        final int itemsWidth = width - (2 * PADDING);

        this.itemsLayout.layout(0, 0, itemsWidth, height);
    }

    /**
     * Notifies changing listeners
     * 
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    protected void notifyChangingListeners(final int oldValue, final int newValue) {
        for (final OnWheelChangedListener listener : this.changingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * Notifies listeners about clicking
     */
    protected void notifyClickListenersAboutClick(final int item) {
        for (final OnWheelClickedListener listener : this.clickingListeners) {
            listener.onItemClicked(this, item);
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (final OnWheelScrollListener listener : this.scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected void notifyScrollingListenersAboutStart() {
        for (final OnWheelScrollListener listener : this.scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if ((this.viewAdapter != null) && (this.viewAdapter.getItemsCount() > 0)) {
            this.updateView();

            this.drawItems(canvas);
            this.drawCenterRect(canvas);
        }

        this.drawShadows(canvas);
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r,
            final int b) {
        this.layout(r - l, b - t);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        this.buildViewForMeasuring();

        final int width = this.calculateLayoutWidth(widthSize, widthMode);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = this.getDesiredHeight(this.itemsLayout);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        this.setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!this.isEnabled() || (this.getViewAdapter() == null)) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (this.getParent() != null) {
                    this.getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!this.isScrollingPerformed) {
                    int distance = (int) event.getY() - (this.getHeight() / 2);
                    if (distance > 0) {
                        distance += this.getItemHeight() / 2;
                    } else {
                        distance -= this.getItemHeight() / 2;
                    }
                    final int items = distance / this.getItemHeight();
                    if ((items != 0) && this.isValidItemIndex(this.currentItem + items)) {
                        this.notifyClickListenersAboutClick(this.currentItem + items);
                    }
                }
                break;
        }

        return this.scroller.onTouchEvent(event);
    }

    /**
     * Rebuilds wheel items if necessary. Caches all unused items.
     * 
     * @return true if items are rebuilt
     */
    private boolean rebuildItems() {
        boolean updated = false;
        final ItemsRange range = this.getItemsRange();
        if (this.itemsLayout != null) {
            final int first = this.recycle.recycleItems(this.itemsLayout, this.firstItem, range);
            updated = this.firstItem != first;
            this.firstItem = first;
        } else {
            this.createItemsLayout();
            updated = true;
        }

        if (!updated) {
            updated = (this.firstItem != range.getFirst())
                    || (this.itemsLayout.getChildCount() != range.getCount());
        }

        if ((this.firstItem > range.getFirst()) && (this.firstItem <= range.getLast())) {
            for (int i = this.firstItem - 1; i >= range.getFirst(); i--) {
                if (!this.addViewItem(i, true)) {
                    break;
                }
                this.firstItem = i;
            }
        } else {
            this.firstItem = range.getFirst();
        }

        int first = this.firstItem;
        for (int i = this.itemsLayout.getChildCount(); i < range.getCount(); i++) {
            if (!this.addViewItem(this.firstItem + i, false)
                    && (this.itemsLayout.getChildCount() == 0)) {
                first++;
            }
        }
        this.firstItem = first;

        return updated;
    }

    /**
     * Removes wheel changing listener
     * 
     * @param listener the listener
     */
    public void removeChangingListener(final OnWheelChangedListener listener) {
        this.changingListeners.remove(listener);
    }

    /**
     * Removes wheel clicking listener
     * 
     * @param listener the listener
     */
    public void removeClickingListener(final OnWheelClickedListener listener) {
        this.clickingListeners.remove(listener);
    }

    /**
     * Removes wheel scrolling listener
     * 
     * @param listener the listener
     */
    public void removeScrollingListener(final OnWheelScrollListener listener) {
        this.scrollingListeners.remove(listener);
    }

    /**
     * Scroll the wheel
     * 
     * @param itemsToSkip items to scroll
     * @param time scrolling duration
     */
    public void scroll(final int itemsToScroll, final int time) {
        final int distance = (itemsToScroll * this.getItemHeight()) - this.scrollingOffset;
        this.scroller.scroll(distance, time);
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     * 
     * @param index the item index
     */
    public void setCurrentItem(final int index) {
        this.setCurrentItem(index, false);
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     * 
     * @param index the item index
     * @param animated the animation flag
     */
    public void setCurrentItem(int index, final boolean animated) {
        if ((this.viewAdapter == null) || (this.viewAdapter.getItemsCount() == 0)) {
            return; // throw?
        }

        final int itemCount = this.viewAdapter.getItemsCount();
        if ((index < 0) || (index >= itemCount)) {
            if (this.isCyclic) {
                while (index < 0) {
                    index += itemCount;
                }
                index %= itemCount;
            } else {
                return; // throw?
            }
        }
        if (index != this.currentItem) {
            if (animated) {
                int itemsToScroll = index - this.currentItem;
                if (this.isCyclic) {
                    final int scroll = (itemCount + Math.min(index, this.currentItem))
                            - Math.max(index, this.currentItem);
                    if (scroll < Math.abs(itemsToScroll)) {
                        itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
                    }
                }
                this.scroll(itemsToScroll, 0);
            } else {
                this.scrollingOffset = 0;

                final int old = this.currentItem;
                this.currentItem = index;

                this.notifyChangingListeners(old, this.currentItem);

                this.invalidate();
            }
        }
    }

    /**
     * Set wheel cyclic flag
     * 
     * @param isCyclic the flag to set
     */
    public void setCyclic(final boolean isCyclic) {
        this.isCyclic = isCyclic;
        this.invalidateWheel(false);
    }

    /**
     * Set the the specified scrolling interpolator
     * 
     * @param interpolator the interpolator
     */
    public void setInterpolator(final Interpolator interpolator) {
        this.scroller.setInterpolator(interpolator);
    }

    /**
     * Sets view adapter. Usually new adapters contain different views, so it
     * needs to rebuild view by calling measure().
     * 
     * @param viewAdapter the view adapter
     */
    public void setViewAdapter(final WheelViewAdapter viewAdapter) {
        if (this.viewAdapter != null) {
            this.viewAdapter.unregisterDataSetObserver(this.dataObserver);
        }
        this.viewAdapter = viewAdapter;
        if (this.viewAdapter != null) {
            this.viewAdapter.registerDataSetObserver(this.dataObserver);
        }

        this.invalidateWheel(true);
    }

    /**
     * Sets the desired count of visible items. Actual amount of visible items
     * depends on wheel layout parameters. To apply changes and rebuild view
     * call measure().
     * 
     * @param count the desired count for visible items
     */
    public void setVisibleItems(final int count) {
        this.visibleItems = count;
    }

    /**
     * Stops scrolling
     */
    public void stopScrolling() {
        this.scroller.stopScrolling();
    }

    /**
     * Updates view. Rebuilds items and label if necessary, recalculate items
     * sizes.
     */
    private void updateView() {
        if (this.rebuildItems()) {
            this.calculateLayoutWidth(this.getWidth(), MeasureSpec.EXACTLY);
            this.layout(this.getWidth(), this.getHeight());
        }
    }
}
