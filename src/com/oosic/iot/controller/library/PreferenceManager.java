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
      public static final String COMMAND_BUTTON = "cmd_btn_";
      public static final String CONTROLLER_BUTTON = "controller_btn_";
      public static final String DIMMER_VALUE = "dimmer";
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
   
   public IotCommand getButtonCommandByIndex(int index) {
      String key = PreferenceItems.COMMAND_BUTTON + index;
      SharedPreferences prefs = getAppSettingsPrefs();
      if (prefs != null) {
         String value = prefs.getString(key, "");
         if (!TextUtils.isEmpty(value)) {
            return IotCommand.parseString(value);
         }
      }
      return null;
   }
   
   public boolean setButtonCommandByIndex(int index, IotCommand command) {
      String key = PreferenceItems.COMMAND_BUTTON + index;
      SharedPreferences prefs = getAppSettingsPrefs();
      if (prefs != null) {
         return prefs.edit().putString(key, command.toString()).commit();
      }
      
      return false;
   }

   public int getButtonAddr(String name) {
      SharedPreferences prefs = getAppSettingsPrefs();
      if (prefs != null) {
         return prefs.getInt(name, 0);
      }
      return 0;
   }
   
   public boolean setButtonAddr(String name, int addr) {
      SharedPreferences prefs = getAppSettingsPrefs();
      if (prefs != null) {
         return prefs.edit().putInt(name, addr).commit();
      }
      
      return false;
   }

   public int getDimmerValue(String name) {
      SharedPreferences prefs = getAppSettingsPrefs();
      if (prefs != null) {
         return prefs.getInt(name, 0);
      }
      return 0;
   }
   
   public boolean setDimmerValue(String name, int addr) {
      SharedPreferences prefs = getAppSettingsPrefs();
      if (prefs != null) {
         return prefs.edit().putInt(name, addr).commit();
      }
      
      return false;
   }

}
