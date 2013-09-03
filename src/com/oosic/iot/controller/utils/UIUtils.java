package com.oosic.iot.controller.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.widget.Toast;

public class UIUtils {

   public static AlertDialog.Builder getAlertDialogBuilder(Context context) {
      if (Build.VERSION.SDK_INT > 11) {
         return new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
      } else {
         return new AlertDialog.Builder(context);
      }
   }
   
   public static Toast showToast(Context context, String msg) {
      Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
      toast.show();
      return toast;
   }
   
}
