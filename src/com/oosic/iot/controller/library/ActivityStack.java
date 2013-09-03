package com.oosic.iot.controller.library;

import java.util.LinkedList;

import android.app.Activity;

public class ActivityStack {
   
//   private static ActivityStack sInstance = null;

   private LinkedList<Activity> mActivities;
   
//   public static ActivityStack getInstance() {
//      if (sInstance == null) {
//         synchronized (ActivityStack.class) {
//            if (sInstance == null) {
//               sInstance = new ActivityStack();
//            }
//         }
//      }
//      return sInstance;
//   }
   
   public ActivityStack() {
      mActivities = new LinkedList<Activity>();
   }
   
   public int getCount() {
      return mActivities.size();
   }
   
   public void push(Activity activity) {
      mActivities.addFirst(activity);
   }
   
   public Activity pop() {
      return mActivities.removeFirst();
   }
   
   public void remove(Activity activity) {
      if (mActivities.contains(activity)) {
         mActivities.remove(activity);
      }
   }
   
   public Activity getTop() {
      return mActivities.getFirst();
   }
   
   public Activity getBottom() {
      return mActivities.getLast();
   }
   
   public void finishAll() {
      while (!mActivities.isEmpty()) {
         mActivities.removeFirst().finish();
      }
   }
   
   public void cleanup() {
      finishAll();
      
//      sInstance = null;
   }
   
}
