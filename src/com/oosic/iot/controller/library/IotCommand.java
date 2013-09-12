package com.oosic.iot.controller.library;

import android.text.TextUtils;

public class IotCommand {

   public static final String REG = "REG";
   public static final String GET = "GET";
   public static final String CHK = "CHK";
   public static final String SCH = "SCH";
   public static final String STS = "STS";
   public static final String DON = "DON";
   public static final String NON = "NON";
   public static final String PND = "PND";
   public static final String OKA = "OKA";
   public static final String NIL = "NIL";
   public static final String FUL = "FUL";

   private static final String SEPARATOR = ",";

   private int index = -1;
   private String command;
   private IotCommandType type;

   public IotCommand() {

   }

   public IotCommand(String command) {
      this.command = command;
   }

   public IotCommand(String command, IotCommandType type) {
      this(command);
      this.type = type;
   }

   public int getIndex() {
      return this.index;
   }
   
   public void setIndex(int index) {
      this.index = index;
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

   @Override
   public String toString() {
      return new StringBuilder(this.command).append(SEPARATOR)
            .append(this.type).toString();
   }

   public static IotCommand parseString(String str) {
      IotCommand result = null;
      String[] arr = str.split(SEPARATOR);
      if (arr != null && arr.length == 2) {
         result = new IotCommand();
         if (!TextUtils.isEmpty(arr[0])) {
            result.setCommand(arr[0]);
         }
         if (!TextUtils.isEmpty(arr[1])) {
            result.setType(IotCommandType.valueOf(arr[1]));
         }
      }
      return result;
   }

}
