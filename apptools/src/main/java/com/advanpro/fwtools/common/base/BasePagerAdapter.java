package com.advanpro.fwtools.common.base;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by zengfs on 2016/2/21.
 * 基本ViewPager的基类
 */
public class BasePagerAdapter extends PagerAdapter {

	private List<? extends BasePager> pagers;

	public BasePagerAdapter(List<? extends BasePager> pagers) {
		this.pagers = pagers;
	}

	public List<? extends BasePager> getPagers() {
		return pagers;
	}
	private int mChildCount = 0;
	
	@Override
	public int getCount() {
		return pagers.size();
	}

	@Override
	public void notifyDataSetChanged() {
		mChildCount=getCount();
		super.notifyDataSetChanged();
	}

	@Override
	public int getItemPosition(Object object)   {
		if ( mChildCount > 0) {
			mChildCount --;
			return POSITION_NONE;
		}
		return super.getItemPosition(object);
	}

	@Override
	public boolean isViewFromObject(View view, Object o) {
		return view == o;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(pagers.get(position).rootView);

		return pagers.get(position).rootView;
	}


	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {

	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
}
