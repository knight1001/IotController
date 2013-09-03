package com.oosic.iot.controller.library;

import android.content.Context;
import android.os.Handler;

public abstract class IotAsyncTask<Result> extends Thread {

   protected Context context;
   protected Handler handler;

   public IotAsyncTask(Handler handler) {
      this.handler = handler;
   }

   public IotAsyncTask(Handler handler, Context context) {
      this(handler);
      this.context = context;
   }

   @Override
   public void run() {
      final Result result = doInBackground();

      if (this.handler != null) {
         this.handler.post(new Runnable() {
            public void run() {
               onPostExecute(result);
            }
         });
      }
   }

   public abstract Result doInBackground();

   public abstract void onPostExecute(Result result);

}

