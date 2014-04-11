
package com.picogram.awesomeness;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class TestFragmentAdapter extends FragmentPagerAdapter {
	protected static final int[] CONTENT = new int[] {
			R.drawable.share, R.drawable.leader, R.drawable.cloud, R.drawable.share, R.drawable.future
	};

	private int mCount = CONTENT.length;

	public TestFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return TestFragment.newInstance(CONTENT[position % CONTENT.length]);
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return TestFragmentAdapter.CONTENT[position % CONTENT.length] + "";
	}

	public void setCount(int count) {
		if (count > 0 && count <= 10) {
			mCount = count;
			notifyDataSetChanged();
		}
	}
}
