package com.advanpro.fwtools.module.stat;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.base.BaseHolder;
import com.advanpro.fwtools.common.base.BaseListAdapter;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.view.TitleBar;
import com.advanpro.fwtools.module.BaseActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zengfs on 2016/1/19.
 * 步态统计
 */
public class GaitStatActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private TitleBar titlebar;
    private HorizontalView horizontalView;
    private LinearLayout linearLayout;

    private List<String> timeStr;//时间数据
    private TimeListAdapter timeListAdapter;
    private Dialog dialog;
    private View v;
    private ListView timeLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat_gait);
        initView();
        initEven();
    }

    //初始化控件
    private void initView() {
        //初始化
        timeStr = new ArrayList<>();
        //初始化时间数据
        String[] vstr = this.getResources().getStringArray(R.array.activity_year);
        for (String s : vstr) {
            timeStr.add(s);
        }
        timeListAdapter=new TimeListAdapter(timeStr);

        v = LayoutInflater.from(this).inflate(R.layout.list_time_view, null);
        timeLv = (ListView) v.findViewById(R.id.timeLv);
        timeLv.setAdapter(timeListAdapter);
        timeLv.setOnItemClickListener(this);

        horizontalView = (HorizontalView) findViewById(R.id.myHorizontalView);
        horizontalView.setType(HorizontalView.PROGRESS);
        linearLayout = (LinearLayout) findViewById(R.id.linearView);
        horizontalView.setLinear(linearLayout);
        MyAdapter adapter = new MyAdapter(this);
        List<String> l = new ArrayList<>();
        for (int i = 1; i < 12 + 1; i++) {
            if (i < 10) {
                l.add(i + "月");
            } else {
                l.add(i + "月");
            }
        }
        adapter.setData(l);

        horizontalView.setWcount(5);
        horizontalView.setAdapter(adapter);

        titlebar = (TitleBar) findViewById(R.id.title_bar);
        titlebar.setEndText(DateUtils.formatDate(new Date(), "yyyy年") + "");
        titlebar.setEndTextViewVisible(true);
        createDialog();
    }


    //弹出对话框，显示时间数据
    private void createDialog() {
        dialog = new Dialog(this, R.style.DialogStyle);
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

    //初始化控件事件
    private void initEven() {
        titlebar.setStartImageButtonVisible(true);

        titlebar.setOnMenuClickListener(new TitleBar.OnMenuClickListener() {
            @Override
            public void onMenuClick(View v) {
                if (v.getId() == R.id.btn_start) {
                    finish();
                }
                if(v.getId()==R.id.tv_end){
                    showDialog();
                }

            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (timeListAdapter.getItem(position) != null) {
            titlebar.setEndText(timeListAdapter.getItem(position));
            horizontalView.setGaitCurrentTime(timeListAdapter.getItem(position));
            hideDialog();
            //initValue(spinner.getText().toString());
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
                    TextView startTime = (TextView) getConvertView().findViewById(R.id.startTime);
                    startTime.setText(value);
                }

                @Override
                protected View createConvertView() {
                    return View.inflate(GaitStatActivity.this, R.layout.year_time_view, null);
                }
            };
        }
    }
}
