package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.UiUtils;

/**
 * Created by zengfs on 2016/1/20.
 * ViewPagerIndicator的Tab内容，TextView格式
 */
public class TextTabContentView extends TextView implements ViewPagerIndicator.OnCheckedChangeListener {
	
	private int normalTextColor;
	private int checkedTextColor;
	
	public TextTabContentView(Context context) {
		this(context, null);
	}

	public TextTabContentView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextTabContentView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		normalTextColor = getResources().getColor(R.color.tab_view_normal_text);
		checkedTextColor = getResources().getColor(R.color.tab_view_checked_text);
		setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		setPadding(0, UiUtils.dip2px(10), 0, UiUtils.dip2px(10));
		setTextColor(normalTextColor);
		setGravity(Gravity.CENTER_HORIZONTAL);
	}

	@Override
	public void onCheckedChanged(boolean isChecked) {
		setTextColor(isChecked ? checkedTextColor : normalTextColor);
	}

	public void setNormalTextColor(int color) {
		normalTextColor = color;
	}

	public void setCheckedTextColor(int color) {
		checkedTextColor = color;
	}
}
