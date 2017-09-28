package com.advanpro.fwtools.module.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.manager.MediaPlayerMgr;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.MathUtils;
import com.advanpro.fwtools.common.util.StringUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.db.Activity;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunPlan;
import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.module.more.DeviceManagerActivity;

import java.util.Date;

/**
 * Created by zengfs on 2016/1/23.
 * 今日活动页面
 */
public class TodayActivityPager extends BasePager implements View.OnClickListener {
    private static final int STEPS = 0;
    private static final int DISTANCE = 1;
    private static final int CALORIE = 2;
    public ActivityFragment fragment; 
	private TextView tvWalkSteps;
	private TextView tvWalkDuration;
	private TextView tvWalkDistance;
	private TextView tvWalkCal;
	private TextView tvRunDistance;
	private TextView tvRunSteps;
	private TextView tvRunDuration;
	private TextView tvRunPace;
	private TextView tvRunCal;
	public ImageButton btnStartRun;
	private TextView tvTotal;
    private TextView tvUnit;
	private FrameLayout layoutTips;
	private TextView tvWarn;
	private TextView tvPlan;
	private TextView tvPlanProcess;
	private ImageView ivClose;	
	private View bindWarnView;
    private TextView connWarn;
    private int currentTotalType;
    private int steps;
    private double calorie;
    private double distance;
    private SwitchRunable switchRunable;

	public TodayActivityPager(Context context, ActivityFragment fragment) {
		super(context);
		this.fragment = fragment;
        switchRunable = new SwitchRunable();
        switchTotalValue();
	}

	@Override
	protected View getRootView() {
		return View.inflate(context, R.layout.pager_today_activity, null);
	}

	@Override
	protected void assignViews() {			
		tvPlan = (TextView) rootView.findViewById(R.id.tv_plan);
		tvPlanProcess = (TextView) rootView.findViewById(R.id.tv_plan_process);
		tvWalkSteps = (TextView) rootView.findViewById(R.id.tv_walk_steps);
		tvWalkDuration = (TextView) rootView.findViewById(R.id.tv_walk_duration);
		tvWalkDistance = (TextView) rootView.findViewById(R.id.tv_walk_distance);
		tvWalkCal = (TextView) rootView.findViewById(R.id.tv_walk_cal);
		tvRunDistance = (TextView) rootView.findViewById(R.id.tv_run_distance);
		tvRunSteps = (TextView) rootView.findViewById(R.id.tv_run_steps);
		tvRunDuration = (TextView) rootView.findViewById(R.id.tv_run_duration);
		tvRunPace = (TextView) rootView.findViewById(R.id.tv_run_pace);
		tvRunCal = (TextView) rootView.findViewById(R.id.tv_run_cal);
		btnStartRun = (ImageButton) rootView.findViewById(R.id.btn_start_run);
		tvTotal = (TextView) rootView.findViewById(R.id.tv_total);
        tvUnit = (TextView) rootView.findViewById(R.id.tv_unit);
		layoutTips = (FrameLayout) rootView.findViewById(R.id.fl_tips);
		tvWarn = (TextView) rootView.findViewById(R.id.tv_warn);
		ivClose = (ImageView) rootView.findViewById(R.id.iv_close);			
		bindWarnView = rootView.findViewById(R.id.rl_warning);
        connWarn = (TextView) rootView.findViewById(R.id.tv_conn_warn);
	}

	@Override
	protected void initViews() {
		Drawable drawable = context.getResources().getDrawable(R.drawable.plan);
		//设置TextView的Drawable大小
		if (drawable != null) {
			drawable.setBounds(0, 0, UiUtils.dip2px(17), UiUtils.dip2px(24));
			tvPlan.setCompoundDrawables(drawable, null, null, null);
		}
		
		tvPlan.setOnClickListener(this);	
		ivClose.setOnClickListener(this);
		btnStartRun.setOnClickListener(this);		
		bindWarnView.setOnClickListener(this);
        rootView.findViewById(R.id.layout_total).setOnClickListener(this);
		
		updateView();
	}

	public void updateView() {
		com.advanpro.fwtools.db.Activity activity = Dao.INSTANCE.queryActivity(DateUtils.getStartOfDay(new Date()).getTime());
		if (activity == null) activity = new Activity();
		tvWalkSteps.setText(String.valueOf(activity.walkSteps));
		tvWalkDuration.setText(String.format("%02d:%02d:%02d", activity.walkTime / 3600,
				activity.walkTime % 3600 / 60, activity.walkTime % 60));
		tvWalkCal.setText((activity.walkCalorie > 0 ? StringUtils.formatDecimal(activity.walkCalorie, 1) : 0) +
				context.getString(R.string.kcal));
		tvWalkDistance.setText((activity.walkDistance > 0 ? StringUtils.formatDecimal(activity.walkDistance, 2) : 0) +
				context.getString(R.string.km));
		tvRunSteps.setText(activity.runningSteps + "");
		tvRunDuration.setText(String.format("%02d:%02d:%02d", activity.runningTime / 3600,
				activity.runningTime % 3600 / 60, activity.runningTime % 60));
		tvRunCal.setText((activity.runningCalorie > 0 ? StringUtils.formatDecimal(activity.runningCalorie, 1) : 0) +
				context.getString(R.string.kcal));
		tvRunDistance.setText(activity.runningDistance > 0 ? StringUtils.formatDecimal(activity.runningDistance, 2) : "0");
		tvRunPace.setText((activity.runRate == 0 ? 0 : String.format("%.1f", activity.runRate)) +
				context.getString(R.string.min_per_km));
		distance = MathUtils.setDoubleAccuracy(activity.runningDistance, 2) + MathUtils.setDoubleAccuracy(activity.walkDistance, 2);
        steps = activity.runningSteps + activity.walkSteps;
        calorie = MathUtils.setDoubleAccuracy(activity.runningCalorie, 1) + MathUtils.setDoubleAccuracy(activity.walkCalorie, 1);
        updateTotalValue();
	}
	
