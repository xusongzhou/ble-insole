package com.advanpro.fwtools.module.stat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advanpro.fwtools.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by DEV002 on 2016/3/24.
 */
public class ChartView extends LinearLayout {
    private static final String CONTENT_R = "跑的太多，请注意多休息!";
    private static final String CONTENT_W = "走路太多，请注意多休息!";
    private static final String CONTENT_SI = "坐的太久，请注意多运动!";
    private static final String CONTENT_S = "站的太久，请注意多休息!";
    private static final String CONTENT_NORMAL = "您的锻炼比较平均！继续加油";
    private static final String NOMAL = "你还没有数据，请赶快去锻炼吧!";

    private CircleView circleView;
    private TextView runningText, walkText, standText, sitdownText, contentText, runningValue, walkValue, sitdownValue, standValue;


    public ChartView(Context context) {
        super(context);
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.circle_view, this);
        runningText = (TextView) findViewById(R.id.runningText);
        walkText = (TextView) findViewById(R.id.walkText);
        standText = (TextView) findViewById(R.id.standText);
        sitdownText = (TextView) findViewById(R.id.sitdownText);
        contentText = (TextView) findViewById(R.id.contentText);
        sitdownValue = (TextView) findViewById(R.id.sitdownValue);
        walkValue = (TextView) findViewById(R.id.walkValue);
        standValue = (TextView) findViewById(R.id.standValue);
        runningValue = (TextView) findViewById(R.id.runningValue);
        circleView = (CircleView) findViewById(R.id.circle);
        circleView.setCount(0);
        circleView.setWalk(0);
        circleView.setStand(0);
        circleView.setSitdown(0);
        circleView.setStand(0);
    }


    public void setData(int walk, int running, int sitdown, int stand) {
        int count = walk + running + sitdown + stand;
        if (count > 0) {
            circleView.setCount(count);
            circleView.setWalk(walk);
            circleView.setRunning(running);
            circleView.setSitdown(sitdown);
            circleView.setStand(stand);
        } else {
            circleView.setCount(0);
            circleView.setWalk(0);
            circleView.setRunning(0);
            circleView.setSitdown(0);
            circleView.setStand(0);
        }
        circleView.invalidate();
        calculate(count, walk, running, sitdown, stand);
    }

    private void calculate(int count, int walk, int running, int sitdown, int stand) {
        List<Double> l = new ArrayList<>();
        if (count > 0) {
            double w = (double) walk / count;
            double r = (double) running / count;
            double si = (double) sitdown / count;
            double s = (double) stand / count;
            l.add(w);
            l.add(r);
            l.add(s);
            l.add(si);
            Collections.sort(l);
            DecimalFormat format = new DecimalFormat("0.00");
            runningText.setText(format.format((r * 100)) + "%");
            runningValue.setText(format.format((double) running / 3600) + "");
            walkText.setText(format.format((w * 100)) + "%");
            walkValue.setText(format.format((double) walk / 3600) + "");
            standText.setText(format.format((s * 100)) + "%");
            standValue.setText(format.format((double) stand / 3600) + "");
            sitdownText.setText(format.format((si * 100)) + "%");
            sitdownValue.setText(format.format((double) sitdown / 3600) + "");
            if (w == r && r == si && si == s) {
                contentText.setText(CONTENT_NORMAL);
            } else if (l.get(l.size() - 1) == w) {
                contentText.setText(CONTENT_W);
            } else if (l.get(l.size() - 1) == r) {
                contentText.setText(CONTENT_R);
            } else if (l.get(l.size() - 1) == si) {
                contentText.setText(CONTENT_SI);
            } else if (l.get(l.size() - 1) == s) {
                contentText.setText(CONTENT_S);
            } else {
                contentText.setText(NOMAL);
            }
        } else {
            runningText.setText("0%");
            runningValue.setText("0");
            walkText.setText("0%");
            walkValue.setText("0");
            standText.setText("0%");
            standValue.setText("0");
            sitdownText.setText("0%");
            sitdownValue.setText("0");
            contentText.setText(NOMAL);
        }
        l.clear();
    }

}
