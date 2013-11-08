package com.oosic.iot.controller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.library.IotResult;
import com.oosic.iot.controller.R;
import com.oosic.iot.controller.utils.UIUtils;
import com.oosic.iot.controller.utils.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PowerStatusActivity extends IotBaseActivity {

   private static final String TAG = "PowerStatusActivity";

   private Handler mHandler = new Handler();
   private IotManager mIotManager;
   private DeviceItem mDevice;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_tv_control);

      init();
      mHandler.post(new Runnable() {
         public void run() {
            relayoutViews();
         }
      });
   }

   private void init() {
      mIotManager = getIotManager();
      initViews();
   }

   private void initViews() {
      DeviceItem device = new DeviceItem();
      device.config = new DeviceConfig(DEV_BW800IR, 200, "192.168.1.200", 5000,
            COMP_ALL, 1);
      device.listener = new PowerDataListener(device);
      mDevice = device;

   }

   private void relayoutViews() {

   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      cleanup();
   }

   private void showToast(String msg) {
      UIUtils.showToast(this, msg);
   }

   private void showMessageDialog(String msg) {
      UIUtils.getAlertDialogBuilder(this).setMessage(msg)
            .setPositiveButton(R.string.ok, null).show();
   }

   private void showProgressDialog() {
   }

   private void hideProgressDialog() {
   }

   private void sendCommand(DeviceItem device, byte[] data) {
      device.socket = mDevice.socket;
      mIotManager.requestSendingUdpData(data, device.config.ip,
            device.config.port, device.socket, mHandler, device.listener);
   }

   private void cleanup() {
      if (mDevice.socket != null && !mDevice.socket.isClosed()) {
         mDevice.socket.close();
      }
      mDevice.socket = null;
   }

   private void analyzeResult(PowerDataListener listener,
         DatagramPacket packet) {
      byte[] data = packet.getData();
      String resultStr = IotManager.toHexString(data, 0, packet.getLength());
      Utils.log(TAG, "analyzeResult: " + resultStr);
      DeviceItem device = listener.device;
      switch (device.config.type) {
      }
   }

   class PowerDataListener extends DeviceDataListener {

      public PowerDataListener(DeviceItem device) {
         super(device);
      }

      @Override
      public void onDataSent(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramSocket) {
            mDevice.socket = (DatagramSocket) obj;
         }
      }

      @Override
      public void onDataReceived(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramPacket) {
            analyzeResult(this, (DatagramPacket) obj);
         }
      }

   }

}
