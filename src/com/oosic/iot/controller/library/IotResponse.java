package com.oosic.iot.controller.library;

public class IotResponse {

   private String ip;
   private String mac;
   private int port;

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

   public int getPort() {
      return this.port;
   }

   public void setPort(int port) {
      this.port = port;
   }

}
