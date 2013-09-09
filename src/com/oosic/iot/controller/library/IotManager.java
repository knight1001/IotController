package com.oosic.iot.controller.library;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.oosic.iot.controller.utils.Utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;

public class IotManager {

   private static final String TAG = "IotManager";

   private static final String SERVER = "38.lzjgo.us";
   private static final int SERVER_PORT = 18882;
   private static final int UDP_COMMAND_PORT = 50050;
   private static final int UDP_RESPONSE_PORT = 51050;

   private Context mContext;
   private Handler mHandler;
   private IotConfig mConfig;
   private Map<String, IotDevice> mDevices;
   private DatagramSocket mListenLocalResponseSocket;
   private Thread mListenLocalResponseTask;
   private boolean mStopListeningLocalResponse = false;
   private IotResponseListener mLocalResponseListener;
   private IotResponseListener mServerResponseListener;

   public IotManager(Context context) {
      mContext = context;
      mDevices = new HashMap<String, IotDevice>();
   }

   public void setHandler(Handler handler) {
      mHandler = handler;
   }

   public void setLocalResponseListener(IotResponseListener listener) {
      mLocalResponseListener = listener;
   }

   public void setServerResponseListener(IotResponseListener listener) {
      mServerResponseListener = listener;
   }

   public IotConfig getConfig() {
      if (mConfig == null) {
         mConfig = new IotConfig(mContext);
      }
      return mConfig;
   }

   public void addDevice(IotDevice dev) {
      if (dev != null && !TextUtils.isEmpty(dev.getIp())) {
         mDevices.put(dev.getIp(), dev);
      }
   }
   
   public List<IotDevice> getDeviceList() {
      List<IotDevice> devices = null;
      if (mDevices.size() > 0) {
         devices = new ArrayList<IotDevice>();
         for (Entry<String, IotDevice> entry : mDevices.entrySet()) {
            devices.add(entry.getValue());
         }
      }
      return devices;
   }
   
   public void requestSendingBroadcast(final String cmd) {
      new Thread(new Runnable() {
         public void run() {
            sendBroadcast(cmd);
         }
      }).start();
   }

