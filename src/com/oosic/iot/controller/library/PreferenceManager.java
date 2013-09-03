package com.oosic.iot.controller.library;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class PreferenceManager {

   private Context mContext = null;

   public class PreferenceFiles {
      public static final String APP_SETTINGS = "app_settings";
   }

   public class PreferenceItems {
      public static final String FIRST_RUN = "first_run";
   }

   public PreferenceManager(Context context) {
      mContext = context;
      init();
   }

   private void init() {
      
   }

   public void cleanup() {
      
   }

   public SharedPreferences getPrefs(String fileName) {
      SharedPreferences prefs = null;
      if (!TextUtils.isEmpty(fileName)) {
         prefs = mContext.getSharedPreferences(fileName,
               Context.MODE_PRIVATE);
      }
      return prefs;
   }

   private SharedPreferences getAppSettingsPrefs() {
      return getPrefs(PreferenceFiles.APP_SETTINGS);
   }

   public boolean isFirstRun() {
      SharedPreferences prefs = getAppSettingsPrefs();
      if (prefs != null) {
         return prefs.getBoolean(PreferenceItems.FIRST_RUN, true);
      }
      return true;
   }

   public boolean setFirstRun(boolean value) {
      SharedPreferences prefs = getAppSettingsPrefs();
      if (prefs != null) {
         return prefs.edit().putBoolean(PreferenceItems.FIRST_RUN, value)
               .commit();
      }
      return false;
   }

}
