package com.advanpro.fwtools.common.util;

import android.content.SharedPreferences;
import com.advanpro.fwtools.MyApplication;

public class PreferencesUtils {
	public static final String PREFERENCE_NAME = "config";

	private PreferencesUtils() {
		throw new AssertionError();
	}

	public static void putString(String key, String value) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		sp.edit().putString(key, value).apply();
	}

	/**
	 * 失败返回默认值为null
	 */
	public static String getString(String key) {
		return getString(key, null);
	}

	public static String getString(String key, String defaultValue) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		return sp.getString(key, defaultValue);
	}

	public static void putInt(String key, int value) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		sp.edit().putInt(key, value).apply();
	}

	/**
	 * 失败返回默认值为-1
	 */
	public static int getInt(String key) {
		return getInt(key, -1);
	}

	public static int getInt(String key, int defaultValue) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		return sp.getInt(key, defaultValue);
	}

	public static void putLong(String key, long value) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		sp.edit().putLong(key, value).apply();
	}

	/**
	 * 失败返回默认值为-1
	 */
	public static long getLong(String key) {
		return getLong(key, -1L);
	}

	public static long getLong(String key, long defaultValue) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		return sp.getLong(key, defaultValue);
	}

	public static void putFloat(String key, float value) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		sp.edit().putFloat(key, value).apply();
	}

	/**
	 * 失败返回默认值为-1
	 */
	public static float getFloat(String key) {
		return getFloat(key, -1F);
	}

	public static float getFloat(String key, float defaultValue) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		return sp.getFloat(key, defaultValue);
	}

	public static void putBoolean(String key, boolean value) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		sp.edit().putBoolean(key, value).apply();
	}

	public static boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		return sp.getBoolean(key, defaultValue);
	}

	public static void removeKey(String key) {
		SharedPreferences sp = MyApplication.getInstance().getSharedPreferences(PREFERENCE_NAME, 0);
		sp.edit().remove(key).apply();
	}
}
