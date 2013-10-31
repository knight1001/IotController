package com.oosic.iot.controller;

import java.net.DatagramSocket;

import com.oosic.iot.controller.library.ActivityStack;
import com.oosic.iot.controller.library.IotDataListener;
import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.library.IotResult;
import com.oosic.iot.controller.library.PreferenceManager;
import com.oosic.iot.controller.utils.UIUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class IotBaseActivity extends Activity {

   public static final int DEV_BW800IR = 8000;
   public static final int DEV_BW8001SW = 8001;
   public static final int DEV_BW800R3 = 8002;
   
   public static final int CMD_TURN_ON = 0;
   public static final int CMD_TURN_OFF = 1;
   public static final int CMD_GET_ADC = 0;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (getActivityStack() == null) {
         ((IotApp) getApplication()).prepareEnvironment();
      }

      getActivityStack().push(this);
   }

   protected void onPause() {
      super.onPause();
   }

   @Override
   protected void onResume() {
      super.onResume();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();

      getActivityStack().remove(this);

      if (getActivityStack().getCount() <= 0) {
         ((IotApp) getApplication()).cleanupEnvironment();
      }
   }

   protected void showToast(Context context, String msg) {
      UIUtils.showToast(context, msg);
   }

   protected ActivityStack getActivityStack() {
      return ((IotApp) getApplication()).getActivityStack();
   }

   protected PreferenceManager getPrefsManager() {
      return ((IotApp) getApplication()).getPrefsManager();
   }
   
   protected IotManager getIotManager() {
      return ((IotApp) getApplication()).getIotManager();
   }
   
   byte[] getCommandByDevice(DeviceConfig config, int cmdType, int channel) {
      byte[] data = new byte[7];
      switch (config.type) {
      case DEV_BW8001SW: {
         break;
      }
      case DEV_BW800IR: {
         break;
      }
      case DEV_BW800R3: {
         break;
	   }
      default:
         break;
      }
      
      return data;
   }
   
   class DeviceConfig {
      int type = 0;
      String ip = "";
      int port = 5000;
      int addr = 0;
      
      public DeviceConfig(int type, int addr, String ip, int port) {
         this.type = type;
         this.addr = addr;
         this.ip = ip;
         this.port = port;
      }
   }
   
   class DeviceItem {
      String name = "";
      DeviceConfig config;
      DatagramSocket socket;
      DeviceDataListener listener;
   }
   
   abstract class DeviceDataListener implements IotDataListener {
      
      String name;
      
      public DeviceDataListener(String name) {
         this.name = name;
      }

   }

}
