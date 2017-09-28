package com.advanpro.fwtools.module.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.UiUtils;

/**
 * Created by zengfs on 2016/1/26.
 * 姿势时长图
 */
public class PoseDurationView extends View {
	private static final int barBg = 0xAA3D3D3E;//横条背景
	private static final int barColor = 0xBBCE2F1E;//时长横条色
	private Paint paint;
	private String[] labels;
	private float[] hours;
	private int colorLabel;
	private int labelSize;
	private int timeLabelSize;
	private int space;//标签与刻度的间隔，纵坐标标签与起始分布线间隔
	private Rect rect;
	private RectF rectF;
	private int range;//量程

	public PoseDurationView(Context context) {
		this(context, null);
	}

	public PoseDurationView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PoseDurationView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		colorLabel = getResources().getColor(R.color.content_text);
		labels = getResources().getStringArray(R.array.pose_types);
		labelSize = UiUtils.dip2px(14);
		timeLabelSize = UiUtils.dip2px(12);
		space = UiUtils.dip2px(5);
		rect = new Rect();
		rectF = new RectF();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		getMeasurementRange();
		paint.setTextSize(timeLabelSize);
		String bracketsHour = getResources().getString(R.string.brackets_hour);
		float unitWidth = paint.measureText(bracketsHour);
		float startX = getLabelMaxWidth() + space;
		float barWidth = getWidth() - startX - 2 * space - unitWidth;
		paint.getTextBounds("0", 0, "0".length(), rect);
		int timeLabelHeight = rect.bottom - rect.top;//用做长刻度高度
		paint.getTextBounds(bracketsHour, 0, bracketsHour.length(), rect);
		int unitHeight = rect.bottom - rect.top;//中文显示高度大于数字
		int barTotalHeight = getHeight() - 3 * space - timeLabelHeight - unitHeight;
		float barHeight = barTotalHeight / (2 * labels.length - 1);		
		paint.setTextSize(labelSize);
		paint.getTextBounds(labels[0], 0, labels[0].length(), rect);
		float labelHeight = rect.bottom - rect.top;		
		
		//画纵坐标标签、右边时长文本
		paint.setStrokeWidth(0);
		paint.setColor(colorLabel);
		for (int i = 0; i < labels.length; i++) {
			canvas.drawText(labels[i] + "-", 0, barHeight / 2 + i * 2 * barHeight + labelHeight / 2 - UiUtils.dip2px(2), paint);
			if (hours != null) {				
				if (hours[i] > 24) hours[i] = 24;
				String text = ((int)hours[i]) == hours[i] ? (int)hours[i] + "" : String.format("%.1f", hours[i]);
				float textWidth = paint.measureText(text);
				canvas.drawText(text, startX + barWidth + (2 * space + unitWidth) / 2 - textWidth / 2, 
						barHeight / 2 + i * 2 * barHeight + labelHeight / 2 - UiUtils.dip2px(2), paint);
			}			
		}
		//刻度、时间标签
		paint.setTextSize(timeLabelSize);
		int rank = range <= 12 ? 4 : 1;
		for (int i = 0; i <= range * rank; i++) {
			float x = startX + i * barWidth / (range * rank); 
			float fromY = barTotalHeight + 2 * space;
			float toY;
			if (i % (rank == 4 ? 4 : 2) == 0) {
				String text = i / rank + "";
				float textWidth = paint.measureText(text);
			    toY = fromY + timeLabelHeight;
				canvas.drawText(text, x - textWidth / 2, toY + space + timeLabelHeight, paint);
				//画单位文本
				if (i == range * rank) {
					canvas.drawText(bracketsHour, x + 2 * space, toY + space + timeLabelHeight, paint);
				}
			} else {
				toY = fromY + timeLabelHeight / 2;
			}
			canvas.drawLine(x, fromY, x, toY, paint);			
		}
		//画横条
		paint.setStyle(Paint.Style.FILL);		
		for (int i = 0; i < labels.length; i++) {
			//背景
			paint.setColor(barBg);
			rectF.set(startX, 2 * i * barHeight, startX + barWidth, 2 * i * barHeight + barHeight);
			canvas.drawRoundRect(rectF, UiUtils.dip2px(2.5f), UiUtils.dip2px(2.5f), paint);
			//时长条
			if (hours != null) {
				paint.setColor(barColor);
                float h = hours[i];
                if (hours[i] < 300f / 3600) h = 0;//小于5分钟当作0小时
				float ratio = h / range;
				if (ratio > 1) ratio = 1;
				rectF.set(startX, 2 * i * barHeight, startX + barWidth * ratio, 2 * i * barHeight + barHeight);
				canvas.drawRoundRect(rectF, UiUtils.dip2px(2.5f), UiUtils.dip2px(2.5f), paint);
			}
		}
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

	//量程
	private void getMeasurementRange() {
		range = 7;
		if (hours != null) {
			for (float hour : hours) {
				if (hour > range) {					
					range = (int) hour + 1;
				}
			}
		}
		if (range >= 12 && range % 2 != 0) range++;
		if (range > 24) range = 24;		
	}
	
	/**
	 * 按跑步，步行，站立，平坐的顺序
	 */
	public void setData(float... hours) {
		if (hours == null || hours.length != labels.length) {
			throw new IllegalArgumentException("参数不合法");
		}
		this.hours = hours;
		invalidate();
	}
}
