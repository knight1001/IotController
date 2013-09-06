package com.oosic.iot.controller;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IotControlActivity extends IotBaseActivity {

   private Button mConfigBtn, mDeviceListBtn, mSearchBtn;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_control);

      findViews();
      initViews();
   }

   private void findViews() {
      mConfigBtn = (Button) findViewById(R.id.config_btn);
      mDeviceListBtn = (Button) findViewById(R.id.device_list_btn);
      mSearchBtn = (Button) findViewById(R.id.search_btn);
   }

   private void initViews() {
      mConfigBtn.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            startActivity(new Intent(IotControlActivity.this,
                  IotConfigActivity.class));
         }
      });
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

}
