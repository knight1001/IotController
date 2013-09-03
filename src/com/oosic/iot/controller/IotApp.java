package com.oosic.iot.controller;

import com.oosic.iot.controller.library.ActivityStack;
import com.oosic.iot.controller.library.PreferenceManager;

import android.app.Activity;
import android.app.Application;
import android.util.DisplayMetrics;

public class IotApp extends Application {

   private ActivityStack mActivityStack;
   private PreferenceManager mPrefsManager;

   @Override
   public void onCreate() {
      super.onCreate();
      
   }

   public ActivityStack getActivityStack() {
      if (mActivityStack == null) {
         mActivityStack = new ActivityStack();
      }
      return mActivityStack;
   }

   public PreferenceManager getPrefsManager() {
      if (mPrefsManager == null) {
         mPrefsManager = new PreferenceManager(this);
      }
      return mPrefsManager;
   }

   public void prepareEnvironment() {
      if (mActivityStack == null) {
         mActivityStack = new ActivityStack();
      }

      if (mPrefsManager == null) {
         mPrefsManager = new PreferenceManager(this);
      }
   }

   public void cleanupEnvironment() {
      if (mActivityStack != null) {
         mActivityStack.cleanup();
         mActivityStack = null;
      }

      if (mPrefsManager != null) {
         mPrefsManager.cleanup();
         mPrefsManager = null;
      }
   }

   @Override
   public void onTerminate() {

   }

   public static int getScreenWidth(Activity context) {
      return context.getWindowManager().getDefaultDisplay().getWidth();
   }

   public static int getScreenHeight(Activity context) {
      return context.getWindowManager().getDefaultDisplay().getHeight();
   }

   public static float getScreenDensity(Activity context) {
      DisplayMetrics dm = new DisplayMetrics();
      context.getWindowManager().getDefaultDisplay().getMetrics(dm);
      return dm.density;
   }

   public static DisplayMetrics getDisplayMetrics(Activity context) {
      DisplayMetrics dm = new DisplayMetrics();
      context.getWindowManager().getDefaultDisplay().getMetrics(dm);
      return dm;
   }

}
