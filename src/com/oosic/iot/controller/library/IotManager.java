package com.oosic.iot.controller.library;

import java.io.IOException;
import java.io.InterruptedIOException;
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

import android.R.id;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.ContactsContract.Contacts.Data;
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
   private IotDataListener mLocalDataListener;
   private IotDataListener mServerDataListener;

   public IotManager(Context context) {
      mContext = context;
      mDevices = new HashMap<String, IotDevice>();
   }

   public void setHandler(Handler handler) {
      mHandler = handler;
   }

   public void setLocalDataListener(IotDataListener listener) {
      mLocalDataListener = listener;
   }

   public void setServerDataListener(IotDataListener listener) {
      mServerDataListener = listener;
   }

   public IotConfig getConfig() {
      if (mConfig == null) {
         mConfig = new IotConfig(mContext);
      }
      return mConfig;
   }

   public boolean hasDevice(String ip) {
      return mDevices.containsKey(ip);
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

   public void requestSendingBroadcast(final byte[] data) {
      new Thread(new Runnable() {
         public void run() {
            sendBroadcast(data);
         }
      }).start();
   }

   private void sendBroadcast(byte[] data) {
      WifiManager wifiManager = (WifiManager) mContext
            .getSystemService(Context.WIFI_SERVICE);
      DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
      byte[] ip = new byte[4];
      ip[0] = (byte) (dhcpInfo.gateway & 0xff);
      ip[1] = (byte) (dhcpInfo.gateway >> 8 & 0xff);
      ip[2] = (byte) (dhcpInfo.gateway >> 16 & 0xff);
      ip[3] = (byte) 255;
      for (int i = 0; i < ip.length; i++) {
         Utils.logi(TAG, "____________gateway: [" + i + "]=" + ip[i]);
      }
      IotEvent event = IotEvent.ERROR;
      try {
         InetAddress addr = InetAddress.getByAddress(ip);
         DatagramSocket socket = new MulticastSocket();
         socket.setBroadcast(true);
         DatagramPacket packet = new DatagramPacket(data, data.length);
         packet.setAddress(addr);
         packet.setPort(UDP_COMMAND_PORT);
         socket.setSoTimeout(10000);
         socket.send(packet);

         Utils.logi(TAG, "___________sendBroadcast: " + new String(data));
         if (mHandler != null && mLocalDataListener != null) {
            final DatagramPacket resultData = packet;
            mHandler.post(new Runnable() {
               public void run() {
                  mLocalDataListener.onDataSent(
                        new IotResult(IotEvent.SUCCESS), resultData);
               }
            });
         }
         return;
      } catch (InterruptedIOException e) {
         event = IotEvent.TIMEOUT;
         e.printStackTrace();
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      final IotEvent result = event;
      if (mHandler != null && mLocalDataListener != null) {
         mHandler.post(new Runnable() {
            public void run() {
               mLocalDataListener.onDataSent(new IotResult(result), null);
            }
         });
      }
   }

   public void requestSendingLocalCommand(final byte[] data) {
      new Thread(new Runnable() {
         public void run() {
            sendLocalCommand(data);
         }
      }).start();
   }

   private void sendLocalCommand(byte[] data) {
      try {
         DatagramSocket socket = new DatagramSocket();
         for (Entry<String, IotDevice> entry : mDevices.entrySet()) {
            IotDevice dev = entry.getValue();
            if (!dev.isSelected()) {
               continue;
            }

            try {
               DatagramPacket packet = new DatagramPacket(data, data.length);
               packet.setPort(UDP_COMMAND_PORT);
               packet.setAddress(InetAddress.getByName(dev.getIp()));
               socket.send(packet);

               Utils.logi(TAG, "___________sendLocalCommand: "
                     + new String(data) + " -> " + dev.getIp());
               if (mHandler != null && mLocalDataListener != null) {
                  final DatagramPacket resultData = packet;
                  mHandler.post(new Runnable() {
                     public void run() {
                        mLocalDataListener.onDataSent(new IotResult(
                              IotEvent.SUCCESS), resultData);
                     }
                  });
               }
            } catch (InterruptedIOException e) {
               e.printStackTrace();
            } catch (UnknownHostException e) {
               e.printStackTrace();
               continue;
            } catch (IOException e) {
               e.printStackTrace();
               continue;
            }
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
            try {
               DatagramSocket socket = new DatagramSocket(UDP_RESPONSE_PORT);
               Utils.logi(TAG, "listeningLocalResponse...");
               while (!mStopListeningLocalResponse) {
                  byte[] buff = new byte[32];
                  DatagramPacket packet = new DatagramPacket(buff, buff.length);
                  try {
                     socket.setReuseAddress(true);
                     socket.receive(packet);

                     Utils.logi(TAG, "___________Received from: "
                           + packet.getAddress().toString());
                     final DatagramPacket data = packet;
                     if (mHandler != null) {
                        mHandler.post(new Runnable() {
                           public void run() {
                              mLocalDataListener.onDataReceived(new IotResult(
                                    IotEvent.SUCCESS), data);
                           }
                        });
                     }
                  } catch (InterruptedIOException e) {
                     e.printStackTrace();
                     continue;
                  } catch (SocketException e) {
                     e.printStackTrace();
                     break;
                  } catch (IOException e) {
                     e.printStackTrace();
                     break;
                  }
               }
               socket.close();
            } catch (SocketException e) {
               e.printStackTrace();
            }
         }
      });
      mListenLocalResponseTask.start();
   }

   public void stopListeningLocalResponse() {
      Utils.logi(TAG, "stopListeningLocalResponse()");
      mStopListeningLocalResponse = true;
      mListenLocalResponseTask = null;
   }

   public void requestSendingServerCommand(final byte[] data) {
      new Thread(new Runnable() {
         public void run() {
            sendServerCommand(data);
         }
      }).start();
   }

   private void sendServerCommand(byte[] data) {
      try {
         InetAddress serverAddress = InetAddress.getByName(SERVER);
         DatagramSocket socket = new DatagramSocket();
         for (Entry<String, IotDevice> entry : mDevices.entrySet()) {
            IotDevice dev = entry.getValue();
            if (!dev.isSelected()) {
               continue;
            }

            int count = 3;
            while (count-- > 0) {
               byte[] mac = dev.getMac().getBytes();
               byte[] buff = new byte[data.length + 1 + mac.length];
               System.arraycopy(data, 0, buff, 0, data.length);
               buff[data.length] = ' ';
               System.arraycopy(mac, 0, buff, data.length + 1, mac.length);
               DatagramPacket packet = new DatagramPacket(buff, buff.length,
                     serverAddress, SERVER_PORT);
               try {
                  socket.send(packet);
               } catch (IOException e) {
                  e.printStackTrace();
                  continue;
               }
               Utils.logi(TAG, "___________sendServerCommand: "
                     + new String(data) + " -> " + dev.getMac());
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
               Utils.logi(TAG, "___________Received: " + result + " from "
                     + recvPacket.getAddress().toString());
               if ("OKA".equals(result)) {
                  startListeningServerResponse(dev.getMac());
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
         String cmd = "CHK".concat(" ").concat(mac);
         DatagramPacket packet = new DatagramPacket(cmd.getBytes(),
               cmd.getBytes().length);
         packet.setAddress(serverAddr);
         packet.setPort(SERVER_PORT);
         try {
            socket.setSoTimeout(5000);
            socket.send(packet);
            Utils.logi(TAG, "___________listenServerResponse: " + cmd);
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
         Utils.logi(TAG, "___________Received: " + result + " from "
               + recvPacket.getAddress().toString());
         if ("DON".equals(result)) {
            if (mHandler != null && mServerDataListener != null) {
               IotCommandResponse response = new IotCommandResponse();
               response.setCommand(result);
               response.setResult("");
               // mServerResponseListener.onReceiveResponse(IotEvent.SUCCESS,
               // response);
            }
         } else {
            startListeningServerResponse(mac);
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

}
