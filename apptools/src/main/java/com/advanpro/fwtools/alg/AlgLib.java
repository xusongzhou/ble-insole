package com.advanpro.fwtools.alg;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.entity.PoseSummary;

import java.util.Calendar;

/**
 * Created by zengfs on 2016/3/30.
 * 算法
 */
public class AlgLib {
    public static final double WALK_FACTOR = 0.37;  // 步行因子，默认值0.45
    public static final double RUN_FACTOR = 0.45; // 跑步因子，默认值0.50
    public static final double CALORIE_FACOTOR = 1.036;  // 卡路里消耗因子，默认值1.036
    public static final int DEFAULT_HEIGHT = 172;
    public static final double DEFAULT_WEIGHT = 60;
    private static double height = DEFAULT_HEIGHT;
    private static double weight = DEFAULT_WEIGHT;
    
    public static void setHeightAndWeight(double height, double weight) {
        AlgLib.height = height;
        AlgLib.weight = weight;
    }
    
    /**
     * 结合左右判断坐站姿
     * @return Constant中的定义姿势类型，否则-1
     */
    public static int parsePose(int leftValue, int rightValue) {
        if ((leftValue & 0x03) == 0x03 || (rightValue & 0x03) == 0x03) {//任意一只智能鞋垫对应的PS/AS bit为11
            return Constant.POSE_STAND;
        } else if ((leftValue & 0x02) == 0x00 && (rightValue & 0x02) == 0x00) {//两只智能鞋垫对应的PS/AS bit同时为0x
            return Constant.POSE_SIT;
        } else {
            return -1;
        }
    }

    /**
     * 单只坐站姿判
     * @return Constant中的定义姿势类型，否则-1
     */
    public static int parsePose(int value) {
        if ((value & 0x03) == 0x03) {//任意一只智能鞋垫对应的PS/AS bit为11
            return Constant.POSE_STAND;
        } else if ((value & 0x02) == 0x00) {//两只智能鞋垫对应的PS/AS bit同时为0x
            return Constant.POSE_SIT;
        } else {
            return -1;
        }
    }

    /**
     * 计算时间所在段号
     * @param cell 分段级别，共96或72
     * @param calendar 日期
     * @return 此日期所在段号，如果级别不是96或72，返回-1
     */
    public static int calcSection(int cell, Calendar calendar) {
        switch(cell) {
            case 96:
                return calendar.get(Calendar.HOUR_OF_DAY) * 4 + calendar.get(Calendar.MINUTE) / 15 + 1;
            case 72:
                return calendar.get(Calendar.HOUR_OF_DAY) * 3 + calendar.get(Calendar.MINUTE) / 20 + 1;
            default:
                return -1;
        }        
    }

    /**
     * 计算卡路里
     * @param dist 距离，单位(千米)
     */
    public static double calculateCalories(double dist) {
        return calculateCalories(dist, weight, CALORIE_FACOTOR);
    }

    /**
     * 使用折中因子0.4算距离
     */
    public static double calcDistance(long steps) {
        return calcDistance(steps, height, 0.4);
    }

    /**
     * 计算跑步距离
     */
    public static double calcRunDistance(long steps) {
        return calcDistance(steps, height, RUN_FACTOR);
    }

    /**
     * 计算步行距离
     */
    public static double calcWalkDistance(long steps) {
        return calcDistance(steps, height, WALK_FACTOR);
    }

    /**
     * 计算距离（千米），dHeight身高，厘米
     */
    public static double calcDistance(long lSteps, double dHeight, double dFactor) {
        if (0 >= lSteps || 0 >= dHeight || 0 >= dFactor)
            return 0;
        dHeight = dHeight / 100; // 米
        return (lSteps * dHeight * dFactor) / 1000;//km
    }

    /**
     * 计算卡路里（千卡），dWeight体重，公斤
     */
    public static double calculateCalories(double dDistance, double dWeight, double dFactor) {
        if (0 >= dDistance || 0 >= dWeight || 0 >= dFactor)
            return 0;
        return dDistance * dWeight * dFactor;
    }

    /**
     * 解析坐站势原始数据，计算时长
     * @param value1 其中一只脚数据，不能为空
     * @param value2 另一只脚数据
     * @return 时长汇总数据，只有坐站势的，步行和跑步忽略。如果value1为空，直接返回null
     */
    public static PoseSummary parsePoseOriginal(byte[] value1, byte[] value2) {        
        if (value1 != null) {
            PoseSummary summary = new PoseSummary();           
            if (value2 == null) {
                for (byte b : value1) {
                    int a = b & 0xff;
                    for (int i = 0; i < 4; i++) {
                        int type = parsePose(a);
                        if (type == Constant.POSE_SIT) summary.sitDuration += 60;
                        else if (type == Constant.POSE_STAND) summary.standDuration += 60;
                        a = a >> 2;
                    }
                }
            } else {
                int loop = value1.length > value2.length ? value2.length : value1.length;
                for (int i = 0; i < loop; i++) {
                    int a1 = value1[i] & 0xff;
                    int a2 = value2[i] & 0xff;
                    for (int j = 0; j < 4; j++) {
                        int type = parsePose(a1, a2);
                        if (type == Constant.POSE_SIT) summary.sitDuration += 60;
                        else if (type == Constant.POSE_STAND) summary.standDuration += 60;
                        a1 >>= 2;
                        a2 >>= 2;
                    }
                }
            }
            return summary;
        } 
        return null;
    }

    /**
     * 根据步态原始数据，判断步态类型
     * @param data 步态原始数据
     * @return Constant里定义的步态类型，无压力返回-1
     */
    public static int parseGait(int data) {
        switch(data) {            
            case 1:		
        		return Constant.GAIT_BIG_TOE;
            case 2:
            case 3:
                return Constant.GAIT_FOREFOOT_VARUS;
            case 4:
            case 5:
                return Constant.GAIT_FOREFOOT_ECTROPION;
            case 6:
            case 7:
                return Constant.GAIT_FOREFOOT;
            case 8:
                return Constant.GAIT_HEEL;
            case 9:
            case 14:
            case 15:
                return Constant.GAIT_SOLE;
            case 10:
            case 11:
                return Constant.GAIT_SOLE_VARUS;
            case 12:
            case 13:
                return Constant.GAIT_SOLE_ECTROPION;
            default:
                return -1;
        }
    }
    
    /**
     * 英里换算成公里
     * @param mile 英里
     * @return 公里值
     */
    public static double mileToKm(double mile) {
        return 1.609344 * mile;
    }

    /**
     * 公里换算成英里
     * @param km 公里
     * @return 英里值
     */
    public static double kmToMile(double km) {
        return 0.6213712 * km;
    } 
}
