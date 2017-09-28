package com.advanpro.fwtools.common.util;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by zengfs on 2015/12/16.
 */
public class StringUtils {
	private static final String[] chsDigits = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};	
	
	/**
	 * byte数组转换成16进制字符串
	 */
	public static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (byte aSrc : src) {
			int v = aSrc & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
			stringBuilder.append(" ");
		}
		return stringBuilder.toString().toUpperCase();
	}

	/**
	 * byte数组转换成2进制字符串
	 */
	public static String bytesToBinaryString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (byte aSrc : src) {
			int v = aSrc & 0xFF;
			String hv = Integer.toBinaryString(v);
			for (int i = 0; i < 8 - hv.length(); i++) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
			stringBuilder.append(" ");
		}
		return stringBuilder.toString();
	}

	/**
	 * 阿拉伯数字转中文，支持单位数
	 */
	public String arabDigitToChinese(int digit) {
		if (digit >= 0 && digit <= 9) {
		    return chsDigits[digit];
		} 
		return null;
	}

	public static String getDouble(double value){
		DecimalFormat format=new DecimalFormat("0.00");
		return format.format(value);
	}

    /**
     * 根据Uri获取图片路径
     */
    public static String getImagePath(Context context, Uri uri) {
        String s = uri.toString();
        if (s.startsWith("content://")) {
            CursorLoader cursorLoader = new CursorLoader(context, uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            Cursor cursor = cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);// 图片在的路径
        } else if (s.startsWith("file://")) {
            return s.substring(7);
        }
        return null;
    }  
    
    /**
     * 转换小数为字符串，不进行4舍5入
     * @param num 数字
     * @param scale 取几位小数
     */
    public static String formatDecimal(double num, int scale) {
        DecimalFormat formater = new DecimalFormat();
        formater.setMaximumFractionDigits(scale);
        formater.setGroupingSize(0);
        formater.setRoundingMode(RoundingMode.FLOOR);
        return formater.format(num);        
    }
}
