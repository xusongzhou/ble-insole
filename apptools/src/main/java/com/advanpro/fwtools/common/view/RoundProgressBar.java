package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by zengfs on 2016/1/13.
 * 圆形进度条，风格有环形和填充。进度条圆环支持颜色渐变
 */
public class RoundProgressBar extends View {
	//用于设置渐变色的颜色集
	private int[] colors;
	//用于设置渐变色的开始色
	private int startColor;
	//用于设置渐变色的结束色
	private int endColor;	
	//默认圆环的颜色
	private int defaultRoundColor;    
	//圆环进度的颜色
	private int progressRoundColor;
	//中间进度百分比的字符串的颜色
	private int textColor;
	//中间进度百分比的字符串的字体
	private float textSize;
	//默认圆环的宽度
	private float defaultRoundWidth;
	//进度圆环的宽度
	private float progressRoundWidth;
	//小圆点的半径
	private float dotRadius;
	//最大进度
	private int max;
	//当前进度
	private int progress;
	//是否显示中间的进度
	private boolean isTextEnabled;
	//是否画小圆点
	private boolean isDotEnabled;
	//是否显示默认圆环
	private boolean isDefaultRoundEnabled;
	//渐变色是否使用颜色数组
	private boolean isArrayColorEnabled;
	//进度条的风格
	private Paint.Style roundStyle;
	//进度环的颜色风格
	private ColorStyle colorStyle;
	private Paint paint;
	//用于定义的圆弧的形状和大小的界限
	private RectF oval;
	//用于记录上次渐变色的参数
	private float cx, cy;
	//上次渐变色实例
	private SweepGradient sweepGradient;
	//是否实例化过渐变色
	private boolean isInstantiated;
	//测量字体高度
	private Rect rect;
	//进度条帽的形状
	private Paint.Cap cap;
	//进度条开始角度
	private float angle;

	public enum ColorStyle {
		SOLID, GRADIENT
	}

	public RoundProgressBar(Context context) {
		this(context, null);
	}

	public RoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		defaultRoundColor = Color.GRAY;
		progressRoundColor = Color.GREEN;
		textColor = Color.GREEN;
		textSize = 15;
		defaultRoundWidth = 5;
		max = 100;
		isTextEnabled = true;
		roundStyle = Paint.Style.STROKE;
		dotRadius = 14;
		isDotEnabled = false;
		colorStyle = ColorStyle.SOLID;
		isDefaultRoundEnabled = true;
		startColor = 0xFFFDD858;
		endColor = 0xFFF54231;
		progressRoundWidth = defaultRoundWidth;
		oval = new RectF();
		rect = new Rect();
		cap = Paint.Cap.BUTT;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		paint.setShader(null);
		paint.setStyle(roundStyle); 
		paint.setStrokeCap(cap);
		//画最外层的大圆环
		float centerX = getWidth() / 2; //获取圆心的x坐标        
		float radius = centerX - dotRadius - defaultRoundWidth / 2; //圆环的半径

		if (isDefaultRoundEnabled) {
			paint.setColor(defaultRoundColor); //设置圆环的颜色
			paint.setStrokeWidth(defaultRoundWidth); //设置圆环的宽度
			canvas.drawCircle(centerX, centerX, radius, paint); //画出圆环 
		}

		//画进度百分比
		if (isTextEnabled) {
			paint.setStrokeWidth(0);
			paint.setColor(textColor);
			paint.setTextSize(textSize);
			paint.setTypeface(Typeface.DEFAULT_BOLD); //设置字体
			//中间的进度百分比，先转换成float在进行除法运算，不然都为0
			int percent = (int) (((float) progress / max) * 100);
			String text = percent + "%";
			float textWidth = paint.measureText(text);//测量字体宽度，我们需要根据字体的宽度设置在圆环中间
			paint.getTextBounds(text, 0, text.length(), rect);
			int textHeight = rect.bottom - rect.top;
			if (percent != 0 && roundStyle == Paint.Style.STROKE) {
				//画出进度百分比
				canvas.drawText(text, centerX - textWidth / 2, centerX + textHeight / 2, paint);
			}
		}

		//画圆弧 ，画圆环的进度
		canvas.rotate(-180 + angle, centerX, centerX);
		paint.setStrokeWidth(progressRoundWidth); //设置圆环的宽度

		oval.set(centerX - radius, centerX - radius, centerX + radius, centerX + radius);
		SweepGradient shader = null;
		switch (colorStyle) {
			case SOLID:
				paint.setShader(null);
				paint.setColor(progressRoundColor);  //设置进度的颜色
				break;
			case GRADIENT:
				//设置环形渐变色
				shader = getShader(centerX, centerX);
				paint.setShader(shader);
				break;
		}
		switch (roundStyle) {
			case STROKE:
				canvas.drawArc(oval, 0, 360 * progress / max, false, paint);  //根据进度画圆弧			
				break;
			case FILL:
			case FILL_AND_STROKE:
				if (progress != 0)
					canvas.drawArc(oval, 0, 360 * progress / max, true, paint);  //根据进度画圆
				break;
		}

