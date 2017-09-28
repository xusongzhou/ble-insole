package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import com.advanpro.fwtools.common.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengfs on 2016/1/19.
 * ViewPager指示器，指示器内容可自定义
 */
public class ViewPagerIndicator extends LinearLayout implements View.OnClickListener {
	private OnTabCheckListener listener;
	private boolean isDividerEnabled = true;//是否显示分隔线
	private int dividerColor = 0x55585858;//分隔线颜色
	private boolean isCheckedBackgroundEnabled;//选中时是否显示背景
	private int checkedBackgroundColor = 0x88CCCCCC;//选中时背景颜色
	private boolean isIndicatorEnabled = true;//指示器是否显示
	private int indicatorColor = 0xFFEC4529;//指示器颜色
	private GradientDrawable indicatorDrawable;
	private LinearLayout lastClickContainer;//上次被点击
	private float indicatorRadio = 0.5f;//指示器长度
	private int currentPosition;
	private View currentContentView;
	private List<LinearLayout> containers = new ArrayList<>();
	private ViewPager viewPager;
	private boolean isPagerSmoothScrollEnabled = true;//是否使用ViewPager页面切换的过滤动画

	private static class Holder {
		public int position;
		public View indicator;
		public View contentView;

		public Holder(int position, View indicator, View contentView) {
			this.position = position;
			this.indicator = indicator;
			this.contentView = contentView;
		}
	}

	/**
	 * TabView可通过实现此接口改变状态
	 */
	public interface OnCheckedChangeListener {
		void onCheckedChanged(boolean isChecked);
	}
	
	public interface OnTabCheckListener {
		void onTabCheck(View contentView, int position);
	}
	
	public ViewPagerIndicator(Context context) {
		this(context, null);
	}

	public ViewPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 设置选中状态改变监听器
	 */
	public void setOnTabCheckListener(OnTabCheckListener listener) {
		this.listener = listener;
	}

	@Override
	public void onClick(View v) {		
		LinearLayout container = (LinearLayout) v;
		//如果两次点击不一样，改变状态
		if (!container.equals(lastClickContainer)) {			
			updateContainer(container);
			if (listener != null) listener.onTabCheck(currentContentView, currentPosition);
			if (viewPager != null) viewPager.setCurrentItem(currentPosition, isPagerSmoothScrollEnabled);
		}
		lastClickContainer = container;
	}

	/*
	 * 去除或更新容器选中状态
	 */
	private void updateContainer(LinearLayout container) {
		//去除上次tab选中状态
		if (lastClickContainer != null) {
			Holder holder = (Holder) lastClickContainer.getTag();
			holder.indicator.setBackgroundColor(Color.TRANSPARENT);
			lastClickContainer.setBackgroundColor(Color.TRANSPARENT);
			if (holder.contentView instanceof OnCheckedChangeListener) {
				((OnCheckedChangeListener)holder.contentView).onCheckedChanged(false);				
			}
		}
		//更新本次tab选中状态
		if (container != null) {
			Holder holder = (Holder) container.getTag();
			holder.indicator.setBackground(getIndicatorDrawable());
			if (holder.contentView instanceof OnCheckedChangeListener) {
				((OnCheckedChangeListener)holder.contentView).onCheckedChanged(true);
			}
			if (isCheckedBackgroundEnabled) container.setBackgroundColor(checkedBackgroundColor);
			currentPosition = holder.position;
			currentContentView = holder.contentView;			
		}
	}

	/**
	 * 是否使用ViewPager页面切换的过滤动画
	 */
	public void setPagerSmoothScrollEnabled(boolean enable) {
		isPagerSmoothScrollEnabled = enable;
	}
	
	/**
	 * 设置是否显示指示器
	 */
	public void setIndicatorEnabled(boolean enable) {
		isIndicatorEnabled = enable;
	}
	
