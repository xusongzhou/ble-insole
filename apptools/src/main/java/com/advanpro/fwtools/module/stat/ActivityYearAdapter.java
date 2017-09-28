package com.advanpro.fwtools.module.stat;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.db.Activity;
import com.advanpro.fwtools.db.Dao;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DEV002 on 2016/3/31.
 */
public class ActivityYearAdapter extends BasePager implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener {
    private BrokenLineView brokenLine;
    private BrokenLinePillar brokenLinePillar;
    private RadioGroup activity_week_group;
    private TextView spinner, startTime, state_line, state_pailler;
    private Dialog dialog;
    private View v;
    private ListView timeLv;
    private boolean walkOrrunning;

    private List<String> timeStr;//时间数据


    private List<String> ylables;
    private List<String> ylablesRun;

    private List<Integer> firstWalkLine;
    private List<Integer> firstRunLine;
    private List<String> firstWalkPillar;
    private List<String> firstRunPillar;
    private TimeListAdapter timeListAdapter;

    private String checkedStr=DateUtils.formatDate(new Date(), "yyyy");

    public ActivityYearAdapter(Context context) {
        super(context);
    }

    @Override
    protected void assignViews() {
        walkOrrunning = true;
        ylables = new ArrayList<>();
        ylablesRun = new ArrayList<>();

        firstWalkLine = new ArrayList<>();
        firstRunLine = new ArrayList<>();
        firstRunPillar = new ArrayList<>();
        firstWalkPillar = new ArrayList<>();

        //初始化
        timeStr = new ArrayList<>();
        //初始化时间数据
        String[] vstr = context.getResources().getStringArray(R.array.activity_year);
        for (String s : vstr) {
            timeStr.add(s);
        }
        timeListAdapter = new TimeListAdapter(timeStr);
    }


    //获取今年的所有运动数据
    private void initValue(String time) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapr = new HashMap<>();

        Map<String, Object> mappillerw = new HashMap<>();
        Map<String, Object> mappillerr = new HashMap<>();

        int countWalk = 0;//总步数
        int countRunning = 0;//总跑步数
        double countWalks = 0;//总步行公里
        double countRunnings = 0;//总跑步公里
        if (time == null) {
            //获取每个月的第一天和最后一天
            for (int i = 1; i < 13; i++) {
                //Date start = DateUtils.getStartOfDay(DateUtils.getEveryMonthFistDay("2016", "" + i)).getTime();
                //Date stop = DateUtils.getStartOfDay(DateUtils.getEveryMonthLastDay("2016", "" + i)).getTime();
                Date start=getTime(false,Integer.parseInt(DateUtils.formatDate(new Date(),"yyyy")),i).getTime();
                Date stop=getTime(true,Integer.parseInt(DateUtils.formatDate(new Date(),"yyyy")),i).getTime();
                //查询每个月运动数据
                List<Activity> ls = Dao.INSTANCE.queryActivities(start, stop);
                if (ls.size() > 0) {
                    for (Activity a : ls) {
                        countWalk += a.getWalkSteps();//叠加每个月的步数
                        countRunning += a.getRunningSteps();//叠加每个月的跑步数
                        countWalks += a.getWalkDistance();//叠加每个月的步行公里数
                        countRunnings += a.getRunningDistance();//叠加每个月的跑步公里数
                    }
                    if (countWalk > 0) {
                        firstWalkLine.add(countWalk);
                        map.put(i + "", countWalk);
                    } else {
                        firstWalkLine.add(0);
                        map.put(i + "", 0);
                    }
                    if (countRunning > 0) {
                        firstRunLine.add(countRunning);
                        mapr.put(i + "", countRunning);
                    } else {
                        firstRunLine.add(0);
                        mapr.put(i + "", 0);

                    }
                    if (countWalks > 0.0000) {
                        firstWalkPillar.add(new DecimalFormat("0.00").format(countWalks));
                        mappillerw.put(i + "", countWalks);
                    } else {
                        firstWalkPillar.add(0 + "");
                        mappillerw.put(i + "", 0);

                    }
                    if (countRunnings > 0.0000) {
                        firstRunPillar.add(new DecimalFormat("0.00").format(countRunnings));
                        mappillerr.put(i + "", countRunnings);
                    } else {
                        firstRunPillar.add(0 + "");
                        mappillerr.put(i + "", 0);
                    }
                    countWalk = 0;
                    countRunning = 0;
                    countWalks = 0;
                    countRunnings = 0;
                } else {
                    firstWalkLine.add(0);
                    firstRunLine.add(0);
                    firstWalkPillar.add(0 + "");
                    firstRunPillar.add(0 + "");
                    map.put("" + i, 0);
                    mappillerw.put("" + i, 0);
                    mapr.put("" + i, 0);
                    mappillerr.put("" + i, 0);
                }
            }
        } else {
            //获取每个月的第一天和最后一天
            for (int i = 1; i < 13; i++) {
//                Date start = DateUtils.getEveryMonthFistDay(time.split("年")[0], "" + i);
//                Date stop = DateUtils.getEveryMonthLastDay(time.split("年")[0], "" + i);
                Date start=getTime(false,Integer.parseInt(time.split("年")[0]),i).getTime();
                Date stop=getTime(true,Integer.parseInt(time.split("年")[0]),i).getTime();
                //查询每个月运动数据
                List<Activity> ls = Dao.INSTANCE.queryActivities(start, stop);
                if (ls.size() > 0) {
                    for (Activity a : ls) {
                        countWalk += a.getWalkSteps();//叠加每个月的步数
                        countRunning += a.getRunningSteps();//叠加每个月的跑步数
                        countWalks += a.getWalkDistance();//叠加每个月的步行公里数
                        countRunnings += a.getRunningDistance();//叠加每个月的跑步公里数
                    }
                    if (countWalk > 0) {
                        firstWalkLine.add(countWalk);
                        map.put(i + "", countWalk);
                    } else {
                        firstWalkLine.add(0);
                        map.put(i + "", 0);
                    }
                    if (countRunning > 0) {
                        firstRunLine.add(countRunning);
                        mapr.put(i + "", countRunning);
                    } else {
                        firstRunLine.add(0);
                        mapr.put(i + "", 0);
                    }
                    if (countWalks > 0.0000) {
                        firstWalkPillar.add(new DecimalFormat("0.00").format(countWalks));
                        mappillerw.put(i + "", countWalks);
                    } else {
                        firstWalkPillar.add(0 + "");
                        mappillerw.put(i + "", 0);

                    }
                    if (countRunnings > 0.0000) {
                        firstRunPillar.add(new DecimalFormat("0.00").format(countRunnings));
                        mappillerr.put(i + "", countRunnings);
                    } else {
                        firstRunPillar.add(0 + "");
                        mappillerr.put(i + "", 0);
                    }
                    countWalk = 0;
                    countRunning = 0;
                    countWalks = 0;
                    countRunnings = 0;
                } else {
                    map.put("" + i, 0);
                    mappillerw.put("" + i, 0);
                    mapr.put("" + i, 0);
                    mappillerr.put("" + i, 0);
                }
            }
        }
        //设置参数
        if (walkOrrunning) {
            setBrokenData(map, mappillerw, walkOrrunning);
        } else {
            setBrokenData(mapr, mappillerr, walkOrrunning);
        }

