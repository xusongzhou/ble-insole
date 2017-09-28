package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by zengfs on 2016/2/2.
 * 可控制是否能滑动
 */
public class MyViewPager extends ViewPager {
	
	private boolean isTouchEnabled = true;
	
	public MyViewPager(Context context) {
		super(context);
	}

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 设置ViewPager是否可滑动
	 */
	public void setTouchEnabled(boolean enable) {
		isTouchEnabled = enable;
	}
	
	/**
	 * 是否禁用其事件，实现不能滑动
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return isTouchEnabled && super.onTouchEvent(ev);
	}

	/**
	 * 是否截触摸事件，让事件可以向内层传递
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return isTouchEnabled && super.onInterceptTouchEvent(ev);
	}
}
