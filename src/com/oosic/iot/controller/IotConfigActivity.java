package com.oosic.iot.controller;

import java.net.DatagramPacket;
import com.oosic.iot.controller.library.IotConfig;
import com.oosic.iot.controller.library.IotConfigListner;
import com.oosic.iot.controller.library.IotDevice;
import com.oosic.iot.controller.library.IotEvent;
import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.R;
import com.oosic.iot.controller.utils.NetworkHelper;
import com.oosic.iot.controller.utils.UIUtils;
import com.oosic.iot.controller.utils.Utils;
import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class IotConfigActivity extends IotBaseActivity implements
      IotConfigListner {

   private static final String TAG = IotConfigActivity.class.getCanonicalName();

   private TextView mSsidView;
   private TextView mPasswordView;
   private TextView mGatewayView;
   private TextView mEncryptionKeyView;
   private TextView mDeviceNameView;
   private ImageButton mStartBtn;
   private ProgressBar mProgressBar;
   private IotManager mIotManager;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_config);

      findViews();
      init();
   }

   private void findViews() {
      mSsidView = (TextView) findViewById(R.id.config_ssid);
      mPasswordView = (TextView) findViewById(R.id.config_password);
      mGatewayView = (TextView) findViewById(R.id.config_gateway);
      mEncryptionKeyView = (TextView) findViewById(R.id.config_key);
      mDeviceNameView = (TextView) findViewById(R.id.config_device_name);
      mStartBtn = (ImageButton) findViewById(R.id.config_start_btn);
      mProgressBar = (ProgressBar) findViewById(R.id.config_progress);
   }

   private void init() {
      mIotManager = getIotManager();
      WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
      WifiInfo wifiInfo = wifiManager.getConnectionInfo();
      if (wifiInfo != null) {
         mSsidView.setText(wifiInfo.getSSID());
      }
      DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
      if (dhcpInfo != null) {
         mGatewayView.setText(NetworkHelper.int2IpAddress(dhcpInfo.gateway));
      }

      initViews();
   }

   private void initViews() {
      mStartBtn.setSelected(false);
      mStartBtn.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            if (!v.isSelected()) {
               startConfig();
            } else {
               stopConfig();
            }
         }
      });
   }

   private void startConfig() {
      if (!isConfigInProgress()) {
         IotConfig config = mIotManager.getConfig();
         try {
            config.setSsid(mSsidView.getText().toString());
            config.setPassword(mPasswordView.getText().toString());
            config.setGatewayIp(mGatewayView.getText().toString());
            config.setEncryptionKey(mEncryptionKeyView.getText().toString());
            config.setAckString(mDeviceNameView.getText().toString());
            config.setHandler(new Handler());
            config.setConfigListner(this);
            config.start();
            // mStartBtn.setText(R.string.stop);
            mStartBtn.setSelected(true);
            mProgressBar.setVisibility(View.VISIBLE);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   private void stopConfig() {
      if (isConfigInProgress()) {
         mIotManager.getConfig().stop();
         // mStartBtn.setText(R.string.start);
         mStartBtn.setSelected(false);
         mProgressBar.setVisibility(View.INVISIBLE);
      }
   }

   private boolean isConfigInProgress() {
      return mProgressBar.getVisibility() == View.VISIBLE;
   }

   private void showExitAlertDialog() {
      UIUtils
            .getAlertDialogBuilder(this)
            .setMessage(R.string.exit_alert_on_config)
            .setPositiveButton(R.string.ok,
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                        finish();
                     }
                  }).setNegativeButton(R.string.cancel, null).show();
   }

   @Override
   public void onBackPressed() {
      if (isConfigInProgress()) {
         showExitAlertDialog();
         return;
      }
      super.onBackPressed();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      stopConfig();
   }

   @Override
   public void onConfigEvent(IotEvent event, Object obj) {
      Utils.logi(TAG, "onConfigEvent: " + event);
      if (IotEvent.isSuccess(event)) {
         if (obj instanceof DatagramPacket) {
            DatagramPacket packet = (DatagramPacket) obj;
            String ip = packet.getAddress().getHostAddress();
            Utils.logi(TAG, "onConfigEvent: device=" + ip);
            if (!mIotManager.hasDevice(ip)) {
               IotDevice dev = new IotDevice();
               dev.setIp(ip);
               mIotManager.addDevice(dev);
            }
         }

         stopConfig();
         UIUtils.getAlertDialogBuilder(this)
               .setMessage(R.string.device_config_ok)
               .setPositiveButton(R.string.ok, null).show();
      }
   }

}
