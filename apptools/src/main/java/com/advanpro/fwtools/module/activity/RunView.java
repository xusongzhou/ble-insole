package com.advanpro.fwtools.module.activity;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.MyApplication;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.alg.AlgLib;
import com.advanpro.fwtools.alg.FatigueAnalysis;
import com.advanpro.fwtools.alg.InjureAnalysis;
import com.advanpro.fwtools.ble.BleDevice;
import com.advanpro.fwtools.ble.BleObserver;
import com.advanpro.fwtools.ble.DeviceMgr;
import com.advanpro.fwtools.common.MyTimer;
import com.advanpro.fwtools.common.manager.MediaPlayerMgr;
import com.advanpro.fwtools.common.manager.ToastMgr;
import com.advanpro.fwtools.common.util.AnimatorUtils;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.SystemUtils;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunRecord;
import com.advanpro.fwtools.entity.State;
import com.advanpro.fwtools.entity.Step;
import com.advanpro.fwtools.module.BaseActivity;
import com.advanpro.ascloud.ASCloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zengfs on 2016/1/24.
 * 跑步页面
 */
public class RunView implements View.OnClickListener, MyTimer.UiTimerTaskCallback {
    private static final int[] numResIds = {R.raw.zero, R.raw.one, R.raw.two, R.raw.three, R.raw.four, R.raw.five,
            R.raw.six, R.raw.seven, R.raw.eight, R.raw.nine, R.raw.ten, R.raw.dot};
    private ActivityFragment fragment;
    private Context context;
    public View rootView;
    private TitleBar titleBar;
    private FrameLayout musicContainer;//音乐播放控件的容器
    private FrameLayout progressContainer;
    private ImageButton btnPushPull;
    private LinearLayout llValues;
    private TextView tvSteps;
    private TextView tvRate;
    private TextView tvDuration;
    private TextView tvCal;
    private Button btnPauseStart;
    private ImageButton btnBackHome;
    private ImageButton btnStop;
    private MusicView musicView;//音乐播放控件
    private boolean musicViewIsOpened;
    private RunProgressView runProgressView;
    public MyTimer myTimer;
    private boolean isPaused;
    private BleObserver observer;
    private boolean isAutoPause;
    private int timeCount;
    private VoiceWarner voiceWarner;
    //---------本次跑步记录---------
    private RunRecord runRecord;
    private Map<Boolean, Boolean> firstArray = new HashMap<>();
    private Map<Boolean, Step.Total> lastStep = new HashMap<>();
	public int cacheSteps;
	private int cacheHwDuration;
    private float lastDistance;
    private int lastDuration;
    private int lastCacheSteps;
    private int currentRealSteps;
    private FatigueAnalysis fatigueAnalysis;
    private InjureAnalysis injureAnalysis;

    public RunView(ActivityFragment fragment) {
        this.fragment = fragment;
        context = fragment.getContext();
        runRecord = new RunRecord(null, DateUtils.getStartOfDay(new Date()).getTime(), new Date(), new Date(),
                0, 0, 0, 0, 0, 0, ASCloud.userInfo.ID, false);
        State.runState = State.RunState.RUNNING;
        ObservableMgr.getRunStateObservable().setChanged();
        initObserver();
        assignViews();
        initViews();
        voiceWarner = new VoiceWarner(context);
        myTimer = new MyTimer();
        myTimer.startTimer(0, 1000, this);
        fatigueAnalysis = new FatigueAnalysis();
        injureAnalysis = new InjureAnalysis();
    }

