package com.advanpro.fwtools.module.stat;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DEV002 on 2016/3/31.
 */
public class ActivityMonthAdapter extends BasePager implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener {
    private final int date = 7;//每周7天
    private BrokenLineView brokenLine;//折线图
    private BrokenLinePillar brokenLinePillar;//柱状图
    private RadioGroup activity_week_group;
    private TextView spinner, time, state_line, state_pailler;
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

    private String checkedStr = DateUtils.formatDate(new Date(), "yyyy/MM");

    public ActivityMonthAdapter(Context context) {
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

        timeStr = new ArrayList<>();
        //初始化时间数据
        String[] vstr = context.getResources().getStringArray(R.array.year);
        for (String s : vstr) {
            timeStr.add(s);
        }
        timeListAdapter = new TimeListAdapter(timeStr);
    }


    //根据当天时间来获取本周的运动数据
    private void initValue(String time) {
        //默认初始数据为当前月份第一天
        if (time == null) {
            setData(DateUtils.getStartOfDay(DateUtils.getEveryMonthFistDay(DateUtils.formatDate(new Date(), "yyyy"), DateUtils.formatDate(new Date(), "MM"))).getTime());
        } else {
            //解析time “2016/4”
            setData(DateUtils.getStartOfDay(DateUtils.parseStringDate(time, "yyyy/MM")).getTime());
        }
    }

