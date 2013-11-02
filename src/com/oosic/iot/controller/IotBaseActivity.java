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

   public static final int COMP_ALL = 0;
   public static final int COMP_RELEAY = 1;
   public static final int COMP_ADC = 2;

   public static final int CMD_GET_RELAY_STATUS = 1;
   public static final int CMD_RELAY_ON = 2;
   public static final int CMD_RELAY_OFF = 3;
   public static final int CMD_GET_ADC = 4;
   public static final int CMD_RELAY_ALL_ON = 5;
   public static final int CMD_RELAY_ALL_OFF = 6;
   public static final int CMD_IR_STUDY = 7;
   public static final int CMD_IR_SEND = 8;

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

   byte[] getCommandByDevice(DeviceConfig config, int command) {
      byte[] data = null;
      switch (config.type) {
      case DEV_BW8001SW:
         if (command == CMD_RELAY_ON || command == CMD_RELAY_OFF
               || command == CMD_RELAY_ALL_ON || command == CMD_RELAY_ALL_OFF) {
            data = new byte[7];
            data[0] = (byte) (config.addr & 0xff);
            data[1] = (byte) 0x50;
            data[2] = (byte) 0xee;
            data[3] = (byte) 0x01;
            data[4] = (byte) 0x01;
            data[5] = (byte) ((command == CMD_RELAY_ON || command == CMD_RELAY_ALL_ON) ? 0x01
                  : 0x00);
            data[6] = (byte) (data[3] ^ data[4] ^ data[5]);
         } else if (command == CMD_GET_ADC) {
            data = new byte[7];
            data[0] = (byte) (config.addr & 0xff);
            data[1] = (byte) 0x50;
            data[2] = (byte) 0xee;
            data[3] = (byte) 0x03;
            data[4] = (byte) 0x00;
            data[5] = (byte) 0x00;
            data[6] = (byte) (data[3] ^ data[4] ^ data[5]);
         }
         break;
      case DEV_BW800IR:
         if (command == CMD_IR_STUDY) {
            data = new byte[7];
            data[0] = (byte) (config.addr & 0xff);
            data[1] = (byte) 0x50;
            data[2] = (byte) 0xfa;
            data[3] = (byte) 0x05;
            data[4] = (byte) (config.channel & 0xff);
            data[5] = (byte) 0x00;
            data[6] = (byte) (data[3] ^ data[4] ^ data[5]);
         } else if (command == CMD_IR_SEND) {
            data = new byte[7];
            data[0] = (byte) (config.addr & 0xff);
            data[1] = (byte) 0x50;
            data[2] = (byte) 0xfa;
            data[3] = (byte) 0x01;
            data[4] = (byte) (config.channel & 0xff);
            data[5] = (byte) 0x00;
            data[6] = (byte) (data[3] ^ data[4] ^ data[5]);
         }
         break;
      case DEV_BW800R3:
         if (command == CMD_GET_RELAY_STATUS) {
            data = new byte[7];
            data[0] = (byte) (config.addr & 0xff);
            data[1] = (byte) 0x50;
            data[2] = (byte) 0xae;
            data[3] = (byte) 0x04;
            data[4] = (byte) 0x00;
            data[5] = (byte) 0x00;
            data[6] = (byte) (data[3] ^ data[4] ^ data[5]);
         } else if (command == CMD_RELAY_ON || command == CMD_RELAY_OFF) {
            data = new byte[7];
            data[0] = (byte) (config.addr & 0xff);
            data[1] = (byte) 0x50;
            data[2] = (byte) 0xae;
            data[3] = (byte) 0x01;
            data[4] = (byte) (config.channel & 0xff);
            data[5] = (byte) ((command == CMD_RELAY_ON) ? 0x01 : 0x00);
            data[6] = (byte) (data[3] ^ data[4] ^ data[5]);
         } else if (command == CMD_RELAY_ALL_ON || command == CMD_RELAY_ALL_OFF) {
            data = new byte[7];
            data[0] = (byte) (config.addr & 0xff);
            data[1] = (byte) 0x50;
            data[2] = (byte) 0xae;
            data[3] = (byte) 0x01;
            data[4] = (byte) ((command == CMD_RELAY_ALL_ON) ? 200 : 201);
            data[5] = (byte) 0x00;
            data[6] = (byte) (data[3] ^ data[4] ^ data[5]);
         }
         break;
      }

      return data;
   }

   class DeviceConfig {

      int type = 0;
      String ip = "";
      int port = 5000;
      int addr = 0;
      int component = 0;
      int channel = 0;

      public DeviceConfig(int type, int addr, String ip, int port,
            int component, int channel) {
         this.type = type;
         this.addr = addr;
         this.ip = ip;
         this.port = port;
         this.component = component;
         this.channel = channel;
      }

   }

   class DeviceItem {

      String name = "";
      DeviceConfig config;
      DatagramSocket socket;
      DeviceDataListener listener;

   }

   abstract class DeviceDataListener implements IotDataListener {

      DeviceItem device;

      public DeviceDataListener(DeviceItem device) {
         this.device = device;
      }

   }

}
