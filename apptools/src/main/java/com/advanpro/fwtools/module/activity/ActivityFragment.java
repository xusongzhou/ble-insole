package com.advanpro.fwtools.module.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.alg.AlgLib;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.BleManager;
import com.advanpro.fwtools.ble.BleObservable;
import com.advanpro.fwtools.ble.BleObserver;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.ble.HisDataReaderMgr;
import com.advanpro.fwtools.common.base.BaseFragment;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.base.BasePagerAdapter;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.MyViewPager;
import com.advanpro.fwtools.common.view.SwitchLayout;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.LastStep;
import com.advanpro.fwtools.db.RunLatlng;
import com.advanpro.fwtools.entity.SimpleObserver;
import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.entity.Step;
import com.advanpro.fwtools.module.BaseActivity;
import com.advanpro.fwtools.module.MainActivity;
import com.advanpro.ascloud.ASCloud;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by zengfs on 2016/1/14.
 * 活动模块主框架
 */
public class ActivityFragment extends BaseFragment<MainActivity> implements SwitchLayout.SwitchListener {
	private List<BasePager> pagers;
	private int space; //页面指示器两个圆点间的距离
	private View viewPoint;//页面指示点
	private MyViewPager viewPager;
	private LinearLayout llIndicator;
	public TodayActivityPager todayActivityPager;
	private GaitPager gaitPager;
	private PosePager posePager;		
	public static ActivityFragment instance;
	private FrameLayout runContainer;
	private RunView runView;
	public SwitchLayout switchLayout;
    private MyBleObserver bleObserver;
	//-----计步-----
	private static Map<String, Boolean> firstArray = new HashMap<>();
	//-----定位-----
	private AMapLocationClient locationClient;
	private AMapLocationClientOption locationOption;
	private LatLng lastLatlng;//用来画路径的上一个坐标点
	public List<RunLatlng> runLatlngs = new ArrayList<>();//用来画轨迹的点
	public float distance;
	public int duration;
	private boolean locationSuccess;
	private int lastSteps = -1;
	private PendingIntent alarmPi;
	private AlarmManager alarm;
	
	@Override
	protected void assignViews() {
		instance = this;		
		viewPager = (MyViewPager) rootView.findViewById(R.id.view_pager);
		llIndicator = (LinearLayout) rootView.findViewById(R.id.ll_indicator);
		viewPoint = rootView.findViewById(R.id.view_point);
		switchLayout = (SwitchLayout) rootView.findViewById(R.id.switch_layout);
		runContainer = (FrameLayout) rootView.findViewById(R.id.run_container);
	}

	@Override
	protected View getRootView(LayoutInflater inflater, ViewGroup container) {
		return inflater.inflate(R.layout.frag_activity, container, false);
	}

	@Override
	protected void initViews() {
		//初始化ViewPager
		pagers = new ArrayList<>();
		todayActivityPager = new TodayActivityPager(getContext(), this);
		gaitPager = new GaitPager(getContext(), this);
		posePager = new PosePager(getContext());
		pagers.add(todayActivityPager);
		pagers.add(gaitPager);
		pagers.add(posePager);
		viewPager.setAdapter(new BasePagerAdapter(pagers));
		viewPager.addOnPageChangeListener(pageChangeListener);
        
		//添加ViewPager指示器
		addIndicator();
		//定位初始化
		initLocation();
		//设置后台唤醒
		initAlarm();

		switchLayout.setSlideOrientation(SwitchLayout.RIGHT);
		switchLayout.setSwitchListener(this);
        bleObserver = new MyBleObserver(ObservableMgr.getBleObservable());     
	}

	private void initAlarm() {
		// 创建Intent对象，action为LOCATION
		Intent alarmIntent = new Intent();
		alarmIntent.setAction("LOCATION");
		// 定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
		// 也就是发送了action 为"LOCATION"的intent
        if (parentActivity == null) return;
		alarmPi = PendingIntent.getBroadcast(parentActivity, 0, alarmIntent, 0);
		// AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
		alarm = (AlarmManager) parentActivity.getSystemService(Context.ALARM_SERVICE);

		//动态注册一个广播
		IntentFilter filter = new IntentFilter();
		filter.addAction("LOCATION");
        parentActivity.registerReceiver(alarmReceiver, filter);
	}
    
