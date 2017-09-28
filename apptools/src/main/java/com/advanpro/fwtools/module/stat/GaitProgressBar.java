package com.advanpro.fwtools.module.stat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.advanpro.fwtools.R;

/**
 * Created by DEV002 on 2016/3/22.
 */
public class GaitProgressBar extends View {

    private int width;//view宽
    private int height;//view高
    private int startcolor;//view开始颜色
    private int stopcolor;//view结束颜色
    private double count;
    private double value;

    public GaitProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GaitProgressBar);
        startcolor = array.getColor(R.styleable.GaitProgressBar_startColor, Color.WHITE);
        stopcolor = array.getColor(R.styleable.GaitProgressBar_stopColor, Color.WHITE);
        array.recycle();
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


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(dipToPx(20));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setShadowLayer(5, 20, 20, Color.YELLOW);
        canvas.drawLine(20, height / 2, width, height / 2, paint);// 画线

        paint.reset();
        paint.setShadowLayer(5, 20, 20, Color.YELLOW);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setStrokeCap(Paint.Cap.ROUND);
        Shader mShader = new LinearGradient(0, 0, 0, 60, new int[]{startcolor, stopcolor}, null, Shader.TileMode.REPEAT);
        paint.setShader(mShader);
        canvas.drawLine(width, height / 2, endValue(count, value), height / 2, paint);// 画线

    }


    private int endValue(double count, double value) {
        int result = 0;
        int v = (int) ((value / count) * (double) (width));
        if (v == width) {
            result = 20;
        } else if (v == 0) {
            result = width;
        } else {
            result = (width - v);
        }
        return result;
    }


    public void setCount(double count) {
        this.count = count;
    }

    public void setValue(double value) {
        this.value = value;
        invalidate();
    }


}
