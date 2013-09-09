package com.oosic.iot.controller.library;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.conn.ConnectTimeoutException;

import com.oosic.iot.controller.utils.Utils;

import android.content.Context;
import android.database.CursorJoiner.Result;
import android.os.Handler;
import android.text.TextUtils;

public class IotConfig {
   
   private static final String TAG = "IotConfig";

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
   private String mEncryptionKey = DEFAULT_ENCRYPTION_KEY;
   private String mAckString = DEFAULT_ACK_STRING;
   private ArrayList<Integer> mEncryptedSettings;

   private MulticastSocket mListenAckSocket;
   private int mListenAckPort;
   private Thread mTransmitTask;
   private boolean mStopTransmitting = false;
   private InetSocketAddress mTransmitSocketAddr;
   private int mTransmitPort = DEFAULT_LOCAL_PORT;
   private Thread mListenAckTask;
   private boolean mStopListening = false;

   private IotConfigListner mConfigListner;

   public IotConfig(Context context) {
      mContext = context;
   }

   public void setHandler(Handler handler) {
      mHandler = handler;
   }

   public void setSsid(String ssid) throws Exception {
      if (TextUtils.isEmpty(ssid)) {
         throw new NullPointerException("SSID is empty");
      }
      if (ssid.length() > MAX_LENGTH_OF_SSID) {
         throw new IotException(IotException.LENGTH_OF_SSID_EXCEEDS,
               IotException.MSG_LENGTH_OF_SSID_EXCEEDS);
      }
      mSsid = ssid;
   }

   public void setGatewayIp(String gatewayIp) {
      mGatewayIp = gatewayIp;
   }

   public void setPassword(String password) throws Exception {
      if (!TextUtils.isEmpty(password)
            && password.length() > MAX_LENGTH_OF_PASSWORD) {
         throw new IotException(IotException.LENGTH_OF_PASSWORD_EXCEEDS,
               IotException.MSG_LENGTH_OF_PASSWORD_EXCEEDS);
      }
      mPassword = password;
   }

   public void setEncryptionKey(String encryptionKey) throws Exception {
      if (encryptionKey != null
            && encryptionKey.length() != LENGTH_OF_ENCRYPTION_KEY) {
         throw new IotException(IotException.ENCRYPTION_KEY_ERROR,
               IotException.MSG_ENCRYPTION_KEY_ERROR);
      }
      mEncryptionKey = encryptionKey;
   }

   public void setAckString(String mdnsAckString) {
      mAckString = mdnsAckString;
   }

   public void setConfigListner(IotConfigListner listner) {
      mConfigListner = listner;
   }

