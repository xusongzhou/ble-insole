package com.advanpro.fwtools.common.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.advanpro.fwtools.R;

/**
 * Created by zengfs on 2016/1/24.
 * 两个布局的切换，一个显示，一个是隐藏状态
 */
public class SwitchLayout extends ViewGroup {
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int TOP = 2;
	public static final int BOTTOM = 3;
	private View defaultView;
	private View secondView;
	private int orientation;//从哪个方向滑进屏幕
	private int duration;
	private int start;
	private int end;
	private SwitchListener listener;
	private boolean isInitialized;
	private boolean isDefaultViewShowing = true;

	public interface SwitchListener {
		/**
		 * 切换开始回调
		 * @param switchLayout 切换发生在的SwitchLayout
		 * @param isDefaultViewShowing 正在显示的页面是否是默认的
		 */
		void onSwitchStart(SwitchLayout switchLayout, boolean isDefaultViewShowing);
		/**
		 * 切换结束回调
		 * @param switchLayout 切换发生在的SwitchLayout
		 * @param isDefaultViewShowing 正在显示的页面是否是默认的
		 */
		void onSwitchEnd(SwitchLayout switchLayout, boolean isDefaultViewShowing);
	}
	
	public SwitchLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchLayout);
		orientation = typedArray.getInt(R.styleable.SwitchLayout_orientation, BOTTOM);
		duration = typedArray.getInt(R.styleable.SwitchLayout_orientation, 300);
		typedArray.recycle();
	}

	private void init() {
		switch(orientation) {
			case LEFT:
				end = 0;
				start = -secondView.getMeasuredWidth();
				break;
			case RIGHT:
				end = 0;
				start = secondView.getMeasuredWidth();
				break;
			case TOP:
				end = 0;
				start = -secondView.getMeasuredHeight();
				break;
			case BOTTOM:
				end = 0;
				start = secondView.getMeasuredHeight();
				break;
		}
	}

	public void setSwitchListener(SwitchListener listener) {
		this.listener = listener;
	}

	/**
	 * 设置隐藏的布局从哪个方向滑进屏幕
	 */
	public void setSlideOrientation(int orientation) {
		if (orientation > 3 || orientation < 0) {
		    throw new IllegalArgumentException("参数不合法");
		}
		this.orientation = orientation;
	}
	
	/**
	 * 切换布局
	 */
	public void switchView() {
		isDefaultViewShowing = !isDefaultViewShowing;
		if (!isInitialized) {
			isInitialized = true;
			init();
		}
		int tmp = start;
		start = end;
		end = tmp;
		ValueAnimator animator = ValueAnimator.ofInt(start, end);
		animator.addUpdateListener(animatorUpdateListener);
		animator.addListener(animatorListener);
		animator.setDuration(duration).start();
	}
	
	private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			if (listener != null) listener.onSwitchStart(SwitchLayout.this, isDefaultViewShowing);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (listener != null) listener.onSwitchEnd(SwitchLayout.this, isDefaultViewShowing);
		}

		@Override
		public void onAnimationCancel(Animator animation) {
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}
	};
	
	private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator animation) {
			int value = (int) animation.getAnimatedValue();
			switch(orientation) {
				case LEFT:
				case RIGHT:
					scrollTo(value, 0);
					break;
				case TOP:
				case BOTTOM:
					scrollTo(0, value);
					break;
			}
		}
	};

	/**
	 * 切换布局时的动画的时长
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * 此方法是SwitchLayout控件测量宽和高时回调.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//测量主界面的长宽		
		defaultView.measure(widthMeasureSpec, heightMeasureSpec);
		//测量第二界面的长宽		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
		int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
		int childWidth = secondView.getLayoutParams().width;
		int childHeight = secondView.getLayoutParams().height;

		secondView.measure(getMeasureSpec(widthMode, width, childWidth),
				getMeasureSpec(heightMode, height, childHeight));		
	}

	/*
	 * 根据不同情况获取不同的孩子测量标准
	 */
	private int getMeasureSpec(int parentMode, int parentSize, int childSize) {
		int childMeasureSpec = 0;
		if (childSize >= 0) {
			childMeasureSpec = MeasureSpec.makeMeasureSpec(childSize, MeasureSpec.EXACTLY);
		} else if (childSize == LayoutParams.MATCH_PARENT) {
			childMeasureSpec = MeasureSpec.makeMeasureSpec(parentSize, parentMode);
		} else if (childSize == LayoutParams.WRAP_CONTENT) {
			int childWidthMode = parentMode == MeasureSpec.EXACTLY ? MeasureSpec.AT_MOST : parentMode;
			childMeasureSpec = MeasureSpec.makeMeasureSpec(parentSize, childWidthMode);
		}
		return childMeasureSpec;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		defaultView.layout(l, t, r, b);
		//根据进入的方向放置子控件的位置
		switch(orientation) {
			case LEFT:
				secondView.layout(-secondView.getMeasuredWidth(), 0, 0, b);
				break;
			case RIGHT:
				secondView.layout(secondView.getMeasuredWidth(), 0, 2 * secondView.getMeasuredWidth(), b);
				break;
			case TOP:
				secondView.layout(0, -secondView.getMeasuredHeight(), r, 0);
				break;
			case BOTTOM:
				secondView.layout(0, secondView.getMeasuredHeight(), r, 2 * secondView.getMeasuredHeight());
				break;
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if(getChildCount() < 2){
			throw new IllegalStateException("布局至少有俩孩子");
		}
		defaultView = getChildAt(0);
		secondView = getChildAt(1);
	}
}
