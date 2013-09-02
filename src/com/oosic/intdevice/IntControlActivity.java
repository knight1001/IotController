package com.oosic.intdevice;

import android.os.Bundle;

public class IntControlActivity extends IntBaseActivity {

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_control);

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
