package com.advanpro.fwtools.common.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zengfs on 2016/1/23.
 * 动画工具类
 */
public class AnimatorUtils {	
	
	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;

	/**
	 * 给View添加线性动画，存在多种布局时效果不好
	 * @param view 要添加动画的View
	 * @param listener 动画状态改变监听器，如start、end、cancel、repeat
	 * @param orientation 方向。
	 * @param duration 动画时长
	 * @param values 值动画的值
	 */
	public static void addIntAnimatorToView(final View view, Animator.AnimatorListener listener, 
											final int orientation, long duration, int... values) {
		ValueAnimator animator = ValueAnimator.ofInt(values);
		final ViewGroup.LayoutParams params = view.getLayoutParams();
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int value = (int) animation.getAnimatedValue();
				if (orientation == VERTICAL) params.height = value;
				else if (orientation == HORIZONTAL) params.width = value;
				else params.height = value;
				view.setLayoutParams(params);
			}
		});
		if (listener != null) animator.addListener(listener);
		animator.setDuration(duration).start();
	}



}
