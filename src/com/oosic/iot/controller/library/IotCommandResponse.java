package com.oosic.iot.controller.library;

public class IotCommandResponse extends IotResponse {

   private String command;
   private String result;

   public String getCommand() {
      return this.command;
   }

   public void setCommand(String command) {
      this.command = command;
   }

   public String getResult() {
      return this.result;
   }

   public void setResult(String result) {
      this.result = result;
   }
}
