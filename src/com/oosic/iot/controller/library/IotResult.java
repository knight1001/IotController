package com.oosic.iot.controller.library;

public class IotResult {

   private IotEvent event;
   private Exception exception;
   
   public IotResult() {
      
   }
   
   public IotResult(IotEvent event) {
      this();
      this.event = event;
   }
   
   public IotResult(IotEvent event, Exception e) {
      this(event);
      this.exception = e;
   }

   public IotEvent getEvent() {
      return this.event;
   }

   public void setEvent(IotEvent event) {
      this.event = event;
   }
   
   public Exception getException() {
      return this.exception;
   }
   
   public void setExeption(Exception e) {
      this.exception = e;
   }
   
   public boolean isSuccess() {
      return this.event.ordinal() == IotEvent.SUCCESS.ordinal();
   }
   
}
