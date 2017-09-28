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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by DEV002 on 2016/3/25.
 */
public class BrokenLinePillar extends View {

    public int width;//view宽
    public int height;//view高
    private double MaxNum = 0;

    private int xPoint;//X轴坐标起点
    private int yPoint;//Y轴坐标起点
    private int xLenght;//X轴长度
    private int yLength;//Y轴长度
    private int xScale;//X轴刻度
    private int yScale;//Y轴刻度
    private int scaleLength;//刻度长度


    private Paint line;//坐标轴画笔工具
    private Paint pillar;//画柱状图工具
    private Paint xText;//X轴文字画笔
    private Paint xLine;//X轴画笔

    private String pillartype;

    private boolean walkOrrunning = true;


    private Map<String, Object> map = new HashMap<>();

    private String[] XWeekLabel;//X轴周显示刻度
    private String[] XMonthLabel;//X轴月显示刻度
    private String[] XYearLabel;//X轴年显示刻度
    private String[] YWeekLable;
    private String[] YMonthLable;
    private String[] YYearLable;


    private List<String> XlistWeekLable;
    private List<String> XlistMonthLable;
    private List<String> XlistYearLable;
    private List<String> YlistWeekLable;
    private List<String> YlistMonthLable;
    private List<String> YlistYearLable;


    private List<String> Xlable;
    private List<String> Ylable = new ArrayList<>();

    /*************************/


    public BrokenLinePillar(Context context) {
        super(context);
        init();
    }

