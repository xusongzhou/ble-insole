package com.advanpro.fwtools.module.stat;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseFragment;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.StringUtils;
import com.advanpro.fwtools.db.Activity;
import com.advanpro.fwtools.db.Alarm;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.entity.GaitSummary;
import com.advanpro.fwtools.module.MainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zengfs on 2016/1/14.
 * 统计主界面
 */
public class StatFragment extends BaseFragment<MainActivity> implements View.OnClickListener {
    private TextView activityText, gaitText, poiseText, tiredText, today_gait, today_killometre, pose_stat, gait_stat, warning;
    private LinearLayout linearActivity, linearGait, linearPose, linearWarning;


    @Override
    protected View getRootView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.frag_stat, container, false);
    }

    @Override
    protected void assignViews() {
        activityText = (TextView) rootView.findViewById(R.id.activity_stat);
        gaitText = (TextView) rootView.findViewById(R.id.gait_state);
        poiseText = (TextView) rootView.findViewById(R.id.poise_stat);
        tiredText = (TextView) rootView.findViewById(R.id.tired_stat);
        linearActivity = (LinearLayout) rootView.findViewById(R.id.linearActivity);
        linearGait = (LinearLayout) rootView.findViewById(R.id.linearGait);
        linearPose = (LinearLayout) rootView.findViewById(R.id.linearPose);
        linearWarning = (LinearLayout) rootView.findViewById(R.id.linearWarning);
        today_gait = (TextView) rootView.findViewById(R.id.today_gait);
        today_killometre = (TextView) rootView.findViewById(R.id.today_kilometre);
        pose_stat = (TextView) rootView.findViewById(R.id.pose_stat);
        gait_stat = (TextView) rootView.findViewById(R.id.gait_stat);
        warning = (TextView) rootView.findViewById(R.id.warning);
    }

    @Override
    protected void initViews() {
        linearActivity.setOnClickListener(this);
        linearGait.setOnClickListener(this);
        linearPose.setOnClickListener(this);
        linearWarning.setOnClickListener(this);
        activityText.setOnClickListener(this);
        gaitText.setOnClickListener(this);
        poiseText.setOnClickListener(this);
        tiredText.setOnClickListener(this);
        loadData();//装载数据
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }


    private void loadData() {
        Date v = DateUtils.getStartOfDay(DateUtils.parseStringDate(DateUtils.formatDate(new Date(), "yyyy/MM/dd"), "yyyy/MM/dd")).getTime();
        Activity a = Dao.INSTANCE.queryActivity(v);
        GaitSummary g = Dao.INSTANCE.queryGaitSummary(v, true);
        GaitSummary gr = Dao.INSTANCE.queryGaitSummary(v, false);
        List<Alarm> alarm = Dao.INSTANCE.queryAlarms(v);
        List<Integer> ls = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        int a1 = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g1 = 0;
        if (alarm.size() > 0) {
            for (Alarm al : alarm) {
                switch (al.getType()) {
                    case Constant.FATIGUE_SOMEWHAT_HARD:
                        a1++;
                        break;
                    case Constant.FATIGUE_HARD:
                        b++;
                        break;
                    case Constant.FATIGUE_VERY_HARD:
                        c++;
                        break;
                    case Constant.FATIGUE_VERY_VERY_HARD:
                        d++;
                        break;
                    case Constant.INJURE_LOW:
                        e++;
                        break;
                    case Constant.INJURE_MIDDLE:
                        f++;
                        break;
                    case Constant.INJURE_HIGH:
                        g1++;
                        break;
                }
            }
            ls.add(a1);
            ls.add(b);
            ls.add(c);
            ls.add(d);
            ls.add(e);
            ls.add(f);
            ls.add(g1);

            map.put("FATIGUE_SOMEWHAT_HARD", a1);
            map.put("FATIGUE_HARD", b);
            map.put("FATIGUE_VERY_HARD", c);
            map.put("FATIGUE_VERY_VERY_HARD", d);
            map.put("INJURE_LOW", e);
            map.put("INJURE_MIDDLE", f);
            map.put("INJURE_HIGH", g1);

            Collections.sort(ls);
            if (map.containsValue(ls.get(ls.size() - 1))) {
                Set<String> set = map.keySet();
                for (String str : set) {
                    if (ls.get(ls.size() - 1).equals(map.get(str))) {
                        if (str.equals("FATIGUE_SOMEWHAT_HARD")) {
                            warning.setText(R.string.fatigue_somewhat_hard);
                        } else if (str.equals("FATIGUE_HARD")) {
                            warning.setText(R.string.fatigue_hard);
                        } else if (str.equals("FATIGUE_VERY_HARD")) {
                            warning.setText(R.string.fatigue_very_hard);
                        } else if (str.equals("FATIGUE_VERY_VERY_HARD")) {
                            warning.setText(R.string.fatigue_very_very_hard);
                        } else if (str.equals("INJURE_LOW")) {
                            warning.setText(R.string.injure_low);
                        } else if (str.equals("INJURE_MIDDLE")) {
                            warning.setText(R.string.injure_middle);
                        } else if (str.equals("INJURE_HIGH")) {
                            warning.setText(R.string.injure_high);
                        } else {
                            warning.setText("");
                        }
                    }
                }
            }
        }
        if (a != null) {
            today_gait.setText(a.getWalkSteps() + "");
            //DecimalFormat format = new DecimalFormat("0.00");
            //today_killometre.setText(format.format(a.getRunningDistance()) + "");
            today_killometre.setText(StringUtils.formatDecimal(a.getRunningDistance(),2));
        }
        Activity ac = Dao.INSTANCE.queryActivity(new Date());
        if (ac != null) {
            List<Integer> l = new ArrayList<>();
            l.add(ac.runningTime);
            l.add(ac.walkTime);
            l.add(ac.sittingTime);
            l.add(ac.standTime);
            Collections.sort(l);
            if (ac.runningTime == l.get(l.size() - 1)) {
                pose_stat.setText("跑步");
            } else if (ac.walkTime == l.get(l.size() - 1)) {
                pose_stat.setText("走路");
            } else if (ac.standTime == l.get(l.size() - 1)) {
                pose_stat.setText("站立");
            } else {
                pose_stat.setText("平坐");
            }
        }
        if (g != null && gr != null) {
            List<Long> l = new ArrayList<>();
            l.add(g.ectropion + gr.ectropion);
            l.add(g.forefoot + gr.forefoot);
            l.add(g.heel + gr.heel);
            l.add(g.sole + gr.sole);
            l.add(g.varus + gr.varus);
            Collections.sort(l);
            if (g.ectropion + gr.ectropion == l.get(l.size() - 1)) {
                gait_stat.setText("外翻");
            } else if (g.forefoot + gr.forefoot == l.get(l.size() - 1)) {
                gait_stat.setText("脚掌");
            } else if (g.heel + gr.heel == l.get(l.size() - 1)) {
                gait_stat.setText("脚跟");
            } else if (g.sole + gr.sole == l.get(l.size() - 1)) {
                gait_stat.setText("全脚");
            } else {
                gait_stat.setText("内翻");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_stat:
                //活动统计
                Intent activity = new Intent(getActivity(), ActivityStatActivity.class);
                startActivity(activity);
                break;
            case R.id.gait_state:
                //步态统计
                Intent gait = new Intent(getActivity(), GaitStatActivity.class);
                startActivity(gait);
                break;
            case R.id.poise_stat:
                //姿态统计
                Intent pose = new Intent(getActivity(), PoseStatActivity.class);
                startActivity(pose);
                break;
            case R.id.tired_stat:
                //疲劳统计
                Intent tired = new Intent(getActivity(), FatigueWarningActivity.class);
                startActivity(tired);
                break;
            case R.id.linearActivity:
                Intent la = new Intent(getActivity(), ActivityStatActivity.class);
                startActivity(la);
                break;
            case R.id.linearGait:
                Intent lg = new Intent(getActivity(), GaitStatActivity.class);
                startActivity(lg);
                break;
            case R.id.linearPose:
                Intent lp = new Intent(getActivity(), PoseStatActivity.class);
                startActivity(lp);
                break;
            case R.id.linearWarning:
                Intent lw = new Intent(getActivity(), FatigueWarningActivity.class);
                startActivity(lw);
                break;
        }
    }
}