   public void start() {
      Utils.logi(TAG, "start()");
      try {
         mTransmitTask = new TransmitSettingsTask(mHandler, mContext);
         mTransmitTask.start();

         mListenAckTask = new ListenAckTask(mHandler, mContext);
         mListenAckTask.start();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void stop() {
      Utils.logi(TAG, "stop()");
      stopListening();
      stopTransmitting();
   }

   private void send(DatagramPacket packet, int port) throws Exception {
      DatagramSocket sock = new DatagramSocket(port);
      sock.send(packet);
      sock.close();
   }

   private void transmitSettings() throws Exception {
      Utils.logi(TAG, "transmitSettings()");
      byte[] syncLBuffer = DEFAULT_SYNC_L_STRING.getBytes();
      byte[] syncHBuffer = DEFAULT_SYNC_H_STRING.getBytes();

      mEncryptedSettings = new SsidEncryption(mSsid, mPassword, mEncryptionKey)
            .getEncryptedData();
      mTransmitSocketAddr = new InetSocketAddress(mGatewayIp, mTransmitPort);
      ArrayList<Integer> packets = mEncryptedSettings;
      int numberOfPackets = mEncryptedSettings.size();
      InetSocketAddress socketAddr = mTransmitSocketAddr;
      int port = mTransmitPort;
      byte[] sendBuff = makePaddedByteArray(1600);
      while (!mStopTransmitting) {
         for (int i = 0; i < DEFAULT_NUMBER_OF_SETUPS; i++) {
            for (int j = 0; j < numberOfPackets; j++) {
               send(new DatagramPacket(sendBuff, (packets.get(j)).intValue(),
                     socketAddr), port);

               if (((packets.get(j)).intValue() != SsidEncryption.FLAG_START)
                     && ((packets.get(j)).intValue() != SsidEncryption.FLAG_SEPARATOR)) {
                  send(new DatagramPacket(syncLBuffer, syncLBuffer.length,
                        socketAddr), port);
                  send(new DatagramPacket(syncHBuffer, syncHBuffer.length,
                        socketAddr), port);
               }
            }
         }

         try {
            Thread.sleep(100L);
         } catch (InterruptedException e) {
            return;
         }
      }
   }

   private void stopTransmitting() {
      mStopTransmitting = true;
      if (mTransmitTask != null && mTransmitTask != Thread.currentThread()) {
         mTransmitTask.interrupt();
      }
   }

   private void stopListening() {
      mStopListening = true;
      if (mListenAckSocket != null) {
         mListenAckSocket.close();
      }
   }

   private void createListenAckSocket() throws Exception {
      InetAddress wildcardAddr = null;
      InetSocketAddress localAddr = new InetSocketAddress(wildcardAddr,
            DEFAULT_LISTEN_PORT);
      mListenAckSocket = new MulticastSocket(null);
      mListenAckSocket.setReuseAddress(true);
      mListenAckSocket.bind(localAddr);
      mListenAckSocket.setTimeToLive(255);
      mListenAckSocket.joinGroup(InetAddress.getByName("224.0.0.251"));
      mListenAckSocket.setBroadcast(true);
   }

   private IotConfigResult waitForAck() throws Exception {
      Utils.logi(TAG, "waitForAck()");
      final int RECV_BUFFER_LENGTH = 16384;
      byte[] recvBuff = new byte[RECV_BUFFER_LENGTH];
      DatagramPacket recvPacket = new DatagramPacket(recvBuff, recvBuff.length);
      int timeout = TIMEOUT_WAITING_FOR_ACK;

      createListenAckSocket();

      while (!mStopListening) {
         long start = System.nanoTime();
         try {
            mListenAckSocket.setSoTimeout(timeout);
            mListenAckSocket.receive(recvPacket);
         } catch (InterruptedIOException e) {
            e.printStackTrace();
            continue;
//            stop();
//            return new IotConfigResult(IotEvent.TIMEOUT, e);
         } catch (Exception e) {
            e.printStackTrace();
            if (!mStopListening) {
               stop();
               return new IotConfigResult(IotEvent.ERROR, e);
            }
            return null;
         }

         Utils.logi(TAG, "waitForAck: " + System.currentTimeMillis());
         if (hasAckString(recvPacket.getData())) {
            Utils.logi(TAG, "waitForAck: Received ACK");
            stop();
            IotConfigResult result = new IotConfigResult(IotEvent.SUCCESS);
            result.setPacket(recvPacket);
            return result;
         }

         timeout = (int) (timeout - (System.nanoTime() - start) / 1000000L);
         if (timeout <= 0) {
            Utils.logi(TAG, "waitForAck: TIMEOUT");
            stop();
            return new IotConfigResult(IotEvent.TIMEOUT);
         }
      }
      return null;
   }

   private class TransmitSettingsTask extends IotAsyncTask<Void> {

      private static final String TAG = "TransmitSettingsTask";
      
      public TransmitSettingsTask(Handler handler, Context context) {
         super(handler, context);
      }

      @Override
      public Void doInBackground() {
         try {
            transmitSettings();
         } catch (Exception e) {
            e.printStackTrace();
         }
         return null;
      }

      @Override
      public void onPostExecute(Void result) {
         Utils.logi(TAG, "onPostExecute");
      }

   }

   private class ListenAckTask extends IotAsyncTask<IotConfigResult> {

      private static final String TAG = "ListenAckTask";
      
      public ListenAckTask(Handler handler, Context context) {
         super(handler, context);
      }

      @Override
      public IotConfigResult doInBackground() {
         try {
            return waitForAck();
         } catch (Exception e) {
            e.printStackTrace();
         }
         return new IotConfigResult(IotEvent.ERROR);
      }

      @Override
      public void onPostExecute(IotConfigResult result) {
         Utils.logi(TAG, "onPostExecute");
         if (result != null && mConfigListner != null) {
            mConfigListner.onConfigEvent(result.getEvent(), result.getPacket());
         }
      }

   }

   private boolean hasAckString(byte[] data) {
      return mAckString.equals(parseAckString(data));
   }

   private String parseAckString(byte[] data) {
      int pos = 12;

      if (data.length < pos + 1) {
         return null;
      }
      int len = data[pos] & 0xff;
      pos++;

      while (len > 0) {
         if (data.length < pos + len) {
            return null;
         }
         pos += len;

         if (data.length < pos + 1) {
            return null;
         }
         len = data[pos] & 0xff;
         pos++;
      }

      pos += 10;

      if (data.length < pos + 1) {
         return null;
      }
      len = data[pos] & 0xff;
      pos++;

      if (data.length < pos + len) {
         return null;
      }

      return new String(data, pos, len);
   }

   private byte[] makePaddedByteArray(int length) {
      byte[] paddedArray = new byte[length];

      for (int x = 0; x < length; x++) {
         paddedArray[x] = '1';
      }

      return paddedArray;
   }

}
