package com.advanpro.fwtools.common.manager;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.advanpro.fwtools.R;
import com.advanpro.fwtools.common.util.UiUtils;

/**
 * Created by zengfs on 2016/3/11.
 */
public class ToastMgr {
	
	private static Toast textToast;
	private static TextView tvText;
	
	/**
	 * 生成默认文本Toast
	 */
	public static void showTextToast(Context context, int duration, String text) {
		if (textToast == null) {
			textToast = new Toast(context);
			textToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, UiUtils.dip2px(80));			
			View view = View.inflate(context, R.layout.toast_text, null);
			tvText = (TextView) view.findViewById(R.id.tv);
			textToast.setView(view);
		}
		tvText.setText(text);
		textToast.setDuration(duration);
		textToast.show();
	}

	/**
	 * 生成默认文本Toast
	 */
	public static void showTextToast(Context context, int duration, int resId) {
		if (textToast == null) {
			textToast = new Toast(context);
			textToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, UiUtils.dip2px(80));
			View view = View.inflate(context, R.layout.toast_text, null);
			tvText = (TextView) view.findViewById(R.id.tv);
			textToast.setView(view);
		}
		tvText.setText(resId);
		textToast.setDuration(duration);
		textToast.show();
	}
}
