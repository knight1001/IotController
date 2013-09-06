package com.oosic.iot.controller;

import com.oosic.iot.controller.library.ActivityStack;
import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.library.PreferenceManager;
import com.oosic.iot.controller.utils.UIUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class IotBaseActivity extends Activity {

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (getActivityStack() == null) {
         ((IotApp) getApplication()).prepareEnvironment();
      }

      getActivityStack().push(this);
   }

   protected void onPause() {
      super.onPause();
   }

   @Override
   protected void onResume() {
      super.onResume();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();

      getActivityStack().remove(this);

      if (getActivityStack().getCount() <= 0) {
         ((IotApp) getApplication()).cleanupEnvironment();
      }
   }

   protected void showToast(Context context, String msg) {
      UIUtils.showToast(context, msg);
   }

   protected ActivityStack getActivityStack() {
      return ((IotApp) getApplication()).getActivityStack();
   }

   protected PreferenceManager getPrefsManager() {
      return ((IotApp) getApplication()).getPrefsManager();
   }
   
   protected IotManager getIotManager() {
      return ((IotApp) getApplication()).getIotManager();
   }

}
