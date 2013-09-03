package com.oosic.iot.controller.library;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

public class IotConfig {

   private static final String DEFAULT_SYNC_L_STRING = "abc";
   private static final String DEFAULT_SYNC_H_STRING = "abcdefghijklmnopqrstuvw";
   private static final String DEFAULT_ENCRYPTION_KEY = "smartconfigAES16";
   private static final String DEFAULT_ACK_STRING = "CC3000";

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
   private byte[] mEncryptionKey = DEFAULT_ENCRYPTION_KEY.getBytes();
   private String mAckString = DEFAULT_ACK_STRING;
   private MulticastSocket mListeningAckSocket;
   private int mListeningAckPort;
   private Thread mTransmittingTask;
   private boolean mStopTransmitting = false;
   private InetSocketAddress mTransmittingSocketAddr;
   private int mTransmittingPort;
   private Thread mListeningAckTask;
   private boolean mStopListening;

   public IotConfig(Context context) {
      mContext = context;
   }

   public void setHandler(Handler handler) {
      mHandler = handler;
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

   public void setGatewayIp(String gatewayIp) {
      mGatewayIp = gatewayIp;
   }

   public void setPassword(String password) throws IntException {
      if (!TextUtils.isEmpty(password)
            && password.length() > MAX_LENGTH_OF_PASSWORD) {
         throw new IntException(IntException.LENGTH_OF_PASSWORD_EXCEEDS,
               IntException.MSG_LENGTH_OF_PASSWORD_EXCEEDS);
      }
      mPassword = password;
   }

   public void setEncryptionKey(byte[] encryptionKey) throws Exception {
      if (encryptionKey != null
            && encryptionKey.length != LENGTH_OF_ENCRYPTION_KEY) {
         throw new IntException(IntException.ENCRYPTION_KEY_ERROR,
               IntException.MSG_ENCRYPTION_KEY_ERROR);
      }
      mEncryptionKey = encryptionKey;
   }

   public void setAckString(String mdnsAckString) {
      mAckString = mdnsAckString;
   }

   public void start() {
      new TransmittingTask(mHandler, mContext).start();
      new ListeningAckTask(mHandler, mContext).start();
   }

   public void stop() {

   }

   private void createListeningAckSocket(int port) throws Exception {
      InetAddress wildcardAddr = null;
      InetSocketAddress localAddr = new InetSocketAddress(wildcardAddr, port);
      mListeningAckSocket = new MulticastSocket(null);
      mListeningAckSocket.setReuseAddress(true);
      mListeningAckSocket.bind(localAddr);
      mListeningAckSocket.setTimeToLive(255);
      mListeningAckSocket.joinGroup(InetAddress.getByName("224.0.0.251"));
      mListeningAckSocket.setBroadcast(true);
   }

   private void send(DatagramPacket packet, int port) throws Exception {
      DatagramSocket sock = new DatagramSocket(port);
      sock.send(packet);
      sock.close();
   }

   private void transmitSettings() throws Exception {
      
   }

   private void stopTransmitting() {

   }

   private class TransmittingTask extends IntAsyncTask<Void> {

      public TransmittingTask(Handler handler, Context context) {
         super(handler, context);
      }

      @Override
      public Void doInBackground() {
         return null;
      }

      @Override
      public void onPostExecute(Void result) {

      }

   }

   private class ListeningAckTask extends IntAsyncTask<DatagramPacket> {

      public ListeningAckTask(Handler handler, Context context) {
         super(handler, context);
      }

      @Override
      public DatagramPacket doInBackground() {
         return null;
      }

      @Override
      public void onPostExecute(DatagramPacket result) {

      }

   }

   private boolean hasAckString(byte[] data) {
      try {
         return mAckString.equals(parseAckString(data));
      } catch (Exception e) {
         // e.printStackTrace();
      }
      return false;
   }

   private String parseAckString(byte[] data) throws Exception {
      final int MDNS_HEADER_SIZE = 12;
      final int MDNS_HEADER_SIZE2 = 10;

      int pos = 12;

      if (data.length < pos + 1) {
         return null;
      }
      int len = data[pos] & 0xFF;
      pos++;

      while (len > 0) {
         if (data.length < pos + len) {
            return null;
         }
         pos += len;

         if (data.length < pos + 1) {
            return null;
         }
         len = data[pos] & 0xFF;
         pos++;
      }

      pos += 10;

      if (data.length < pos + 1) {
         return null;
      }
      len = data[pos] & 0xFF;
      pos++;

      if (data.length < pos + len) {
         return null;
      }
      return new String(data, pos, len);
   }

   private byte[] encryptData(byte[] data) throws Exception {
      if (this.mEncryptionKey == null)
         return data;
      if (this.mEncryptionKey.length == 0) {
         return data;
      }
      int ZERO_OFFSET = 0;
      int AES_LENGTH = 16;
      int DATA_LENGTH = 32;

      Cipher c = null;
      byte[] encryptedData = null;
      byte[] paddedData = new byte[32];
      byte[] aesKey = new byte[16];

      for (int x = 0; x < 16; x++) {
         if (x < this.mEncryptionKey.length) {
            aesKey[x] = this.mEncryptionKey[x];
         } else {
            aesKey[x] = 0;
         }

      }

      int dataOffset = 0;
      if (data.length < 32) {
         paddedData[dataOffset] = (byte) data.length;
         dataOffset++;
      }

      System.arraycopy(data, 0, paddedData, dataOffset, data.length);
      dataOffset += data.length;

      while (dataOffset < 32) {
         paddedData[dataOffset] = 0;
         dataOffset++;
      }

      c = Cipher.getInstance("AES/ECB/NoPadding");

      SecretKeySpec k = null;
      k = new SecretKeySpec(aesKey, "AES");

      c.init(1, k);

      encryptedData = c.doFinal(paddedData);

      return encryptedData;
   }

   private byte[] makePaddedByteArray(int length) throws Exception {
      byte[] paddedArray = new byte[length];

      for (int x = 0; x < length; x++) {
         paddedArray[x] = (byte) "1".charAt(0);
      }

      return paddedArray;
   }

}
