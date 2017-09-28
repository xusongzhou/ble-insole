package com.advanpro.fwtools.module;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zengfs on 2016/1/13.
 * Activity的基类
 */
public class BaseActivity extends FragmentActivity {

	private static final Map<String, Activity> activities = new HashMap<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		synchronized (activities) {
			activities.put(this.getClass().getName(), this);
		}
	}
	
	@Override
	protected void onDestroy() {
		synchronized (activities) {
			activities.remove(this.getClass().getName());
		}
		super.onDestroy();
	}

	public static Activity getActivity(String className) {
		return activities.get(className);
	}
	
	/**
	 * 关闭所有打开的Activity，杀死进程
	 */
	public static void completeExit() {
		Map<String, Activity> activityMap = new HashMap<>(BaseActivity.activities);
		for (Map.Entry<String, Activity> entry : activityMap.entrySet()) {
			BaseActivity.activities.get(entry.getKey()).finish();
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
}
