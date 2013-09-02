package com.oosic.intdevice;

import android.os.Bundle;

public class IntConfigActivity extends IntBaseActivity {

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_config);

      findViews();
      initViews();
   }

   private void findViews() {

   }

   private void initViews() {

   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

}
