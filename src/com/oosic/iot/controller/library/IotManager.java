package com.oosic.iot.controller.library;

import java.net.InetSocketAddress;
import java.util.Map;

import android.content.Context;
import android.os.Handler;

public class IotManager {

   private Context mContext;
   private Handler mHandler;
   private IotConfig mConfig;
   private Map<String, InetSocketAddress> mDeviceSocketAddrs;
   private Map<String, String> mDeviceMacAddrs;
   
   public IotManager(Context context) {
      mContext = context;
   }
   
   public void setHandler(Handler handler) {
      mHandler = handler;
   }
   
   public IotConfig getConfig() {
      if (mConfig == null) {
         mConfig = new IotConfig(mContext);
      }
      return mConfig;
   }

}