    public BrokenLinePillar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BrokenLinePillar);
        pillartype = array.getString(R.styleable.BrokenLinePillar_pillar);
        array.recycle();
        init();
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
        scaleLength = 10;
        xLenght = width - xPoint;
        yLength = height - 30 - xPoint;
        xPoint = 80;
        yPoint = height - dipToPx(20);
        yScale = yLength / 6;
        setMeasuredDimension(width, height);
    }

    protected int dipToPx(int dip) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }


    public void init() {
        XlistWeekLable = new ArrayList<>();
        XlistMonthLable = new ArrayList<>();
        XlistYearLable = new ArrayList<>();
        YlistWeekLable = new ArrayList<>();
        YlistMonthLable = new ArrayList<>();
        YlistYearLable = new ArrayList<>();

        Xlable = new ArrayList<>();

        XWeekLabel = getResources().getStringArray(R.array.week);
        XMonthLabel = getResources().getStringArray(R.array.xMonth);
        XYearLabel = getResources().getStringArray(R.array.year);

        YWeekLable = getResources().getStringArray(R.array.ylable_week);
        YMonthLable = getResources().getStringArray(R.array.ylable_month);
        YYearLable = getResources().getStringArray(R.array.ylable_year);


        for (String s : XWeekLabel) {
            XlistWeekLable.add(s);
        }
        for (String s : XMonthLabel) {
            XlistMonthLable.add(s);
        }
        for (String s : XYearLabel) {
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


        line = new Paint();
        line.setColor(Color.BLACK);
        line.setAntiAlias(true);

        pillar = new Paint();
        pillar.setAntiAlias(true);
        pillar.setStrokeWidth(dipToPx(12));

        xText = new Paint();
        xText.setAntiAlias(true);
        xText.setColor(Color.WHITE);
        xText.setTextSize(dipToPx(10));

        xLine = new Paint();
        xLine.setColor(Color.WHITE);
        xLine.setAntiAlias(true);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (walkOrrunning) {
            LinearGradient LinearGradient = new LinearGradient(xScale, 0, xScale, height, Color.RED, Color.YELLOW, Shader.TileMode.MIRROR);
            pillar.setShader(LinearGradient);
        } else {
            LinearGradient LinearGradient = new LinearGradient(xScale, height, 0, 0, Color.RED, Color.BLUE, Shader.TileMode.MIRROR);
            pillar.setShader(LinearGradient);
        }
        /**************************/
        if (pillartype.contains("WEEK") || pillartype.equals("WEEK")) {
            Xlable = XlistWeekLable;
        } else if (pillartype.contains("MONTH") || pillartype.equals("MONTH")) {
            Xlable = XlistMonthLable;
        } else if (pillartype.contains("YEAR") || pillartype.equals("YEAR")) {
            Xlable = XlistYearLable;
        } else {
            Xlable = XlistWeekLable;
        }
        /**************************/

        xScale = xLenght / (Xlable.size() + 2);
        //canvas.drawLine(xPoint+10,yPoint,xPoint+10,yPoint-yLength,xLine);//画Y轴
        canvas.drawLine(xPoint, yPoint, xLenght + xPoint - 20, yPoint, xLine);//画X轴
        canvas.drawText("(公里)", xPoint - 60, yPoint - Ylable.size() * yScale + 20, xText);
        /*****************/
        for (int i = 0; i < Ylable.size(); i++) {
            if (i != 0) {
                for (int j = 0; j < xLenght / 25; j++) {
                    if (j % 2 != 0) {
                        //虚线
                        canvas.drawLine(25 * j + xPoint - 25, yPoint - i * yScale, 25 * j + 10 + xPoint, yPoint - i * yScale, line);
                    }
                }
            }
            canvas.drawText(Ylable.get(i), xPoint - dipToPx(20), yPoint - i * yScale + dipToPx(5), xText);
        }
        /*****************/
        for (int i = 0; i < map.size(); i++) {
            if (pillartype.equals("WEEK")) {
                if (i == 0) {
                    Double d = Double.parseDouble(map.get(map.size() + "").toString());//取周日的数据
                    if (Double.parseDouble(new DecimalFormat("0.00").format(d)) > 0.00) {
                        //canvas.drawCircle(xPoint + xScale + i * xScale, Float.parseFloat((yPoint - getY(Double.parseDouble(map.get(i + 1 + "").toString())) + 8) + ""), 10, pillar);
                        if ((getY(d) - dipToPx(4)) > dipToPx(6)) {
                            canvas.drawLine(xPoint + xScale + i * xScale, yPoint, xPoint + xScale + i * xScale, (float) (yPoint - (getY(d) - dipToPx(4))), pillar);
                            canvas.drawCircle(xPoint + xScale + i * xScale, (float) (yPoint - (getY(d) - dipToPx(4))), dipToPx(6), pillar);
                        }

                    }
                } else {
                    Double d = Double.parseDouble(map.get(i + "").toString());//取周一到周六的数据
                    if (Double.parseDouble(new DecimalFormat("0.00").format(d)) > 0.00) {
                        //canvas.drawCircle(xPoint + xScale + i * xScale, Float.parseFloat((yPoint - getY(Double.parseDouble(map.get(i + 1 + "").toString())) + 8) + ""), 10, pillar);
                        if ((getY(d) - dipToPx(4)) > dipToPx(6)) {
                            canvas.drawLine(xPoint + xScale + i * xScale, yPoint, xPoint + xScale + i * xScale, (float) (yPoint - (getY(d) - dipToPx(4))), pillar);
                            canvas.drawCircle(xPoint + xScale + i * xScale, (float) (yPoint - (getY(d) - dipToPx(4))), dipToPx(6), pillar);
                        }

                    }
                }
            } else {
                Double d = Double.parseDouble(map.get(i + 1 + "").toString());
                if (Double.parseDouble(new DecimalFormat("0.00").format(d)) > 0.00) {
                    //canvas.drawCircle(xPoint + xScale + i * xScale, Float.parseFloat((yPoint - getY(Double.parseDouble(map.get(i + 1 + "").toString())) + 8) + ""), 10, pillar);
                    if ((getY(d) - dipToPx(4)) > dipToPx(6)) {
                        canvas.drawLine(xPoint + xScale + i * xScale, yPoint, xPoint + xScale + i * xScale, (float) (yPoint - (getY(d) - dipToPx(4))), pillar);
                        canvas.drawCircle(xPoint + xScale + i * xScale, (float) (yPoint - (getY(d) - dipToPx(4))), dipToPx(6), pillar);
                    }

                }
            }
        }


        for (int i = 0; i < Xlable.size(); i++) {
            canvas.drawCircle(xPoint + (i) * xScale + xScale, yPoint, 4, xLine);
            if (pillartype.contains("MONTH")) {
                canvas.drawText(Xlable.get(i), xPoint + (i) * xScale - scaleLength*3 + xScale, yPoint + scaleLength * 2 + 10, xText);
            } else {
                canvas.drawText(Xlable.get(i), xPoint + (i) * xScale - scaleLength*2 + xScale, yPoint + scaleLength * 2 + 10, xText);
            }
        }
        MaxNum = 0;
        map.clear();
    }

    private double getY(double value) {
        DecimalFormat format = new DecimalFormat("0.00");
        format.setRoundingMode(RoundingMode.FLOOR);
        if (MaxNum == 0) {
            List<Double> list = new ArrayList<>();
            for (String str : Ylable) {
                list.add(Double.parseDouble(new DecimalFormat("0.00").format(Double.parseDouble(str))));
            }
            Collections.sort(list);
            if (!list.isEmpty()) {
                Collections.sort(list);
            }
            MaxNum = list.get(list.size() - 1);
        }
        double rev = Double.parseDouble(format.format(value));
        double str = rev / MaxNum;
        if (str == 1) {
            return yScale * 6;
        } else {
            return ((str * 6) * (double) yScale);
        }

    }


    public void setMapData(Map<String, Object> map) {
        this.map = map;
    }

    public void setYlabe(List<String> ylables) {
        this.Ylable = ylables;
    }

    public void setMonthLable(List<String> months) {
        this.XlistMonthLable = months;
    }

    public void setActivity(boolean walkOrrunning) {
        this.walkOrrunning = walkOrrunning;
        invalidate();
    }


}
