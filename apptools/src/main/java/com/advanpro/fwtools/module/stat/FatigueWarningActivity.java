package com.advanpro.fwtools.module.stat;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.module.BaseActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zengfs on 2016/1/19.
 * 疲惫预警
 */
public class FatigueWarningActivity extends BaseActivity {


    private TitleBar titlebar;
    private HorizontalView horizontalView;
    private LinearLayout layout;
    private List<String> value = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fatigue_warning);
        initView();
        initEven();
    }

    //初始化控件
    private void initView() {
        for (int i = 1; i < 13; i++) {
            addList(i);
        }
        titlebar = (TitleBar) findViewById(R.id.title_bar);
        horizontalView = (HorizontalView) findViewById(R.id.myHorizontalView);
        layout = (LinearLayout) findViewById(R.id.linearView);
        horizontalView.setPadding(80);
        horizontalView.setType(HorizontalView.LISTVIEW);
        horizontalView.setLinear(layout);
        MyAdapter myAdapter = new MyAdapter(this);
        myAdapter.setData(value);
        horizontalView.setWcount(5);
        horizontalView.setAdapter(myAdapter);
    }


    //初始化控件事件
    private void initEven() {
        titlebar.setStartImageButtonVisible(true);
        titlebar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                if (v.getId() == R.id.btn_start) {
                    finish();
                }
            }
        });
    }

    private void addList(int month) {
        int i = 0;
        final int index = 7;
        int start = 1;
        int stop = 7;
        int count = getMonthCount(month) / index;
        if (getMonthCount(month) % index > 0) {
            count += 1;
        }
        for (; i < count; i++) {
            String time;
            String startV = "" + start;
            String stopV = "" + stop;
            if (start < 10) {
                startV = "0" + start;
            }
            if (stop < 10) {
                stopV = "0" + stop;
            }
            if (month < 10) {
                time = "0" + month + "/" + startV + "-" + "0" + month + "/" + stopV;
            } else {
                time = "0" + month + "/" + startV + "-" + "0" + month + "/" + stopV;
            }
            start = stop + 1;
            stop += 7;
            if (i == 3) {
                stop = getMonthCount(month);
            }
            value.add(time);
        }
    }

    public int getMonthCount(int month) {
        return DateUtils.getEveryMonthCountTime(DateUtils.formatDate(new Date(), "yyyy"), month + "");
    }

}
