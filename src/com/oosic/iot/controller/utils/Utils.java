package com.oosic.iot.controller.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Utils {

	private static final String APP_NAME = "IotController";

	private static final boolean DEBUG = true;

	public static PackageInfo getPackageInfo(Context context) {
	   try {
         PackageManager pm = context.getPackageManager();
         return pm.getPackageInfo(context.getPackageName(), 0);
      } catch (NameNotFoundException e) {
         e.printStackTrace();
      }
      return null;
	}
	
	public static int getVersionCode(Context context) {
      return getPackageInfo(context).versionCode;
	}
   
	public static String getVersionName(Context context) {
      return getPackageInfo(context).versionName;
	}
	
   
	public static void log(String tag, String info) {
		logi(tag, info);
	}

	public static void logd(String tag, String info) {
		if (DEBUG) {
			Log.d(APP_NAME + ">>>>>>>>>>" + tag, "-------->" + info);
		}
	}

	public static void loge(String tag, String info) {
		if (DEBUG) {
			Log.e(APP_NAME + ">>>>>>>>>>" + tag, "-------->" + info);
		}
	}

	public static void logi(String tag, String info) {
		if (DEBUG) {
			Log.i(APP_NAME + ">>>>>>>>>>" + tag, "-------->" + info);
		}
	}

	public static void logv(String tag, String info) {
		if (DEBUG) {
			Log.v(APP_NAME + ">>>>>>>>>>" + tag, "-------->" + info);
		}
	}

	public static void logw(String tag, String info) {
		if (DEBUG) {
			Log.w(APP_NAME + ">>>>>>>>>>" + tag, "-------->" + info);
		}
	}

}
