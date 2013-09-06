package com.oosic.iot.controller;

import com.oosic.iot.controller.library.IotConfig;
import com.oosic.iot.controller.library.IotConfigListner;
import com.oosic.iot.controller.library.IotEvent;
import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.utils.NetworkHelper;
import com.oosic.iot.controller.utils.UIUtils;
import com.oosic.iot.controller.utils.Utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
      mIotManager = ((IotApp) getApplication()).getIotManager();
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
      IotConfig config = mIotManager.getConfig();
      try {
         config.setSsid(mSsidView.getText().toString());
         config.setPassword(mPasswordView.getText().toString());
         config.setGatewayIp(mGatewayView.getText().toString());
         config.setEncryptionKey(mEncryptionKeyView.getText().toString());
         config.setAckString(mDeviceNameView.getText().toString());
         config.start();
         // mStartBtn.setText(R.string.stop);
         mStartBtn.setSelected(true);
         mProgressBar.setVisibility(View.VISIBLE);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void stopConfig() {
      mIotManager.getConfig().stop();
      // mStartBtn.setText(R.string.start);
      mStartBtn.setSelected(false);
      mProgressBar.setVisibility(View.INVISIBLE);
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();

      stopConfig();
   }

   @Override
   public void onConfigEvent(IotEvent event, Object obj) {
      Utils.logi(TAG, "onConfigEvent: " + "event=" + event + " obj=" + obj);
      if (IotEvent.isSuccess(event)) {
         stopConfig();
         UIUtils.getAlertDialogBuilder(this).setTitle(R.string.device_config)
               .setMessage(R.string.device_config_ok);
      }
   }

}
