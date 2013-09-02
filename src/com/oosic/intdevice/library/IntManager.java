package com.oosic.intdevice.library;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

public class IntManager {

   private static final String DEFAULT_SYNC_L_STRING = "abc";
   private static final String DEFAULT_SYNC_H_STRING = "abcdefghijklmnopqrstuvw";
   private static final String DEFAULT_ENCRYPTION_KEY = "smartconfigAES16";
   private static final String DEFAULT_MDNS_ACK_STRING = "CC3000";

   private static final int LENGTH_OF_ENCRYPTION_KEY = 16;
   private static final int MAX_LENGTH_OF_PASSWORD = 32;
   private static final int MAX_LENGTH_OF_SSID = 32;

   private static final int DEFAULT_LISTEN_PORT = 5353;
   private static final int DEFAULT_LOCAL_PORT = 15000;
   private static final int TIMEOUT_WAITING_FOR_ACK = 30000;
   private static final int DEFAULT_NUMBER_OF_SETUPS = 4;
   private static final int DEFAULT_NUMBER_OF_SYNCS = 10;

   private Context mContext;
   private Handler mHandler;
   private String mPassword;
   private String mSsid;
   private String mGatewayIp;
   private String mEncryptionKey = DEFAULT_ENCRYPTION_KEY;
   private String mDnsAckString = DEFAULT_MDNS_ACK_STRING;

   public IntManager(Context context) {
      mContext = context;
   }

   public Handler getHandler() {
      return mHandler;
   }

   public void setHandler(Handler handler) {
      mHandler = handler;
   }

   public String getPassword() {
      return mPassword;
   }

   public void setPassword(String password) throws IntException {
      if (!TextUtils.isEmpty(password)
            && password.length() > MAX_LENGTH_OF_PASSWORD) {
         throw new IntException(IntException.LENGTH_OF_PASSWORD_EXCEEDS,
               IntException.MSG_LENGTH_OF_PASSWORD_EXCEEDS);
      }
      mPassword = password;
   }

   public String getSsid() {
      return mSsid;
   }

   public void setSsid(String ssid) throws IntException, NullPointerException {
      if (TextUtils.isEmpty(ssid)) {
         throw new NullPointerException("SSID is empty");
      }
      if (ssid.length() > MAX_LENGTH_OF_SSID) {
         throw new IntException(IntException.LENGTH_OF_SSID_EXCEEDS,
               IntException.MSG_LENGTH_OF_SSID_EXCEEDS);
      }
      mSsid = ssid;
   }

   public String getGatewayIp() {
      return mGatewayIp;
   }

   public void setGatewayIp(String gatewayIp) {
      mGatewayIp = gatewayIp;
   }

   public String getEncryptionKey() {
      return mEncryptionKey;
   }

   public void setEncryptionKey(String encryptionKey) throws Exception {
      if (!TextUtils.isEmpty(encryptionKey) && encryptionKey.length() != 16) {
         throw new IntException(IntException.ENCRYPTION_KEY_ERROR,
               IntException.MSG_ENCRYPTION_KEY_ERROR);
      }
      mEncryptionKey = encryptionKey;
   }

   public String getMdnsAckString() {
      return mDnsAckString;
   }

   public void setMdnsAckString(String mdnsAckString) {
      mDnsAckString = mdnsAckString;
   }

}