   private void sendBroadcast(String cmd) {
      WifiManager wifiManager = (WifiManager) mContext
            .getSystemService(Context.WIFI_SERVICE);
      DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
      byte[] data = new byte[4];
      data[0] = (byte) (dhcpInfo.gateway & 0xff);
      data[1] = (byte) (dhcpInfo.gateway >> 8 & 0xff);
      data[2] = (byte) (dhcpInfo.gateway >> 16 & 0xff);
      data[3] = (byte) 255;
      for (int i = 0; i < data.length; i++) {
         Utils.logi(TAG, "____________gateway: [" + i + "]=" + data[i]);
      }
      try {
         InetAddress addr = InetAddress.getByAddress(data);
         DatagramSocket socket = new MulticastSocket();
         socket.setBroadcast(true);
         data = cmd.getBytes();
         DatagramPacket packet = new DatagramPacket(data, data.length);
         packet.setAddress(addr);
         socket.setSoTimeout(10000);
         socket.send(packet);
         Utils.logi(TAG, "___________sendBroadcast: " + cmd);
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void requestSendingLocalCommand(final String cmd) {
      new Thread(new Runnable() {
         public void run() {
            sendLocalCommand(cmd);
         }
      }).start();
   }

   private void sendLocalCommand(String cmd) {
      try {
         byte[] data = cmd.getBytes();
         DatagramPacket packet = new DatagramPacket(data, data.length);
         DatagramSocket socket = new DatagramSocket();
         packet.setPort(UDP_COMMAND_PORT);
         for (Entry<String, IotDevice> entry : mDevices.entrySet()) {
            try {
               packet.setAddress(InetAddress.getByName(entry.getValue().getIp()));
               socket.send(packet);
            } catch (UnknownHostException e) {
               e.printStackTrace();
               continue;
            } catch (IOException e) {
               e.printStackTrace();
               continue;
            }
            Utils.logi(TAG, "___________sendLocalCommand: " + cmd + " -> "
                  + entry.getValue().getIp());
         }
      } catch (SocketException e) {
         e.printStackTrace();
      }
   }

   private DatagramSocket getListenLocalResponseSocket() throws SocketException {
      if (mListenLocalResponseSocket == null
            || mListenLocalResponseSocket.isClosed()) {
         mListenLocalResponseSocket = new DatagramSocket(UDP_RESPONSE_PORT);
      }
      return mListenLocalResponseSocket;
   }

   public void startListeningLocalResponse() {
      if (mListenLocalResponseTask != null) {
         return;
      }
      mListenLocalResponseTask = new Thread(new Runnable() {
         public void run() {
            while (!mStopListeningLocalResponse) {
               byte[] buff = new byte[32];
               DatagramPacket packet = new DatagramPacket(buff, buff.length);
               try {
                  DatagramSocket socket = getListenLocalResponseSocket();
                  socket.setReuseAddress(true);
                  socket.receive(packet);

                  Utils.logi(TAG, "___________from: "
                        + packet.getAddress().toString());
                  Utils.logi(TAG, "___________SocketAddress: "
                        + packet.getSocketAddress().toString());
                  processLocalResponse(packet);
               } catch (SocketException e) {
                  e.printStackTrace();
               } catch (IOException e) {
                  e.printStackTrace();
               }
            }

            if (mListenLocalResponseSocket != null) {
               mListenLocalResponseSocket.close();
               mListenLocalResponseSocket = null;
            }
         }
      });
      mListenLocalResponseTask.start();
   }

   public void stopListeningLocalResponse() {
      mStopListeningLocalResponse = true;
   }

   private void processLocalResponse(DatagramPacket packet) {
      if (packet.getData().length < 3) {
         return;
      }
      byte[] data = packet.getData();
      byte[] cmd = new byte[3];
      System.arraycopy(data, 0, cmd, 0, cmd.length);
      String cmdString = new String(cmd);
      byte[] ip = new byte[4];
      byte[] mac = new byte[11];
      if (IotCommand.STS.equalsIgnoreCase(cmdString)) {
         int number = data[3] - 48;
         System.arraycopy(data, 4, ip, 0, ip.length);
         InetAddress inetAddress;
         try {
            inetAddress = InetAddress.getByAddress(ip);
            IotDevice dev = new IotDevice();
            dev.setIp(inetAddress.getHostAddress());
            System.arraycopy(data, 8, mac, 0, mac.length);
            String macString = getMacString(mac);
            dev.setMac(macString);
            mDevices.put(dev.getIp(), dev);
            byte status = data[19];
            Utils.logi(TAG, "________________" + cmdString + number + "/"
                  + inetAddress.getHostAddress() + "/" + macString + "/"
                  + status);
            if (mHandler != null && mLocalResponseListener != null) {
               final IotCommandResponse response = new IotCommandResponse();
               response.setIp(inetAddress.getHostAddress());
               response.setMac(macString);
               response.setPort(packet.getPort());
               response.setCommand(cmdString);
               response.setResult(String.valueOf(status));
               mHandler.post(new Runnable() {
                  public void run() {
                     mLocalResponseListener.onReceiveResponse(IotEvent.SUCCESS,
                           response);
                  }
               });
            }
         } catch (UnknownHostException e) {
            e.printStackTrace();
         }
      } else {
         Utils.logi(TAG, "____________processLocalResponse: " + cmdString);
      }
   }

   public void requestSendingServerCommand(final String cmd) {
      new Thread(new Runnable() {
         public void run() {
            sendServerCommand(cmd);
         }
      }).start();
   }

   private void sendServerCommand(String cmd) {
      byte[] data = cmd.getBytes();
      try {
         InetAddress serverAddress = InetAddress.getByName(SERVER);
         DatagramSocket socket = new DatagramSocket();
         for (Entry<String, IotDevice> entry : mDevices.entrySet()) {
            int count = 3;
            while (count-- > 0) {
               data = cmd.concat(" ").concat(entry.getValue().getMac()).getBytes();
               DatagramPacket packet = new DatagramPacket(data, data.length,
                     serverAddress, SERVER_PORT);
               try {
                  socket.send(packet);
               } catch (IOException e) {
                  e.printStackTrace();
                  continue;
               }
               Utils.logi(TAG, "___________sendServerCommand: " + cmd + " -> "
                     + entry.getValue());
               byte[] recvData = new byte[32];
               DatagramPacket recvPacket = new DatagramPacket(recvData,
                     recvData.length);
               try {
                  socket.receive(recvPacket);
               } catch (SocketTimeoutException e) {
                  e.printStackTrace();
                  continue;
               } catch (IOException e) {
                  e.printStackTrace();
                  continue;
               }
               String result = new String(recvPacket.getData(), 0,
                     recvPacket.getLength());
               Utils.logi(TAG, "___________Received message: " + result);
               Utils.logi(TAG, "___________from: "
                     + recvPacket.getAddress().toString());
               if ("OK".equals(result)) {
                  prepareListeningServerResponse();
                  break;
               }
            }
         }
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (UnknownHostException e) {
         e.printStackTrace();
      }
   }

   private void requestListeningServerResponse() {
      if (mHandler != null) {
         mHandler.postDelayed(new Runnable() {
            public void run() {
               prepareListeningServerResponse();
            }
         }, 1000);
      }
   }

   private void prepareListeningServerResponse() {
      for (Entry<String, IotDevice> entry : mDevices.entrySet()) {
         startListeningServerResponse(entry.getValue().getMac());
      }
   }

   private void startListeningServerResponse(final String mac) {
      new Thread(new Runnable() {
         public void run() {
            listenServerResponse(mac);
         }
      }).start();
   }

   private void listenServerResponse(String mac) {
      try {
         InetAddress serverAddr = InetAddress.getByName(SERVER);
         DatagramSocket socket = new DatagramSocket();
         socket.setSoTimeout(5000);
         socket.setReuseAddress(true);
         String cmd = "CHK ".concat(mac);
         DatagramPacket packet = new DatagramPacket(cmd.getBytes(),
               cmd.getBytes().length);
         packet.setAddress(serverAddr);
         packet.setPort(SERVER_PORT);
         try {
            socket.setSoTimeout(5000);
            socket.send(packet);
            Utils.logi(TAG, "___________listenServerResponse: " + cmd + " to "
                  + mac);
         } catch (IOException e) {
            e.printStackTrace();
            return;
         }
         byte[] recvData = new byte[32];
         DatagramPacket recvPacket = new DatagramPacket(recvData,
               recvData.length);
         try {
            socket.setSoTimeout(10000);
            socket.receive(recvPacket);
         } catch (IOException e) {
            e.printStackTrace();
            return;
         }
         String result = new String(recvPacket.getData(), 0,
               recvPacket.getLength());
         Utils.logi(TAG, "___________Received message: " + result);
         Utils.logi(TAG, "___________from: "
               + recvPacket.getAddress().toString());
         if ("DON".equals(result)) {
            if (mHandler != null && mServerResponseListener != null) {
               IotCommandResponse response = new IotCommandResponse();
               response.setCommand(result);
               response.setResult("");
               mServerResponseListener.onReceiveResponse(IotEvent.SUCCESS,
                     response);
            }
         } else {
            requestListeningServerResponse();
         }
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (UnknownHostException e) {
         e.printStackTrace();
      }
   }

   private void sendUdpCommand(DatagramSocket socket, InetAddress addr,
         String cmd) {
      if (!TextUtils.isEmpty(cmd)) {
         byte[] data = cmd.getBytes();
         DatagramPacket packet = new DatagramPacket(data, data.length);
         packet.setAddress(addr);
         sendUdpPacket(socket, packet);
      }
   }

   private void sendUdpCommand(DatagramSocket socket, InetAddress addr,
         int port, String cmd) {
      if (!TextUtils.isEmpty(cmd)) {
         byte[] data = cmd.getBytes();
         DatagramPacket packet = new DatagramPacket(data, data.length);
         packet.setAddress(addr);
         packet.setPort(port);
         sendUdpPacket(socket, packet);
      }
   }

   private void sendUdpPacket(DatagramSocket socket, DatagramPacket packet) {
      try {
         if (!socket.isClosed()) {
            socket.send(packet);
         }
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static String getMacString(byte[] data) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < data.length; i++) {
         if (i % 2 == 0) {
            builder.append(data[i]);
         } else {
            builder.append(":");
         }
      }
      return builder.toString();
   }

}
