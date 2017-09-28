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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DEV002 on 2016/3/30.
 */
public class ActivityWeekAdapter extends BasePager implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener {

    private BrokenLineView brokenLine;
    private BrokenLinePillar brokenLinePillar;
    private RadioGroup activity_week_group;
    private TextView spinner, startTime, stopTime, state_line, state_pailler;
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

    private String checkedStr=null;

    public ActivityWeekAdapter(Context context) {
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
        Date start;
        Date stop = DateUtils.initDate();
        for (int i = 0; i < 53; i++) {
            if (i == 0) {
                start = getD(DateUtils.initDate());
                stop = DateUtils.getDay(start, 6);
            } else {
                start = DateUtils.getDay(stop, 1);
                stop = DateUtils.getDay(start, 6);
            }
            timeStr.add((new SimpleDateFormat("yyyy/MM/dd").format(start) + "~~" + new SimpleDateFormat("yyyy/MM/dd").format(stop)));
        }

    }

    public static Date getD(Date date) {
        switch (DateUtils.getDayOfWeek(date)) {
            case 1:
                date = DateUtils.getDay(date, 6);
                break;
            case 2:
                break;
            case 3:
                date = DateUtils.getDay(date, -1);

                break;
            case 4:
                date = DateUtils.getDay(date, -2);

                break;
            case 5:
                date = DateUtils.getDay(date, -3);

                break;
            case 6:
                date = DateUtils.getDay(date, -4);

                break;
            case 7:
                date = DateUtils.getDay(date, -5);
                break;

            default:
                break;
        }
        return date;
    }

