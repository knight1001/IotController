package com.oosic.iot.controller;

import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.utils.UIUtils;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SensorControlActivity extends IotBaseActivity {

   private static final String TAG = "IotControlActivity";

   private TextView mTempView, mHumidityView, mFanSwitcherView, mLightView,
         mLightSwitcherView, mFormolView;
   private Handler mHandler = new Handler();
   private IotManager mIotManager;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_sensorcontrol);

      findViews();
      init();
   }

   private void findViews() {
      mTempView = (TextView) findViewById(R.id.temp_sensor);
      mHumidityView = (TextView) findViewById(R.id.humidity_sensor);
      mFanSwitcherView = (TextView) findViewById(R.id.fan_switcher);
   }

   private void init() {
      mIotManager = getIotManager();
      initViews();
   }

   private void initViews() {
      mFanSwitcherView.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {

         }
      });
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

   }

   private void showToast(String msg) {
      UIUtils.showToast(this, msg);
   }

   private void showMessageDialog(String msg) {
      UIUtils.getAlertDialogBuilder(this).setMessage(msg)
            .setPositiveButton(R.string.ok, null).show();
   }

}
