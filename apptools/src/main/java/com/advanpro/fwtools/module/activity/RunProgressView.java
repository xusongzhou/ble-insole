package com.advanpro.fwtools.module.activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.RoundProgressBar;

/**
 * Created by zengfs on 2016/1/16.
 * 跑步进度条控件
 */
public class RunProgressView {
	private static final int[] resIds = {R.drawable.zero, R.drawable.one, R.drawable.two, R.drawable.three,
			R.drawable.four, R.drawable.five, R.drawable.six, R.drawable.seveen, R.drawable.eight, R.drawable.nine};
	private Context context;
	public View rootView;
	//超出范围提示
	private TextView tvNumber;
	//数字图片容器
	private LinearLayout imgContainer;
	private RoundProgressBar progressBar;
	private float targetDistance;

	public RunProgressView(ViewGroup container) {
		this.context = container.getContext();
		assignViews(container);
		initViews();
	}

	private void assignViews(ViewGroup container) {
		rootView = View.inflate(context, R.layout.view_run_progress, container);
		tvNumber = (TextView) rootView.findViewById(R.id.tv_num);
		imgContainer = (LinearLayout) rootView.findViewById(R.id.layout_number);
		progressBar = (RoundProgressBar) rootView.findViewById(R.id.progress_bar);
	}

	private void initViews() {
		initProgressBar();		
	}

	private void initProgressBar() {		
		progressBar.setDefaultRoundColor(UiUtils.getColor(R.color.inner_ring));
		progressBar.setDefaultRoundWidth(UiUtils.dip2px(2));
		progressBar.setProgressRoundWidth(UiUtils.dip2px(3));
		progressBar.setDotRadius(UiUtils.dip2px(5));
		progressBar.setDotEnabled(true);
		progressBar.setTextEnabled(false);
		progressBar.setColorStyle(RoundProgressBar.ColorStyle.GRADIENT);
	}

	/**
	 * 设置目标距离
	 * @param distance 距离，单位：公里
	 */
	public void setTargetDistance(float distance) {
		this.targetDistance = distance;
	}
	
	/**
	 * 设置显示的数值
	 * @param number 数值，单位：公里
	 */
	public void setNumber(float number) {
		progressBar.setProgress((int) ((number / targetDistance) * 100));
		//达到临界值，用正常的TextView显示数值
		if (number >= 1000) {
			tvNumber.setText(String.format("%.2f", number));
			tvNumber.setVisibility(View.VISIBLE);
			imgContainer.setVisibility(View.INVISIBLE);
			return;
		}

		tvNumber.setVisibility(View.INVISIBLE);
		imgContainer.setVisibility(View.VISIBLE);

		imgContainer.getChildAt(0).setVisibility(number >= 100 ? View.VISIBLE : View.GONE);
		imgContainer.getChildAt(1).setVisibility(number >= 10 ? View.VISIBLE : View.GONE);

		//先将数字放大转成整型，依次拆解出各数字
		int num = (int) (number * 100);
		imgContainer.getChildAt(0).setBackgroundResource(resIds[num / 10000]);//百位
		imgContainer.getChildAt(1).setBackgroundResource(resIds[num % 10000 / 1000]);//十位
		imgContainer.getChildAt(2).setBackgroundResource(resIds[num % 1000 / 100]);//个位
		imgContainer.getChildAt(4).setBackgroundResource(resIds[num % 100 / 10]);//小数
		imgContainer.getChildAt(5).setBackgroundResource(resIds[num % 10]);//小数
	}
}
