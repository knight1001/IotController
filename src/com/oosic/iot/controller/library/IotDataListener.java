package com.oosic.iot.controller.library;

public interface IotDataListener {

   public void onDataSent(IotResult result, Object obj);
   
   public void onDataReceived(IotResult result, Object obj);
   
}
