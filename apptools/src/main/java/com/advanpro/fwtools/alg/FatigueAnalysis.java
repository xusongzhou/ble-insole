package com.advanpro.fwtools.alg;

import android.util.SparseArray;

import com.advanpro.fwtools.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeng on 2016/5/25.
 */
public class FatigueAnalysis {
    private SparseArray<List<Double>> pArray = new SparseArray<>();
    private double lastDist;//上次距离

    /**
     * 每三分钟调用一次
     * @param distance 从开始跑步到目前为止产生的距离
     * @return 疲劳等级
     */
    public int analysis(double distance) {
        if (distance <= lastDist) return -1;
        double Pcurr = 300 / (distance - lastDist);
        lastDist = distance;
        if (getPaceRank(Pcurr) < 2) return -1;        
        savePaceToArray(Pcurr);
        double Pst = getPst();
        if (Pst == 0) return -1; 
        double dp = (Pcurr - Pst) * 100 / Pst;
        return getFatigueRank(dp);
    }
    
    private int getFatigueRank(double dp) {
        if (dp <= 0.05) return -1;
        else if (dp > 0.05 && dp <= 0.1) return Constant.FATIGUE_SOMEWHAT_HARD;
        else if (dp > 0.1 && dp <= 0.15) return Constant.FATIGUE_HARD;
        else if (dp > 0.15 && dp <= 0.3) return Constant.FATIGUE_VERY_HARD;
        else if (dp > 0.3) return Constant.FATIGUE_VERY_VERY_HARD;
        return -1;
    }
    
    //保存当前配速到等级表
    private void savePaceToArray(double p) {
        int rank = getPaceRank(p);
        if (rank == 0) return;
        List<Double> ps = pArray.get(rank);
        if (ps == null) {
            ps = new ArrayList<>();
            pArray.put(rank, ps);
        }
        ps.add(p);
    }
    
    //配速等级
    private int getPaceRank(double p) {
        if (p > 12 * 60) return 1;
        else if (p > 10 * 60 && p <= 12 * 60) return 2;
        else if (p > 8 * 60 && p <= 10 * 60) return 3;
        else if (p > 6 * 60 && p <= 8 * 60) return 4;
        else if (p > 4 * 60 && p <= 6 * 60) return 5;
        else if (p > 2 * 60 && p <= 4 * 60) return 6;
        else if (p > 0 && p <= 2 * 60) return 7;
        return 0;
    }
    
    //获取平稳配速，秒/公里
    private float getPst() {
        List<Double> max = null;
        for (int i = 0; i < pArray.size(); i++) {
            List<Double> list = pArray.valueAt(i);
            if (list != null) {
                if (max == null || list.size() > max.size()) {
                    max = list;
                }
            }
        }
        if (max != null && max.size() > 0) {
            float sum = 0;
            for (Double d : max) {
                sum += d;
            }
            return sum / max.size();
        }
        return 0;
    }
}
