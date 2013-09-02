package com.oosic.intdevice.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

public class NetworkHelper {

   public static boolean isWifiEnabled(Context context) {
      WifiManager manager = (WifiManager) context
            .getSystemService(Context.WIFI_SERVICE);
      if (manager != null) {
         return manager.isWifiEnabled();
      }
      return false;
   }

   public static boolean isNetworkAvailable(Context context) {
      ConnectivityManager connManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connManager != null) {
         NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
         if (networkInfo != null) {
             return networkInfo.isAvailable() && networkInfo.isConnected();
         }
      }
      return false;
   }
   
   public static int getAvailableNetworkType(Context context) {
      ConnectivityManager connManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connManager != null) {
         NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isAvailable()) {
             return networkInfo.getType();
         }
      }
      return -1;
   }

   public static boolean isWifiNetworkAvailable(Context context) {
      ConnectivityManager connManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connManager != null) {
         NetworkInfo networkInfo = connManager
               .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         if (networkInfo != null) {
            return networkInfo.isAvailable() && networkInfo.isConnected();
         }
      }
      return false;
   }

   public static boolean isMobileNetworkAvailable(Context context) {
      ConnectivityManager connManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
      if (connManager != null) {
         NetworkInfo networkInfo = connManager
               .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            return networkInfo.isAvailable() && networkInfo.isConnected();
      }
      return false;
   }

   public static void enterNetworkSettings(Context context) {
      Intent intent = new Intent();
      if (Integer.valueOf(Build.VERSION.SDK) > 10) {
         intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
      } else {
         ComponentName comp = new ComponentName("com.android.settings",
               "com.android.settings.WirelessSettings");
         intent.setComponent(comp);
         intent.setAction("android.intent.action.VIEW");
      }

      try {
         context.startActivity(intent);
      } catch (ActivityNotFoundException e) {
         e.printStackTrace();
      }
   }

}
