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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DEV002 on 2016/3/25.
 */
public class BrokenLineView extends View {

    private int width;//view宽
    private int height;//view高
    private double MaxNum = 0;

    public int XPoint = 80;    //原点的X坐标

    private Paint xPaint;//X轴画笔
    private Paint xStrokePaint;//X轴虚线画笔
    private Paint xTextPaint;//X轴文字画笔
    private Paint yTextPaint;//Y轴文字画笔
    private Paint xCircle;//X轴圆点画笔
    private Paint xStrokeCircle;//虚线圆点画笔
    private Paint bokenlinePaint;//折线画笔

    private int yPoint = 0;
    private int yLength = 0;
    private int yScale = 0;
    private int xLength = 0;
    private int xScale = 0;

    private boolean walkOrrun = true;//默认跑步
    private String type;


    private String[] XWeekLable;//X轴周显示刻度
    private String[] XMonthLable;//X轴月显示刻度
    private String[] XYearLable;//X轴年显示刻度
    private String[] YWeekLable;
    private String[] YMonthLable;
    private String[] YYearLable;


    /*************************/
    private List<String> XlistWeekLable;
    private List<String> XlistMonthLable;
    private List<String> XlistYearLable;
    private List<String> YlistWeekLable;
    private List<String> YlistMonthLable;
    private List<String> YlistYearLable;


    private List<String> Xlable;
    private List<String> Ylable = new ArrayList<>();

    /*************************/


    private Map<String, Object> map = new HashMap<>();//接受外面传进的数据

    private List<Integer> pointData;


    public BrokenLineView(Context context) {
        super(context);
    }