        Collections.sort(firstRunLine);
        Collections.sort(firstRunPillar);
        Collections.sort(firstWalkLine);
        Collections.sort(firstWalkPillar);

        List<Integer> l = new ArrayList<>();
        List<Double> ls = new ArrayList<>();
        if (firstRunLine.size() > 0) {
            l.add(Integer.parseInt(firstRunLine.get(firstRunLine.size() - 1) + ""));
        }
        if (firstWalkLine.size() > 0) {
            l.add(Integer.parseInt(firstWalkLine.get(firstWalkLine.size() - 1) + ""));
        }

        Collections.sort(l);
        if (l.size() > 0) {
            int v = l.get(l.size() - 1);
            ylables.clear();
            if (v > 0) {
                if(v<=60){
                    String[] str = context.getResources().getStringArray(R.array.ylable_week);
                    ylables.clear();
                    for (String s : str) {
                        ylables.add(s);
                    }
                }else{
                    for (int i = 0; i < 7; i++) {
                        if (i == 0) {
                            ylables.add("0");
                        } else if (i == 6) {
                            ylables.add(v + "");
                        } else {
                            ylables.add(((v / 6) * i) + "");
                        }
                    }
                }
            } else {
                String[] str = context.getResources().getStringArray(R.array.ylable_week);
                ylables.clear();
                for (String s : str) {
                    ylables.add(s);
                }
            }
        } else {
            String[] str = context.getResources().getStringArray(R.array.ylable_week);
            ylables.clear();
            for (String s : str) {
                ylables.add(s);
            }
        }
        l.clear();
        if (firstRunPillar.size() > 0) {
            ls.add(Double.parseDouble(firstRunPillar.get(firstRunPillar.size() - 1)));
        }
        if (firstWalkPillar.size() > 0) {
            ls.add(Double.parseDouble(firstWalkPillar.get(firstWalkPillar.size() - 1)));
        }
        Collections.sort(ls);
        if (ls.size() > 0) {
            double v = ls.get(ls.size() - 1);
            ylablesRun.clear();
            if (v > 0) {
                if(v<=0.12){
                    String[] str = context.getResources().getStringArray(R.array.ylable_km);
                    ylablesRun.clear();
                    for (String s : str) {
                        ylablesRun.add(s);
                    }
                }else{
                    for (int i = 0; i < 7; i++) {
                        if (i == 0) {
                            ylablesRun.add(0 + "");
                        } else if (i == 6) {
                            ylablesRun.add(v + "");
                        } else {
                            ylablesRun.add(new DecimalFormat("0.00").format(((v / (double) 6)) * i));
                        }
                    }
                }

            } else {
                String[] str = context.getResources().getStringArray(R.array.ylable_km);
                ylablesRun.clear();
                for (String s : str) {
                    ylablesRun.add(s);
                }
            }
        } else {
            String[] str = context.getResources().getStringArray(R.array.ylable_km);
            ylablesRun.clear();
            for (String s : str) {
                ylablesRun.add(s);
            }
        }
        ls.clear();
        firstRunLine.clear();
        firstWalkLine.clear();
        firstWalkPillar.clear();
        firstRunPillar.clear();
    }


    private Calendar getTime(boolean flag,int year,int month){
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH,1);
        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.YEAR, year);
        if(flag)calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar;
    }


    @Override
    protected void initViews() {
        v = LayoutInflater.from(context).inflate(R.layout.list_time_view, null);
        timeLv = (ListView) v.findViewById(R.id.timeLv);
        brokenLine = (BrokenLineView) rootView.findViewById(R.id.brokenLine);
        brokenLinePillar = (BrokenLinePillar) rootView.findViewById(R.id.brokerLine1);
        spinner = (TextView) rootView.findViewById(R.id.spinner);
        spinner.setText(checkedStr);
        state_line = (TextView) rootView.findViewById(R.id.state_line);
        state_pailler = (TextView) rootView.findViewById(R.id.state_pailler);
        state_line.setText("每月步行步数统计");
        state_pailler.setText("每月步行总里程统计");
        activity_week_group = (RadioGroup) rootView.findViewById(R.id.activity_week_group);
        initValue(checkedStr);
        brokenLine.setYLable(ylables);
        brokenLinePillar.setYlabe(ylablesRun);
        timeLv.setAdapter(timeListAdapter);
        timeLv.setOnItemClickListener(this);
        spinner.setOnClickListener(this);
        activity_week_group.setOnCheckedChangeListener(this);
        createDialog();
    }

    @Override
    protected View getRootView() {
        return LayoutInflater.from(context).inflate(R.layout.activity_year_view, null);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.spinner) {
            showDialog();
        }
    }

    //弹出对话框，显示时间数据
    private void createDialog() {
        dialog = new Dialog(context, R.style.DialogStyle);
        dialog.setContentView(v);
    }

    //打开对话框
    private void showDialog() {
        dialog.show();
    }

    //关闭对话框
    private void hideDialog() {
        dialog.dismiss();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.group_left) {
            RadioButton b = (RadioButton) rootView.findViewById(R.id.group_left);
            b.setBackgroundResource(R.drawable.checked);
            walkOrrunning = true;
            initValue(checkedStr);
            state_line.setText("每月步行步数统计");
            state_pailler.setText("每月步行总里程统计");
        } else if (checkedId == R.id.group_eight) {
            RadioButton b = (RadioButton) rootView.findViewById(R.id.group_eight);
            b.setBackgroundResource(R.drawable.checked_right);
            walkOrrunning = false;
            initValue(checkedStr);
            state_line.setText("每月跑步步数统计");
            state_pailler.setText("每月跑步总里程统计");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (timeListAdapter.getItem(position) != null) {
            checkedStr=timeListAdapter.getItem(position);
            spinner.setText(checkedStr);
            hideDialog();
            initValue(checkedStr);
        }
    }


    //重新设置图表数据
    private void setBrokenData(Map<String, Object> line, Map<String, Object> piller, boolean walkOrrunning) {
        if (walkOrrunning) {
            brokenLine.setMapData(line);
            brokenLine.setActivity(walkOrrunning);
            brokenLinePillar.setMapData(piller);
            brokenLinePillar.setActivity(walkOrrunning);
        } else {
            brokenLine.setMapData(line);
            brokenLine.setActivity(walkOrrunning);
            brokenLinePillar.setMapData(piller);
            brokenLinePillar.setActivity(walkOrrunning);
        }
    }

    public class TimeListAdapter extends BaseListAdapter<String> {

        public TimeListAdapter(List<String> lists) {
            super(lists);
        }

        @Override
        protected BaseHolder getHolder() {
            return new BaseHolder() {
                @Override
                protected void setData(Object data, int position) {
                    String value = (String) data;
                    startTime = (TextView) getConvertView().findViewById(R.id.startTime);
                    startTime.setText(value);
                }

                @Override
                protected View createConvertView() {
                    return View.inflate(context, R.layout.year_time_view, null);
                }
            };
        }
    }
}