	public static void setFirstState(String mac, boolean b) {
        firstArray.put(mac, b);
	}
    
    private boolean isFirstState(String mac) {
        Boolean b = firstArray.get(mac);
        return b == null || b;
    }
    
	private SimpleObserver walkObserver = new SimpleObserver(ObservableMgr.getActivityObservable()) {
		@Override
		public void update(Observable observable, Object data) {
			if (todayActivityPager != null) todayActivityPager.updateView();
            if (posePager != null) posePager.updateDurationView();
		}
	};
	
	private class MyBleObserver extends BleObserver {
        
        public MyBleObserver(BleObservable bleObservable) {
            super(bleObservable);
        }

        @Override
		public void onConnectionStateChange(final BleDevice device, final State.ConnectionState state) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state != State.ConnectionState.CONNECTED) {
                        gaitPager.updateView(device, 0, 0);
                    }
                    todayActivityPager.updateConnWarnView();
                }
            });
		}

		@Override
		public void onGaitRealtimeDataChanged(final BleDevice device, final int data, final int impactRank) {
            UiUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gaitPager.updateView(device, data, impactRank);
                }
            });
		}
		
		@Override
		public void onStepRealtimeDataChanged(BleDevice device, Step.Apart info) {
            //查询此设备已存最后一次计步数据
			LastStep theStep = null;
            Date date = new Date();
			if(DeviceMgr.getConnectedCount() == 2) {
				theStep = Dao.INSTANCE.queryLastStep(date,
						DeviceMgr.getBoundDevice(!device.isLeft).mac);
			}

/*            if (lastStep == null) {
                lastStep = new LastStep();
                lastStep.date = DateUtils.getStartOfDay(new Date()).getTime();
                lastStep.mac = device.mac;
            }
*/
			if(theStep != null) {
				Dao.INSTANCE.insertOrUpdateWalkRun(new Date(),
						info.walkSteps + theStep.walkSteps, info.walkDuration +theStep.walkTime,
						info.runSteps + theStep.runningSteps, info.runDuration + theStep.runningTime);
			} else {
				Dao.INSTANCE.insertOrUpdateWalkRun(new Date(),
						info.walkSteps, info.walkDuration, info.runSteps, info.runDuration);
			}
/*            if (State.runState == State.RunState.STOP) {
                //计算增量
                int stepsIncr = info.walkSteps + info.runSteps;
                int durIncr = info.walkDuration + info.runDuration;
                if (isFirstState(device.mac)) {
					setFirstState(device.mac, false);
					if (stepsIncr >= 0 && durIncr >= 0) {
						Dao.INSTANCE.insertOrUpdateWalkRun(new Date(),
								info.walkSteps, info.walkDuration, info.runSteps, info.runDuration);
					}
				} else {
					if (DeviceMgr.getConnectedCount() == 2) {
						Dao.INSTANCE.insertOrUpdateWalkRun(new Date(),
						info.walkSteps, info.walkDuration, info.runSteps, info.runDuration);
					} else if (DeviceMgr.getConnectedCount() == 1) {
						Dao.INSTANCE.insertOrUpdateWalk(new Date(), stepsIncr * 2, durIncr);
					}
				}
			}
*/
			theStep = null;
			theStep = Dao.INSTANCE.queryLastStep(date,device.mac);
			if(theStep == null) {
				theStep = new LastStep();
				theStep.date = DateUtils.getStartOfDay(date).getTime();
				theStep.mac = device.mac;
            }
            //保存此次计步数据
			theStep.walkSteps = info.walkSteps;
			theStep.walkTime = info.walkDuration;
			theStep.runningSteps = info.runSteps;
			theStep.runningTime = info.runDuration;
            Dao.INSTANCE.insertOrUpdateLastStep(theStep);
            
            posePager.updateDurationView();
		}

		@Override
		public void onPoseRealtimeDataChanged(BleDevice device, int data) {
			posePager.onPoseDataRead(device, data);
		}
	}
	
	public void addRunView() {
		startLocation();
		runView = new RunView(this);
		runContainer.addView(runView.rootView);
	}
	
	@Override
	public void onSwitchStart(SwitchLayout switchLayout, boolean isDefaultViewShowing) {
		if (State.runState == State.RunState.STOP) {
			todayActivityPager.btnStartRun.setBackgroundResource(R.drawable.start_run);			
		} else {
			todayActivityPager.btnStartRun.setBackgroundResource(R.drawable.running);
		}
	}

	@Override
	public void onSwitchEnd(SwitchLayout switchLayout, boolean isDefaultViewShowing) {		
		if (State.runState == State.RunState.STOP) {
			destroyRunView();
		}
	}
	
	private void initLocation() {
		locationClient = new AMapLocationClient(parentActivity.getApplicationContext());
		locationOption = new AMapLocationClientOption();
		// 设置定位模式为高精度模式
		locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		locationClient.setLocationListener(locationListener);// 设置定位监听		
		locationOption.setOnceLocation(false);// 设置为不是单次定位
		locationOption.setInterval(2000);
		
		// 设置是否需要显示地址信息
		locationOption.setNeedAddress(true);
		locationClient.setLocationOption(locationOption);// 设置定位参数	
	}

	/**
	 * 启动定位
	 */
	public void startLocation() {
		locationClient.startLocation();
		if(null != alarm && null != alarmPi){
			//设置一个闹钟，2秒之后每隔一段时间执行启动一次定位程序
			alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 2000,
					60000, alarmPi);
		}
	}

	/**
	 * 停止定位，并将所有变量恢复初始值
	 */
	public void stopLocation() {
		locationClient.stopLocation();
		//停止定位的时候取消闹钟
		if(null != alarm && null != alarmPi){
			alarm.cancel(alarmPi);
		}
		locationSuccess = false;
		runLatlngs.clear();
		lastLatlng = null;
        lastSteps = -1;
		distance = 0;
		duration = 0;
	}
	
	//初始化ViewPager指示器
	private void addIndicator() {
		int space = UiUtils.dip2px(6); //单位转换
		for (int i = 0; i < pagers.size(); i++) {
			View view = new View(getContext());
			view.setBackgroundResource(R.drawable.white_ring);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(space, space);
			//第二个点开始设置间隔
			if (i > 0) {
				params.leftMargin = space;
			}
			//点之间的坐标距离
			if (i == 1) {
				this.space = params.width + space;
			}
			view.setLayoutParams(params);
			llIndicator.addView(view);
		}
	}

	private AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation aMapLocation) {            
			if (!locationSuccess && runView != null) {	
			    if (lastSteps == -1)  {
                    lastSteps = runView.cacheSteps;
			    } else {
                    distance += AlgLib.calcRunDistance(runView.cacheSteps - lastSteps) * 1000;
                    lastSteps = runView.cacheSteps;
                    runView.updateView();
			    }   
			}
			if (aMapLocation != null) {
				if (aMapLocation.getErrorCode() == 0) {					
					Activity pathActivity = BaseActivity.getActivity(PathActivity.class.getName());
					if (pathActivity != null) {
						((PathActivity) pathActivity).onLocationChanged(aMapLocation);
					}

//                    ToastMgr.showTextToast(getContext(), 0, getLocationStr(aMapLocation));
                    
					//精度在30米
					if (aMapLocation.getAccuracy() <= 30) {
						LatLng curLatlng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
						locationSuccess = true;//定位成功
						if (lastLatlng == null) {
							lastLatlng = curLatlng;
							if (runView != null) lastSteps = runView.cacheSteps;
						} else {				
							float increment = AMapUtils.calculateLineDistance(lastLatlng, curLatlng);
							//移动大于2米
							if (increment >= 2) {
								if (runView != null) {									
									double d = AlgLib.calcRunDistance(runView.cacheSteps - lastSteps) * 1000;
									//计步无数据变化，GPS产生的距离丢弃
									if (State.runState == State.RunState.RUNNING && d > 0) distance += increment;
									runView.updateView();
									lastSteps = runView.cacheSteps;
								}
                                lastLatlng = curLatlng;
							}
                            //保存轨迹点
                            runLatlngs.add(new RunLatlng(null, aMapLocation.getTime(), curLatlng.latitude,
                                    curLatlng.longitude, 0, ASCloud.userInfo.ID));                            
                            if (pathActivity != null) {
                                ((PathActivity) pathActivity).updatePath();
                            }
						}						
					} else if (locationSuccess && runView != null) {
                        distance += AlgLib.calcRunDistance(runView.cacheSteps - lastSteps) * 1000;
                        lastSteps = runView.cacheSteps;
                        runView.updateView();
					}					
				} else {
                    if (locationSuccess) lastSteps = -1;
					locationSuccess = false;
                    lastLatlng = null;
					LogUtil.d("ActivityFragment", "ansobuy--定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo());
				}
			}
		}
	};

	private ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			//根据滑动偏移量实时更新指示点的布局参数
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPoint.getLayoutParams();
			params.leftMargin = (int) (space * (position + positionOffset));
			viewPoint.setLayoutParams(params);
		}

		@Override
		public void onPageSelected(int position) {
			if (position == 2) posePager.scrollDistributionView();
		}
	};

	@Override
	public void onResume() {
		todayActivityPager.onResume();
		super.onResume();
	}

    @Override
    public void onStart() {
        super.onStart();
    }

	private void destroyRunView() {
		if (runView != null) {
			runView.destroy();
			runContainer.removeAllViews();
			runView = null;
		}		
	}
	
	@Override
	public void onDestroy() {
		instance = null;
		ObservableMgr.getBleObservable().deleteObserver(bleObserver);
		ObservableMgr.getActivityObservable().deleteObserver(walkObserver);
		destroyRunView();
		if (null != locationClient) {
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
		if(parentActivity != null && null != alarmReceiver){
			try {
                getActivity().unregisterReceiver(alarmReceiver);
                alarmReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e("ActivityFragment", e.toString());
            }
        }
		super.onDestroy();		
	}

    /**
     * 每整分钟回调一次
     */
    public void onTimeChanged() {
        HisDataReaderMgr.INSTANCE.readHisData();
		//10分钟读一次设备电量
		if (Calendar.getInstance().get(Calendar.MINUTE) % 10 == 0) {
			BleManager.INSTANCE.readAllBattery();
		}
    }

	private BroadcastReceiver alarmReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals("LOCATION")){
				if(null != locationClient){
					locationClient.startLocation();
				}
			}
		}
	};

    /**
     * 根据定位结果返回定位信息的字符串
     * @param location
     */
    public synchronized static String getLocationStr(AMapLocation location){
        if(null == location){
            return null;
        }
        StringBuffer sb = new StringBuffer();
        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if(location.getErrorCode() == 0){
            sb.append("定位成功" + "\n");
            sb.append("定位类型: " + location.getLocationType() + "\n");
            sb.append("经    度    : " + location.getLongitude() + "\n");
            sb.append("纬    度    : " + location.getLatitude() + "\n");
            sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
            sb.append("提供者    : " + location.getProvider() + "\n");

            if (location.getProvider().equalsIgnoreCase(
                    android.location.LocationManager.GPS_PROVIDER)) {
                // 以下信息只有提供者是GPS时才会有
                sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                sb.append("角    度    : " + location.getBearing() + "\n");
                // 获取当前提供定位服务的卫星个数
                sb.append("星    数    : "
                        + location.getExtras().getInt("satellites", 0) + "\n");
            } else {
                // 提供者是GPS时是没有以下信息的
                sb.append("国    家    : " + location.getCountry() + "\n");
                sb.append("省            : " + location.getProvince() + "\n");
                sb.append("市            : " + location.getCity() + "\n");
                sb.append("城市编码 : " + location.getCityCode() + "\n");
                sb.append("区            : " + location.getDistrict() + "\n");
                sb.append("区域 码   : " + location.getAdCode() + "\n");
                sb.append("地    址    : " + location.getAddress() + "\n");
                sb.append("兴趣点    : " + location.getPoiName() + "\n");
            }
        } else {
            //定位失败
            sb.append("定位失败" + "\n");
            sb.append("错误码:" + location.getErrorCode() + "\n");
            sb.append("错误信息:" + location.getErrorInfo() + "\n");
            sb.append("错误描述:" + location.getLocationDetail() + "\n");
        }
        return sb.toString();
    }
}
