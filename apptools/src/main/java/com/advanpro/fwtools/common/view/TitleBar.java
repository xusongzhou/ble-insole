package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import com.advanpro.fwtools.R;

/**
 * Created by zengfs on 2016/1/21.
 * 标题栏
 */
public class TitleBar extends FrameLayout implements View.OnClickListener {

	private ImageButton btnStart;
	private TextView tvStart;
	private TextView tvTitle;
	private ImageButton btnEnd;
	private TextView tvEnd;
	private OnMenuClickListener listener;

	public interface OnMenuClickListener {
		void onMenuClick(View v);
	}
	
	public TitleBar(Context context) {
		this(context, null);
	}

	public TitleBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TitleBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
		boolean isStartTvVisible = typedArray.getBoolean(R.styleable.TitleBar_startTextViewVisible, false);
		boolean isStartImgBtnVisible = typedArray.getBoolean(R.styleable.TitleBar_startImageButtonVisible, false);
		boolean isEndTvVisible = typedArray.getBoolean(R.styleable.TitleBar_endTextViewVisible, false);
		boolean isEndImgBtnVisible = typedArray.getBoolean(R.styleable.TitleBar_endImageButtonVisible, false);
		String startText = typedArray.getString(R.styleable.TitleBar_startText);
		String endText = typedArray.getString(R.styleable.TitleBar_endText);
		String title = typedArray.getString(R.styleable.TitleBar_title);
		Drawable startImgBtnDrawable = typedArray.getDrawable(R.styleable.TitleBar_startImageButtonSrc);
		Drawable endImgBtnDrawable = typedArray.getDrawable(R.styleable.TitleBar_endImageButtonSrc);
		typedArray.recycle();
		View view = View.inflate(getContext(), R.layout.view_title_bar, this);
		btnStart = (ImageButton) view.findViewById(R.id.btn_start);
		tvStart = (TextView) view.findViewById(R.id.tv_start);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		btnEnd = (ImageButton) view.findViewById(R.id.btn_end);
		tvEnd = (TextView) view.findViewById(R.id.tv_end);
		btnStart.setVisibility(isStartImgBtnVisible ? VISIBLE : INVISIBLE);
		btnEnd.setVisibility(isEndImgBtnVisible ? VISIBLE : INVISIBLE);
		tvStart.setVisibility(isStartTvVisible ? VISIBLE : INVISIBLE);
		tvEnd.setVisibility(isEndTvVisible ? VISIBLE : INVISIBLE);
		tvTitle.setText(title);
		tvStart.setText(startText);
		tvEnd.setText(endText);
		if (startImgBtnDrawable != null) btnStart.setImageDrawable(startImgBtnDrawable);
		btnEnd.setImageDrawable(endImgBtnDrawable);
	}

	@Override
	public void onClick(View v) {
		listener.onMenuClick(v);
	}

	public void setOnMenuClickListener(OnMenuClickListener listener) {
		if (listener != null) {
			this.listener = listener;
			tvStart.setOnClickListener(this);
			tvEnd.setOnClickListener(this);
			btnStart.setOnClickListener(this);
			btnEnd.setOnClickListener(this);
		}		
	}
    
	public void setStartTextViewVisible(boolean enable) {
		tvStart.setVisibility(enable ? VISIBLE : INVISIBLE);
	}

	public void setEndTextViewVisible(boolean enable) {
		tvEnd.setVisibility(enable ? VISIBLE : INVISIBLE);
	}

	public void setStartImageButtonVisible(boolean enable) {
		btnStart.setVisibility(enable ? VISIBLE : INVISIBLE);
	}

	public void setEndImageButtonVisible(boolean enable) {
		btnEnd.setVisibility(enable ? VISIBLE : INVISIBLE);
	}

	public void setStartTextViewEnabled(boolean enable) {
		tvStart.setEnabled(enable);
	}

	public void setEndTextViewEnabled(boolean enable) {
		tvEnd.setEnabled(enable);
	}

	public void setStartImageButtonEnabled(boolean enable) {
		btnStart.setEnabled(enable);
	}

	public void setEndImageButtonEnabled(boolean enable) {
		btnEnd.setEnabled(enable);
	}
	
	public void setStartText(String text) {
		tvStart.setText(text);
	}

	public void setStartText(int resId) {
		tvStart.setText(resId);
	}
	
	public void setEndText(String text) {
		tvEnd.setText(text);
	}

	public void setEndText(int resId) {
		tvEnd.setText(resId);
	}
	
	public void setTitle(String text) {
		tvTitle.setText(text);
	}


	public void setTitle(int resId) {
		tvTitle.setText(resId);
	}
	
	public void setStartImageButtonSrc(Drawable drawable) {
		btnStart.setImageDrawable(drawable);
	}

	public void setEndImageButtonSrc(Drawable drawable) {
		btnEnd.setImageDrawable(drawable);
	}

	public void setStartImageButtonSrc(int resId) {
		btnStart.setImageResource(resId);
	}

	public void setEndImageButtonSrc(int resId) {
		btnEnd.setImageResource(resId);
	}
	
	public void setTitleTypeface (Typeface tf) {
		tvTitle.setTypeface(tf);
	}

	public void setStartTextTypeface (Typeface tf) {
		tvStart.setTypeface(tf);
	}

	public void setEndTextTypeface (Typeface tf) {
		tvEnd.setTypeface(tf);
	}

	public void setTitleTextSize (int unit, float size) {
		tvTitle.setTextSize(unit, size);
	}

	public void setStartTextSize (int unit, float size) {
		tvStart.setTextSize(unit, size);
	}

	public void setEndTextSize (int unit, float size) {
		tvEnd.setTextSize(unit, size);
	}
}
