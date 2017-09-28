package com.advanpro.fwtools.common.util;

import com.advanpro.fwtools.MyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

public class LogUtil {
	//改此处控制log输出与否
	public static boolean isDebug = true;

	public static void v(String tag, String msg) {
		if (isDebug)
			android.util.Log.v(tag, msg);
	}

	public static void v(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.v(tag, msg, t);
	}

	public static void d(String tag, String msg) {
		if (isDebug)
			android.util.Log.d(tag, msg);
	}

	public static void d(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.d(tag, msg, t);
	}

	public static void i(String tag, String msg) {
		if (isDebug)
			android.util.Log.i(tag, msg);
	}

	public static void i(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.i(tag, msg, t);
	}

	public static void w(String tag, String msg) {
		if (isDebug)
			android.util.Log.w(tag, msg);
	}

	public static void w(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.w(tag, msg, t);
	}

	public static void e(String tag, String msg) {
		if (isDebug)
			android.util.Log.e(tag, msg);
	}

	public static void e(String tag, String msg, Throwable t) {
		if (isDebug)
			android.util.Log.e(tag, msg, t);
	}
    
    public static void saveLog(File file, String log) {
        if (isDebug) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
                out.write(log);
                out.newLine();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void saveLog(File file, Throwable t) {
        if (isDebug) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println("--------------------" + DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS") +
                        "--------------------");
                pw.println("appVersion=" + MyApplication.getAppVersion());
                t.printStackTrace(pw);
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(sw.toString().getBytes());
                fos.close();
                pw.close();
                sw.close();
            } catch (Exception e) {
                e.printStackTrace();
            } 
        }
    }
}