    public BrokenLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BrokenLineView);
        type = array.getString(R.styleable.BrokenLineView_type);
        array.recycle();
        pointData = new ArrayList<>();//点坐标，用来绘图

        /**********************/
        XlistWeekLable = new ArrayList<>();
        XlistMonthLable = new ArrayList<>();
        XlistYearLable = new ArrayList<>();
        YlistWeekLable = new ArrayList<>();
        YlistMonthLable = new ArrayList<>();
        YlistYearLable = new ArrayList<>();

        Xlable = new ArrayList<>();
        /**********************/


        XWeekLable = getResources().getStringArray(R.array.week);//初始化周时间
        XMonthLable = getResources().getStringArray(R.array.xMonth);
        XYearLable = getResources().getStringArray(R.array.year);//初始化年时间

        YWeekLable = getResources().getStringArray(R.array.ylable_week);//初始化Y轴周刻度
        YMonthLable = getResources().getStringArray(R.array.ylable_month);//初始化Y轴月刻度
        YYearLable = getResources().getStringArray(R.array.ylable_year);//初始化Y轴年刻度


        /********/
        for (String s : XWeekLable) {
            XlistWeekLable.add(s);
        }
        for (String s : XMonthLable) {
            XlistMonthLable.add(s);
        }
        for (String s : XYearLable) {
            XlistYearLable.add(s);
        }
        for (String s : YWeekLable) {
            YlistWeekLable.add(s);
        }
        for (String s : YMonthLable) {
            YlistMonthLable.add(s);
        }
        for (String s : YYearLable) {
            YlistYearLable.add(s);
        }


        Xlable = XlistWeekLable;


        /********/

        initPaint();
    }

    public BrokenLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private void initPaint() {
        xPaint = new Paint();
        xPaint.setColor(Color.WHITE);
        xPaint.setAntiAlias(true);
        xStrokePaint = new Paint();
        xStrokePaint.setColor(Color.BLACK);
        xStrokePaint.setAntiAlias(true);
        xTextPaint = new Paint();
        xTextPaint.setAntiAlias(true);
        xTextPaint.setTextSize(dipToPx(10));
        xTextPaint.setColor(Color.WHITE);
        yTextPaint = new Paint();
        yTextPaint.setAntiAlias(true);
        yTextPaint.setTextSize(dipToPx(10));
        yTextPaint.setColor(Color.WHITE);

        xCircle = new Paint();
        xCircle.setAntiAlias(true);
        xCircle.setColor(getResources().getColor(R.color.a3));
        xCircle.setStrokeCap(Paint.Cap.ROUND);

        xStrokeCircle = new Paint();
        xStrokeCircle.setAntiAlias(true);
        xStrokeCircle.setColor(Color.YELLOW);
        xStrokeCircle.setStrokeCap(Paint.Cap.ROUND);

        bokenlinePaint = new Paint();
        bokenlinePaint.setAntiAlias(true);
        bokenlinePaint.setStrokeWidth(4);


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


        /**********************/

        yPoint = height - XPoint;
        yLength = height - XPoint - 20;
        yScale = yLength / 7;
        xLength = width - XPoint - 20;

        /**********************/

        setMeasuredDimension(width, height);
    }

    private int dipToPx(int dip) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (walkOrrun) {
            LinearGradient LinearGradient = new LinearGradient(xScale, 0, xScale, height, Color.RED, Color.YELLOW, Shader.TileMode.MIRROR);
            bokenlinePaint.setShader(LinearGradient);
        } else {
            LinearGradient LinearGradient = new LinearGradient(xScale, height, 0, 0, Color.RED, Color.BLUE, Shader.TileMode.MIRROR);
            bokenlinePaint.setShader(LinearGradient);
        }

        /**************************/
        if (type.contains("WEEK") || type.equals("WEEK")) {
            Xlable = XlistWeekLable;
        } else if (type.contains("MONTH") || type.equals("MONTH")) {
            Xlable = XlistMonthLable;
        } else if (type.contains("YEAR") || type.equals("YEAR")) {
            Xlable = XlistYearLable;
        } else {
            Xlable = XlistWeekLable;
        }
        /**************************/

        xScale = xLength / Xlable.size();
        //canvas.drawLine(XPoint+10,yPoint,XPoint+10,yPoint-yLength,xPaint);//画Y轴
        //画Y轴刻度
        /****************/
        for (int i = 0; i < Ylable.size(); i++) {
            if (i != 0) {

                for (int j = 0; j < xLength / 25; j++) {
                    if (j % 2 != 0) {
                        //虚线
                        canvas.drawLine(25 * j + XPoint - 25, yPoint - i * yScale, 25 * j + 10 + XPoint, yPoint - i * yScale, xStrokePaint);
                    }
                }
            }

            canvas.drawText(new DecimalFormat("0.00").format(Double.parseDouble(Ylable.get(i)) / 1000).equals("0.00") ? "0" : new DecimalFormat("0.00").format(Double.parseDouble(Ylable.get(i)) / 1000), XPoint - 60, yPoint - i * yScale + 10, yTextPaint);
        }
        canvas.drawText("(步数K)", XPoint - 75, yPoint - Ylable.size() * yScale + 20, yTextPaint);
        /****************/
        canvas.drawLine(XPoint, yPoint, xLength + XPoint, yPoint, xPaint);//画X轴

        //画X轴刻度
        /******************/
        for (int i = 0; i < Xlable.size(); i++) {
            canvas.drawCircle(XPoint + i * xScale, yPoint, 5, xCircle);
            if (type.contains("MONTH")) {
                canvas.drawText(Xlable.get(i), XPoint + i * xScale - 10, yPoint + 30, xTextPaint);
            }
            canvas.drawText(Xlable.get(i), XPoint + i * xScale - 10, yPoint + 30, xTextPaint);

        }
        /******************/

        int stop;
        for (int i = 0; i < map.size(); i++) {
            if (type.equals("WEEK")) {
                if (i == 0) {
                    stop = yPoint - (getYPoint(Integer.parseInt(new DecimalFormat("0").format(Double.parseDouble(map.get((map.size()) + "").toString())))));
                } else {
                    stop = yPoint - (getYPoint(Integer.parseInt(new DecimalFormat("0").format(Double.parseDouble(map.get((i) + "").toString())))));
                }
            } else {
                stop = yPoint - (getYPoint(Integer.parseInt(new DecimalFormat("0").format(Double.parseDouble(map.get((i + 1) + "").toString())))));
            }
            pointData.add(stop);
        }


        if (pointData.size() > 0) {
            if (pointData.size() - 1 > 0) {
                for (int i = 0; i < pointData.size() - 1; i++) {
                    canvas.drawLine(XPoint + i * xScale, pointData.get(i), XPoint + xScale + (i * xScale), pointData.get(i + 1), bokenlinePaint);
                    canvas.drawCircle(XPoint + i * xScale, pointData.get(i), 5, xCircle);
                    if (i == pointData.size() - 2) {
                        canvas.drawCircle(XPoint + (i + 1) * xScale, pointData.get(pointData.size() - 1), 5, xCircle);
                    }
                }
            } else {
                canvas.drawLine(XPoint, pointData.get(0), XPoint, pointData.get(0), bokenlinePaint);
                canvas.drawCircle(XPoint + 0 * xScale, pointData.get(0), 5, xCircle);
            }
        }


        pointData.clear();//清空数据，防止界面重复绘制
        map.clear();
        MaxNum = 0;
    }


    //获取点坐标
    private int getYPoint(int value) {
        if (MaxNum == 0) {
            List<Integer> va = new ArrayList<>();
            for (String str : Ylable) {
                va.add(Integer.parseInt(str));
            }
            if (!va.isEmpty()) {
                Collections.sort(va);
            }
            MaxNum = (double) va.get(va.size() - 1);
        }

        double str = (double) value / MaxNum;
        if (str == 1) {
            return yScale * 6;
        } else {
            return (int) ((str * 6) * (double) yScale);
        }
    }

    public void setMapData(Map<String, Object> map) {
        this.map = map;
    }

    public void setYLable(List<String> ylable) {
        this.Ylable = ylable;
    }

    public void setMonthLable(List<String> months) {
        this.XlistMonthLable = months;
    }

    public void setActivity(boolean walkOrrun) {
        this.walkOrrun = walkOrrun;
        invalidate();
    }


}