    //设置数据
    public void setData(Date times) {
        Date start = DateUtils.getStartOfDay(DateUtils.getEveryMonthFistDay(DateUtils.formatDate(times, "yyyy"), DateUtils.formatDate(times, "MM"))).getTime();
        String t=DateUtils.formatDate(start,"yyyy-MM-dd");
        Date stop = DateUtils.getStartOfDay(DateUtils.getDay(start, 6)).getTime();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapr = new HashMap<>();

        Map<String, Object> mappillerw = new HashMap<>();
        Map<String, Object> mappillerr = new HashMap<>();
        int countWalksteps = 0;
        double countWalkDistance = 0;
        int countRunsteps = 0;
        double countRunDistance = 0;

        if (times.equals(DateUtils.getStartOfDay(DateUtils.parseStringDate(DateUtils.formatDate(new Date(), "yyyy/MM"), "yyyy/MM")).getTime())
                || times == (DateUtils.getStartOfDay(DateUtils.parseStringDate(DateUtils.formatDate(new Date(), "yyyy/MM"), "yyyy/MM")).getTime())) {
            String time = DateUtils.formatDate(new Date(), "dd");//查询当前时间
            int timevalue = Integer.parseInt(time) / date;
            int timeyu = Integer.parseInt(time) % date;//判断当天是在本月的第几周

            if (timevalue > 0) {
                if (timeyu > 0) {
                    timevalue += 1;
                }
                for (int i = 1; i <= timevalue; i++) {
                    List<Activity> l = Dao.INSTANCE.queryActivities(start, stop);//查询周数据
                    for (Activity a : l) {//循环叠加数据
                        countWalksteps += a.getWalkSteps();
                        countWalkDistance += a.getWalkDistance();
                        countRunsteps += a.getRunningSteps();
                        countRunDistance += a.getRunningDistance();
                    }
                    if (countWalksteps > 0) {
                        firstWalkLine.add(countWalksteps);
                        map.put("" + i, countWalksteps);
                    } else {
                        firstWalkLine.add(0);
                        map.put("" + i, 0);
                    }
                    if (countWalkDistance > 0.0000) {
                        firstRunLine.add(countRunsteps);
                        mappillerw.put("" + i, countWalkDistance);
                    } else {
                        firstRunLine.add(0);
                        mappillerw.put("" + i, 0);
                    }
                    if (countRunsteps > 0) {
                        firstWalkPillar.add(new DecimalFormat("0.00").format(countWalkDistance));
                        mapr.put("" + i, countRunsteps);
                    } else {
                        firstWalkPillar.add(0 + "");
                        mapr.put("" + i, 0);
                    }
                    if (countRunDistance > 0.0000) {
                        firstRunPillar.add(new DecimalFormat("0.00").format(countRunDistance));
                        mappillerr.put("" + i, countRunDistance);
                    } else {
                        firstRunPillar.add(0 + "");
                        mappillerr.put("" + i, 0);
                    }

                    if (i == timevalue) {//查询第一天到当前天数的数据
                        start = DateUtils.getDay(start, 1);
                        stop = DateUtils.getStartOfDay(new Date()).getTime();
                    } else {
                        start = DateUtils.getDay(stop, 1);
                        stop = DateUtils.getDay(stop, 7);
                    }

                    //清空值
                    countWalksteps = 0;
                    countWalkDistance = 0;
                    countRunsteps = 0;
                    countRunDistance = 0;
                }
            } else {
                stop = DateUtils.getStartOfDay(new Date()).getTime();//查询第一天到当前的数据
                List<Activity> l = Dao.INSTANCE.queryActivities(start, stop);//查询周数据
                for (Activity a : l) {//循环叠加数据
                    countWalksteps += a.getWalkSteps();
                    countWalkDistance += a.getWalkDistance();
                    countRunsteps += a.getRunningSteps();
                    countRunDistance += a.getRunningDistance();
                }
                if (countWalksteps > 0) {
                    firstWalkLine.add(countWalksteps);
                    map.put("" + 1, countWalksteps);
                } else {
                    firstWalkLine.add(0);
                    map.put("" + 1, 0);
                }
                if (countWalkDistance > 0.0000) {
                    firstWalkPillar.add(new DecimalFormat("0.00").format(countWalkDistance));
                    mappillerw.put("" + 1, countWalkDistance);
                } else {
                    firstWalkPillar.add(0+"");
                    mappillerw.put("" + 1, 0);
                }
                if (countRunsteps > 0) {
                    firstRunLine.add(countRunsteps);
                    mapr.put("" + 1, countRunsteps);
                } else {
                    firstRunLine.add(0);
                    mapr.put("" + 1, 0);
                }
                if (countRunDistance > 0.0000) {
                    firstRunPillar.add(new DecimalFormat("0.00").format(countRunDistance));
                    mappillerr.put("" + 1, countRunDistance);
                } else {
                    firstRunPillar.add(0+"");
                    mappillerr.put("" + 1, 0);
                }
                countWalksteps = 0;
                countWalkDistance = 0;
                countRunsteps = 0;
                countRunDistance = 0;
            }

        } else {
            int count = DateUtils.getEveryMonthCountTime(DateUtils.formatDate(times, "yyyy/MM").split("/")[0], DateUtils.formatDate(times, "yyyy/MM").split("/")[1]);
            int timevalue = count / date;
            int timeyu = count % date;
            if (timeyu > 0) {
                timevalue += 1;
            }
            for (int i = 1; i <= timevalue; i++) {
                String t1=DateUtils.formatDate(start,"yyyy-MM-dd");
                String t2=DateUtils.formatDate(stop,"yyyy-MM-dd");
                List<Activity> l = Dao.INSTANCE.queryActivities(start, stop);
                    for (Activity a : l) {
                        countWalksteps += a.getWalkSteps();
                        countWalkDistance += a.getWalkDistance();
                        countRunsteps += a.getRunningSteps();
                        countRunDistance += a.getRunningDistance();
                    }
                    if (countWalksteps > 0) {
                        firstWalkLine.add(countWalksteps);
                        map.put("" + i, countWalksteps);
                    } else {
                        firstWalkLine.add(0);
                        map.put("" + i, 0);
                    }
                    if (countWalkDistance > 0.0000) {
                        firstWalkPillar.add(new DecimalFormat("0.00").format(countWalkDistance));
                        mappillerw.put("" + i, countWalkDistance);
                    } else {
                        firstWalkPillar.add(0+"");
                        mappillerw.put("" + i, 0);
                    }
                    if (countRunsteps > 0) {
                        firstRunLine.add(countRunsteps);
                        mapr.put("" + i, countRunsteps);
                    } else {
                        firstRunLine.add(0);
                        mapr.put("" + i, 0);
                    }
                    if (countRunDistance > 0.0000) {
                        firstRunPillar.add(new DecimalFormat("0.00").format(countRunDistance));
                        mappillerr.put("" + i, countRunDistance);
                    } else {
                        firstRunPillar.add(0+"");
                        mappillerr.put("" + i, 0);
                    }
//                    if (i == timevalue) {
//                        if (timeyu > 0) {
//                            start = DateUtils.getDay(start, 1);
//                            stop = DateUtils.getStartOfDay(DateUtils.getDay(start, timeyu)).getTime();
//                        }
//                    } else {
//                        start = DateUtils.getDay(start, 1);
//                        stop = DateUtils.getDay(start, 6);
//                    }
                if (i == timevalue) {//查询第一天到当前天数的数据
                    start = DateUtils.getDay(start, 1);
                    stop = DateUtils.getStartOfDay(new Date()).getTime();
                } else {
                    start = DateUtils.getDay(stop, 1);
                    stop = DateUtils.getDay(stop, 7);
                }
                    //清空值
                    countWalksteps = 0;
                    countWalkDistance = 0;
                    countRunsteps = 0;
                    countRunDistance = 0;
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
            int v = (int) (Double.parseDouble(l.get(l.size() - 1) + ""));
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
        firstRunPillar.clear();
        firstWalkPillar.clear();
    }


    @Override
    protected void beforeInitViews() {
        Log.i("TAG","month --->before");
        v = LayoutInflater.from(context).inflate(R.layout.list_time_view, null);
        timeLv = (ListView) v.findViewById(R.id.timeLv);
        spinner = (TextView) rootView.findViewById(R.id.spinner);
        spinner.setText(checkedStr);
        state_line = (TextView) rootView.findViewById(R.id.state_line);
        state_pailler = (TextView) rootView.findViewById(R.id.state_pailler);
        activity_week_group = (RadioGroup) rootView.findViewById(R.id.activity_week_group);
        brokenLine = (BrokenLineView) rootView.findViewById(R.id.brokenLine);
        int count = DateUtils.getEveryMonthCountTime(DateUtils.formatDate(new Date(), "yyyy"), DateUtils.formatDate(new Date(), "MM"));
        List<String> l = new ArrayList<>();
        if (count > 0) {
            if (count % 7 != 0) {
                //该月有五周
                for (int i = 1; i <= 5; i++) {
                    l.add("第" + i + "周");
                }
            } else {
                //该月有四周
                for (int i = 1; i <= 4; i++) {
                    l.add("第" + i + 1 + "周");
                }
            }
        }
        timeLv.setAdapter(timeListAdapter);
        timeLv.setOnItemClickListener(this);
        brokenLine.setMonthLable(l);
        brokenLinePillar = (BrokenLinePillar) rootView.findViewById(R.id.brokerLine1);
        brokenLinePillar.setMonthLable(l);
        initValue(checkedStr);
        brokenLine.setYLable(ylables);
        brokenLinePillar.setYlabe(ylablesRun);
        createDialog();
    }


    @Override
    protected void initViews() {
        state_line.setText("每周步行步数统计");
        state_pailler.setText("每周步行总里程统计");
        spinner.setOnClickListener(this);
        activity_week_group.setOnCheckedChangeListener(this);

    }

    @Override
    protected View getRootView() {
        return LayoutInflater.from(context).inflate(R.layout.activity_month_view, null);
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
            state_line.setText("每周步行步数统计");
            state_pailler.setText("每周步行总里程统计");
        } else if (checkedId == R.id.group_eight) {
            RadioButton b = (RadioButton) rootView.findViewById(R.id.group_eight);
            b.setBackgroundResource(R.drawable.checked_right);
            walkOrrunning = false;
            initValue(checkedStr);
            state_line.setText("每周跑步步数统计");
            state_pailler.setText("每周跑步总里程统计");
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (timeListAdapter.getItem(position) != null) {
            checkedStr = DateUtils.formatDate(new Date(), "yyyy/") + timeListAdapter.getItem(position).toString().replace("月", "");
            hideDialog();
            spinner.setText(checkedStr);
            //点击不同月份切换数据
            timeListAdapter.getItem(position).toString().replace("月", "");
            initValue(DateUtils.formatDate(new Date(), "yyyy/") + timeListAdapter.getItem(position).toString().replace("月", ""));

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
                    time = (TextView) getConvertView().findViewById(R.id.time);
                    time.setText(value);

                }

                @Override
                protected View createConvertView() {
                    return View.inflate(context, R.layout.month_time_view_item, null);
                }
            };
        }
    }
}
