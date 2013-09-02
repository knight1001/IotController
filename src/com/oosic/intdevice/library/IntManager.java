package com.oosic.intdevice.library;

import java.net.InetSocketAddress;
import java.util.Map;

import android.content.Context;
import android.os.Handler;

public class IntManager {

   private Context mContext;
   private Handler mHandler;
   private IntConfig mConfig;
   private Map<String, InetSocketAddress> mDeviceSocketAddrs;
   private Map<String, String> mDeviceMacAddrs;
   
   public IntManager(Context context) {
      mContext = context;
   }
   
   public void setHandler(Handler handler) {
      mHandler = handler;
   }
   
   public IntConfig getConfig() {
      if (mConfig == null) {
         mConfig = new IntConfig(mContext);
      }
      return mConfig;
   }

}
