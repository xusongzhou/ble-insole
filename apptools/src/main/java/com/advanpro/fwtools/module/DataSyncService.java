package com.advanpro.fwtools.module;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.common.MyTimer;
import com.advanpro.fwtools.common.manager.ThreadMgr;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.db.Activity;
import com.advanpro.fwtools.db.Alarm;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.Gait;
import com.advanpro.fwtools.db.RunLatlng;
import com.advanpro.fwtools.db.RunPlan;
import com.advanpro.fwtools.db.RunRecord;
import com.advanpro.fwtools.entity.SimpleObservable;
import com.advanpro.fwtools.entity.SimpleObserver;
import com.advanpro.fwtools.module.activity.RunPlanParser;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.ascloud.CloudMsg;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zeng on 2016/4/24.
 * 同步云端数据服务
 */
public class DataSyncService extends Service implements MyTimer.TimerTaskCallback {
	private static final String TAG = DataSyncService.class.getSimpleName();	
	private MyTimer myTimer;
	private MyObserver observer;
	private int[] types = {Constant.TABLE_ACTIVITY, Constant.TABLE_GAIT, Constant.TABLE_ALARM, 
            Constant.TABLE_RUN_PLAN, Constant.TABLE_RUN_RECORD};
	private Queue<Integer> tasks = new ConcurrentLinkedQueue<>();
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		myTimer = new MyTimer();
		observer = new MyObserver(ObservableMgr.getSyncDbObservable());
		//先进行一次全面数据同步
		processFullSync();
	}

	private void processFullSync() {
		ThreadMgr.INSTANCE.getSPool().execute(new Runnable() {
			@Override
			public void run() {
				for (int type : types) {
					upload(type);
                    download(type);                    
				}
                //启动定时器开始定时上传
				myTimer.startTimer(1000, 600000, DataSyncService.this);
			}
		});
	}

    //上传数据，同步方式
	private void upload(int type) {
		switch(type) {
			case Constant.TABLE_ACTIVITY:
				List<Activity> activities = Dao.INSTANCE.queryActivityNotSync();
				for (Activity activity : activities) {
					try {
                        if (TextUtils.isEmpty(activity.leftDevId) && TextUtils.isEmpty(activity.rightDevId)) {
                            continue;
                        }                        
						CloudMsg req = new CloudMsg("/insole/activity/add");
						req.put("userId", activity.userId);
						req.put("leftDevId", activity.leftDevId == null ? "" : activity.leftDevId);
						req.put("rightDevId", activity.rightDevId == null ? "" : activity.rightDevId);
						req.put("startTime", activity.date.getTime());
						req.put("standTime", activity.standTime);
						req.put("sittingTime", activity.sittingTime);
						req.put("runRate", activity.runRate);
						req.put("walkSteps", activity.walkSteps);
						req.put("walkTime", activity.walkTime);
						req.put("walkDistance", activity.walkDistance);
						req.put("walkCalorie", activity.walkCalorie);
						req.put("runningSteps", activity.runningSteps);
						req.put("runningTime", activity.runningTime);
						req.put("runningDistance", activity.runningDistance);
						req.put("runningCalorie", activity.runningCalorie);
						ASCloud.sendMsg(req);
						//将已上传标志设置为true，更新到数据库
						activity.sync = true;
						Dao.INSTANCE.insertOrUpdateActivity(activity);
					} catch (Exception e) {
						LogUtil.e(TAG, "ansobuy--" + e.toString());
					}
				}				
				break;
			case Constant.TABLE_GAIT:
				List<Gait> gaits = Dao.INSTANCE.queryGaitNotSync();
				for (Gait gait : gaits) {
					try {
						CloudMsg req = new CloudMsg("/insole/gait/add");
						req.put("userId", gait.userId);
						req.put("startTime", gait.date.getTime());
						req.put("foot", gait.foot);
						req.put("varus", gait.varus);
						req.put("ectropion", gait.ectropion);
						req.put("forefoot", gait.forefoot);
						req.put("heel", gait.heel);
						req.put("sole", gait.sole);
						ASCloud.sendMsg(req);
						gait.sync = true;
						Dao.INSTANCE.insertOrUpdateGait(gait);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case Constant.TABLE_ALARM:
				List<Alarm> alarms = Dao.INSTANCE.queryAlarmNotSync();
				for (Alarm alarm : alarms) {
					try {
						CloudMsg req = new CloudMsg("/insole/alarm/add");
						req.put("userId", alarm.userId);
						req.put("startTime", alarm.date.getTime());
						req.put("type", alarm.type);
						ASCloud.sendMsg(req);
						alarm.sync = true;
						Dao.INSTANCE.insertOrUpdateAlarm(alarm.date, alarm.type, alarm.userId, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case Constant.TABLE_RUN_PLAN:
				List<RunPlan> plans = Dao.INSTANCE.queryRunPlanNotSync();
				for (RunPlan plan : plans) {
					try {
						CloudMsg req = new CloudMsg("/insole/plan/add");
						req.put("userId", plan.userId);
						req.put("startTime", plan.startDate.getTime());
						req.put("type", plan.type);
						req.put("progress", RunPlanParser.getDayIndexOfPlan());
						req.put("trainDay", plan.fulfillState);
						ASCloud.sendMsg(req);
						plan.sync = true;
						Dao.INSTANCE.insertOrUpdateRunPlan(plan);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;
			case Constant.TABLE_RUN_RECORD:
				try {
					List<RunRecord> records = Dao.INSTANCE.queryRunRecordNotSync();
					for (RunRecord record : records) {
						List<RunLatlng> runLatlngs = Dao.INSTANCE.queryRunLatlngs(record.id);
                        //没有轨迹数据不上传记录
                        if (runLatlngs.size() == 0) continue;
						for (RunLatlng latlng : runLatlngs) {
							CloudMsg requ = new CloudMsg("/insole/running/coord/add");
							requ.put("userId", latlng.userId);
							requ.put("startTime", record.startTime.getTime());
							requ.put("time", latlng.time);
							requ.put("x", latlng.latitude);
							requ.put("y", latlng.longitude);
							ASCloud.sendMsg(requ);
						}
						CloudMsg req = new CloudMsg("/insole/running/add");
						req.put("userId", record.userId);
						req.put("startTime", record.startTime.getTime());
						req.put("endTime", record.endTime.getTime());
						req.put("distance", record.distance);
						ASCloud.sendMsg(req);
						record.sync = true;
						Dao.INSTANCE.insertOrUpdatetRunRecord(record);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}

		 
	}
	
    //下载数据，同步方式
	private void download(int type) {
		long beginTime = new GregorianCalendar(2016, 0, 1).getTimeInMillis();
		Date date = Dao.INSTANCE.querySyncDate(type);
		if (date != null) beginTime = date.getTime();
		switch(type) {
		    case Constant.TABLE_ACTIVITY:
				try {
					CloudMsg req = new CloudMsg("/insole/activity/query");
					req.put("userId", ASCloud.userInfo.ID);
					req.put("beginTime", beginTime);
					CloudMsg resp = ASCloud.sendMsgNoException(req);
					if (resp != null) {
                        LogUtil.d(TAG, "ansobuy--活动统计：" + resp.toString());
						List<CloudMsg> list = resp.getList("datas");
						if (list != null) {
							for (CloudMsg obj : list) {
								Activity activity = new Activity();
								activity.userId = obj.getLong("userId");
								activity.leftDevId = obj.getString("leftDevId");
								activity.rightDevId = obj.getString("rightDevId");
								activity.date = new Date(obj.getLong("startTime"));
								activity.standTime = (int) obj.getLong("standTime");
								activity.sittingTime = (int) obj.getLong("sittingTime");
								activity.runRate = obj.getDouble("runRate");
								activity.walkSteps = (int) obj.getLong("walkSteps");
								activity.walkTime = (int) obj.getLong("walkTime");
								activity.walkDistance = obj.getDouble("walkDistance");
								activity.walkCalorie = obj.getDouble("walkCalorie");
								activity.runningSteps = (int) obj.getLong("runningSteps");
								activity.runningTime = (int) obj.getLong("runningTime");
								activity.runningDistance = obj.getDouble("runningDistance");
								activity.runningCalorie = obj.getDouble("runningCalorie");
								activity.sync = true;
								Dao.INSTANCE.insertOrUpdateActivity(activity);
							}
						}
					}
                    //保存下载记录
                    Dao.INSTANCE.insertOrUpdateSyncDate(type, new Date());
				} catch (Exception e) {
					LogUtil.e(TAG, "ansobuy--" + e.toString());
				}				
				break;
			case Constant.TABLE_GAIT:
				try {
					CloudMsg req = new CloudMsg("/insole/gait/query");
					req.put("userId", ASCloud.userInfo.ID);
					req.put("beginTime", beginTime);
					CloudMsg resp = ASCloud.sendMsgNoException(req);
					if (resp != null) {
                        LogUtil.d(TAG, "ansobuy--步态统计：" + resp.toString());
						List<CloudMsg> list = resp.getList("datas");
						if (list != null) {
							for (CloudMsg obj : list) {
								Gait gait = new Gait();
								gait.userId = obj.getLong("userId");
								gait.date = new Date(obj.getLong("startTime"));
								gait.foot = (int) obj.getLong("foot");
								gait.varus = (int) obj.getLong("varus");
								gait.ectropion = (int) obj.getLong("ectropion");
								gait.forefoot = (int) obj.getLong("forefoot");
								gait.heel = (int) obj.getLong("heel");
								gait.sole = (int) obj.getLong("sole");
								gait.sync = true;
								Dao.INSTANCE.insertOrUpdateGait(gait);
							}
						}
					}
                    //保存下载记录
                    Dao.INSTANCE.insertOrUpdateSyncDate(type, new Date());
				} catch (Exception e) {
					LogUtil.e(TAG, "ansobuy--" + e.toString());
				}
				break;
			case Constant.TABLE_ALARM:
				try {
					CloudMsg req = new CloudMsg("/insole/alarm/query");
					req.put("userId", ASCloud.userInfo.ID);
					req.put("beginTime", beginTime);
					CloudMsg resp = ASCloud.sendMsgNoException(req);
					if (resp != null) {
                        LogUtil.d(TAG, "ansobuy--疲劳提醒：" + resp.toString());
						List<CloudMsg> list = resp.getList("datas");
						if (list != null) {
							for (CloudMsg obj : list) {
								Dao.INSTANCE.insertOrUpdateAlarm(new Date(obj.getLong("startTime")),
										(int) obj.getLong("type"), obj.getLong("userId"), true);
							}
						}
					}
                    //保存下载记录
                    Dao.INSTANCE.insertOrUpdateSyncDate(type, new Date());
				} catch (Exception e) {
					LogUtil.e(TAG, "ansobuy--" + e.toString());
				}
				break;
			case Constant.TABLE_RUN_PLAN:
                try {
                    CloudMsg req = new CloudMsg("/insole/plan/query");
                    req.put("userId", ASCloud.userInfo.ID);
                    req.put("beginTime", beginTime);
                    CloudMsg resp = ASCloud.sendMsgNoException(req);
                    if (resp != null) {
                        LogUtil.d(TAG, "ansobuy--跑步计划：" + resp.toString());
                        List<CloudMsg> list = resp.getList("datas");
                        if (list != null) {
                            for (CloudMsg obj : list) {
                                if (Dao.INSTANCE.queryRunPlan() != null) continue;
                                RunPlan plan = new RunPlan();
                                plan.userId = obj.getLong("userId");
                                plan.startDate = new Date(obj.getLong("startTime"));
                                plan.type = (int) obj.getLong("type");
                                plan.fulfillState = obj.getString("trainDay");
                                plan.sync = true;
                                Dao.INSTANCE.insertOrUpdateRunPlan(plan);
                            }
                        }
                    }
                    //保存下载记录
                    Dao.INSTANCE.insertOrUpdateSyncDate(type, new Date());
                } catch (Exception e) {
                    LogUtil.e(TAG, "ansobuy--" + e.toString());
                }
				break;
			case Constant.TABLE_RUN_RECORD:
                try {
                    CloudMsg req = new CloudMsg("/insole/running/query");
                    req.put("userId", ASCloud.userInfo.ID);
                    req.put("beginTime", beginTime);
                    CloudMsg resp = ASCloud.sendMsg(req);
                    if (resp != null) {
                        LogUtil.d(TAG, "ansobuy--跑步记录：" + resp.toString());
                        List<CloudMsg> list = resp.getList("datas");
                        if (list != null) {
                            for (CloudMsg obj : list) {
                                Date startTime = new Date(obj.getLong("startTime"));
                                long userId = obj.getLong("userId");
                                RunRecord record = Dao.INSTANCE.queryRunRecord(userId, startTime);
                                if (record != null) continue;//记录已存在，不替换
                                record = new RunRecord();
                                record.userId = userId;
                                record.startTime = startTime;
                                record.date = DateUtils.getStartOfDay(record.startTime).getTime();
                                record.endTime = new Date(obj.getLong("endTime"));
                                record.distance = obj.getDouble("distance");
                                record.sync = true;
                                //下载跑步轨迹
                                CloudMsg request = new CloudMsg("/insole/running/coord/query");
                                request.put("userId", ASCloud.userInfo.ID);
                                request.put("startTime", obj.getLong("startTime"));
                                CloudMsg response = ASCloud.sendMsgNoException(request);
                                List<RunLatlng> runLatlngs = new ArrayList<>();
                                if (response != null) {
                                    LogUtil.d(TAG, "ansobuy--" + response.toString());
                                    List<CloudMsg> datas = response.getList("datas");
                                    if (datas != null) {
                                        for (CloudMsg data : datas) {
                                            RunLatlng latlng = new RunLatlng();
                                            latlng.userId = data.getLong("userId");
                                            latlng.time = data.getLong("time");
                                            latlng.latitude = data.getDouble("x");
                                            latlng.longitude = data.getDouble("y");
                                            runLatlngs.add(latlng);
                                        }
                                    }
                                }
                                Dao.INSTANCE.insertOrUpdatetRunRecord(record, runLatlngs);
                            }
                        }
                    }
                    //保存下载记录
                    Dao.INSTANCE.insertOrUpdateSyncDate(type, new Date());
                } catch (Exception e) {
                    LogUtil.e(TAG, "ansobuy--" + e.toString());
                }
				break;
		}
	}
	
	@Override
	public void onDestroy() {
		ObservableMgr.getSyncDbObservable().deleteObserver(observer);
		myTimer.stopTimer();
	}

	@Override
	public void runTimerTask() {
        while (!tasks.isEmpty()) {
            upload(tasks.remove());
        }
		
		//如果MainActivity销毁了，而且没有上传任务，退出服务
		android.app.Activity activity = BaseActivity.getActivity(MainActivity.class.getName());
		if (activity == null && tasks.isEmpty()) stopSelf();
	}

	private class MyObserver extends SimpleObserver {
		public MyObserver(SimpleObservable myObservable) {
			super(myObservable);
		}

		@Override
		public void update(Observable observable, Object data) {
			SimpleObservable obser = (SimpleObservable) observable;			
			if (!tasks.contains(obser.changeType)) tasks.add(obser.changeType);
		}
	}
}
