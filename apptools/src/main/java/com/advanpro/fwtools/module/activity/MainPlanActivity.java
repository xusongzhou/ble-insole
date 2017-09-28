package com.advanpro.fwtools.module.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.ArrowItemView;
import com.advanpro.fwtools.common.view.RoundProgressBar;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunPlan;
import com.advanpro.fwtools.module.BaseActivity;

import java.util.Date;

/**
 * Created by zengfs on 2016/2/24.
 * 跑步计步主界面
 */
public class MainPlanActivity extends BaseActivity implements View.OnClickListener {
	private ArrowItemView[] items;
	private TitleBar titleBar;
	private TextView tvProcess;
	private TextView tvPercent;
	private RoundProgressBar progressBar;
	private TextView tvWarn;
	private RelativeLayout rlPlan;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_plan);
		assignViews();
		initViews();		
	}

	protected void assignViews() {
		items = new ArrowItemView[3];
		items[0] = (ArrowItemView) findViewById(R.id.item_daily_plan);
		items[1] = (ArrowItemView) findViewById(R.id.item_lose_weight_plan);
		items[2] = (ArrowItemView) findViewById(R.id.item_marathon_plan);
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		tvProcess = (TextView) findViewById(R.id.tv_process);
		progressBar = (RoundProgressBar) findViewById(R.id.progress_bar);
		tvWarn = (TextView) findViewById(R.id.tv_warn);
		tvPercent = (TextView) findViewById(R.id.tv_percent);
		rlPlan = (RelativeLayout) findViewById(R.id.rl_plan);
	}

	protected void initViews() {
		initTitleBar();
		for (ArrowItemView item : items) {
			item.setOnClickListener(this);
		}
		initProgressBar();
	}

	private void initProgressBar() {
		progressBar.setTextEnabled(false);
		progressBar.setStrokeCap(Paint.Cap.ROUND);
		progressBar.setAngle(90);
		progressBar.setDefaultRoundWidth(UiUtils.dip2px(18));
		progressBar.setDefaultRoundColor(UiUtils.getColor(R.color.run_plan_progress_bg));
		progressBar.setProgressRoundWidth(UiUtils.dip2px(18));
		progressBar.setProgressRoundColor(UiUtils.getColor(R.color.run_plan_progress));		
	}
	
	private void initTitleBar() {
		titleBar.setStartImageButtonVisible(true);
		titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
			@Override
			public void onMenuClick(View v) {
				if (v.getId() == R.id.btn_start) {
					finish();
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateView();
	}

	private void updateView() {
		RunPlan plan = Dao.INSTANCE.queryRunPlan();		
		for (int i = 0; i < items.length; i++) {
			if (i < 2) {
				items[i].setTitleColor(plan != null && plan.getType() == i + 1 ? UiUtils.getColor(R.color.content_text) :
						UiUtils.getColor(R.color.content_text) & 0x99FFFFFF);
				items[i].setArrowColor(plan != null && plan.getType() == i + 1 ? UiUtils.getColor(R.color.content_text) : Color.GRAY);
			} else {
				items[i].setTitleColor(plan != null && plan.getType() >= Constant.PLAN_MARATHON_TRAINING_5KM ? 
						UiUtils.getColor(R.color.content_text) : UiUtils.getColor(R.color.content_text) & 0x99FFFFFF);
				items[i].setArrowColor(plan != null && plan.getType() >= Constant.PLAN_MARATHON_TRAINING_5KM ? 
						UiUtils.getColor(R.color.content_text) : Color.GRAY);
			}			
		}	
		if (plan != null) {
		    rlPlan.setVisibility(View.VISIBLE);
			int todayIndex = DateUtils.daysBetween(plan.getStartDate(), new Date()) + 1;
			float progress = RunPlanParser.getTrainedDays(plan.getFulfillState()) * 1f / 
					RunPlanParser.getTotalDayCount(plan.getType()) * 100;
			progressBar.setProgress((int) progress);
			tvPercent.setText((progress == (int)progress ? (int)progress : String.format("%.1f", progress)) + "%");
			int week = todayIndex / 7 + (todayIndex % 7 == 0 ? 0 : 1);			
			String whichWeekAndDay = getString(R.string.which_week_and_day).replaceFirst("\\?", week + "").replace("?", todayIndex % 7 + "");
			tvProcess.setText(RunPlanParser.getPlanString(plan.getType()) + whichWeekAndDay);
			tvWarn.setText(getString(R.string.run_plan_warn).replace("?", RunPlanParser.getTotalDayCount(plan.getType()) - todayIndex + ""));
		} else {
		    rlPlan.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		    case R.id.item_daily_plan:	
			case R.id.item_lose_weight_plan:
				Intent intent = new Intent(this, DailyAndLosePlanActivity.class);
				intent.putExtra(Constant.EXTRA_RUN_PLAN_TYPE, v.getId() == R.id.item_daily_plan ? 
						Constant.PLAN_DAILY_FITNESS : Constant.PLAN_LOSE_WEIGHT_EXERCISE);
				startActivity(intent);
				break;
			case R.id.item_marathon_plan:
				startActivity(new Intent(this, MarathonPlanActivity.class));
				break;
		}
	}
}
