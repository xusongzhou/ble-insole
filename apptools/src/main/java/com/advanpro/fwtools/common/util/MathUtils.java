package com.advanpro.fwtools.common.util;

/**
 * Created by zeng on 2016/6/1.
 */
public class MathUtils {
    /**
     * 精确到几位小数，不进行4舍5入
     * @param num 数字
     * @param scale 取几位小数
     */
    public static double setDoubleAccuracy(double num, int scale) {
        return ((int)(num * Math.pow(10, scale))) / Math.pow(10, scale);
    }
}
