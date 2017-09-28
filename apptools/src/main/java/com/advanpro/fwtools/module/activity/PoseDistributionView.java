package com.advanpro.fwtools.module.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.UiUtils;
import com.advanpro.fwtools.db.PoseLine;

import java.util.Date;
import java.util.List;

/**
 * Created by zengfs on 2016/1/25.
 * 姿势分布图
 */
public class PoseDistributionView extends LinearLayout {
    private String[] labels;
    private int colorLabel;
    private int labelSize;
    private int timeLabelSize;
    private int space;//标签与刻度的间隔，纵坐标标签与起始分布线间隔
    private int contentViewWidth;
    private int labelHeight;
    private int timeLabelHeight;
    private List<PoseLine> models;
    private HorizontalScrollView scrollView;
    private float timeLabelWidth;
    private ScaleGestureDetector scaleGestureDetector;
    private ContentView contentView;
    private int firstWidth;
    private int firstScrollX;
    private float firstCenter;
    private boolean first = true;

    public PoseDistributionView(Context context) {
        this(context, null);
    }

    public PoseDistributionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PoseDistributionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParent();
        addChildren(context);
    }

    private void addChildren(Context context) {
        addView(new YaxisView(context));
        scrollView = new HorizontalScrollView(context);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollView.setHorizontalScrollBarEnabled(false);
        LinearLayout layout = new LinearLayout(context);
        contentView = new ContentView(context);
        layout.addView(contentView, contentViewWidth, -1);
        scrollView.addView(layout, -2, -1);
        LayoutParams params = new LayoutParams( -1, -1);
        params.setMarginStart(UiUtils.dip2px(5));
        scrollView.setLayoutParams(params);
        scaleGestureDetector = new ScaleGestureDetector(context, gestureListener);
        scrollView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //如果是多点触摸，交给手势识别器处理触摸事件
                if (event.getPointerCount() >= 2) scaleGestureDetector.onTouchEvent(event);
                return false;
            }
        });
        addView(scrollView);
    }

    private void initParent() {
        colorLabel = getResources().getColor(R.color.content_text);
        labels = getResources().getStringArray(R.array.pose_types);
        labelSize = UiUtils.dip2px(14);
        timeLabelSize = UiUtils.dip2px(12);
        space = UiUtils.dip2px(5);
        contentViewWidth = UiUtils.dip2px(1440);
        Rect rect = new Rect();
        //测量字体高度
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(labelSize);
        paint.getTextBounds(labels[0], 0, labels[0].length(), rect);
        labelHeight = rect.bottom - rect.top;
        paint.setTextSize(timeLabelSize);
        String text = "00:00";
        paint.getTextBounds(text, 0, text.length(), rect);
        timeLabelHeight = rect.bottom - rect.top;
        timeLabelWidth = paint.measureText(text);
    }

    /**
     * 根据当前时间滚动分布图
     */
    public void scrollChart() {
        long millisInDay = DateUtils.getMillisInDay(new Date());
        //当前时间的标签在分布图的横坐标
        float textPos = timeLabelWidth / 2 + millisInDay * (contentViewWidth - timeLabelWidth) / (3600000 * 24);
        //需要显示的宽度
        float showWidth = UiUtils.getScreenWidth() * 0.75f;
        if (textPos < showWidth) return;
        scrollView.scrollTo((int) (textPos - showWidth), 0);
    }

    ScaleGestureDetector.SimpleOnScaleGestureListener gestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            ViewGroup.LayoutParams params = contentView.getLayoutParams();
            //记录刚开始时位置信息，用于固定缩放中心点
            if (first && detector.isInProgress()) {
                first = false;
                firstWidth = params.width;
                firstCenter = detector.getFocusX();
                firstScrollX = scrollView.getScrollX();
            }
            //设置缩放比例，限制缩放大小
            if (scaleFactor < 1) {
                if (params.width < UiUtils.dip2px(1440)) {
                    return false;
                }
                scaleFactor *= 0.98;
            } else if (scaleFactor > 1) {
                if (params.width > UiUtils.dip2px(14400)) {
                    return false;
                }
                scaleFactor *= 1.02;
            }
            params.width *= scaleFactor;
            contentViewWidth = params.width;
            contentView.setLayoutParams(params);
            int toScrollX = (int) ((firstScrollX + firstCenter) * params.width / firstWidth - firstCenter);
            if (toScrollX > 0) scrollView.scrollTo(toScrollX, 0);//保持缩放中心点不变
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            first = true;
        }
    };


    /**
     * 纵坐标标签
     */
    public class YaxisView extends View {
        private Paint paint;

        public YaxisView(Context context) {
            super(context);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension((int) getLabelMaxWidth(), MeasureSpec.getSize(heightMeasureSpec));
        }

        //获取纵坐标标签的文字最大宽度
        private float getLabelMaxWidth() {
            paint.setTextSize(labelSize);
            float max = 0;
            for (String label : labels) {
                float textWidth = paint.measureText(label + "-");
                if (textWidth > max) max = textWidth;
            }
            return max;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            //纵坐标标签间隔
            float labelSpace = (getHeight() - 2 * (space + timeLabelHeight) -
                    labelHeight) / (labels.length - 1);
            //画纵坐标标签
            paint.setStrokeWidth(0);
            paint.setTextSize(labelSize);
            paint.setColor(colorLabel);
            for (int i = 0; i < labels.length; i++) {
                canvas.drawText(labels[i] + "-", 0, i * labelSpace + labelHeight, paint);
            }
        }
    }

    /**
     * 图表内容
     */
    public class ContentView extends View {
        private Paint paint;
        private LinearGradient linearGradient;
        //是否实例化过渐变色
        private boolean isInstantiated;

        public ContentView(Context context) {
            super(context);
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            paint.setShader(null);
            //分布线的总高度
            int lineTotalHeight = getHeight() - 2 * (space + timeLabelHeight) - labelHeight;
            float lineSpace = lineTotalHeight / (labels.length - 1);//纵坐标标签间隔
            //画刻度、画时间标签
            paint.setStrokeWidth(0);
            paint.setColor(colorLabel);
            paint.setTextSize(timeLabelSize);
            float lineTotalWidth = getWidth() - timeLabelWidth;//刻度总长度
            float fromY = lineTotalHeight + labelHeight + space;
            canvas.drawLine(timeLabelWidth / 2, fromY, getWidth() - timeLabelWidth / 2, fromY, paint);
            int level = UiUtils.px2dip(getWidth()) / 1440;
            for (int i = 0; i <= 96; i++) {
                float x = timeLabelWidth / 2 + i * lineTotalWidth / 96;
                float toY;
                if (i % 4 == 0) {
                    toY = fromY + timeLabelHeight;
                    String text = String.format("%02d:%02d", i / 4, 0);
                    canvas.drawText(text, x - timeLabelWidth / 2, toY + timeLabelSize, paint);
                } else {
                    toY = fromY + timeLabelHeight;
                    if (level >= 4) {
                        String text = String.format("%02d:%02d", i / 4, (i % 4) * 15);
                        canvas.drawText(text, x - timeLabelWidth / 2, toY + timeLabelSize, paint);
                    } else if (level >= 2 && i % 2 == 0) {
                        String text = String.format("%02d:%02d", i / 4, 30);
                        canvas.drawText(text, x - timeLabelWidth / 2, toY + timeLabelSize, paint);
                    } else {
                        toY = fromY + timeLabelHeight / 2;
                    }
                }
                canvas.drawLine(x, fromY, x, toY, paint);
            }
            //画分布线
            if (models != null) {
                int lineWidth = UiUtils.dip2px(2);
                paint.setStrokeWidth(lineWidth);
                paint.setShader(getShader());
                for (int i = 0; i < models.size(); i++) {
                    PoseLine model = models.get(i);
                    float unit = lineTotalWidth / (3600000 * 24);
                    float startX = timeLabelWidth / 2 + model.startMillis * unit;
                    float stopX = timeLabelWidth / 2 + model.endMillis * unit;
                    float y = labelHeight / 2 + UiUtils.dip2px(3) + (model.type - 1) * lineSpace;
                    canvas.drawLine(startX, y, stopX, y, paint);
                    //如果上段结束时间和本段开始时间相同，将两段首尾相连
                    if (i > 0) {
                        PoseLine lastModel = models.get(i - 1);
                        if (model.startMillis == lastModel.endMillis && model.type != lastModel.type) {
                            float startY = labelHeight / 2 + UiUtils.dip2px(3) + (lastModel.type - 1) * lineSpace;
                            int sign = model.type < lastModel.type ? 1 : -1;
                            canvas.drawLine(startX, startY + sign * lineWidth / 2, startX, y - sign * lineWidth / 2, paint);
                        }
                    }
                }
            }
        }

        //获取渐变色实例，避免在onDraw中多次实例化
        private LinearGradient getShader() {
            if (!isInstantiated) {
                isInstantiated = true;
                linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(),
                        0xFFFDD858, 0xFFF54231, LinearGradient.TileMode.CLAMP);
            }
            return linearGradient;
        }
    }

    /**
     * 设置图标数据源
     */
    public void setData(List<PoseLine> poseLines) {
        if (poseLines != null) {
            this.models = poseLines;
            invalidate();
        }
    }
}
