package com.advanpro.fwtools.module.activity;

import android.content.Context;
import android.view.View;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.alg.AlgLib;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.db.Activity;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.PoseLine;
import com.advanpro.fwtools.entity.State;
import com.advanpro.ascloud.ASCloud;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zengfs on 2016/1/18.
 * 姿势页面
 */
public class PosePager extends BasePager {

	private PoseDistributionView distributionView;
	private PoseDurationView durationView;
	private Map<Boolean, PoseInfo> infoMap = new HashMap<>();

	public PosePager(Context context) {
		super(context);
	}

	@Override
	protected View getRootView() {
		return View.inflate(context, R.layout.pager_pose, null);
	}

	@Override
	protected void assignViews() {
		distributionView = (PoseDistributionView) rootView.findViewById(R.id.distribution_view);
		durationView = (PoseDurationView) rootView.findViewById(R.id.duration_view);
	}

	@Override
	protected void initViews() {
		Dao.INSTANCE.deletePoseLines();
		updateDistriView();	
		updateDurationView();
	}

	public void onPoseDataRead(BleDevice device, int value) {
		int sec = timeToSec(Calendar.getInstance());
		PoseInfo info1 = new PoseInfo(sec, value);
		infoMap.put(device.isLeft, info1);
		//如果连接了两只，结合判断
		if (DeviceMgr.getConnectedCount() == 2) {
			PoseInfo info2 = infoMap.get(!device.isLeft);
			if (info2 != null && info2.sec == sec) {
				parse(info1, info2);
			}
		} else {
			parse(info1, null);
		}
		updateDistriView();
	}

	private void parse(PoseInfo info1, PoseInfo info2) {
		int startTime = secToMillis(info1.sec);
		PoseLine line = new PoseLine();
		line.date = DateUtils.getStartOfDay(new Date()).getTime();
		line.userId = ASCloud.userInfo.ID;
		line.startMillis = startTime;
		line.endMillis = startTime + 60000;
        if (State.runState == State.RunState.STOP) {
		    int pose;
            if (info2 != null) pose = AlgLib.parsePose(info1.value, info2.value);
            else pose = AlgLib.parsePose(info1.value);
            if (pose != -1) {
                line.type = pose;
            } else {
                line.type = Constant.POSE_WALK;
            }
        } else {
            line.type = Constant.POSE_RUN;
        }		

		Dao.INSTANCE.insertOrUpdatePoseLine(line);
	}

	private void updateDistriView() {
		List<PoseLine> lines = Dao.INSTANCE.queryPoseLines(new Date());
		distributionView.setData(lines);
	}
	
	public void updateDurationView() {
		Activity activity = Dao.INSTANCE.queryActivity(DateUtils.getStartOfDay(new Date()).getTime());
		if (activity == null) activity = new Activity();
		durationView.setData(activity.runningTime / 3600f, activity.walkTime / 3600f,
				activity.standTime / 3600f, activity.sittingTime / 3600f);
	}

	/**
	 * 滚动分布图到当前时间
	 */
	public void scrollDistributionView() {
		distributionView.scrollChart();
	}

	//时间转换成段数
	private int timeToSec(Calendar c) {
		return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
	}

	//段数转换成此段的开始毫秒值
	private int secToMillis(int sec) {
		return sec * 60000;
	}

	private static class PoseInfo {
		int sec;
		int value;

		public PoseInfo(int sec, int value) {
			this.sec = sec;
			this.value = value;
		}
	}
}
