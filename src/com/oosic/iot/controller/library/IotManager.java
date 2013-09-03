package com.oosic.iot.controller.library;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Handler;

public class IotManager {
   
   private static final String SERVER = "38.lzjgo.us";
   private static final int SERVER_PORT = 18882;

   private Context mContext;
   private Handler mHandler;
   private IotConfig mConfig;
   private Map<String, InetSocketAddress> mDeviceSocketAddrs;
   private Map<String, String> mDeviceMacAddrs;
   
   public IotManager(Context context) {
      mContext = context;
      mDeviceSocketAddrs = new HashMap<String, InetSocketAddress>();
      mDeviceMacAddrs = new HashMap<String, String>();
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
   
   public void addDeviceSocketAddr(String ip, InetSocketAddress addr) {
      mDeviceSocketAddrs.put(ip, addr);
   }
   
   public void addDeviceMacAddr(String ip, String mac) {
      mDeviceMacAddrs.put(ip, mac);
   }

}
