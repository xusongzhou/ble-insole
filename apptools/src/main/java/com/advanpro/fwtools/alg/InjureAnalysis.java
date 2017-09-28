package com.advanpro.fwtools.alg;

import com.advanpro.fwtools.Constant;
import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.fwtools.db.RunRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by zeng on 2016/6/2.
 */
public class InjureAnalysis {
    private double avgWeekDist;
    private double curWeekDist;
    private double lastDistOneMinute;
    private double lastDistThreeMinute;
    private double avgPace;
    private double avgDuration;
    private double avgDistance;

    public InjureAnalysis() {
        List<RunRecord> list = Dao.INSTANCE.queryRunRecords(true);
        if (list.size() > 0) {
            //周里程
            int n = DateUtils.weeksBetween(list.get(0).date, new Date()) + 1;
            if (n > 1) {
                int circle = n > 5 ? 5 : n - 1;
                Date date = DateUtils.getFirstDayOfWeek(new Date());
                curWeekDist = Dao.INSTANCE.queryRunDistance(date, new Date());
                if (curWeekDist == -1) curWeekDist = 0;
                double totalWeekDist = 0;
                for (int i = 0; i < circle; i++) {
                    date.setTime(date.getTime() - 7 * 24 * 3600000L);
                    Date end = DateUtils.getLastDayOfWeek(date);
                    double d = Dao.INSTANCE.queryRunDistance(date, end);
                    //没有数据或小于3公里，记为0
                    totalWeekDist += (d == -1 || d < 3) ? 0 : d;
                }
                avgWeekDist = totalWeekDist / circle;
            }
            //单次跑步配速,时长,里程均值
            int circle = list.size() > 5 ? 5 : list.size();
            double totalPace = 0;
            double totalDur = 0;
            double totalDis = 0;
            for (int i = 1; i <= circle; i++) {
                RunRecord record = list.get(list.size() - i);
                totalPace += record.rate >= 10 ? 0 : record.rate;
                totalDur += record.duration < 10 * 60 ? 0 : record.duration;
                totalDis += record.distance < 1 ? 0 : record.distance;
            }
            avgPace = totalPace / circle;
            avgDuration = totalDur / circle;
            avgDistance = totalDis / circle;
        }
    }

    /**
     * 根据周里程分析，1分钟调用一次
     * @param dist 本次跑步距离
     */
    public int analysisWithWeekDistance(double dist) {
        if (avgWeekDist == 0) return -1;
        double var = dist - lastDistOneMinute;        
        lastDistOneMinute = dist;
        if (var <= 0) return -1;
        double cur = curWeekDist + var;
        double r = (cur - avgWeekDist) / avgWeekDist;
        if (r < 0.1) {
            return -1;
        } else if (r >= 0.1 && r < 0.2) {
            return Constant.INJURE_LOW;
        } else if (r >= 0.2 && r < 0.3) {
            return Constant.INJURE_MIDDLE;
        } else{
            return Constant.INJURE_HIGH;
        }
    }

    /**
     * 根据单次跑步平均配速分析，3分钟调用一次
     * @param dist 本次跑步距离
     */
    public int analysisWithPace(double dist) {        
        if (dist <= lastDistThreeMinute || avgPace == 0) return -1;
        double p = 3 / (dist - lastDistThreeMinute);
        lastDistThreeMinute = dist;        
        double r = (p - avgPace) / avgPace;
        if (r < 0.05) {
            return -1;
        } else if (r >= 0.05 && r < 0.1) {
            return Constant.INJURE_LOW;
        } else if (r >= 0.1 && r < 0.2) {
            return Constant.INJURE_MIDDLE;
        } else {
            return Constant.INJURE_HIGH;
        }
    }

    /**
     * 根据单次跑步运动时长析，1分钟调用一次
     * @param duration 本次跑步持续的时长
     */
    public int analysisWithDuration(int duration) {
        if (avgDuration == 0) return -1;
        double r = (duration - avgDuration) / avgDuration;
        if (r < 0.2) {
            return -1;
        } else if (r >= 0.2 && r < 0.25) {
            return Constant.INJURE_LOW;
        } else if (r >= 0.25 && r < 0.3) {
            return Constant.INJURE_MIDDLE;
        } else {
            return Constant.INJURE_HIGH;
        }
    }

    /**
     * 根据单次跑步里程长析，1分钟调用一次
     * @param dist 本次跑步的里程
     */
    public int analysisWithDistance(double dist) {
        if (avgDistance == 0) return -1;
        double r = (dist - avgDistance) / avgDistance;
        if (r < 0.1) {
            return -1;
        } else if (r >= 0.1 && r < 0.2) {
            return Constant.INJURE_LOW;
        } else if (r >= 0.2 && r < 0.3) {
            return Constant.INJURE_MIDDLE;
        } else{
            return Constant.INJURE_HIGH;
        }
    }
}
