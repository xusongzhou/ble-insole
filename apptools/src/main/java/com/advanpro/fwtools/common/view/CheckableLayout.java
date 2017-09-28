package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * Created by zengfs on 2016/2/24.
 * 可选中布局
 */
public class CheckableLayout extends FrameLayout implements Checkable, View.OnClickListener {
	private boolean checked;
	private OnCheckedChangeListener listener;

	public interface OnCheckedChangeListener {
		void onCheckedChanged(CheckableLayout layout, boolean isChecked);
	}
	
	public CheckableLayout(Context context) {
		this(context, null);
	}

	public CheckableLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CheckableLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOnClickListener(this);
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {		
		if (listener != null) {
			this.listener = listener;
			setClickable(true);
		}
	}

	@Override
	public void onClick(View v) {
		checked = !checked;
		if (listener != null) listener.onCheckedChanged(this, checked);
	}	
	
	@Override
	public void setChecked(boolean checked) {
		if (this.checked != checked) {
		    this.checked = checked;
			if (listener != null) listener.onCheckedChanged(this, this.checked);
		}
	}

	@Override
	public boolean isChecked() {
		return checked;
	}

	@Override
	public void toggle() {
		setChecked(!checked);
	}
}