    private void initObserver() {
        observer = new BleObserver(ObservableMgr.getBleObservable()) {
            @Override
            public void onStepRealtimeDataChanged(BleDevice device, Step.Apart info) {
                if (isFirstState(device.isLeft)) {
                    setFirstState(device.isLeft, false);                    
                } else {
                    Step.Total total = lastStep.get(device.isLeft);
                    if (total != null) {
                        //计算增量
                        int stepsIncr = info.walkSteps + info.runSteps - total.steps;
                        int durIncr = info.walkDuration + info.runDuration - total.duration;
                        if (stepsIncr >= 0 && durIncr >= 0) {
                            currentRealSteps += stepsIncr;
                            if (!isPaused) {
                                if (DeviceMgr.getConnectedCount() == 2) {
                                    cacheSteps += stepsIncr;
                                    cacheHwDuration += durIncr;
                                } else if (DeviceMgr.getConnectedCount() == 1) {
                                    cacheSteps += stepsIncr * 2;
                                    cacheHwDuration += durIncr;
                                }
                            }
                        }
                    }
                }
                lastStep.put(device.isLeft, new Step.Total(info.walkSteps + info.runSteps,
                        info.walkDuration + info.runDuration));
                tvSteps.setText(String.valueOf(cacheSteps));                
            }
        };
    }

