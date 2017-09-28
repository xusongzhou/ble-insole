package com.advanpro.fwtools.module.stat;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.db.Alarm;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.entity.GaitSummary;
import com.advanpro.fwtools.entity.PoseSummary;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 滑动标题栏
 * Created by DEV002 on 2016/3/25.
 */
public class HorizontalView extends HorizontalScrollView {

    public static int CIRCLE = 1;
    public static int PROGRESS = 2;
    public static int LISTVIEW = 3;

    private View contentView;//关联的View
    private HorizontalAdapter adapter;//适配器
    private RadioGroup radioGroup;
    private LayoutInflater inflater;//布局加载器
    private Context context;
    private int everyoneW = 300;//每一个标签的宽度
    private int wcount = 5;//屏幕上显示的标签个数
    private LinearLayout l;//包裹View的LinearLayout布局
    private GaitProgressBar leftInner, leftOut, leftInnerNormal, leftAll, leftBack, rightInner, rightOut, rightInnerNormal, rightAll, rightBack;
    private TextView leftInnerText, leftOutText, leftNormalText, leftAllText, leftText, rightInnerText, rightOutText, rightNormalText, rightAllText, rightText, warningText;
    private ChartView view;
    private int type;
    private int padding = 0;
    private ListView listView;
    private List<Alarm> alarmList = new ArrayList<>();
    private ListAdapter listAdapter = new ListAdapter();
    private String currentTime = DateUtils.formatDate(new Date(), "yyyy年");
    private String posecurrentTime = DateUtils.formatDate(new Date(), "yyyy年");
    private String gaitmonth = DateUtils.formatDate(new Date(), "MM月");
    private String posemonth = DateUtils.formatDate(new Date(), "MM月");
    ;

    public HorizontalView(Context context) {
        super(context);
    }

