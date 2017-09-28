package com.advanpro.fwtools.common;

import com.advanpro.fwtools.MyApplication;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zengfs on 2016/1/21.
 * 计时器，回调可运行在UI线程或子线程
 */
public class MyTimer {
    final Object lock = new Object();
	private Timer timer;
	private TimerTask task;	

	public interface UiTimerTaskCallback {
		void runOnUiTimerTask();
	}
	
	public interface TimerTaskCallback {
		void runTimerTask();
	}
	
	/**
	 * 开始计时器
	 * @param delay 延时多长时间开始计时
	 * @param period 执行任务的周期
	 */
	public void startTimer(long delay, long period, final TimerTaskCallback callback) {
        synchronized (lock) {           
            if (timer == null && task == null) {
                timer = new Timer();
                task = new TimerTask() {
                    @Override
                    public void run() {
                        if (callback != null) callback.runTimerTask();

                    }
                };
                timer.schedule(task, delay, period);
            }
        }
	}

	/**
	 * 开始计时器
	 * @param delay 延时多长时间开始计时
	 * @param period 执行任务的周期
	 * @param callback 运行在ui线程的回调    
	 */
	public void startTimer(long delay, long period, final UiTimerTaskCallback callback) {
        synchronized (lock) {
            if (timer == null && task == null) {
                timer = new Timer();
                task = new TimerTask() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            MyApplication.getHandler().post(new Runnable() {
                                @Override
                                public void run() {								
                                    callback.runOnUiTimerTask();
                                }
                            });
                        }
                    }
                };
                timer.schedule(task, delay, period);
            }
        }
	}
	
	public void stopTimer() {
        synchronized (lock) {
            if (timer != null && task != null) {
                timer.cancel();
                task.cancel();
                timer = null;
                task = null;
            }
        }
	}
	
	public boolean isRunning() {
		return timer != null && task != null;
	}
}
