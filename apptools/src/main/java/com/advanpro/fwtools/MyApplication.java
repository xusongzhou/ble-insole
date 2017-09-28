package com.advanpro.fwtools;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;

import com.advanpro.fwtools.common.util.DateUtils;
import com.advanpro.fwtools.common.util.IOUtils;
import com.advanpro.fwtools.common.util.LogUtil;
import com.advanpro.fwtools.common.util.PreferencesUtils;
import com.advanpro.fwtools.db.Dao;
import com.advanpro.ascloud.ASCloud;
import com.advanpro.aswear.ASWear;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by zengfs on 2016/1/13.
 */
public class MyApplication extends Application {
	private static MyApplication instance;
	private static Handler handler;
	private static int mainId;	
    private static String appVersion;
	public static boolean isVoiceEnable;

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.isDebug = true;//打开日志输出
		instance = this;
		handler = new Handler();
		mainId = android.os.Process.myTid();
        Dao.INSTANCE.initialize(this);//初始化数据库操作类
        ASWear.init(this);
        ASCloud.init(this, "http://test.ansobuy.cn:9000");
		isVoiceEnable = PreferencesUtils.getBoolean(Constant.SP_VOICE_ENABLE);
        Thread.currentThread().setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
	}

	public static MyApplication getInstance() {
		return instance;
	}

	public static Handler getHandler() {
		return handler;
	}

	public static int getMainTid() {
		return mainId;
	}
    
    public static String getAppVersion() {
        if (appVersion == null) {
            PackageManager pm = instance.getPackageManager();
            try {
                PackageInfo info = pm.getPackageInfo(instance.getPackageName(), 0);
                appVersion = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appVersion;
    }

    /**
     * 未捕获的异常
     */
    private class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            FileOutputStream fos = null;
            StringWriter sw = null;
            PrintWriter pw = null;
            try {
                sw = new StringWriter();
                pw = new PrintWriter(sw);
                //获取手机的环境
                Field[] fields = Build.class.getDeclaredFields();
                pw.println("--------------------" + DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS") + 
                        "--------------------");
                for (Field field : fields) {
                    field.setAccessible(true);
                    pw.println(field.getName() + "=" + field.get(null));
                }
                pw.println("appVersion=" + getAppVersion());
                ex.printStackTrace(pw);
                File file = new File(ASWear.AppSoreDir, "ansobuy_ex_log.txt");
                fos = new FileOutputStream(file, true);
                fos.write(sw.toString().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(fos, pw, sw);
            }
        }
    }
}
