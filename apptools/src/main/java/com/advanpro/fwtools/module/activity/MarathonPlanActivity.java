package com.advanpro.fwtools.module.activity;

import android.os.Bundle;
import android.view.View;
import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.view.TextTabContentView;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.common.view.ViewPagerIndicator;
import com.advanpro.fwtools.module.BaseActivity;

/**
 * Created by zengfs on 2016/2/24.
 * 马拉松训练计划
 */
public class MarathonPlanActivity extends BaseActivity implements ViewPagerIndicator.OnTabCheckListener {
	private TitleBar titleBar;
	private ViewPagerIndicator viewPagerIndicator;
	private RunPlanView[] runPlanViews = new RunPlanView[3];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_marathon_plan);
		assignViews();
		initViews();
	}

	protected void assignViews() {
		titleBar = (TitleBar) findViewById(R.id.title_bar);
		viewPagerIndicator = (ViewPagerIndicator) findViewById(R.id.view_pager_indicator);
		runPlanViews[0] = (RunPlanView) findViewById(R.id.first);
		runPlanViews[1] = (RunPlanView) findViewById(R.id.second);
		runPlanViews[2] = (RunPlanView) findViewById(R.id.third);
	}

	protected void initViews() {
		initTitleBar();
		viewPagerIndicator.setTabContentViews(createTabViews());
		viewPagerIndicator.setOnTabCheckListener(this);
		runPlanViews[0].loadData(Constant.PLAN_MARATHON_TRAINING_5KM);
		runPlanViews[1].loadData(Constant.PLAN_MARATHON_TRAINING_10KM);
		runPlanViews[2].loadData(Constant.PLAN_MARATHON_TRAINING_FULL);
	}

	private View[] createTabViews() {
		String[] titles = {"5KM", "10KM", getString(R.string.full_marathon)};
		TextTabContentView[] tabs = new TextTabContentView[titles.length];
		for (int i = 0; i < titles.length; i++) {
			tabs[i] = new TextTabContentView(this);
			tabs[i].setText(titles[i]);
		}		
		return tabs;
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
	public void onTabCheck(View contentView, int position) {
		for (int i = 0; i < runPlanViews.length; i++) {
			if (i == position) {
				runPlanViews[i].updateView();
				runPlanViews[i].setVisibility(View.VISIBLE);
			} else {
				runPlanViews[i].setVisibility(View.INVISIBLE);
			}
		}
	}
}
