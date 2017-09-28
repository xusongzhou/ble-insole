package com.advanpro.fwtools.module.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.advanpro.fwtools.R;

/**
 * Created by zengfs on 2016/3/1.
 * 跑步计划字item
 */
public class PlanSubItemView extends FrameLayout {
	private View divider;
	private ImageView ivChk;
	private TextView tvTitle;
	private TextView tvContent;
	private View froeground;
	private boolean isChecked;
	private boolean checkable;
	private boolean isCurrent;
	private int indexOfPlan;
	private RunPlanView runPlanView;

	public PlanSubItemView(Context context) {
		this(context, null);
	}

	public PlanSubItemView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlanSubItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		View rootView = View.inflate(getContext(), R.layout.layout_plan_subitem, this);
		divider = rootView.findViewById(R.id.divider);
		ivChk = (ImageView) rootView.findViewById(R.id.iv_chk);
		tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
		tvContent = (TextView) rootView.findViewById(R.id.tv_content);
		froeground = rootView.findViewById(R.id.froeground);
		ivChk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setChecked(!isChecked, false);
			}
		});
	}

	public void setChecked(boolean checked, boolean isInit) {
		if (checkable) {
			isChecked = checked;
			int checkedResId = isCurrent ? R.drawable.check_bright : R.drawable.check_dark;
			ivChk.setImageResource(checked ? checkedResId : R.drawable.uncheck);
			//初始化界面时不进行选中状态保存
			if (runPlanView != null && !isInit) {
			    runPlanView.saveFulfillState(indexOfPlan, isChecked);
			}
		}
	}

	public boolean isCheckable() {
		return checkable;
	}

	public void setRunPlanView(RunPlanView runPlanView) {
		this.runPlanView = runPlanView;
	}

	public void setIndexOfPlan(int indexOfPlan) {
		this.indexOfPlan = indexOfPlan;
	}

	public void setCurrent(boolean current) {
		isCurrent = current;
	}

	public void setCheckable(boolean checkable) {
		this.checkable = checkable;
	}

	public void setTitle(String title) {
		tvTitle.setText(title);
	}

	public void setContent(String content) {
		tvContent.setText(content);
	}

	public void setDividerVisible(boolean enable) {
		divider.setVisibility(enable ? VISIBLE : INVISIBLE);
	}

	public void setFroegroundVisible(boolean enable) {
		froeground.setVisibility(enable ? VISIBLE : INVISIBLE);
	}
}
