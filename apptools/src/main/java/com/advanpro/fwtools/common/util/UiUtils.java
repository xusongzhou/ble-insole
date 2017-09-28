package com.advanpro.fwtools.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import com.advanpro.fwtools.MyApplication;

/**
 * Created by Zeng on 2015/7/13.
 * 其中很多方法都依赖于MyApplication类
 */
public class UiUtils {
    
    private static Toast mToast;
    
    /**
	 * 获取屏幕宽度
	 */
	public static int getScreenWidth() {
		return getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 获取屏幕高度
	 */
	public static int getScreenHeight() {
		return getResources().getDisplayMetrics().heightPixels;
	}
    
    /** 
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素) 
     */  
    public static int dip2px(float dpValue) {  
        final float scale = getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static int px2dip(float pxValue) {  
        final float scale = getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    } 
    
	public static Resources getResources() {
		return MyApplication.getInstance().getResources();
	}

	/**
	 * 获取strings.xml文件中的字符串数组
	 */
	public static String[] getStringArray(int id) {
		return getResources().getStringArray(id);
	}

	public static String getString(int id) {
		return getResources().getString(id);
	}
	
	/**
	 * 获取上下文，实质：application
	 */
	public static Context getContext() {
		return MyApplication.getInstance();
	}
	
	public static View inflate(int res) {
		return inflate(res, null);
	}

	public static View inflate(int res, ViewGroup root) {
		return View.inflate(getContext(), res, root);
	}

	/**
	 * 将任务放到主线程执行。无需关心当前在什么线程，直接调用
	 */
	public static void runOnUiThread(Runnable runnable) {
		if (isMainThread()) {
		    runnable.run();
		} else {
		    MyApplication.getHandler().post(runnable);
		}
	}

	/**
	 * 获取dimens.xml文件中的属性值
	 */
	public static int getDimens(int resId) {
		return (int) getResources().getDimension(resId);
	}

	/**
	 * 延时执行Runnable任务
	 */
	public static boolean postDelayed(Runnable task, long delayMillis) {
		return MyApplication.getHandler().postDelayed(task, delayMillis);
	}

	/**
	 * 停止Runnable任务
	 */
	public static void cancel(Runnable task) {
		MyApplication.getHandler().removeCallbacks(task);
	}

	/**
	 * 判断当前是否是主线程
	 */
	public static boolean isMainThread() {
		return Process.myTid() == MyApplication.getMainTid();
	}

	/**
	 * 可以在任意线程调用此方法打印Toast
	 */
	public static void ShowToast(final CharSequence text, final int duration) {
		if (isMainThread()) {
			Toast.makeText(getContext(), text, duration).show();
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), text, duration).show();
				}
			});
		}
	}

    /**
	 * 任意线程单例Toast，重复调用会覆盖，只显示最新的
	 */
	public static void showToastImmediately(final CharSequence text, final int duration) {
		if (isMainThread()) {
			showSingleToast(text, duration);
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showSingleToast(text, duration);
				}
			});
		}
	}
    
    /**
	 * 单例的Toast，重复调用会覆盖，只显示最新的
	 */
	public static void showSingleToast(CharSequence text, int duration) {
		if (mToast == null) {
			mToast = Toast.makeText(getContext(), "", duration);
		}
		mToast.setText(text);
		mToast.show();
	}

	/**
	 * 单例的Toast，重复调用会覆盖，只显示最新的
	 */
	public static void showSingleToast(int resId, int duration) {
		if (mToast == null) {
			mToast = Toast.makeText(getContext(), "", duration);
		}
		mToast.setText(resId);
		mToast.show();
	}
    
    /**
	 * 获取资源图片
	 */
	public static Drawable getDrawable(int resId) {
		return getResources().getDrawable(resId);
	}
	
	/**
	 * 获取资源颜色
	 */
	public static int getColor(int resId) {
		return getResources().getColor(resId);
	}
	
	/**
	 * 将自己从容器中移除
	 */
	public static void removeFromContainer(View view) {
		ViewParent parent = view.getParent();
		if (parent instanceof  ViewGroup) {
		    ViewGroup group = (ViewGroup) parent;
			group.removeView(view);
		}
	}

	/**
	 * 获取状态栏高度
	 */
	public static int getStatusBarHeight(Context context) {
		int result = 0;
		Resources res = context.getResources();
		int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = res.getDimensionPixelSize(resourceId);
		}
		return result;
	}

    /**
     * 将方形bitmap转换为圆形bitmap
     */
    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        int x = bitmap.getWidth();
        canvas.drawCircle(x / 2, x / 2, x / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }
    
    public static void sendBroadcast(Intent intent) {
        getContext().sendBroadcast(intent);
    }
}
