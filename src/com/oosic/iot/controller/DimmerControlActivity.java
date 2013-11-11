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
import android.widget.ProgressBar;

public class DimmerControlActivity extends IotBaseActivity {

   private static final String TAG = "DimmerControlActivity";

   private static final int CMD_DIMMER_GET_CURRENT = CMD_USR_BASE + 1;
   private static final int CMD_DIMMER_INCREASE = CMD_USR_BASE + 2;
   private static final int CMD_DIMMER_DECREASE = CMD_USR_BASE + 3;

   private static final int MAX_VALUE = 6;
   private static final int DEFAULT_VALUE = 5;

   private ProgressBar mProgressBar;
   private Button mIncreaseBtn, mDecreaseBtn, mSwitcherBtn;
   private Handler mHandler = new Handler();
   private IotManager mIotManager;
   private DimmerItem mDevice;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_dimmer);

      findViews();
      init();
      mHandler.post(new Runnable() {
         public void run() {
            relayoutViews();
         }
      });
   }

   private void findViews() {
      mProgressBar = (ProgressBar) findViewById(R.id.dimmer_progress);
      mIncreaseBtn = (Button) findViewById(R.id.dimmer_increase);
      mDecreaseBtn = (Button) findViewById(R.id.dimmer_decrease);
      mSwitcherBtn = (Button) findViewById(R.id.dimmer_switcher);
   }

   private void init() {
      mIotManager = getIotManager();
      initViews();

      sendCommand(mDevice, getCommand(mDevice, mDevice.command));
   }

   private void initViews() {
      DimmerItem device = new DimmerItem();
      device.config = new DeviceConfig(DEV_BW800LT, 202, "192.168.1.202", 5000,
            COMP_ALL, 1);
      device.command = CMD_DIMMER_GET_CURRENT;
      device.listener = new DimmerDataListener(device);
      mDevice = device;

      DimmerItem item = new DimmerItem(device);
      item.command = CMD_DIMMER_INCREASE;
      item.listener = new DimmerDataListener(item);
      mIncreaseBtn.setTag(item);
      mIncreaseBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            if (mSwitcherBtn.isSelected()) {
               DimmerItem item = (DimmerItem) v.getTag();
               sendCommand(item, getCommand(item, item.command));
            }
         }
      });

      item = new DimmerItem(device);
      item.command = CMD_DIMMER_DECREASE;
      item.listener = new DimmerDataListener(item);
      mDecreaseBtn.setTag(item);
      mDecreaseBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            if (mSwitcherBtn.isSelected()) {
               DimmerItem item = (DimmerItem) v.getTag();
               sendCommand(item, getCommand(item, item.command));
            }
         }
      });

      item = new DimmerItem(device);
      item.listener = new DimmerDataListener(item);
      item.command = CMD_RELAY_ON;
      mSwitcherBtn.setText(R.string.off);
      mSwitcherBtn.setSelected(false);
      mSwitcherBtn.setTag(item);
      mSwitcherBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            DimmerItem item = (DimmerItem) v.getTag();
            sendCommand(item, getCommand(item, item.command));
         }
      });

      mProgressBar.setProgress(0);
      mProgressBar.setMax(MAX_VALUE);
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

   private byte[] getCommand(DeviceItem device, int command) {
      byte[] data = new byte[7];
      data[0] = (byte) (device.config.addr & 0xff);
      data[1] = (byte) 0x50;
      data[2] = (byte) 0xbe;
      switch (command) {
      case CMD_DIMMER_GET_CURRENT:
         data[3] = (byte) 0x04;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x00;
         break;
      case CMD_DIMMER_INCREASE:
         data[3] = (byte) 0x05;
         data[4] = (byte) 0x01;
         data[5] = (byte) 0x00;
         break;
      case CMD_DIMMER_DECREASE:
         data[3] = (byte) 0x05;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x00;
         break;
      case CMD_RELAY_ON:
         data[3] = (byte) 0x81;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x01;
         break;
      case CMD_RELAY_OFF:
         data[3] = (byte) 0x81;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x05;
         break;
      }
      data[6] = (byte) (data[3] ^ data[4] ^ data[5]);
      return data;
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

   private void analyzeResult(DimmerDataListener listener, DatagramPacket packet) {
      byte[] data = packet.getData();
      String resultStr = IotManager.toHexString(data, 0, packet.getLength());
      String str = resultStr.length() <= 6 ? resultStr : resultStr.substring(0,
            6);
      Utils.log(TAG, "analyzeResult: " + resultStr);
      DimmerItem item = (DimmerItem) listener.device;
      switch (item.command) {
      case CMD_DIMMER_GET_CURRENT:
         if (packet.getLength() == 6 && "50eb04".equals(str)) {
            // mProgressBar.setMax(6);
            // mProgressBar.setProgress(data[3]);
         }
         break;
      case CMD_DIMMER_INCREASE:
         if ("50eb05".equals(resultStr)) {
            int progress = mProgressBar.getProgress() + 1;
            mProgressBar.setProgress(progress <= MAX_VALUE ? progress
                  : MAX_VALUE);
         }
         break;
      case CMD_DIMMER_DECREASE:
         if ("50eb05".equals(resultStr)) {
            int progress = mProgressBar.getProgress() - 1;
            mProgressBar.setProgress(progress >= 1 ? progress
                  : 1);
         }
         break;
      case CMD_RELAY_ON:
      case CMD_RELAY_OFF:
         if (packet.getLength() == 6 && "50eb81".equals(str)) {
            boolean on = (data[3] != 0);
            mSwitcherBtn.setText(on ? R.string.on : R.string.off);
            mSwitcherBtn.setSelected(on);
            // mProgressBar.setProgress(data[4]);
            mProgressBar.setProgress(on ? DEFAULT_VALUE : 0);
            item.command = on ? CMD_RELAY_OFF : CMD_RELAY_ON;
         }
         break;
      }
   }

   class DimmerDataListener extends DeviceDataListener {

      public DimmerDataListener(DimmerItem device) {
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

   class DimmerItem extends DeviceItem {
      int command = 0;

      public DimmerItem() {
         super();
      }

      public DimmerItem(DeviceItem device) {
         this.name = device.name;
         this.config = device.config;
         this.listener = device.listener;
         this.socket = device.socket;
      }
   }

}
