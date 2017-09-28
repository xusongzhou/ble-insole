package com.advanpro.fwtools.module.stat;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BasePager;
import com.advanpro.fwtools.common.base.BasePagerAdapter;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.common.view.TextTabContentView;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.common.view.ViewPagerIndicator;
import com.advanpro.fwtools.module.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zengfs on 2016/1/19.
 * 活动统计
 */
public class ActivityStatActivity extends BaseActivity {


    private List<BasePager> pagers;
    private ViewPager viewPager;
    private ViewPagerIndicator pagerIndicator;
    private TitleBar titleBar;
    private BasePagerAdapter pagerAdapter;
    private ActivityYearAdapter yearAdapter;
    private ActivityMonthAdapter monthAdapter;
    private ActivityWeekAdapter weekAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat_activity);
        assignViews();
        initViews();
    }

    protected void assignViews() {
        yearAdapter = new ActivityYearAdapter(this);
        monthAdapter = new ActivityMonthAdapter(this);
        weekAdapter = new ActivityWeekAdapter(this);
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        pagerIndicator = (ViewPagerIndicator) findViewById(R.id.view_pager_indicator);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 1) {
                    weekAdapter.beforeInitViews();
                }
                if (i == 2) {
                    monthAdapter.beforeInitViews();
                }
                if (i == 3) {
                    yearAdapter.initViews();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        weekAdapter.beforeInitViews();
        monthAdapter.beforeInitViews();
        yearAdapter.initViews();
    }

    protected void initViews() {
        initTitleBar();

        //初始化ViewPager
        pagers = new ArrayList<>();
        pagers.add(new ActivityAdapter(this));
        pagers.add(weekAdapter);
        pagers.add(monthAdapter);
        pagers.add(yearAdapter);
        pagerAdapter = new BasePagerAdapter(pagers);

        viewPager.setAdapter(pagerAdapter);
        pagerIndicator.setDividerEnabled(false);
        pagerIndicator.setIndicatorEnabled(false);
        pagerIndicator.setBackgroundColor(UiUtils.getColor(R.color.tab_bg));
        pagerIndicator.setTabContentViews(createTextTabViews());
        pagerIndicator.setViewPager(viewPager);
    }

    //初始化标题栏
    private void initTitleBar() {
        titleBar.setStartImageButtonVisible(true);
        titleBar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                if (v.getId() == R.id.btn_start) {
                    finish();
                }
            }
        });
    }

    private TextTabContentView[] createTextTabViews() {
        int[] resIds = {R.string.day, R.string.week, R.string.month, R.string.year};
        TextTabContentView[] textTabContentViews = new TextTabContentView[resIds.length];
        for (int i = 0; i < resIds.length; i++) {
            textTabContentViews[i] = new TextTabContentView(this);
            textTabContentViews[i].setText(resIds[i]);
            textTabContentViews[i].setCheckedTextColor(UiUtils.getColor(R.color.tab_text));
        }
        return textTabContentViews;
    }
}
