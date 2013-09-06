package com.oosic.iot.controller.library;

public enum IotEvent {

   SUCCESS,
   ERROR,
   TIMEOUT;
   
   public static final boolean isSuccess(IotEvent event) {
      return SUCCESS.compareTo(event) == 0;
   }
   
}
