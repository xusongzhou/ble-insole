package com.advanpro.fwtools.module.activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunPlan;
import com.advanpro.ascloud.ASCloud;

import java.util.Date;
import java.util.List;

/**
 * Created by zengfs on 2016/2/26.
 * 计划View
 */
public class RunPlanView extends FrameLayout implements View.OnClickListener {
	private Button btnStart;
	private Button btnFinish;
	private LinearLayout llContainer;
	private int type;
	private PlanItemView[] planItemViews;
	private int dayIndexOfPlan;
	private boolean equalsSavedPlan;
	private char[] fulfillStates;
	private RunPlan currentPlan;
	private int subItemHeight = -1;
	private Dialog switchWarnDialog;
	private Dialog stopWarnDialog;
	private RunPlan savedPlan;

	public RunPlanView(Context context) {
		this(context, null);
	}

	public RunPlanView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RunPlanView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		View view = View.inflate(getContext(), R.layout.view_run_plan, this);
		btnStart = (Button) view.findViewById(R.id.btn_start);
		btnFinish = (Button) view.findViewById(R.id.btn_finish);
		llContainer = (LinearLayout) view.findViewById(R.id.ll_container);		
		btnStart.setOnClickListener(this);
		btnFinish.setOnClickListener(this);		
	}

	/**
	 * 设置数据源
	 */
	public void loadData(int type) {
		List<RunPlanParser.Item> items = RunPlanParser.getPlanData(type);
		if (items == null) return;
		this.type = type;
		currentPlan = new RunPlan();
		updateConditions();			
		btnFinish.setVisibility(equalsSavedPlan ? VISIBLE : INVISIBLE);
		btnStart.setEnabled(!equalsSavedPlan);
		int subIndex = 1;
		if (planItemViews == null) planItemViews = new PlanItemView[items.size()];
		for (int i = 0; i < planItemViews.length; i++) {
			planItemViews[i] = new PlanItemView(getContext());
			llContainer.addView(planItemViews[i], -1, -2);
			if (subItemHeight != -1) planItemViews[i].setSubItemHeight(subItemHeight);
			planItemViews[i].setData(this, type, i + 1, subIndex);
			subIndex += items.get(i).subItems.size();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		    case R.id.btn_start:
				if (currentPlan == null) return;				
				if (savedPlan != null && savedPlan.getType() != type) {
					if (switchWarnDialog == null) {
						switchWarnDialog = new Dialog(getContext(), R.style.DialogStyle);
						switchWarnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
						switchWarnDialog.setContentView(createSwitchWarnDialogView());
					}
				    switchWarnDialog.show();
				} else {
					startPlan();
				}				
				break;
			case R.id.btn_finish:	
				updateConditions();
				//已全部完成训练，点结束时直接结束，否则弹出提示对话框
				if (RunPlanParser.getDayIndexOfPlan() < RunPlanParser.getTotalDayCount(type)) {
					if (stopWarnDialog == null) {
						stopWarnDialog = new Dialog(getContext(), R.style.DialogStyle);
						stopWarnDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
						stopWarnDialog.setContentView(createStopWarnDialogView());
					}
					stopWarnDialog.show();
				} else {
					Dao.INSTANCE.deleteRunPlan();
					updateView();
				}				
				break;
		}
	}

	private void startPlan() {
		currentPlan.setStartDate(new Date());
		currentPlan.setSync(false);
		Dao.INSTANCE.insertOrUpdateRunPlan(currentPlan);
		updateView();
	}

	private View createSwitchWarnDialogView() {
		View view = View.inflate(getContext(), R.layout.dialog_msg, null);
		TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
		view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchWarnDialog.dismiss();
				startPlan();
			}
		});
		view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchWarnDialog.dismiss();
			}
		});
		String text = getResources().getString(R.string.switch_run_plan_warning);
		String src = RunPlanParser.getPlanString(savedPlan.getType());		
		String target = RunPlanParser.getPlanString(type);
		if (src != null && target != null) {
			text = text.replaceFirst("\\?", src).replaceFirst("\\?", src).replace("?", target);
			tvContent.setText(text);
		}		
		return view;
	}

	private View createStopWarnDialogView() {
		View view = View.inflate(getContext(), R.layout.dialog_msg, null);
		TextView tvContent = (TextView) view.findViewById(R.id.tv_content);
		view.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopWarnDialog.dismiss();
				Dao.INSTANCE.deleteRunPlan();
				updateView();
			}
		});
		view.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopWarnDialog.dismiss();
			}
		});
		String text = getResources().getString(R.string.stop_run_plan_warning);
		String src = RunPlanParser.getPlanString(type);
		if (src != null) {
			text = text.replace("?", src);
			tvContent.setText(text);
		}
		return view;
	}
	
	public void setSubItemHeight(int height) {
		subItemHeight = height;
	}
	
	public void updateView() {
		updateConditions();
		btnFinish.setVisibility(equalsSavedPlan ? VISIBLE : INVISIBLE);
		btnStart.setEnabled(!equalsSavedPlan);
		for (PlanItemView planItemView : planItemViews) {
			planItemView.updateView();
		}
	}

	public int getDayIndexOfPlan() {
		return dayIndexOfPlan;
	}

	public boolean isEqualsSavedPlan() {
		return equalsSavedPlan;
	}

	/**
	 * 保存训练状态
	 */
	public void saveFulfillState(int dayIndexOfPlan, boolean value) {
		if(currentPlan == null || dayIndexOfPlan < 1 || dayIndexOfPlan > fulfillStates.length) return;
		fulfillStates[dayIndexOfPlan - 1] = value ? '1' : '0';
		currentPlan.setFulfillState(new String(fulfillStates));
		currentPlan.setSync(false);
		Dao.INSTANCE.insertOrUpdateRunPlan(currentPlan);
	}

	/**
	 * 查找这天是否训练了
	 */
	public boolean isFulfill(int dayIndexOfPlan) {
		return fulfillStates != null && dayIndexOfPlan >= 1 && dayIndexOfPlan <= fulfillStates.length && 
				fulfillStates[dayIndexOfPlan - 1] == '1';
	}
	
	//获取决定界面状态的条件
	private void updateConditions() {
		//进行到计划的第几天
		dayIndexOfPlan = RunPlanParser.getDayIndexOfPlan();
		savedPlan = Dao.INSTANCE.queryRunPlan();
		//当前计划是否是正在进行的计划
		equalsSavedPlan = savedPlan != null && savedPlan.getType() == type;	
		if (currentPlan != null) {
			if (equalsSavedPlan) {
				currentPlan = savedPlan;
			} else {
				currentPlan.setType(type);
				currentPlan.setUserId(ASCloud.userInfo.ID);
				currentPlan.setFulfillState(RunPlanParser.createInitFulfillState(type));
			}
			fulfillStates = currentPlan.getFulfillState().toCharArray();
		}
	}
}
