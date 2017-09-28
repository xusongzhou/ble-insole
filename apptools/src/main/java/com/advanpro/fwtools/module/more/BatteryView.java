package com.advanpro.fwtools.module.more;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zengfs on 2016/1/26.
 * 电量控件
 */
public class BatteryView extends View {
	
	private Paint paint;
	private RectF rectF;
	private int percent;
	private Rect rect;
	
	public BatteryView(Context context) {
		this(context, null);
	}

	public BatteryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rectF = new RectF();
		rect = new Rect();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//电池外框
		int stroke = getWidth() / 25;
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(stroke);
		paint.setColor(Color.WHITE);
		rectF.set(stroke / 2, stroke / 2, getWidth() * 0.9f - stroke / 2, getHeight() - stroke /2);
		canvas.drawRoundRect(rectF, stroke / 2, stroke / 2, paint);
		paint.setStyle(Paint.Style.FILL);
		float x = getWidth() * 0.9f - stroke / 2;
		rectF.set(x, getHeight() / 6, x + getWidth() / 10, getHeight() * 5 / 6);
		canvas.drawRoundRect(rectF, stroke / 2, stroke / 2, paint);
		//进度条
		paint.setColor(getProgressColor());
		canvas.drawRect(stroke, stroke, (getWidth() * 0.9f - stroke) * percent / 100, getHeight() - stroke, paint);
		//进度文本
		float textSize = (getHeight() - 2 * stroke) * 0.65f;
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(Color.WHITE);
		paint.setTextSize(textSize);
		String text = percent + "%";
		float textWidth = paint.measureText(text);
		paint.getTextBounds(text, 0, text.length(), rect);
		canvas.drawText(text, getWidth() * 0.45f - textWidth / 2, getHeight() / 2 + (rect.bottom - rect.top) / 2, paint);
	}

	/**
	 * 设置电量百分比
	 */
	public void setPercent(int percent) {
		if (percent < 0) percent = 0;
		if (percent > 100) percent = 100;
		this.percent = percent;
		invalidate();
	}
	
	private int getProgressColor() {
		if (percent <= 20) return Color.RED;
		else if (percent <= 80) return 0xFFEEAF22;
		else return 0xFF00C30E;
	}
}