		//画圆环上小圆点
		if (isDotEnabled) {
			paint.setStyle(Paint.Style.FILL);
			switch (colorStyle) {
				case SOLID:
					paint.setColor(progressRoundColor);
					break;
				case GRADIENT:
					if (progress == 0) {
						paint.setShader(null);
						paint.setColor(isArrayColorEnabled ? colors[0] : startColor);
					} else if (progress == max) {
						paint.setShader(null);
						paint.setColor(isArrayColorEnabled ? colors[colors.length - 1] : endColor);
					} else {
						paint.setShader(shader);
					}
					break;
			}
			//小圆点到圆环的半径
			canvas.drawCircle((float) (centerX + radius * Math.cos(Math.toRadians(360 * progress / max))),
					(float) (centerX + radius * Math.sin(Math.toRadians(360 * progress / max))), dotRadius, paint);
		}
	}

	//获取渐变色实例，避免在onDraw中多次实例化
	private SweepGradient getShader(float cx, float cy) {
		if (this.cx != cx || this.cy != cy || !isInstantiated) {
			this.cx = cx;
			this.cy = cy;
			isInstantiated = true;
			if (isArrayColorEnabled) sweepGradient = new SweepGradient(cx, cy, colors, null);
			else sweepGradient = new SweepGradient(cx, cy, startColor, endColor);
		}
		return sweepGradient;
	}
	
	public synchronized int getMax() {
		return max;
	}

	/**
	 * 设置进度的最大值
	 */
	public synchronized void setMax(int max) {
		if (max < 0) {
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度，需要同步
	 */
	public synchronized int getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 */
	public synchronized void setProgress(int progress) {
		if (progress < 0) {
			progress = 0;
		}
		if (progress > max) {
			progress = max;
		}
		if (progress <= max) {
			this.progress = progress;
			postInvalidate();
		}

	}

	/**
	 * 设置进度条开始角度，0~360
	 */
	public void setAngle(float angle) {
		if (angle > 360) angle = 360;
		if (angle < 0) angle = 0;
		this.angle = angle;
	}
	
	/**
	 * 进度条帽的形状
	 */
	public void setStrokeCap(Paint.Cap cap) {
		this.cap = cap;
	}
	
	/**
	 * 设置进度圆环的颜色风格
	 */
	public void setColorStyle(ColorStyle colorStyle) {
		this.colorStyle = colorStyle;
	}

	/**
	 * 设置默认圆环的颜色
	 */
	public void setDefaultRoundColor(int defaultRoundColor) {
		this.defaultRoundColor = defaultRoundColor;
	}

	/**
	 * 设置进度圆环颜色
	 */
	public void setProgressRoundColor(int progressRoundColor) {
		this.progressRoundColor = progressRoundColor;
	}

	/**
	 * 设置进度圆环颜色
	 */
	public void setProgressRoundColor(int[] colors) {
		this.colors = colors;
		isArrayColorEnabled = true;
	}

	/**
	 * 设置进度圆环颜色
	 */
	public void setProgressRoundColor(int startColor, int endColor) {
		this.startColor = startColor;
		this.endColor = endColor;
		isArrayColorEnabled = false;
	}

	/**
	 * 设置进度文本的颜色
	 */
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	/**
	 * 设置进度文本的大小
	 */
	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	/**
	 * 设置默认圆环的宽度
	 */
	public void setDefaultRoundWidth(float defaultRoundWidth) {
		this.defaultRoundWidth = defaultRoundWidth;
	}

	/**
	 * 设置进度圆环的宽度
	 */
	public void setProgressRoundWidth(float progressRoundWidth) {
		this.progressRoundWidth = progressRoundWidth;
	}

	/**
	 * 设置小圆点的半径长度
	 */
	public void setDotRadius(float dotRadius) {
		this.dotRadius = dotRadius;
	}

	/**
	 * 设置进度条整体风格
	 */
	public void setRoundStyle(Paint.Style roundStyle) {
		this.roundStyle = roundStyle;
	}

	/**
	 * 设置进度文本是否显示
	 */
	public void setTextEnabled(boolean textEnabled) {
		isTextEnabled = textEnabled;
	}

	/**
	 * 设置小圆点是否显示
	 */
	public void setDotEnabled(boolean dotEnabled) {
		isDotEnabled = dotEnabled;
	}

	/**
	 * 设置默认圆环是否显示
	 */
	public void setDefaultRoundEnabled(boolean defaultRoundEnabled) {
		isDefaultRoundEnabled = defaultRoundEnabled;
	}
}