    private void initViews() {
        initTitleBar();
        btnPauseStart.setOnClickListener(this);
        initMusicView();
        runProgressView = new RunProgressView(progressContainer);
        btnPushPull.setOnClickListener(this);
        btnBackHome.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    private void assignViews() {
        rootView = View.inflate(context, R.layout.view_run, null);
        titleBar = (TitleBar) rootView.findViewById(R.id.title_bar);
        musicContainer = (FrameLayout) rootView.findViewById(R.id.music_container);
        progressContainer = (FrameLayout) rootView.findViewById(R.id.progress_container);
        btnPushPull = (ImageButton) rootView.findViewById(R.id.btn_push_pull);
        llValues = (LinearLayout) rootView.findViewById(R.id.ll_values);
        tvSteps = (TextView) rootView.findViewById(R.id.tv_steps);
        tvRate = (TextView) rootView.findViewById(R.id.tv_rate);
        tvDuration = (TextView) rootView.findViewById(R.id.tv_duration);
        tvCal = (TextView) rootView.findViewById(R.id.tv_cal);
        btnPauseStart = (Button) rootView.findViewById(R.id.btn_pause_start);
        btnBackHome = (ImageButton) rootView.findViewById(R.id.btn_back_home);
        btnStop = (ImageButton) rootView.findViewById(R.id.btn_stop);
    }

    //跑步界面标题栏
    private void initTitleBar() {
        titleBar.setTitle(R.string.running);
        titleBar.setEndImageButtonVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_end://轨迹
                        context.startActivity(new Intent(context, PathActivity.class));
                        break;
                }
            }
        });
    }

    public void setFirstState(boolean isLeft, boolean b) {
        firstArray.put(isLeft, b);
    }

    private boolean isFirstState(boolean isLeft) {
        Boolean b = firstArray.get(isLeft);
        return b == null || b;
    }
    
    /**
     * 更新距离及配速
     */
    public void updateView() {
		float d = fragment.distance / 1000;
		runProgressView.setNumber(d);        
        //卡路里消耗
        tvCal.setText(d == 0 ? "0" : String.format("%.1f", AlgLib.calculateCalories(d)));
    }
    
    public void updateRate() {
        //更新配速
        float dist = (fragment.distance - lastDistance ) / 1000;
        if (dist != 0) {
            double rate = ((fragment.duration - lastDuration) / 60d) / dist;
            tvRate.setText(String.format("%.1f", rate));
        } else {
            tvRate.setText(String.valueOf(0));
        }
    }

	private void processFatigue(int type) {
        if (type != -1) {
            //振动提示
            SystemUtils.vibrate(MyApplication.getInstance(), new long[]{100, 400, 100, 400}, -1);
            String text = "";
            int resId = -1;
            switch(type) {
                case Constant.FATIGUE_SOMEWHAT_HARD:
                    text = context.getString(R.string.fatigue_somewhat_hard);
                    resId = R.raw.fatigue_somewhat_hard;
                    break;
                case Constant.FATIGUE_HARD:
                    text = context.getString(R.string.fatigue_hard);
                    resId = R.raw.fatigue_hard;
                    break;
                case Constant.FATIGUE_VERY_HARD:
                    text = context.getString(R.string.fatigue_very_hard);
                    resId = R.raw.fatigue_very_hard;
                    break;
                case Constant.FATIGUE_VERY_VERY_HARD:
                    text = context.getString(R.string.fatigue_very_very_hard);
                    resId = R.raw.fatigue_very_very_hard;
                    break;
            }
            //弹框提示
            if (!text.isEmpty()) {
                ToastMgr.showTextToast(context, 1, text);
            }
            if (resId != -1 && MyApplication.isVoiceEnable) {
                MediaPlayerMgr.INSTANCE.play(context, resId);
            }
            Dao.INSTANCE.insertOrUpdateAlarm(new Date(), type, ASCloud.userInfo.ID, false);
        }        
	}

    private void processInjure(int type) {
        if (type != -1) {
            //振动提示
            SystemUtils.vibrate(MyApplication.getInstance(), new long[]{100, 400, 100, 400}, -1);
            String text = "";
            int resId = -1;
            switch(type) {
                case Constant.INJURE_LOW:
                    text = context.getString(R.string.injure_low);
                    resId = R.raw.injure_low;
                    break;
                case Constant.INJURE_MIDDLE:
                    text = context.getString(R.string.injure_middle);
                    resId = R.raw.injure_middle;
                    break;
                case Constant.INJURE_HIGH:
                    text = context.getString(R.string.injure_high);
                    resId = R.raw.injure_high;
                    break;
            }
            //弹框提示
            if (!text.isEmpty()) {
                ToastMgr.showTextToast(context, 1, text);
            }
            if (resId != -1 && MyApplication.isVoiceEnable) {
                MediaPlayerMgr.INSTANCE.play(context, resId);
            }
            Dao.INSTANCE.insertOrUpdateAlarm(new Date(), type, ASCloud.userInfo.ID, false);
        }
    }
    
	//将音乐播放View收起来
    private void initMusicView() {
        musicView = new MusicView(musicContainer);
        musicView.initialize();
        ViewGroup.LayoutParams params = musicContainer.getLayoutParams();
        params.height = 0;
        musicContainer.setLayoutParams(params);
    }

    //控制音乐播放View的收起及打开，带动画效果
    private void foldOrUnfoldMusicView() {
        int musicStartHeight = musicViewIsOpened ? measureHeight(musicContainer) : 0;
        int musicEndHeight = musicViewIsOpened ? 0 : measureHeight(musicContainer);
        int valuesStartHeight = musicViewIsOpened ? 0 : measureHeight(llValues);
        int valuesEndHeight = musicViewIsOpened ? measureHeight(llValues) : 0;
        musicViewIsOpened = !musicViewIsOpened;
        //MusicView动画
        AnimatorUtils.addIntAnimatorToView(musicContainer, animatorListener, AnimatorUtils.VERTICAL, 300,
                musicStartHeight, musicEndHeight);
        //TextViews动画
        AnimatorUtils.addIntAnimatorToView(llValues, null, AnimatorUtils.VERTICAL, 300, valuesStartHeight, valuesEndHeight);
    }

    private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animator) {
            if (musicViewIsOpened) {
                btnPushPull.setBackgroundResource(R.drawable.music_up);
            } else {
                btnPushPull.setBackgroundResource(R.drawable.music_down);
            }
        }
        @Override
        public void onAnimationStart(Animator animator) { }
        @Override
        public void onAnimationCancel(Animator animator) { }
        @Override
        public void onAnimationRepeat(Animator animator) { }
    };

    //测量高度
    private int measureHeight(View view) {
        view.measure(0, 0);
        return view.getMeasuredHeight();
    }

    @Override
    public void runOnUiTimerTask() {
        if (!isPaused) {
            fragment.duration++;
            voiceWarner.processVoice(fragment.duration);
            tvDuration.setText(String.format("%02d:%02d", fragment.duration / 60, fragment.duration % 60));
            Activity pathActivity = BaseActivity.getActivity(PathActivity.class.getName());
            if (pathActivity != null) {
                ((PathActivity) pathActivity).updateText();
            }
        }

        timeCount++;
        if (timeCount % 10 == 0) {
            //如果10秒步数不改变，暂停。自动暂停可以自动继续，手动暂停不自动继续
            if (!DeviceMgr.isAllDisconnected()) {
                if (currentRealSteps == lastCacheSteps && !isPaused) {
                    isAutoPause = true;
                    pauseOrGoon(true);
                }
                lastCacheSteps = currentRealSteps;
            }
            //10秒更新配速
            updateRate();
            lastDistance = fragment.distance;
            lastDuration = fragment.duration;
        }
        //1分钟分析损伤
        if (timeCount % 60 == 0) {
            processInjure(injureAnalysis.analysisWithWeekDistance(fragment.distance / 1000));
            processInjure(injureAnalysis.analysisWithDuration(fragment.duration));
            processInjure(injureAnalysis.analysisWithDistance(fragment.distance / 1000));
        }
        
        if (timeCount == 180) {
            timeCount = 0;      
            processFatigue(fatigueAnalysis.analysis(fragment.distance / 1000));
            processInjure(injureAnalysis.analysisWithPace(fragment.distance / 1000));
        }
        
        //自动继续
        if (currentRealSteps != lastCacheSteps && isAutoPause && isPaused) {
            isAutoPause = false;
            pauseOrGoon(false);
        }
    }

    //停止后台线程
    public void destroy() {
        myTimer.stopTimer();
        fragment.stopLocation();
        musicView.destroy();
        State.runState = State.RunState.STOP;
        ObservableMgr.getRunStateObservable().setChanged();
        Activity activity = BaseActivity.getActivity(PathActivity.class.getName());
        if (activity != null) activity.finish();
        ObservableMgr.getBleObservable().deleteObserver(observer);
    }

    private void pauseOrGoon(boolean pause) {
        isPaused = pause;
        if (MyApplication.isVoiceEnable) {
            MediaPlayerMgr.INSTANCE.play(context, isPaused ? R.raw.pause_run : R.raw.goon_run);
        }
        btnPauseStart.setBackgroundResource(isPaused ? R.drawable.goon_run : R.drawable.pause_run);
        State.runState = isPaused ? State.RunState.PAUSE : State.RunState.RUNNING;
        ObservableMgr.getRunStateObservable().setChanged();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_push_pull:
                foldOrUnfoldMusicView();
                break;
            case R.id.btn_pause_start:
                //如果没有全部连接，提示
                if (DeviceMgr.isAllConnected()) {
                    pauseOrGoon(!isPaused);
                    isAutoPause = false;
                    timeCount = 0;
                } else {
                    new AlertDialog.Builder(context).setMessage(R.string.disconn_run_warn)
                            .setNegativeButton(R.string.cancel, null)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pauseOrGoon(!isPaused);
                                    isAutoPause = false;
                                    timeCount = 0;
                                }
                            }).show();
                } 
                break;
            case R.id.btn_stop:   
                //语音
                if (MyApplication.isVoiceEnable) {
                    List<Integer> voices = new LinkedList<>();
                    voices.add(R.raw.run);
                    voices.addAll(resolveNumber((int) (fragment.distance / 10)));
                    voices.add(R.raw.km);
                    voices.add(R.raw.use_time);
                    voices.addAll(resolveDuration(fragment.duration));
                    MediaPlayerMgr.INSTANCE.play(context, voices);
                }
                            
                if (cacheSteps > 0 && fragment.distance > 0) {
					//记录跑步
					runRecord.endTime = new Date();
					runRecord.distance = fragment.distance / 1000;
					runRecord.calorie = AlgLib.calculateCalories(fragment.distance / 1000);
					runRecord.rate = (fragment.duration / 60d) / (fragment.distance / 1000);
					runRecord.steps = cacheSteps;
					runRecord.duration = fragment.duration;
					runRecord.hwDuration = cacheHwDuration;
					Dao.INSTANCE.insertOrUpdatetRunRecord(runRecord, fragment.runLatlngs);
					//保存跑步数据到跑量
                    com.advanpro.fwtools.db.Activity activity = Dao.INSTANCE.queryActivity(new Date());
                    if (activity == null) {
                        activity = new com.advanpro.fwtools.db.Activity();
                        activity.userId = ASCloud.userInfo.ID;
                        activity.date = DateUtils.getStartOfDay(new Date()).getTime();
                    }
                    activity.runningSteps += cacheSteps;
                    activity.runningTime += fragment.duration;
                    activity.runningDistance += fragment.distance / 1000;
                    Dao.INSTANCE.insertOrUpdateRun(activity.date, activity.runningSteps, 
                            activity.runningDistance, activity.runningTime);
                }				
				
				destroy();
                fragment.switchLayout.switchView();
                break;
            case R.id.btn_back_home:
                fragment.switchLayout.switchView();
                break;
        }
    }

    /**
     * 将数字分解成对应语音资源数组的索引
     */
    private List<Integer> resolveNumber(int num) {
        int oNum = num;
        List<Integer> list = new ArrayList<>();
        int count = 0;
        boolean lessOne = num < 100;
        while (num > 0 || lessOne) {
            count++;
            int cell = num % 10;
            num /= 10;

            if (count == 1) {
                if (cell > 0) list.add(numResIds[cell]);
            } else if (count == 2) {
                if ((list.size() > 0 && list.get(0) > 0) || cell > 0) {
                    list.add(numResIds[cell]);
                    list.add(numResIds[11]);
                }
            } else {
                lessOne = false;
                if (count == 3) {
                    if(oNum < 1000 || cell != 0) {
                        if (oNum == 200) list.add(R.raw.liang);
                        else list.add(numResIds[cell]);
                    }
                } else if (count == 4) {
                    if (cell == 1) {
                        list.add(numResIds[10]);
                    } else {
                        list.add(numResIds[10]);
                        list.add(numResIds[cell]);
                    }
                }
            }
        }
        Collections.reverse(list);
        //超出语音范围
        if (oNum >= 10000) {
            list.clear();
            list.add(numResIds[11]);
            list.add(numResIds[11]);
            list.add(numResIds[11]);
        }
        return list;
    }

    /**
     * 分解成时长
     */
    private List<Integer> resolveDuration(int duration) {
        List<Integer> result = new LinkedList<>();
        int hour = duration / 3600;
        if (hour > 0) {
            result.addAll(resolveToInt(hour));
            result.add(R.raw.hour);
        }
        int minute = duration % 3600 / 60;
        if (minute > 0) {
            result.addAll(resolveToInt(minute));
            result.add(R.raw.minute);
        }
        int second = duration % 60;
        if (second > 0) {
            result.addAll(resolveToInt(second));
            result.add(R.raw.second);
        }
        return result;
    }

    /**
     * 将数字分解成对应语音资源数组的索引，不带小数
     */
    private List<Integer> resolveToInt(int num) {
        int oNum = num;
        List<Integer> list = new ArrayList<>();
        int count = 0;
        while (num > 0) {
            count++;
            int cell = num % 10;
            num /= 10;
            if (count == 1) {
                if(oNum < 10 || cell != 0) {
                    if (oNum == 2) list.add(R.raw.liang);
                    else list.add(numResIds[cell]);
                }
            } else if (count == 2) {
                if (cell == 1) {
                    list.add(numResIds[10]);
                } else {
                    list.add(numResIds[10]);
                    list.add(numResIds[cell]);
                }
            }
        }
        Collections.reverse(list);
        return list;
    }
}
