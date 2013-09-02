package com.oosic.intdevice.library;

public class IntException extends Exception {

   public static final int ENCRYPTION_KEY_ERROR = -1;
   public static final int LENGTH_OF_SSID_EXCEEDS = -2;
   public static final int LENGTH_OF_PASSWORD_EXCEEDS = -3;

   public static final String MSG_ENCRYPTION_KEY_ERROR = "Encryption key must have 16 characters!";
   public static final String MSG_LENGTH_OF_SSID_EXCEEDS = "Network name (SSID) is too long! Maximum length is 32 characters.";
   public static final String MSG_LENGTH_OF_PASSWORD_EXCEEDS = "Password is too long! Maximum length is 32 characters.";

   protected int code = 0;

   public IntException(int errorCode, String errorMsg) {
      super(errorMsg);
      this.code = errorCode;
   }

   public int getCode() {
      return this.code;
   }

}