    //根据当天时间来获取本周的运动数据
    private void initValue() {
        Date startTime;
        Date stopTime;
        String date = DateUtils.getDayOfWeek(new Date(), DateUtils.WeekFormat.CN);
        if (DateUtils.Week.星期一.equals(date) || date.contains("" + DateUtils.Week.星期一)) {
            startTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), 0);
            stopTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), 6);
        } else if (DateUtils.Week.星期二.equals(date) || date.contains("" + DateUtils.Week.星期二)) {
            startTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -1);
            stopTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), 5);
        } else if (DateUtils.Week.星期三.equals(date) || date.contains("" + DateUtils.Week.星期三)) {
            startTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -2);
            stopTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), 4);
        } else if (DateUtils.Week.星期四.equals(date) || date.contains("" + DateUtils.Week.星期四)) {
            startTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -3);
            stopTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), 3);
        } else if (DateUtils.Week.星期五.equals(date) || date.contains("" + DateUtils.Week.星期五)) {
            startTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -4);
            stopTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), 2);
        } else if (DateUtils.Week.星期六.equals(date) || date.contains("" + DateUtils.Week.星期六)) {
            startTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -5);
            stopTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), 1);
        } else {
            startTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -6);
            stopTime = DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), 0);
        }
        checkedStr=DateUtils.formatDate(startTime, "yyyy/MM/dd") + "~~" + DateUtils.formatDate(stopTime, "yyyy/MM/dd");
        spinner.setText(checkedStr);
        setData(startTime);
    }


    //设置数据
    public void setData(Date start) {
        Date time;
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapr = new HashMap<>();

        Map<String, Object> mappillerw = new HashMap<>();
        Map<String, Object> mappillerr = new HashMap<>();
        time = start;
        //查询周一到周日的数据
        for (int i = 1; i < 8; i++) {
            Activity l = Dao.INSTANCE.queryActivity(time);
            if (l != null) {
                if (l.getWalkSteps() > 0) {
                    firstWalkLine.add(l.getWalkSteps());
                    map.put("" + i, l.getWalkSteps());
                } else {
                    firstWalkLine.add(0);
                    map.put("" + i, 0);
                }
                if (l.getRunningSteps() > 0) {
                    firstRunLine.add(l.getRunningSteps());
                    mapr.put("" + i, l.getRunningSteps());
                } else {
                    firstRunLine.add(0);
                    mapr.put("" + i, 0);
                }
                if (l.getWalkDistance() > 0.0000) {
                    firstWalkPillar.add(new DecimalFormat("0.00").format(l.getWalkDistance()));
                    mappillerw.put("" + i, l.getWalkDistance());
                } else {
                    firstWalkPillar.add(0 + "");
                    mappillerw.put("" + i, 0);
                }
                if (l.getRunningDistance() > 0.0000) {
                    firstRunPillar.add(new DecimalFormat("0.00").format(l.getRunningDistance()));
                    mappillerr.put("" + i, l.getRunningDistance());
                } else {
                    firstRunPillar.add(0 + "");
                    mappillerr.put("" + i, 0);
                }
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
            time = DateUtils.getStartOfDay(DateUtils.getDay(start, i)).getTime();
        }
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
        List<Double> ls=new ArrayList<>();
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
                if(v<0.12){
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


    @Override
    protected void beforeInitViews() {
        v = LayoutInflater.from(context).inflate(R.layout.list_time_view, null);
        timeLv = (ListView) v.findViewById(R.id.timeLv);
        timeListAdapter = new TimeListAdapter(timeStr);
        timeLv.setAdapter(timeListAdapter);
        timeLv.setOnItemClickListener(this);
        brokenLine = (BrokenLineView) rootView.findViewById(R.id.brokenLine);
        brokenLinePillar = (BrokenLinePillar) rootView.findViewById(R.id.brokerLine1);
        spinner = (TextView) rootView.findViewById(R.id.spinner);
        state_line = (TextView) rootView.findViewById(R.id.state_line);
        state_pailler = (TextView) rootView.findViewById(R.id.state_pailler);
        activity_week_group = (RadioGroup) rootView.findViewById(R.id.activity_week_group);
        if(checkedStr==null){
            initValue();
        }else{
            updateData(checkedStr);
        }

        brokenLine.setYLable(ylables);
        brokenLinePillar.setYlabe(ylablesRun);
        createDialog();
    }

    @Override
    protected void initViews() {
        state_line.setText("每日步行步数统计");
        state_pailler.setText("每日步行总里程统计");
        spinner.setOnClickListener(this);
        activity_week_group.setOnCheckedChangeListener(this);
    }

    @Override
    protected View getRootView() {
        return LayoutInflater.from(context).inflate(R.layout.activity_week_view, null);
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
            updateData(checkedStr);
            state_line.setText("每日步行步数统计");
            state_pailler.setText("每日步行总里程统计");
        } else if (checkedId == R.id.group_eight) {
            RadioButton b = (RadioButton) rootView.findViewById(R.id.group_eight);
            b.setBackgroundResource(R.drawable.checked_right);
            walkOrrunning = false;
            updateData(checkedStr);
            state_line.setText("每日跑步步数统计");
            state_pailler.setText("每日跑步总里程统计");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (timeListAdapter.getItem(position) != null) {
            checkedStr=timeListAdapter.getItem(position);
            spinner.setText(checkedStr);
            hideDialog();
            updateData(checkedStr);
        }
    }

    //更新数据，重新绘制图表
    private void updateData(String value) {
        String[] time = value.split("~~");
        Date date = DateUtils.getStartOfDay(DateUtils.parseStringDate(time[0], "yyyy/MM/dd")).getTime();
        setData(date);
    }


    //添加数据
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
                    String[] values = value.split("~~");
                    startTime = (TextView) getConvertView().findViewById(R.id.startTime);
                    startTime.setText(values[0]);
                    stopTime = (TextView) getConvertView().findViewById(R.id.stopTime);
                    stopTime.setText(values[1]);
                }

                @Override
                protected View createConvertView() {
                    return View.inflate(context, R.layout.timelistview_item, null);
                }
            };
        }
    }
}
