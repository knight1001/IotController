package com.oosic.iot.controller.library;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
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

   private static final String SERVER = "180.168.145.238";// "38.lzjgo.us";
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
      int ipInt = dhcpInfo.gateway | ~dhcpInfo.netmask;
      byte[] ipBytes = new byte[4];
      ipBytes[0] = (byte) (ipInt & 0xff);
      ipBytes[1] = (byte) (ipInt >> 8 & 0xff);
      ipBytes[2] = (byte) (ipInt >> 16 & 0xff);
      ipBytes[3] = (byte) (ipInt >> 24 & 0xff);
      IotEvent event = IotEvent.ERROR;
      try {
         InetAddress addr = InetAddress.getByAddress(ipBytes);
         Utils.logi(TAG, "sendBroadcast: " + addr.getHostAddress());
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

   private static String toHexString(byte[] buff, int offset, int length) {
      StringBuffer sb = new StringBuffer();
      for (int i = offset; i < length; i++) {
         sb.append(Integer.toHexString(buff[i] >> 4 & 0x0f)).append(
               Integer.toHexString(buff[i] & 0x0f));
      }
      return sb.toString();
   }

   private DatagramSocket mUdpSocket = null;

   private void sendLocalCommand(byte[] data) {
      try {
         if (mUdpSocket == null) {
            mUdpSocket = new DatagramSocket();
         }
         DatagramSocket socket = mUdpSocket;
         for (Entry<String, IotDevice> entry : mDevices.entrySet()) {
            IotDevice dev = entry.getValue();
            if (!dev.isSelected()) {
               continue;
            }

            try {
               DatagramPacket packet = new DatagramPacket(data, data.length);
               packet.setPort(5000);// UDP_COMMAND_PORT);
               packet.setAddress(InetAddress.getByName(dev.getIp()));
               socket.connect(new InetSocketAddress(InetAddress.getByName(dev
                     .getIp()), 5000));
               socket.setReuseAddress(true);
               socket.setSoTimeout(10000);
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

               byte[] recvBuff = new byte[1024];
               DatagramPacket recvPacket = new DatagramPacket(recvBuff,
                     recvBuff.length);
               socket.setSoTimeout(10000);
               socket.receive(recvPacket);
               Utils.logi(TAG, "_______________sendLocalCommand: RECV="
                     + toHexString(recvBuff, 0, recvPacket.getLength()));
            } catch (InterruptedIOException e) {
               e.printStackTrace();
            } catch (UnknownHostException e) {
               e.printStackTrace();
               // continue;
            } catch (IOException e) {
               e.printStackTrace();
               // continue;
               // } finally {
               // socket.close();
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
         final String cmd = new String(data, 0, 3);
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
                     + new String(buff) + " -> " + dev.getMac());
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
               final String devMac = dev.getMac();
               final String result = new String(recvPacket.getData(), 0,
                     recvPacket.getLength());
               Utils.logi(TAG, "___________Received: " + result + " from "
                     + recvPacket.getAddress().toString());

               if (mHandler != null && mServerDataListener != null) {
                  mHandler.post(new Runnable() {
                     public void run() {
                        IotResult res = new IotResult(IotEvent.SUCCESS);
                        IotCommandResponse resp = new IotCommandResponse();
                        resp.setCommand(cmd);
                        resp.setMac(devMac);
                        resp.setResult(result);
                        mServerDataListener.onDataSent(res, resp);
                     }
                  });
               }
               if (!IotCommand.REG.equals(cmd) && !IotCommand.GET.equals(cmd)
                     && !IotCommand.CHK.equals(cmd)
                     && IotCommand.OKA.equals(result)) {
                  startListeningServerResponse(dev.getMac());
                  break;
               } else if (IotCommand.NON.equals(result)) {
                  break;
               } else if (IotCommand.FUL.equals(result)) {
                  break;
               } else {
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

   private void listenServerResponse(final String mac) {
      try {
         InetAddress serverAddr = InetAddress.getByName(SERVER);
         DatagramSocket socket = new DatagramSocket();
         socket.setSoTimeout(5000);
         socket.setReuseAddress(true);
         String cmd = IotCommand.CHK.concat(" ").concat(mac);
         DatagramPacket packet = new DatagramPacket(cmd.getBytes(),
               cmd.getBytes().length);
         packet.setAddress(serverAddr);
         packet.setPort(SERVER_PORT);
         try {
            socket.setSoTimeout(10000);
            socket.send(packet);
            Utils.logi(TAG, "___________listenServerResponse: " + cmd);
         } catch (IOException e) {
            e.printStackTrace();
            return;
         }
         byte[] recvData = new byte[1024];
         DatagramPacket recvPacket = new DatagramPacket(recvData,
               recvData.length);
         try {
            socket.setSoTimeout(10000);
            socket.receive(recvPacket);
         } catch (IOException e) {
            e.printStackTrace();
            return;
         } finally {
            socket.close();
         }
         final String result = new String(recvPacket.getData(), 0,
               recvPacket.getLength());
         Utils.logi(TAG, "___________Received: " + result + " from "
               + recvPacket.getAddress().toString());
         if (IotCommand.DON.equals(result)) {
            if (mHandler != null && mServerDataListener != null) {
               mHandler.post(new Runnable() {
                  public void run() {
                     IotResult res = new IotResult(IotEvent.SUCCESS);
                     IotCommandResponse resp = new IotCommandResponse();
                     resp.setCommand(IotCommand.CHK);
                     resp.setMac(mac);
                     resp.setResult(result);
                     mServerDataListener.onDataReceived(res, resp);
                  }
               });
            }
         } else if (IotCommand.PND.equals(result)) {
            if (!IotCommand.REG.equals(cmd) && !IotCommand.GET.equals(cmd)) {
               if (mHandler != null) {
                  mHandler.postDelayed(new Runnable() {
                     public void run() {
                        startListeningServerResponse(mac);
                     }
                  }, 10000);
               }
            }
         } else {

         }
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (UnknownHostException e) {
         e.printStackTrace();
      }
   }

   public DatagramSocket genUdpSocket(String ip, int port) {
      try {
         DatagramSocket socket = new DatagramSocket();
         socket.connect(new InetSocketAddress(InetAddress.getByName(ip), port));
         socket.setReuseAddress(true);
         socket.setSoTimeout(10000);
         return socket;
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (UnknownHostException e) {
         e.printStackTrace();
      }
      return null;
   }

   public DatagramPacket genUdpPacket(byte[] data, String ip, int port) {
      try {
         DatagramPacket packet = new DatagramPacket(data, data.length);
         packet.setAddress(InetAddress.getByName(ip));
         packet.setPort(port);
         return packet;
      } catch (UnknownHostException e) {
         e.printStackTrace();
      }
      return null;
   }

   public void requestSendingUdpPacket(final DatagramSocket socket,
         final DatagramPacket packet, final Handler handler,
         final IotDataListener listener) {
      new Thread(new Runnable() {
         public void run() {
            sendUdpPacket(socket, packet, handler, listener);
         }
      }).start();
   }

   public void sendUdpPacket(final DatagramSocket socket,
         DatagramPacket packet, final Handler handler,
         final IotDataListener listener) {
      try {
         if (!socket.isClosed() && !socket.isConnected()) {
            socket.send(packet);

            if (handler != null && listener != null) {
               handler.post(new Runnable() {
                  public void run() {
                     listener.onDataSent(new IotResult(IotEvent.SUCCESS),
                           socket);
                  }
               });
            }

            byte[] recvBuff = new byte[1024];
            final DatagramPacket recvPacket = new DatagramPacket(recvBuff,
                  recvBuff.length);
            socket.setSoTimeout(10000);
            socket.receive(recvPacket);

            if (handler != null && listener != null) {
               handler.post(new Runnable() {
                  public void run() {
                     listener.onDataSent(new IotResult(IotEvent.SUCCESS),
                           recvPacket);
                  }
               });
            }
         }
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (SocketException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void requestSendingTcpCommand(final byte[] data) {
      new Thread(new TcpRunnable(data)).start();
   }

   private class TcpRunnable implements Runnable {

      private byte[] data;

      public TcpRunnable(byte[] data) {
         this.data = data;
      }

      @Override
      public void run() {
         try {
            InetAddress addr = InetAddress.getByName("192.168.1.201");
            sendTcpCommand(this.data, addr);
         } catch (UnknownHostException e) {
            e.printStackTrace();
         }
      }

   }

   private Socket mTcpSocket;

   public void sendTcpCommand(byte[] data, InetAddress addr) {
      try {
         OutputStream os = null;
         ByteArrayInputStream bais = null;

         try {
            if (mTcpSocket == null || mTcpSocket.isClosed()
                  || !mTcpSocket.isConnected()) {
               mTcpSocket = new Socket();
               SocketAddress remoteAddr = new InetSocketAddress(addr, 5000);
               mTcpSocket.setSoTimeout(10000);
               mTcpSocket.connect(remoteAddr);
               mTcpSocket.setKeepAlive(true);
               // mTcpSocket.setSoLinger(true, 10000);
               new Thread(new RecvTcpResponseRunnable(mTcpSocket)).start();
            }
            Socket clientSocket = mTcpSocket;

            try {
               int length = 0, total = 0;
               byte[] buff = new byte[1500];
               os = clientSocket.getOutputStream();
               bais = new ByteArrayInputStream(data);

               while ((length = bais.read(buff)) > 0) {
                  os.write(buff, 0, length);
                  os.flush();
                  total += length;
               }

               Utils.logi(
                     TAG,
                     "___________Send message: "
                           + toHexString(data, 0, data.length));
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
               if (bais != null) {
                  bais.close();
               }
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private class RecvTcpResponseRunnable implements Runnable {

      private Socket clientSocket;

      public RecvTcpResponseRunnable(Socket clientSocket) {
         this.clientSocket = clientSocket;
      }

      @Override
      public void run() {
         InputStream is = null;
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         int length = 0;
         int offset = 0;
         byte[] buff = new byte[1500];

         while (!this.clientSocket.isClosed()
               && this.clientSocket.isConnected()
               && !this.clientSocket.isInputShutdown()) {
            try {
               is = this.clientSocket.getInputStream();
               Utils.logi(TAG, "READING......");
               length = is.read(buff);
               Utils.logi(TAG, "length=" + length);
               baos.write(buff, offset, length);
               offset += length;
               Utils.logi(
                     TAG,
                     "___________Received message: "
                           + toHexString(baos.toByteArray(), 0, length));
            } catch (IOException e) {
               e.printStackTrace();
               break;
            }
         }
         try {
            baos.flush();
            baos.close();
            this.clientSocket.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

}
