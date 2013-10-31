package com.oosic.iot.controller;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.oosic.iot.controller.library.IotAdapter;
import com.oosic.iot.controller.library.IotCommand;
import com.oosic.iot.controller.library.IotCommandResponse;
import com.oosic.iot.controller.library.IotCommandType;
import com.oosic.iot.controller.library.IotDataListener;
import com.oosic.iot.controller.library.IotDevice;
import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.library.IotResult;
import com.oosic.iot.controller.library.PreferenceManager;
import com.oosic.iot.controller.utils.UIUtils;
import com.oosic.iot.controller.utils.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

public class IotControlActivity extends IotBaseActivity {

   private static final String TAG = "IotControlActivity";

   private ImageView mConfigBtn, mDeviceListBtn, mSearchBtn;
   private GridView mCommandGridView;
   private CommandAdapter mCommandAdapter;
   private DeviceAdapter mDeviceAdapter;
   private IotManager mIotManager;
   private Handler mHandler = new Handler();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_control);

      findViews();
      init();
   }

   private void findViews() {
      mConfigBtn = (ImageView) findViewById(R.id.config_btn);
      mDeviceListBtn = (ImageView) findViewById(R.id.device_list_btn);
      mSearchBtn = (ImageView) findViewById(R.id.search_btn);
      mCommandGridView = (GridView) findViewById(R.id.command_grid);
   }

   private void init() {
      initIotManager();
      initViews();
   }

   private void initIotManager() {
      mIotManager = getIotManager();

      // IotDevice dev = new IotDevice();
      // dev.setIp("none");
      // dev.setMac("11:22:33:44:55:66");
      // mIotManager.addDevice(dev);

      mIotManager.setHandler(mHandler);
      mIotManager.setLocalDataListener(new IotDataListener() {
         public void onDataReceived(IotResult result, Object obj) {
            if (result.isSuccess()) {
               if (obj != null) {
                  if (obj instanceof DatagramPacket) {
                     processLocalResponse((DatagramPacket) obj);
                  }
               }
            }
         }

         @Override
         public void onDataSent(IotResult result, Object obj) {
            if (obj != null && obj instanceof DatagramPacket) {
               DatagramPacket packet = (DatagramPacket) obj;
               if (result.isSuccess()) {
                  showToast(getString(R.string.send_data_ok, packet
                        .getAddress().getHostAddress()));
               } else {
                  showToast(getString(R.string.send_data_error, packet
                        .getAddress().getHostAddress()));
               }
            }
         }
      });
      mIotManager.setServerDataListener(new IotDataListener() {
         public void onDataReceived(IotResult result, Object obj) {
            if (obj != null && obj instanceof IotCommandResponse) {
               IotCommandResponse response = (IotCommandResponse) obj;
               if (IotCommand.DON.equals(response.getResult())) {
                  showToast(getString(R.string.command_already_taken));
               } else {
                  showMessageDialog(getString(R.string.received_command_result,
                        response.getCommand() + " " + response.getMac(),
                        response.getResult()));
               }
            }
         }

         @Override
         public void onDataSent(IotResult result, Object obj) {
            if (obj != null && obj instanceof IotCommandResponse) {
               IotCommandResponse response = (IotCommandResponse) obj;
               if (!IotCommand.REG.equals(response.getCommand())
                     && !IotCommand.GET.equals(response.getCommand())
                     && !IotCommand.CHK.equals(response.getCommand())) {
                  if (IotCommand.OKA.equals(response.getResult())) {
                     showToast(getString(R.string.send_to_server_ok,
                           response.getCommand() + " " + response.getMac()));
                  } else if (IotCommand.NON.equals(response.getResult())) {
                     showToast(getString(R.string.device_not_register,
                           response.getMac()));
                  } else if (IotCommand.FUL.equals(response.getResult())) {
                     showToast(getString(R.string.server_full));
                  }
               } else {
                  showMessageDialog(getString(R.string.sent_command_result,
                        response.getCommand() + " " + response.getMac(),
                        response.getResult()));
               }
            }
         }
      });
   }

   private void initViews() {
      mDeviceListBtn.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            showDeviceListDialog();
         }
      });

      mSearchBtn.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            mIotManager.startListeningLocalResponse();
            mIotManager.requestSendingBroadcast(IotCommand.SCH.getBytes());
            showToast(getString(R.string.find_device));
         }
      });

      mConfigBtn.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            startActivity(new Intent(IotControlActivity.this,
                  IotConfigActivity.class));
         }
      });

      List<IotCommand> commands = new ArrayList<IotCommand>();
      PreferenceManager prefsManager = getPrefsManager();
      for (int i = 1; i <= 8; i++) {
         IotCommand cmd = null;
         if (prefsManager != null) {
            cmd = prefsManager.getButtonCommandByIndex(i);
         }
         if (cmd == null) {
            cmd = new IotCommand("CD" + i, IotCommandType.LOCAL_BROADCAST);
         }
         cmd.setIndex(i);
         commands.add(cmd);
      }
      mCommandAdapter = new CommandAdapter(this, commands);
      mCommandGridView.setAdapter(mCommandAdapter);
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      mIotManager.stopListeningLocalResponse();
   }

   private void showToast(String msg) {
      UIUtils.showToast(this, msg);
   }

   private void showMessageDialog(String msg) {
      UIUtils.getAlertDialogBuilder(this).setMessage(msg)
            .setPositiveButton(R.string.ok, null).show();
   }

   private void showCommandConfigDialog(IotCommand cmd) {
      IotCommand old = new IotCommand(String.copyValueOf(cmd.getCommand()
            .toCharArray()), cmd.getType());
      LayoutInflater inflater = LayoutInflater.from(this);
      ViewGroup view = (ViewGroup) inflater.inflate(R.layout.command_config,
            null);
      ViewGroup layout = (ViewGroup) view
            .findViewById(R.id.command_config_layout);
      if (layout != null) {
         layout.setTag(old);
      }
      EditText cmdView = (EditText) view.findViewById(R.id.command);
      if (cmdView != null) {
         cmdView.setTag(cmd);
         cmdView.setText(cmd.getCommand());
      }
      RadioGroup cmdTypeView = (RadioGroup) view
            .findViewById(R.id.command_type_selection);
      if (cmdTypeView != null) {
         cmdTypeView.setTag(cmd);
         if (IotCommandType.isLocalBroadcast(cmd.getType())) {
            cmdTypeView.check(R.id.local_broadcast);
         } else if (IotCommandType.isLocalPeerToPeer(cmd.getType())) {
            cmdTypeView.check(R.id.local_peer_to_peer);
         } else if (IotCommandType.isRemoteServer(cmd.getType())) {
            cmdTypeView.check(R.id.remote_server);
         }

         cmdTypeView
               .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                  public void onCheckedChanged(RadioGroup group, int checkedId) {
                     if (group.getTag() != null) {
                        IotCommand cmd = (IotCommand) group.getTag();
                        if (checkedId == R.id.local_broadcast) {
                           cmd.setType(IotCommandType.LOCAL_BROADCAST);
                        } else if (checkedId == R.id.local_peer_to_peer) {
                           cmd.setType(IotCommandType.LOCAL_PEER_TO_PEER);
                        } else if (checkedId == R.id.remote_server) {
                           cmd.setType(IotCommandType.REMOTE_SERVER);
                        }
                     }
                  }
               });
      }

      AlertDialog.Builder builder = UIUtils.getAlertDialogBuilder(this);
      builder
            .setIcon(R.drawable.icon)
            .setTitle(R.string.command_config)
            .setView(view)
            .setPositiveButton(R.string.ok,
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                        Dialog dlg = (Dialog) dialog;
                        EditText cmdView = (EditText) dlg
                              .findViewById(R.id.command);
                        if (cmdView != null) {
                           if (cmdView.getTag() != null) {
                              IotCommand cmd = (IotCommand) cmdView.getTag();
                              cmd.setCommand(cmdView.getText().toString());

                              PreferenceManager prefsManager = getPrefsManager();
                              if (prefsManager != null) {
                                 prefsManager.setButtonCommandByIndex(
                                       cmd.getIndex(), cmd);
                              }
                           }
                        }

                        mCommandAdapter.notifyDataSetChanged();
                     }
                  })
            .setNegativeButton(R.string.cancel,
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                        Dialog dlg = (Dialog) dialog;
                        ViewGroup layout = (ViewGroup) dlg
                              .findViewById(R.id.command_config_layout);
                        if (layout != null) {
                           IotCommand old = (IotCommand) layout.getTag();
                           if (old != null) {
                              EditText cmdView = (EditText) dlg
                                    .findViewById(R.id.command);
                              if (cmdView != null) {
                                 if (cmdView.getTag() != null) {
                                    IotCommand cmd = (IotCommand) cmdView
                                          .getTag();
                                    cmd.setCommand(old.getCommand());
                                    cmd.setType(old.getType());
                                 }
                              }
                           }
                        }
                     }
                  }).show();
   }

   private void showDeviceListDialog() {
      List<IotDevice> devices = mIotManager.getDeviceList();
      if (devices == null || devices.size() <= 0) {
         showToast(getString(R.string.no_device));
         return;
      }

      mDeviceAdapter = new DeviceAdapter(this, devices);
      LayoutInflater inflater = LayoutInflater.from(this);
      ListView listView = (ListView) inflater.inflate(R.layout.list, null);
      listView.setAdapter(mDeviceAdapter);

      AlertDialog.Builder builder = UIUtils.getAlertDialogBuilder(this);
      builder.setIcon(R.drawable.icon).setTitle(R.string.device_list)
            .setView(listView).show();
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
            mIotManager.addDevice(dev);
            byte status = data[19];
            String result = cmdString + number + "/"
                  + inetAddress.getHostAddress() + "/" + macString + "/"
                  + status;
            Utils.logi(TAG, "________________" + result);
            UIUtils.getAlertDialogBuilder(this).setMessage(result)
                  .setPositiveButton(R.string.ok, null).show();
         } catch (UnknownHostException e) {
            e.printStackTrace();
         }
      } else {
         Utils.logi(TAG, "____________processLocalResponse: " + cmdString);
         String ipAddr = packet.getAddress().getHostAddress();
         if (!mIotManager.hasDevice(ipAddr)) {
            IotDevice dev = new IotDevice();
            dev.setIp(ipAddr);
            mIotManager.addDevice(dev);
         }
      }
   }

   public static String getMacString(byte[] data) {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < data.length; i++) {
         if (i % 2 == 0) {
            builder.append(Integer.toHexString(data[i] >> 4 & 0x0f)).append(
                  Integer.toHexString(data[i] & 0x0f));
         } else {
            builder.append(":");
         }
      }
      return builder.toString();
   }

   private class CommandAdapter extends IotAdapter<IotCommand> {

      public CommandAdapter(Context context) {
         super(context);
      }

      public CommandAdapter(Context context, List<IotCommand> data) {
         super(context, data);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            convertView = inflater.inflate(R.layout.command_cell, null);
         }

         if (this.data.size() <= 0 || position < 0
               || position >= this.data.size()) {
            return convertView;
         }

         IotCommand cmd = this.data.get(position);
         if (cmd != null) {
            Button cmdBtn = (Button) convertView.findViewById(R.id.command_btn);
            if (cmdBtn != null) {
               cmdBtn.setTag(cmd);
               cmdBtn.setText(cmd.getCommand());
               cmdBtn.setOnClickListener(new View.OnClickListener() {
                  public void onClick(View v) {
                     mIotManager.startListeningLocalResponse();
                     if (v.getTag() != null) {
                        IotCommand cmd = (IotCommand) v.getTag();
                        if (IotCommandType.isLocalBroadcast(cmd.getType())) {
                           mIotManager.requestSendingBroadcast(cmd.getCommand()
                                 .getBytes());
                        } else if (IotCommandType.isLocalPeerToPeer(cmd
                              .getType())) {
                           mIotManager.requestSendingLocalCommand(cmd
                                 .getCommand().getBytes());
                        } else if (IotCommandType.isRemoteServer(cmd.getType())) {
                           mIotManager.requestSendingServerCommand(cmd
                                 .getCommand().getBytes());
                        }
                     }
                  }
               });
               cmdBtn.setOnLongClickListener(new View.OnLongClickListener() {
                  public boolean onLongClick(View v) {
                     if (v.getTag() != null) {
                        IotCommand cmd = (IotCommand) v.getTag();
                        showCommandConfigDialog(cmd);
                     }
                     return false;
                  }
               });
            }

            TextView cmdDescView = (TextView) convertView
                  .findViewById(R.id.command_desc);
            if (cmdDescView != null) {
               cmdDescView
                     .setText(IotCommandType.getDescription(cmd.getType()));
            }
            convertView.setTag(cmd);
         }

         return convertView;
      }

   }

   private class DeviceAdapter extends IotAdapter<IotDevice> {

      public DeviceAdapter(Context context) {
         super(context);
      }

      public DeviceAdapter(Context context, List<IotDevice> data) {
         super(context, data);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(this.context);
            convertView = inflater.inflate(R.layout.device_cell, null);
         }

         if (this.data.size() <= 0 || position < 0
               || position >= this.data.size()) {
            return convertView;
         }

         IotDevice dev = this.data.get(position);
         if (dev != null) {
            TextView ipView = (TextView) convertView
                  .findViewById(R.id.device_ip);
            if (ipView != null) {
               ipView.setTag(dev);
               ipView.setText(dev.getIp());
            }

            TextView macView = (TextView) convertView
                  .findViewById(R.id.device_mac);
            if (macView != null) {
               macView.setTag(dev);
               macView.setText(dev.getMac());
            }

            CheckBox selView = (CheckBox) convertView
                  .findViewById(R.id.device_selection);
            if (selView != null) {
               selView.setTag(dev);
               selView.setChecked(dev.isSelected());
               selView
                     .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton buttonView,
                              boolean isChecked) {
                           IotDevice dev = (IotDevice) buttonView.getTag();
                           if (dev != null) {
                              dev.setSelected(isChecked);
                           }
                        }
                     });
            }

            convertView.setTag(dev);
         }

         return convertView;
      }

   }

}
