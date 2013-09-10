package com.oosic.iot.controller.library;

public class IotDevice {

   private String ip;
   private String mac;
   private boolean selected = true;

   public String getIp() {
      return this.ip;
   }

   public void setIp(String ip) {
      this.ip = ip;
   }

   public String getMac() {
      return this.mac;
   }

   public void setMac(String mac) {
      this.mac = mac;
   }

   public boolean isSelected() {
      return this.selected;
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }

}
