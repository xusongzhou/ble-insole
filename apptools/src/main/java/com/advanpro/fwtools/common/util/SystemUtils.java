package com.advanpro.fwtools.common.util;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by zeng on 2016/4/29.
 * 系统工具
 */
public class SystemUtils {
    /**
     * 判断网络是否可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isAvailable();
    }
    
    /**
     * 判断服务是否正在运行
     * @param context 上下文
     * @param className 服务的完整类名
     */
    public static boolean isServiceRunning(Context context, String className) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(200);
        for (ActivityManager.RunningServiceInfo serviceInfo : services) {
            if (serviceInfo.service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取总内存大小，单位是byte
     */
    public static long getTotalMemSize() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/meminfo")));
            char[] info = br.readLine().toCharArray();
            StringBuilder sb = new StringBuilder();
            for (char c : info) {
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }
            long kbSize = Long.parseLong(sb.toString());
            br.close();
            return (kbSize * 1024);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * 获取可用内存大小，单位byte
     */
    public static long getAvailMemSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo);
        return outInfo.availMem;
    }
    
    /**
     * 获取正在运行的进程数
     */
    public static int getRunningProcessCount(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        return runningAppProcesses==null ? 0:runningAppProcesses.size();
    }

    /**
     * 振动
     * @param context 上下文
     * @param pattern 振动模式
     * @param repeat 重复次数
     * @return 振动器实例
     */
    public static Vibrator vibrate(Context context, long[] pattern, int repeat) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, repeat);
        return vibrator;
    }

    /**
     * 振动
     * @param context 上下文
     * @param millis 振动持续时长
     * @return 振动器实例
     */
    public static Vibrator vibrate(Context context, long millis) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(millis);
        return vibrator;
    }
}
