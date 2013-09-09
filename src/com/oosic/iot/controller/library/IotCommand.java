package com.oosic.iot.controller.library;

public class IotCommand {
   
   public static final String STS = "STS";
   public static final String CHK = "CHK";
   
   private String command;
   private IotCommandType type;
   
   public IotCommand(String command) {
      this.command = command;
   }
   
   public String getCommand() {
      return this.command;
   }
   
   public void setCommand(String cmd) {
      this.command = cmd;
   }
   
   public IotCommandType getType() {
      return this.type;
   }
   
   public void setType(IotCommandType type) {
      this.type = type;
   }
   
}