	private void updatePlanProcessText() {
		RunPlan plan = Dao.INSTANCE.queryRunPlan();
		if (plan == null) {
		    tvPlanProcess.setText(R.string.run_plan_not_start);
		} else {
			int todayIndex = DateUtils.daysBetween(plan.startDate, new Date()) + 1;
			if (todayIndex > RunPlanParser.getTotalDayCount(plan.type)) {
				Dao.INSTANCE.deleteRunPlan();
				tvPlanProcess.setText(R.string.run_plan_not_start);
			} else {
				int week = todayIndex / 7 + (todayIndex % 7 == 0 ? 0 : 1);
				String whichWeekAndDay = context.getString(R.string.which_week_and_day).replaceFirst("\\?", week + "")
                        .replace("?", todayIndex % 7 == 0 ? "7" : todayIndex % 7 + "");
				tvPlanProcess.setText(RunPlanParser.getPlanString(plan.type) + whichWeekAndDay);
				if (todayIndex == RunPlanParser.getTotalDayCount(plan.type)) {
					ToastMgr.showTextToast(context, Toast.LENGTH_LONG, context.getString(R.string.plan_end_warn));
				}				
			}			
		}
	}

	public void onResume() {
		updatePlanProcessText();
		bindWarnView.setVisibility(DeviceMgr.getBoundDeviceCount() == 0 ? View.VISIBLE : View.INVISIBLE);
	}
	
    public void updateConnWarnView() {
        boolean leftDisconn = false;
        boolean rightDisconn = false;
        for (BleDevice dev : DeviceMgr.getBoundDevices()) {
            State.ConnectionState state = State.getConnectionState(dev.mac);
            if (state != null && state != State.ConnectionState.CONNECTED) {
                if (dev.isLeft) leftDisconn = true;
                else rightDisconn = true;
            }
        }
        if (!leftDisconn && !rightDisconn) {
            connWarn.setVisibility(View.INVISIBLE);
        } else {
            connWarn.setVisibility(View.VISIBLE);
            if (leftDisconn && rightDisconn) {
                connWarn.setText(R.string.device_disconnected);
            } else if (leftDisconn) {
                connWarn.setText(context.getString(R.string.left) + context.getString(R.string.device_disconnected));
            } else {
                connWarn.setText(context.getString(R.string.right) + context.getString(R.string.device_disconnected));
            }
        }
    }
    
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btn_start_run:
				if (State.runState == null || State.runState == State.RunState.STOP) {
                    //如果没有全部连接，提示
					if (DeviceMgr.isAllConnected()) {
                        startRun();
					} else {
						new AlertDialog.Builder(context).setMessage(R.string.disconn_run_warn)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startRun();
                                    }
                                }).show();
					} 
				} else {
					fragment.switchLayout.switchView();
				}								
				break;
            case R.id.layout_total:
                switchTotalValue();
                break;
			case R.id.iv_close:
				layoutTips.setVisibility(View.GONE);
				break;
			case R.id.rl_warning:
				context.startActivity(new Intent(context, DeviceManagerActivity.class));				
				break;
			case R.id.tv_plan:
				context.startActivity(new Intent(context, MainPlanActivity.class));
				break;
		}
	}

    private void switchTotalValue() {
        UiUtils.cancel(switchRunable);
        if (++currentTotalType > 2) {
            currentTotalType = 0;
        }
        updateTotalValue();
        UiUtils.postDelayed(switchRunable, 3000);
    }

    private void updateTotalValue() {
        switch(currentTotalType) {
            case STEPS:
                tvTotal.setText(String.valueOf(steps));
                tvUnit.setText(context.getString(R.string.unit_step));
        		break;
            case DISTANCE:
                tvTotal.setText(distance > 0 ? StringUtils.formatDecimal(distance, 2) : "0");
                tvUnit.setText(context.getString(R.string.km));
                break;
            case CALORIE:
                tvTotal.setText(calorie > 0 ? StringUtils.formatDecimal(calorie, 1) : "0");
                tvUnit.setText(context.getString(R.string.kcal));
        		break;
        }
    }

    private void startRun() {
        if (MyApplication.isVoiceEnable) {
            MediaPlayerMgr.INSTANCE.play(context, R.raw.start_run);
        }
        fragment.addRunView();
        fragment.switchLayout.switchView();
    }
    
    private class SwitchRunable implements Runnable {
        @Override
        public void run() {
            switchTotalValue();
        }
    } 
}
