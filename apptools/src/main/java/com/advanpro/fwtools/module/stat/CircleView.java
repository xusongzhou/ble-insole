package com.advanpro.fwtools.module.stat;//package com.advanpro.asinsole.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.XChartUtil;

import java.text.DecimalFormat;

/**
 * Created by DEV002 on 2016/3/21.
 */
public class CircleView extends View {

    private int width;//view宽
    private int height;//view高
    private int count;
    private int walk;
    private int running;
    private int sitdown;
    private int stand;


    public CircleView(Context context) {
        super(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.EXACTLY || widthSpecMode == MeasureSpec.AT_MOST) {
            width = widthSpecSize;
        } else {
            width = 0;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            height = dipToPx(15);
        } else {
            height = heightSpecSize;
        }
        setMeasuredDimension(width, height);
    }

    private int dipToPx(int dip) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }


    public void onDraw(Canvas canvas) {
        //画布背景

        //画笔初始化
        Paint PaintArc = new Paint();
        PaintArc.setColor(getResources().getColor(R.color.a2));//walk color
        PaintArc.setStrokeWidth(80);
        PaintArc.setStyle(Paint.Style.STROKE);

        Paint PaintGree = new Paint();
        PaintGree.setColor(getResources().getColor(R.color.a3));//sit down color
        PaintGree.setStyle(Paint.Style.STROKE);
        PaintGree.setStrokeWidth(80);

        Paint PaintBlue = new Paint();
        PaintBlue.setColor(Color.GRAY);

        Paint PaintYellow = new Paint();
        PaintYellow.setColor(getResources().getColor(R.color.a4));//stand up color
        PaintYellow.setStyle(Paint.Style.STROKE);
        PaintYellow.setStrokeWidth(80);

        Paint PaintW = new Paint();
        PaintW.setColor(getResources().getColor(R.color.a1));//running color
        PaintW.setStyle(Paint.Style.STROKE);
        PaintW.setStrokeWidth(80);

        //抗锯齿
        PaintArc.setAntiAlias(true);
        PaintYellow.setAntiAlias(true);
        PaintGree.setAntiAlias(true);
        PaintW.setAntiAlias(true);
        PaintBlue.setAntiAlias(true);

        PaintBlue.setTextSize(20);

        float cirX = width / 2;
        float cirY = height / 2;
        float radius = (cirX + cirY) / 4;
        //先画个圆确定下显示位置


        float arcLeft = (cirX - radius);
        float arcTop = (cirY - radius);
        float arcRight = (cirX + radius);
        float arcBottom = (cirY + radius);
        RectF arcRF0 = new RectF(arcLeft, arcTop, arcRight, arcBottom);

        ////////////////////////////////////////////////////////////
        //饼图标题
        // canvas.drawText("author:xiongchuanliang",60,ScrHeight - 270, PaintBlue);

        //位置计算类
        XChartUtil xcalc = new XChartUtil();

        //实际用于计算的半径
        float calcRadius = radius;
        ////////////////////////////////////////////////////////////
        //初始角度

        float pAngle = getValue(count, walk);
        float pAngle1 = getValue(count, running);
        float pAngle2 = getValue(count, sitdown);
        float pAngle3 = getValue(count, stand);

        if (pAngle > 0) {
            canvas.drawArc(arcRF0, 0 + 1.5f, pAngle + 2.0f, false, PaintArc);
        } else {
            canvas.drawArc(arcRF0, 0, pAngle, false, PaintArc);
        }
        if (pAngle1 > 0) {
            canvas.drawArc(arcRF0, pAngle + 2.0f, pAngle1 + 1.5f, false, PaintW);
        } else {
            canvas.drawArc(arcRF0, pAngle, pAngle1, false, PaintW);
        }
        if (pAngle2 > 0) {
            canvas.drawArc(arcRF0, pAngle1 + pAngle + 1.5f, pAngle2 + 2.0f, false, PaintYellow);
        } else {
            canvas.drawArc(arcRF0, pAngle1 + pAngle, pAngle2, false, PaintYellow);
        }
        if (pAngle3 > 0) {
            canvas.drawArc(arcRF0, pAngle + pAngle1 + pAngle2 + 1.5f, pAngle3 + 2.0f, false, PaintGree);
        } else {
            canvas.drawArc(arcRF0, pAngle + pAngle1 + pAngle2, pAngle3, false, PaintGree);
        }
        Log.i("TAG", "CIRCLE---" + (pAngle) + "---" + pAngle1 + "----" + pAngle2 + "-----" + pAngle3);
        //填充扇形

        //canvas.drawCircle(cirX,cirY,radius,PaintArc);
//        canvas.drawArc(arcRF0, 0+0.5f, 360+0.5f, false, PaintArc);
//        canvas.drawArc(arcRF0, 0+0.5f, 0+0.5f, false, PaintW);
//        canvas.drawArc(arcRF0,0+0.5f,0+0.5f,false,PaintYellow);
//        canvas.drawArc(arcRF0,0+0.5f,0+0.5f,false,PaintGree);

//        canvas.drawArc(arcRF0, 0, pAngle, false, PaintArc);
//        canvas.drawArc(arcRF0, pAngle, pAngle1, false, PaintW);
//        canvas.drawArc(arcRF0,pAngle1+pAngle,pAngle2,false,PaintYellow);
//        canvas.drawArc(arcRF0,pAngle+pAngle1+pAngle2,pAngle3,false,PaintGree);

        //canvas.drawArc(arcRF0, pAngle1,pAngle2, true,PaintYellow);

        ////////////////
        xcalc.CalcArcEndPointXY(cirX, cirY, calcRadius, pAngle / 2);
        canvas.drawText(getText(count, walk), xcalc.getPosX(), xcalc.getPosY(), PaintBlue);


        //计算并在扇形中心标注上百分比    130%
        xcalc.CalcArcEndPointXY(cirX, cirY, calcRadius, (pAngle1 / 2) + pAngle);
        canvas.drawText(getText(count, running), xcalc.getPosX(), xcalc.getPosY(), PaintBlue);
        //////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////
        //填充扇形

        //计算并在扇形中心标注上百分比   40%
        xcalc.CalcArcEndPointXY(cirX, cirY, calcRadius, pAngle + pAngle1 + pAngle2 / 2);
        canvas.drawText(getText(count, sitdown), xcalc.getPosX(), xcalc.getPosY(), PaintBlue);

        ////////////////////////////////////////////////////////////
        //计算并在扇形中心标注上百分比  190%
        xcalc.CalcArcEndPointXY(cirX, cirY, calcRadius, pAngle + pAngle1 + pAngle2 + pAngle3 / 2);
        canvas.drawText(getText(count, stand), xcalc.getPosX(), xcalc.getPosY(), PaintBlue);
        ////////////////////////////////////////////////////////////

    }


    private float getValue(int count, int type) {
        if (count > 0) {
            float v = (((float) type / count) * 360);
            return v;
        } else {
            return 0;
        }

    }

    private String getText(int count, int type) {
        DecimalFormat format = new DecimalFormat("0.00");
        if (count > 0) {
            double value = (double) type / count;
            if (value > 0.0) {
                return format.format(((double) type / count) * 100) + "%";
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getWalk() {
        return walk;
    }

    public void setWalk(int walk) {
        this.walk = walk;
    }

    public int getRunning() {
        return running;
    }

    public void setRunning(int running) {
        this.running = running;
    }

    public int getSitdown() {
        return sitdown;
    }

    public void setSitdown(int sitdown) {
        this.sitdown = sitdown;
    }

    public int getStand() {
        return stand;
    }

    public void setStand(int stand) {
        this.stand = stand;
    }
}