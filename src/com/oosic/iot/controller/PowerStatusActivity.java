package com.oosic.iot.controller;

import java.io.Reader;
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
import android.widget.TextView;

public class PowerStatusActivity extends IotBaseActivity {

   private static final String TAG = "PowerStatusActivity";

   private static final int CMD_GET_POWER = 1;
   private static final int DURATION_GET_POWER = 60000; // ms

   private TextView mPowerView;
   private Button mReadPowerBtn;
   private Handler mHandler = new Handler();
   private IotManager mIotManager;
   private DeviceItem mDevice;
   private ReadPowerRunnable mReadPowerRunnable = new ReadPowerRunnable();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_power);

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

      readPower();
      requestReadPower(DURATION_GET_POWER);
   }

   private void initViews() {
      DeviceItem device = new DeviceItem();
      device.config = new DeviceConfig(DEV_BW800PW, 220, "192.168.1.220", 5000,
            COMP_ALL, 1);
      device.listener = new PowerDataListener(device);
      mDevice = device;

      mPowerView = (TextView) findViewById(R.id.power_value);
      mPowerView.setText(getString(R.string.current_power_value, ""));
      mReadPowerBtn = (Button) findViewById(R.id.read_power_btn);
      mReadPowerBtn.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            requestReadPower(1000);
         }
      });
   }

   private void relayoutViews() {

   }

   private void readPower() {
      sendCommand(mDevice, getCommand(mDevice, CMD_GET_POWER));
   }

   private void requestReadPower(long delayMillis) {
      mHandler.removeCallbacks(mReadPowerRunnable);
      mHandler.postDelayed(mReadPowerRunnable, delayMillis);
   }

   class ReadPowerRunnable implements Runnable {

      @Override
      public void run() {
         readPower();
         requestReadPower(DURATION_GET_POWER);
      }

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
      data[2] = (byte) 0xee;
      switch (command) {
      case CMD_GET_POWER:
         data[3] = (byte) 0x02;
         data[4] = (byte) 0x00;
         data[5] = (byte) 0x00;
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

   private void analyzeResult(PowerDataListener listener, DatagramPacket packet) {
      byte[] data = packet.getData();
      String resultStr = IotManager.toHexString(data, 0, packet.getLength());
      Utils.log(TAG, "analyzeResult: " + resultStr);
      DeviceItem device = listener.device;
      String str = resultStr.length() <= 6 ? resultStr: resultStr.substring(0, 6);
      if (packet.getLength() == 8 && "50eb02".equals(str)) {
         long value = ((data[6] << 24) & 0xff000000)
               | ((data[5] << 16) & 0xff0000) | ((data[4] << 8) & 0xff00)
               | data[3] & 0xff;
         double powerValue = (double) value / (double) 3200;
         String powerStr = Double.toString(powerValue);
         int length = powerStr.length();
         String result = powerStr;
         int dotIndex = powerStr.indexOf('.');
         if (dotIndex > 0) {
            dotIndex += 4;
            if (dotIndex > length) {
               dotIndex = length;
            }
            result = powerStr.substring(0, dotIndex);
         }
         mPowerView.setText(getString(R.string.current_power_value, result));
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
