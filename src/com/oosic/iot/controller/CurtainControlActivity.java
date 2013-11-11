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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CurtainControlActivity extends IotBaseActivity {

   private static final String TAG = "CurtainControlActivity";

   public static final int CMD_OPEN = 1;
   public static final int CMD_CLOSE = 2;
   public static final int CMD_STOP = 3;
   public static final int CMD_SETUP_OPEN_DURATION = 4;
   public static final int CMD_SETUP_CLOSE_DURATION = 5;
   public static final int CMD_GET_DURATION = 6;

   private EditText mOpenDurationView, mCloseDurationView;
   private Button mOpenDurationSetupBtn, mCloseDurationSetupBtn;
   private Button mOpenBtn, mCloseBtn, mStopBtn;
   private ViewGroup mWaitingLayout;
   private TextView mWaitingTextView;
   private Handler mHandler = new Handler();
   private IotManager mIotManager;
   private CurtainItem mDevice;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_curtain_control);

      findViews();
      init();
      mHandler.post(new Runnable() {
         public void run() {
            relayoutViews();
         }
      });
   }

   private void findViews() {
      mOpenDurationView = (EditText) findViewById(R.id.curtain_open_duration);
      mCloseDurationView = (EditText) findViewById(R.id.curtain_close_duration);
      mOpenDurationSetupBtn = (Button) findViewById(R.id.open_duration_setup_btn);
      mCloseDurationSetupBtn = (Button) findViewById(R.id.close_duration_setup_btn);
      mOpenBtn = (Button) findViewById(R.id.open_curtain_btn);
      mCloseBtn = (Button) findViewById(R.id.close_curtain_btn);
      mStopBtn = (Button) findViewById(R.id.stop_curtain_btn);
      mWaitingLayout = (ViewGroup) findViewById(R.id.curtain_progress_layout);
      mWaitingTextView = (TextView) findViewById(R.id.curtain_progress_prompt);
   }

   private void init() {
      mIotManager = getIotManager();
      initViews();

      sendCommand(mDevice, getCommand(mDevice, CMD_GET_DURATION, 0));
   }

   private void initViews() {
      CurtainItem device = new CurtainItem();
      device.config = new DeviceConfig(DEV_BW800CU, 203, "192.168.1.203", 5000,
            COMP_ALL, 1);
      device.command = CMD_GET_DURATION;
      device.listener = new CurtainDataListener(device);
      mDevice = device;

      CurtainItem item = new CurtainItem(device);
      item.command = CMD_SETUP_OPEN_DURATION;
      item.listener = new CurtainDataListener(item);
      mOpenDurationSetupBtn.setTag(item);
      mOpenDurationSetupBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            CurtainItem item = (CurtainItem) v.getTag();
            try {
               long duration = Long.parseLong(mOpenDurationView.getText()
                     .toString());
               if (duration > 65535) {
	               showToast(getString(R.string.duration_too_large));
	               return;
               }
               sendCommand(item, getCommand(item, item.command, (int) duration));
            } catch (NumberFormatException e) {
               showToast(getString(R.string.illegal_character));
            }
         }
      });
      item = new CurtainItem(device);
      item.command = CMD_SETUP_CLOSE_DURATION;
      item.listener = new CurtainDataListener(item);
      mCloseDurationSetupBtn.setTag(item);
      mCloseDurationSetupBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            CurtainItem item = (CurtainItem) v.getTag();
            try {
               long duration = Long.parseLong(mCloseDurationView.getText()
                     .toString());
               if (duration > 65535) {
	               showToast(getString(R.string.duration_too_large));
	               return;
               }
               sendCommand(item, getCommand(item, item.command, (int) duration));
            } catch (NumberFormatException e) {
               showToast(getString(R.string.illegal_character));
            }
         }
      });
      item = new CurtainItem(device);
      item.command = CMD_OPEN;
      item.listener = new CurtainDataListener(item);
      mOpenBtn.setTag(item);
      mOpenBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            CurtainItem item = (CurtainItem) v.getTag();
            showProgressDialog(getString(R.string.opening_curtain));
            sendCommand(item, getCommand(item, item.command, 0));
         }
      });
      item = new CurtainItem(device);
      item.command = CMD_CLOSE;
      item.listener = new CurtainDataListener(item);
      mCloseBtn.setTag(item);
      mCloseBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            CurtainItem item = (CurtainItem) v.getTag();
            showProgressDialog(getString(R.string.closing_curtain));
            sendCommand(item, getCommand(item, item.command, 0));
         }
      });
      item = new CurtainItem(device);
      item.command = CMD_STOP;
      item.listener = new CurtainDataListener(item);
      mStopBtn.setTag(item);
      mStopBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            CurtainItem item = (CurtainItem) v.getTag();
            showProgressDialog(getString(R.string.stopping_curtain));
            sendCommand(item, getCommand(item, item.command, 0));
         }
      });
      
      mOpenDurationView.clearFocus();
      mCloseDurationView.clearFocus();
      mOpenBtn.requestFocus();
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

   private void showProgressDialog(String msg) {
//      mWaitingTextView.setText(msg);
//      mWaitingLayout.setVisibility(View.VISIBLE);
   }

   private void hideProgressDialog() {
//      mWaitingLayout.setVisibility(View.INVISIBLE);
   }

   private byte[] getCommand(DeviceItem device, int command, int value) {
      byte[] data = new byte[7];
      data[0] = (byte) (device.config.addr & 0xff);
      data[1] = (byte) 0x50;
      data[2] = (byte) 0xde;
      switch (command) {
      case CMD_OPEN:
         data[3] = (byte) 0x02;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x00;
         break;
      case CMD_CLOSE:
         data[3] = (byte) 0x03;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x00;
         break;
      case CMD_STOP:
         data[3] = (byte) 0x04;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x00;
         break;
      case CMD_GET_DURATION:
         data[3] = (byte) 0x08;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x00;
         break;
      case CMD_SETUP_OPEN_DURATION:
         data[3] = (byte) 0x05;
         data[4] = (byte) (value & 0xff);
         data[5] = (byte) ((value >> 8) & 0xff);
         break;
      case CMD_SETUP_CLOSE_DURATION:
         data[3] = (byte) 0x06;
         data[4] = (byte) (value & 0xff);
         data[5] = (byte) ((value >> 8) & 0xff);
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

   private void analyzeResult(CurtainDataListener listener,
         DatagramPacket packet) {
      byte[] data = packet.getData();
      String resultStr = IotManager.toHexString(data, 0, packet.getLength());
      Utils.log(TAG, "analyzeResult: " + resultStr);
      CurtainItem device = (CurtainItem) listener.device;
      switch (device.command) {
      case CMD_GET_DURATION:
         if (packet.getLength() == 6) {
            mOpenDurationView.setText(String.valueOf((data[2] & 0xff)
                  | (data[3] << 8 & 0xff00)));
            mCloseDurationView.setText(String.valueOf((data[4] & 0xff)
                  | (data[5] << 8 & 0xff00)));
         }
         break;
      case CMD_SETUP_OPEN_DURATION:
         if ("50db05".equals(resultStr)) {
            showToast(getString(R.string.setup_x_success,
                  getString(R.string.curtain_open_duration)));
         }
         break;
      case CMD_SETUP_CLOSE_DURATION:
         if ("50db06".equals(resultStr)) {
            showToast(getString(R.string.setup_x_success,
                  getString(R.string.curtain_close_duration)));
         }
         break;
      case CMD_OPEN:
         if ("50db02".equals(resultStr)) {
            mOpenBtn.setTextColor(getResources().getColor(android.R.color.white));
            hideProgressDialog();
         }
         break;
      case CMD_CLOSE:
         if ("50db03".equals(resultStr)) {
            mOpenBtn.setTextColor(getResources().getColor(android.R.color.white));
            hideProgressDialog();
         }
         break;
      case CMD_STOP:
         if ("50db04".equals(resultStr)) {
            mOpenBtn.setTextColor(getResources().getColor(android.R.color.white));
            hideProgressDialog();
         }
         break;
      }
   }

   class CurtainDataListener extends DeviceDataListener {

      public CurtainDataListener(DeviceItem device) {
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

   class CurtainItem extends DeviceItem {

      int command = 0;

      public CurtainItem() {
         super();
      }

      public CurtainItem(DeviceItem device) {
         this.name = device.name;
         this.config = device.config;
         this.listener = device.listener;
         this.socket = device.socket;
      }

   }

}
