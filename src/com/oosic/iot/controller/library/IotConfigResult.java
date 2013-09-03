package com.oosic.iot.controller.library;

import java.net.DatagramPacket;

public class IotConfigResult extends IotResult {
   
   private DatagramPacket packet;

   public IotConfigResult() {
      super();
   }
   
   public IotConfigResult(IotEvent event) {
      super(event);
   }
   
   public IotConfigResult(IotEvent event, Exception e) {
      super(event, e);
   }

   public DatagramPacket getPacket() {
      return this.packet;
   }

   public void setPacket(DatagramPacket packet) {
      this.packet = packet;
   }
   
}
