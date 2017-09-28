package com.advanpro.fwtools.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by zeng on 2016/4/15.
 * 圆角图片
 */
public class RoundImageView extends ImageView {
	
    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable == null || getWidth() == 0 || getHeight() == 0) return;
		Bitmap b = ((BitmapDrawable) drawable).getBitmap();
		if (b == null) return;
		Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
		Bitmap circleBitmap = getCircleBitmap(bitmap, getWidth());
		canvas.drawBitmap(circleBitmap, 0, 0, null);
	}

	private Bitmap getCircleBitmap(Bitmap b, int diameter) {
		Bitmap bitmap;
		if (b.getWidth() != diameter || b.getHeight() != diameter) {
		    bitmap = Bitmap.createScaledBitmap(b, diameter, diameter, false);
		} else {
		    bitmap = b;
		}
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		int color = 0xff424242;
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());		
		canvas.drawARGB(0, 0, 0, 0);
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setColor(color);
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, p);
		p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, p);
		return output;
	}
}
