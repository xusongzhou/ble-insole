package com.advanpro.fwtools.module;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.advanpro.fwtools.common.util.UiUtils;

/**
 * Created by zengfs on 2016/1/14.
 * 底部导航栏标签
 */
public class TabIndicatorView extends LinearLayout {
	
	private ImageView tabView;
	private View redPoint;
	private int selectedColor;
	
	public TabIndicatorView(Context context) {
		this(context, null);
	}

	public TabIndicatorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TabIndicatorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
		init(context);
	}

	private void init(Context context) {
		LinearLayout layout = new LinearLayout(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, UiUtils.dip2px(50));
		layout.setPadding(0, 0, 0, UiUtils.dip2px(5));
		layout.setGravity(Gravity.CENTER_HORIZONTAL);
		layout.setLayoutParams(params);
		tabView = new ImageView(context);//创建标签图片
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);	
		tabView.setLayoutParams(params);
		layout.addView(tabView);//添加到layout
		redPoint = new View(context);
		params = new LayoutParams(UiUtils.dip2px(10), UiUtils.dip2px(10));
		params.topMargin = UiUtils.dip2px(10);
		redPoint.setLayoutParams(params);	
		redPoint.setBackground(getRedPoint());
		redPoint.setVisibility(INVISIBLE);
		layout.addView(redPoint);//添加到layout
		addView(layout);//添加到根布局
		selectedColor = 0xFFE43F15;
	}

	private Drawable getRedPoint() {
		GradientDrawable drawable = new GradientDrawable();
		drawable.setColor(Color.RED);
		drawable.setShape(GradientDrawable.OVAL);
		return drawable;
	}
	
	public void setSelected(boolean isSelected) {
		if (isSelected) {
		    tabView.setColorFilter(selectedColor);
		} else {
		    tabView.setColorFilter(Color.TRANSPARENT);
		}
	}
	
	/**
	 * 设置标签图片
	 * @param resId 图片资源id
	 */
	public void setTabResource(int resId) {
		tabView.setImageResource(resId);
	}

	/**
	 * 是否显示小红点
	 */
	public void setRedPointVisible(boolean visible) {
		redPoint.setVisibility(visible ? VISIBLE : INVISIBLE);
	}

	/**
	 * 设置选中时的颜色
	 */
	public void setSelectedColor(int color) {
		this.selectedColor = color;
	}
}