	/**
	 * 设置选中tab
	 */
	public void check(int postion) {
		try {
			onClick(containers.get(postion));			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 获取当前选中的标签所在位置
	 */
	public int getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * 设置选中时是否显示背景
	 */
	public void setCheckedBackgroundEnabled(boolean enable) {
		isCheckedBackgroundEnabled = enable;
	}

	/**
	 * 设置分隔线颜色
	 */
	public void setDividerColor(int dividerColor) {
		this.dividerColor = dividerColor;
	}

	/**
	 * 设置指示器颜色
	 */
	public void setIndicatorColor(int indicatorColor) {
		this.indicatorColor = indicatorColor;
	}

	/**
	 * 设置指示器长度与单个Tab长度的比值
	 * @param radio 范围0 ~ 1，当为1时填充Tab
	 */
	public void setIndicatorRadio(float radio) {
		if (radio <= 0) isIndicatorEnabled = false;
		else if (radio > 1) indicatorRadio = 1;
		else indicatorRadio = radio;
	}
	
	/**
	 * 设置选中时背景颜色
	 */
	public void setCheckedBackgroundColor(int checkedBackgroundColor) {
		this.checkedBackgroundColor = checkedBackgroundColor;
	}

	/**
	 * 设置是否显示分隔线
	 */
	public void setDividerEnabled(boolean dividerEnabled) {
		isDividerEnabled = dividerEnabled;
	}

	/**
	 * 设置标签内容
	 */
	public void setTabContentViews(View... contentViews) {
		if (contentViews == null || contentViews.length == 0) throw new IllegalArgumentException("参数不合法");		
		removeAllViews();
		int tabCount = 0;
		for (View contentView : contentViews) {
			tabCount++;
			contentView.measure(0, 0);
			if (tabCount > 1 && isDividerEnabled) addView(createDivider());
			addView(createContainer(contentView), new LayoutParams(0, -2, 1));
		}
	}

	/**
	 * 设置ViewPager。调用此方法前，确保已进行ViewPager.setAdapter(pagerAdapter);
	 */
	public void setViewPager(ViewPager viewPager) {
		this.viewPager = viewPager;
		viewPager.addOnPageChangeListener(new pageChangeListener());
	}
	
	private class pageChangeListener extends ViewPager.SimpleOnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
			try {
				updateContainer(containers.get(position));
				lastClickContainer = containers.get(position);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private View createDivider() {
		View view = new View(getContext());
		LayoutParams params = new LayoutParams(UiUtils.dip2px(0.5f), -1);
		params.setMargins(0, UiUtils.dip2px(10), 0, UiUtils.dip2px(10));
		view.setLayoutParams(params);
		view.setBackgroundColor(dividerColor);
		return view;
	}

	private View createContainer(View tabView) {
		LinearLayout rootContainer = new LinearLayout(getContext());
		rootContainer.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout indicatorContainer = new LinearLayout(getContext());
		indicatorContainer.setWeightSum(1 / indicatorRadio);	
		indicatorContainer.setGravity(Gravity.CENTER);
		View indicator = new View(getContext());
		indicator.setLayoutParams(new LayoutParams(0, UiUtils.dip2px(2), 1));		
		indicatorContainer.addView(indicator);
		if (isIndicatorEnabled) rootContainer.addView(indicatorContainer, -1, -2);
		
		rootContainer.addView(tabView, -1, -2);
		
		containers.add(rootContainer);
		rootContainer.setTag(new Holder(containers.size() - 1, indicator, tabView));
		if (containers.size() - 1 == 0) {
			updateContainer(rootContainer);//默认第一个标签选中
			lastClickContainer = rootContainer;
		}
		rootContainer.setOnClickListener(this);
		return rootContainer;
	}

	private Drawable getIndicatorDrawable() {
		if (indicatorDrawable == null) {
			indicatorDrawable = new GradientDrawable();
			indicatorDrawable.setCornerRadius(UiUtils.dip2px(5));
		}		
		indicatorDrawable.setColor(indicatorColor);
		return indicatorDrawable;
	}
}
