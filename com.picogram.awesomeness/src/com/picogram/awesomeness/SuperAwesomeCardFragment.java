package com.picogram.awesomeness;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class SuperAwesomeCardFragment extends Fragment {

	private static final String ARG_POSITION = "position";
	private static final String TAG = "SuperAwesomeCardFragment";

	public static SuperAwesomeCardFragment newInstance(final int position) {
		final SuperAwesomeCardFragment f = new SuperAwesomeCardFragment();
		final Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	private int position;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.position = this.getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {
		final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		final FrameLayout fl = new FrameLayout(this.getActivity());
		fl.setLayoutParams(params);
		View view;
		if (this.position == 4)
		{
			view = inflater.inflate(R.layout.include_create_step_five, null);
			fl.addView(view);
		} else if (this.position == 3)
		{
			view = inflater.inflate(R.layout.include_create_step_four, null);
			fl.addView(view);
		} else if (this.position == 2)
		{
			view = inflater.inflate(R.layout.include_create_step_three, null);
			fl.addView(view);
		} else if (this.position == 1)
		{
			view = inflater.inflate(R.layout.include_create_step_two, null);
			fl.addView(view);
		} else
		{
			view = inflater.inflate(R.layout.include_create_step_one, null);
			fl.addView(view);
		}
		fl.addView(view);
		return fl;
	}

}
