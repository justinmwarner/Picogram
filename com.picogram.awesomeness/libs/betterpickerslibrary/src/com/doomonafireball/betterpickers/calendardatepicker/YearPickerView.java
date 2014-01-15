/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.doomonafireball.betterpickers.calendardatepicker;

import com.doomonafireball.betterpickers.R;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog.OnDateChangedListener;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a selectable list of years.
 */
public class YearPickerView extends ListView implements OnItemClickListener, OnDateChangedListener {

	private class YearAdapter extends ArrayAdapter<String> {

		public YearAdapter(final Context context, final int resource, final List<String> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final TextViewWithCircularIndicator v = (TextViewWithCircularIndicator)
					super.getView(position, convertView, parent);
			v.requestLayout();
			final int year = YearPickerView.this.getYearFromTextView(v);
			final boolean selected = YearPickerView.this.mController.getSelectedDay().year == year;
			v.drawIndicator(selected);
			if (selected) {
				YearPickerView.this.mSelectedView = v;
			}
			return v;
		}
	}

	private static final String TAG = "YearPickerView";
	private final CalendarDatePickerController mController;
	private YearAdapter mAdapter;
	private final int mViewSize;
	private final int mChildSize;

	private TextViewWithCircularIndicator mSelectedView;

	/**
	 * @param context
	 */
	public YearPickerView(final Context context, final CalendarDatePickerController controller) {
		super(context);
		this.mController = controller;
		this.mController.registerOnDateChangedListener(this);
		final ViewGroup.LayoutParams frame = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(frame);
		final Resources res = context.getResources();
		this.mViewSize = res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
		this.mChildSize = res.getDimensionPixelOffset(R.dimen.year_label_height);
		this.setVerticalFadingEdgeEnabled(true);
		this.setFadingEdgeLength(this.mChildSize / 3);
		this.init(context);
		this.setOnItemClickListener(this);
		this.setSelector(new StateListDrawable());
		this.setDividerHeight(0);
		this.onDateChanged();
	}

	public int getFirstPositionOffset() {
		final View firstChild = this.getChildAt(0);
		if (firstChild == null) {
			return 0;
		}
		return firstChild.getTop();
	}

	private int getYearFromTextView(final TextView view) {
		return Integer.valueOf(view.getText().toString());
	}

	private void init(final Context context) {
		final ArrayList<String> years = new ArrayList<String>();
		for (int year = this.mController.getMinYear(); year <= this.mController.getMaxYear(); year++) {
			years.add(String.format("%d", year));
		}
		this.mAdapter = new YearAdapter(context, R.layout.calendar_year_label_text_view, years);
		this.setAdapter(this.mAdapter);
	}

	@Override
	public void onDateChanged() {
		this.mAdapter.notifyDataSetChanged();
		this.postSetSelectionCentered(this.mController.getSelectedDay().year - this.mController.getMinYear());
	}

	@Override
	public void onInitializeAccessibilityEvent(final AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent(event);
		if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
			event.setFromIndex(0);
			event.setToIndex(0);
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		this.mController.tryVibrate();
		final TextViewWithCircularIndicator clickedView = (TextViewWithCircularIndicator) view;
		if (clickedView != null) {
			if (clickedView != this.mSelectedView) {
				if (this.mSelectedView != null) {
					this.mSelectedView.drawIndicator(false);
					this.mSelectedView.requestLayout();
				}
				clickedView.drawIndicator(true);
				clickedView.requestLayout();
				this.mSelectedView = clickedView;
			}
			this.mController.onYearSelected(this.getYearFromTextView(clickedView));
			this.mAdapter.notifyDataSetChanged();
		}
	}

	public void postSetSelectionCentered(final int position) {
		this.postSetSelectionFromTop(position, (this.mViewSize / 2) - (this.mChildSize / 2));
	}

	public void postSetSelectionFromTop(final int position, final int offset) {
		this.post(new Runnable() {

			@Override
			public void run() {
				YearPickerView.this.setSelectionFromTop(position, offset);
				YearPickerView.this.requestLayout();
			}
		});
	}
}
