package com.oosic.iot.controller.library;

public interface IotResponseListener {

   public void onReceiveResponse(IotEvent event, IotResponse response);
   
}
