package com.advanpro.fwtools.module.activity;

import android.os.Bundle;
import android.view.View;
import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.module.BaseActivity;

/**
 * Created by zengfs on 2016/2/24.
 * 日常健身计划和减肥锻炼计划集成
 */
public class DailyAndLosePlanActivity extends BaseActivity {
	private TitleBar titleBar;
	private RunPlanView runPlanView;
	private int planType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daily_and_lose_plan);
		planType = getIntent().getIntExtra(Constant.EXTRA_RUN_PLAN_TYPE, Constant.PLAN_DAILY_FITNESS);
        assignViews();
        initViews();
	}

	protected void assignViews() {
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		runPlanView = (RunPlanView) findViewById(R.id.run_plan_view);
	}

	protected void initViews() {
		initTitleBar();
		if (planType == Constant.PLAN_LOSE_WEIGHT_EXERCISE) runPlanView.setSubItemHeight(UiUtils.dip2px(140));
		runPlanView.loadData(planType);
	}

	private void initTitleBar() {
		titleBar.setStartImageButtonVisible(true);
		titleBar.setTitle(RunPlanParser.getPlanString(planType));
		titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
			@Override
			public void onMenuClick(View v) {
				if (v.getId() == R.id.btn_start) {
					finish();
				}
			}
		});
	}
}