    public HorizontalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        inflater = LayoutInflater.from(context);
        contentView = inflater.inflate(R.layout.linear, null);
        radioGroup = (RadioGroup) contentView.findViewById(R.id.radioGroup);
        addView(contentView);
    }

    public HorizontalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    //初始化标题栏数据
    private void init() {
        radioGroup.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            RadioButton v = (RadioButton) adapter.getView(i);
            v.setButtonDrawable(android.R.color.transparent);
            if (padding > 0) {
                v.setLayoutParams(new ViewGroup.LayoutParams(everyoneW + padding, (everyoneW + padding) / 2));
            } else {
                v.setLayoutParams(new ViewGroup.LayoutParams(everyoneW, everyoneW / 2));
            }
            v.setGravity(Gravity.CENTER);
            String time = DateUtils.formatDate(new Date(), "yyyy年MM月");
            if (time.equals(v.getText().toString()) || time.contains(v.getText().toString())) {
                v.setChecked(true);
                v.setTextColor(getResources().getColor(R.color.tab_text));
            } else {
                if (Integer.parseInt(DateUtils.formatDate(new Date(), "dd")) % 7 > 0) {
                    if (v.getText().toString().equals(DateUtils.formatDate(DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -Integer.parseInt(DateUtils.formatDate(new Date(), "dd")) % 7 + 1), "MM/dd")) ||
                            v.getText().toString().contains(DateUtils.formatDate(DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -Integer.parseInt(DateUtils.formatDate(new Date(), "dd")) % 7 + 1), "MM/dd"))) {
                        v.setChecked(true);
                        v.setTextColor(getResources().getColor(R.color.tab_text));
                    } else {
                        v.setTextColor(Color.WHITE);
                    }
                } else {
                    if (v.getText().toString().equals(DateUtils.formatDate(DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -Integer.parseInt(DateUtils.formatDate(new Date(), "dd")) % 7), "MM/dd")) ||
                            v.getText().toString().contains(DateUtils.formatDate(DateUtils.getDay(DateUtils.getStartOfDay(new Date()).getTime(), -Integer.parseInt(DateUtils.formatDate(new Date(), "dd")) % 7), "MM/dd"))) {
                        v.setChecked(true);
                        v.setTextColor(getResources().getColor(R.color.tab_text));
                    } else {
                        v.setTextColor(Color.WHITE);
                    }
                }
            }
            radioGroup.addView(v);
        }
        final RadioButton bb = (RadioButton) radioGroup.getChildAt(radioGroup.getCheckedRadioButtonId());
        if (bb != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    smoothScrollTo(bb.getLeft() - getWidth() / 2 + bb.getWidth() / 2, 0);
                }
            });
        }
        if (type == CIRCLE) {
            view = (ChartView) l.findViewById(R.id.chartView);
            setCircleData(DateUtils.formatDate(new Date(), "yyyy年MM月dd日"));
        } else if (type == PROGRESS) {
            leftInnerText = (TextView) l.findViewById(R.id.leftInnerText);
            leftOutText = (TextView) l.findViewById(R.id.leftOutText);
            leftNormalText = (TextView) l.findViewById(R.id.leftNormalText);
            leftAllText = (TextView) l.findViewById(R.id.leftAllText);
            leftText = (TextView) l.findViewById(R.id.leftText);
            rightInnerText = (TextView) l.findViewById(R.id.rightInnerText);
            rightOutText = (TextView) l.findViewById(R.id.rightOutText);
            rightNormalText = (TextView) l.findViewById(R.id.rightNormalText);
            rightAllText = (TextView) l.findViewById(R.id.rightAllText);
            rightText = (TextView) l.findViewById(R.id.rightText);

            leftInner = (GaitProgressBar) l.findViewById(R.id.leftInner);
            leftOut = (GaitProgressBar) l.findViewById(R.id.leftOut);
            leftInnerNormal = (GaitProgressBar) l.findViewById(R.id.leftNormal);
            leftAll = (GaitProgressBar) l.findViewById(R.id.leftAll);
            leftBack = (GaitProgressBar) l.findViewById(R.id.left);
            rightInner = (GaitProgressBar) l.findViewById(R.id.rightInner);
            rightOut = (GaitProgressBar) l.findViewById(R.id.rightOut);
            rightInnerNormal = (GaitProgressBar) l.findViewById(R.id.rightNormal);
            rightAll = (GaitProgressBar) l.findViewById(R.id.rightAll);
            rightBack = (GaitProgressBar) l.findViewById(R.id.right);
            setpProgressData(DateUtils.formatDate(new Date(), "yyyy年MM月dd日"));
        } else if (type == LISTVIEW) {
            listView = (ListView) l.findViewById(R.id.warningLv);
            warningText = (TextView) l.findViewById(R.id.warningText);
            int date = Integer.parseInt(DateUtils.formatDate(new Date(), "dd"));
            String time;
            if (date % 7 == 0) {
                String start = DateUtils.formatDate(DateUtils.getStartOfDay(DateUtils.getDay(new Date(), -6)).getTime(), "MM/dd");
                String stop = DateUtils.formatDate(DateUtils.getStartOfDay(new Date()).getTime(), "MM/dd");
                time = start + "-" + stop;
            } else {
                String start = DateUtils.formatDate(DateUtils.getStartOfDay(DateUtils.getDay(new Date(), -((date % 7) - 1))).getTime(), "MM/dd");
                String stop = DateUtils.formatDate(DateUtils.getStartOfDay(DateUtils.getDay(new Date(), 7 - (date % 7))).getTime(), "MM/dd");
                time = start + "-" + stop;
            }
            setListViewData(time);
        }
        setListener();

    }


    public void setLinear(LinearLayout l) {
        this.l = l;

    }

    //设置标题栏显示标题的个数
    public void setWcount(int wcount) {
        this.wcount = wcount;
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        everyoneW = metrics.widthPixels / wcount;
        if (everyoneW == 0) {
            everyoneW = 200;
        }
    }

    //设置适配器
    public void setAdapter(HorizontalAdapter adapter) {
        this.adapter = adapter;
        init();
    }

    //设置监听，点击不通的title切换对应的数据
    public void setListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton bb = (RadioButton) group.getChildAt(checkedId);
                if (bb.getId() == checkedId) {
                    bb.setTextColor(getResources().getColor(R.color.tab_text));
                    smoothScrollTo(bb.getLeft() - getWidth() / 2 + bb.getWidth() / 2, 0);
                    if (type == CIRCLE) {
//                        String time = DateUtils.formatDate(new Date(), "yyyy年") + bb.getText().toString() + "月";
                        String time = posecurrentTime + bb.getText().toString();
                        setCircleData(time);
                        posemonth = bb.getText().toString();
                        Log.i("TAG", "posemonth----->" + posemonth);
                    } else if (type == PROGRESS) {
                        String time = currentTime + bb.getText().toString();
                        setpProgressData(time);
                        gaitmonth = bb.getText().toString();
                        Log.i("TAG", "gaitmonth----->" + gaitmonth);
                    } else {
                        updateLv(bb.getText().toString());
                    }
                }
                for (int i = 0; i < group.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) group.getChildAt(i);
                    if (radioButton.getId() != checkedId) {
                        radioButton.setTextColor(Color.WHITE);
                    }
                }
            }

        });
    }


    private void setListViewData(String time) {
        String[] str = time.split("-");
        Date start = DateUtils.getStartOfDay(DateUtils.parseStringDate(DateUtils.formatDate(new Date(), "yyyy") + "/" + str[0], "yyyy/MM/dd")).getTime();
        Date stop = DateUtils.getStartOfDay(DateUtils.parseStringDate(DateUtils.formatDate(new Date(), "yyyy") + "/" + str[1], "yyyy/MM/dd")).getTime();
        List<Alarm> list = Dao.INSTANCE.queryAlarms(start, stop);
        if (list.size() > 0) {
            warningText.setText(list.size() + "");
            for (int i = 0; i < list.size(); i++) {
                if (i == list.size() - 1) {
                    alarmList.add(list.get(i));
                } else {
                    alarmList.add(list.get(i));
                    alarmList.add(new Alarm());
                }
            }
            listView.setAdapter(listAdapter);
        }
    }


    private void updateLv(String time) {
        String[] str = time.split("-");
        Date start = DateUtils.getStartOfDay(DateUtils.parseStringDate(DateUtils.formatDate(new Date(), "yyyy") + "/" + str[0], "yyyy/MM/dd")).getTime();
        Date stop = DateUtils.getStartOfDay(DateUtils.parseStringDate(DateUtils.formatDate(new Date(), "yyyy") + "/" + str[1], "yyyy/MM/dd")).getTime();
        List<Alarm> list = Dao.INSTANCE.queryAlarms(start, stop);
        if (list.size() > 0) {
            warningText.setText(list.size() + "");
            for (int i = 0; i < list.size(); i++) {
                if (i == list.size() - 1) {
                    alarmList.add(list.get(i));
                } else {
                    alarmList.add(list.get(i));
                    alarmList.add(new Alarm());
                }
            }
        } else {
            warningText.setText("0");
            alarmList.clear();
        }
        listAdapter.notifyDataSetChanged();
    }

    private void setCircleData(String time) {
        String year = time.substring(0, 4);
        time = time.substring(time.indexOf("年") + 1);
        time = time.substring(0, time.indexOf("月"));
        Date start = DateUtils.getEveryMonthFistDay(year, time);
        Date stop = DateUtils.getEveryMonthLastDay(year, time);
        PoseSummary pose = Dao.INSTANCE.queryPoseSummary(start, stop);
        if (pose != null) {
            view.setData((int) pose.walkDuration, (int) pose.runDuration, (int) pose.sitDuration, (int) pose.standDuration);
        } else {
            view.setData(0, 0, 0, 0);
        }
    }

    private void setpProgressData(String time) {
        String year = time.substring(0, 4);
        time = time.substring(time.indexOf("年") + 1);
        time = time.substring(0, time.indexOf("月"));
        Date start = DateUtils.getEveryMonthFistDay(year, time);
        Date stop = DateUtils.getEveryMonthLastDay(year, time);
        GaitSummary leftgait = Dao.INSTANCE.queryGaitSummary(start, stop, true);//获取左脚数据
        GaitSummary rightgait = Dao.INSTANCE.queryGaitSummary(start, stop, false);//获取右脚数据

        long left = leftgait.ectropion + leftgait.heel + leftgait.sole + leftgait.varus + leftgait.forefoot;
        long right = rightgait.ectropion + rightgait.heel + rightgait.sole + rightgait.varus + rightgait.forefoot;
        if (left > 0) {
            DecimalFormat format = new DecimalFormat("0.0");
            leftInnerText.setText(format.format(((double) leftgait.varus / left) * 100) + "0" + " %");
            leftOutText.setText(format.format(((double) leftgait.ectropion / left) * 100) + "0" + " %");
            leftNormalText.setText(format.format(((double) leftgait.forefoot / left) * 100) + "0" + " %");
            leftAllText.setText(format.format(((double) leftgait.sole / left) * 100) + "0" + " %");
            leftText.setText(format.format(((double) leftgait.heel / left) * 100) + "0" + " %");
        } else {
            leftInnerText.setText("0" + " %");
            leftOutText.setText("0" + " %");
            leftNormalText.setText("0" + " %");
            leftAllText.setText("0" + " %");
            leftText.setText("0" + " %");
        }
        if (right > 0) {
            DecimalFormat format = new DecimalFormat("0.0");
            rightInnerText.setText(format.format(((double) rightgait.varus / right) * 100) + "0" + " %");
            rightOutText.setText(format.format(((double) rightgait.ectropion / right) * 100) + "0" + " %");
            rightNormalText.setText(format.format(((double) rightgait.forefoot / right) * 100) + "0" + " %");
            rightAllText.setText(format.format(((double) rightgait.sole / right) * 100) + "0" + " %");
            rightText.setText(format.format(((double) rightgait.heel / right) * 100) + "0" + " %");
        } else {
            rightInnerText.setText("0" + " %");
            rightOutText.setText("0" + " %");
            rightNormalText.setText("0" + " %");
            rightAllText.setText("0" + " %");
            rightText.setText("0" + " %");
        }

        if (leftgait != null) {
            leftInner.setCount(left);
            leftInner.setValue(leftgait.varus);
            leftOut.setCount(left);
            leftOut.setValue(leftgait.ectropion);
            leftInnerNormal.setCount(left);
            leftInnerNormal.setValue(leftgait.forefoot);
            leftAll.setCount(left);
            leftAll.setValue(leftgait.sole);
            leftBack.setCount(left);
            leftBack.setValue(leftgait.heel);
        }
        if (rightgait != null) {
            rightInner.setCount(right);
            rightInner.setValue(rightgait.varus);
            rightOut.setCount(right);
            rightOut.setValue(rightgait.ectropion);
            rightInnerNormal.setCount(right);
            rightInnerNormal.setValue(rightgait.forefoot);
            rightAll.setCount(right);
            rightAll.setValue(rightgait.sole);
            rightBack.setCount(right);
            rightBack.setValue(rightgait.heel);
        }
//        switch (DeviceMgr.getBoundDeviceType()) {
//            case Constant.DEVICE_TYPE_BASIC:
//                leftInner.setVisibility(View.GONE);
//                rightInner.setVisibility(View.GONE);
//                leftOut.setVisibility(View.GONE);
//                rightOut.setVisibility(View.GONE);
//                leftInnerText.setVisibility(View.GONE);
//                rightInnerText.setVisibility(View.GONE);
//                leftOutText.setVisibility(View.GONE);
//                rightOutText.setVisibility(View.GONE);
//                break;
//            case Constant.DEVICE_TYPE_POPULARITY:
//                leftInner.setVisibility(View.VISIBLE);
//                rightInner.setVisibility(View.VISIBLE);
//                leftOut.setVisibility(View.VISIBLE);
//                rightOut.setVisibility(View.VISIBLE);
//                leftInnerText.setVisibility(View.VISIBLE);
//                rightInnerText.setVisibility(View.VISIBLE);
//                leftOutText.setVisibility(View.VISIBLE);
//                rightOutText.setVisibility(View.VISIBLE);
//                break;
//            case Constant.DEVICE_TYPE_ENHANCED:
//                leftInner.setVisibility(View.VISIBLE);
//                rightInner.setVisibility(View.VISIBLE);
//                leftOut.setVisibility(View.VISIBLE);
//                rightOut.setVisibility(View.VISIBLE);
//                leftInnerText.setVisibility(View.VISIBLE);
//                rightInnerText.setVisibility(View.VISIBLE);
//                leftOutText.setVisibility(View.VISIBLE);
//                rightOutText.setVisibility(View.VISIBLE);
//                break;
//            case -1:
//                leftInner.setVisibility(View.GONE);
//                rightInner.setVisibility(View.GONE);
//                leftOut.setVisibility(View.GONE);
//                rightOut.setVisibility(View.GONE);
//                leftInnerText.setVisibility(View.GONE);
//                rightInnerText.setVisibility(View.GONE);
//                leftOutText.setVisibility(View.GONE);
//                rightOutText.setVisibility(View.GONE);
//                break;
//        }


    }


    public void setType(int type) {
        this.type = type;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void setGaitCurrentTime(String time) {
        this.currentTime = time;
        setpProgressData(time + gaitmonth);
    }

    public void setPoseCurrent(String time) {
        this.posecurrentTime = time;
        setCircleData(time + posemonth);
    }

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return alarmList.size();
        }

        @Override
        public Object getItem(int position) {
            return alarmList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Alarm alarm = alarmList.get(position);
            if (alarm != null) {
                if (position % 2 == 0) {
                    convertView = View.inflate(context, R.layout.warning_type_content, null);
                    ImageView img = (ImageView) convertView.findViewById(R.id.warningImg);
                    TextView content = (TextView) convertView.findViewById(R.id.warningContent);
                    TextView time = (TextView) convertView.findViewById(R.id.warningTime);
                    if (alarm != null) {
                        time.setText(DateUtils.formatDate(alarm.getDate(), "MM/dd HH:mm:ss ") + "");
                        switch (alarm.type) {
                            case Constant.FATIGUE_SOMEWHAT_HARD:
                                content.setText(R.string.fatigue_somewhat_hard);
                                break;
                            case Constant.FATIGUE_HARD:
                                content.setText(R.string.fatigue_hard);
                                break;
                            case Constant.FATIGUE_VERY_HARD:
                                content.setText(R.string.fatigue_very_hard);
                                break;
                            case Constant.FATIGUE_VERY_VERY_HARD:
                                content.setText(R.string.fatigue_very_very_hard);
                                break;
                            case Constant.INJURE_LOW:
                                content.setText(R.string.injure_low);
                                break;
                            case Constant.INJURE_MIDDLE:
                                content.setText(R.string.injure_middle);
                                break;
                            case Constant.INJURE_HIGH:
                                content.setText(R.string.injure_high);
                                break;
                        }
                    }
                } else {
                    convertView = View.inflate(context, R.layout.driver, null);
                }
            } else {
                return convertView;
            }
            return convertView;
        }
    }

}
