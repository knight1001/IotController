package com.oosic.iot.controller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.library.IotResult;
import com.oosic.iot.controller.library.PreferenceManager;
import com.oosic.iot.controller.R;
import com.oosic.iot.controller.utils.UIUtils;
import com.oosic.iot.controller.utils.Utils;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

public class IrControlActivity extends IotBaseActivity implements
      OnClickListener, OnLongClickListener {

   private static final String TAG = "IotControlActivity";

   private Button mSelectedButton;
   private Handler mHandler = new Handler();
   private IotManager mIotManager;
   private DeviceItem mDevice;
   private Map<String, Button> mButtonMap = new HashMap<String, Button>();
   private Map<String, Integer> mButtonAddrMap = new HashMap<String, Integer>();
   private Map<String, Integer> mButtonResouceMap = new HashMap<String, Integer>();
   private HashSet<String> mAvailableButtons = new HashSet<String>();
   private ProgressDialog mProgressDialog;
   private Map<String, Action> mStudyActionMap = new HashMap<String, Action>();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_ircontrol);

      init();
      mHandler.post(new Runnable() {
         public void run() {
            relayoutViews();
         }
      });
   }

   private void init() {
      mIotManager = getIotManager();
      initViews();
      initActions();
   }

   private void initActions() {
      mStudyActionMap.put(STUDY_SUCCESS, new StudySuccessAction());
      mStudyActionMap.put(STUDY_FAILURE, new StudyFailureAction());
      mStudyActionMap.put(STUDY_TIMEOUT, new StudyTimeoutAction());
      mStudyActionMap.put(STUDY_SEND_SUCCESS, new StudySendSuccessAction());
      mStudyActionMap.put(STUDY_SEND_FAILURE, new StudySendFailureAction());
   }

   private void initViews() {
      DeviceItem device = new DeviceItem();
      device.config = new DeviceConfig(DEV_BW800IR, 200, "192.168.1.200", 5000,
            COMP_ALL, 1);
      device.listener = new ControllerDataListener(device);
      mDevice = device;

      PreferenceManager prefsManager = getPrefsManager();
      for (int i = 0; i < BUTTONS.size(); i++) {
         ButtonItem item = BUTTONS.get(i);
         int buttonAddr = i + 1;
         device = new DeviceItem();
         device.name = item.name;
         device.config = new DeviceConfig(mDevice.config.type,
               mDevice.config.addr, mDevice.config.ip, mDevice.config.port,
               mDevice.config.component, buttonAddr);
         device.listener = new ControllerDataListener(device);
         Button button = (Button) findViewById(item.id);
         button.setTag(device);
         button.setOnClickListener(this);
         button.setOnLongClickListener(this);
         mButtonMap.put(item.name, button);
         mButtonAddrMap.put(item.name, buttonAddr);
         mButtonResouceMap.put(item.name, item.id);

         int addr = prefsManager.getButtonAddr(item.name);
         if (addr > 0) {
            mAvailableButtons.add(item.name);
         }
      }

      Button button = mButtonMap.get(STUDY);
      button.setSelected(false);
      button.setTextColor(0xffffffff);
   }

   private void relayoutViews() {

   }

   @Override
   public void onClick(View v) {
      Button button = (Button) v;
      DeviceItem device = (DeviceItem) v.getTag();
      Utils.logi(TAG, "onClick: " + device.name);
      if (mButtonResouceMap.get(device.name) != R.id.btn_study) {
         if (!mButtonMap.get(STUDY).isSelected()) {
            button.setSelected(false);
            button.setTextColor(0xffffffff);
            if (mAvailableButtons.contains(device.name)) {
               sendCommand(device,
                     getCommandByDevice(device.config, CMD_IR_SEND),
                     HEX_RESULTS);
            }
         }
      }
   }

   @Override
   public boolean onLongClick(View v) {
      Button button = (Button) v;
      DeviceItem device = (DeviceItem) v.getTag();
      Utils.logi(TAG, "onLongClick: " + device.name);
      if (mButtonResouceMap.get(device.name) == R.id.btn_study) {
         if (mSelectedButton == null) {
            button.setSelected(!button.isSelected());
            button.setTextColor(button.isSelected() ? 0xffff0000 : 0xffffffff);
            showToast(getResources().getString(
                  button.isSelected() ? R.string.enter_study_mode
                        : R.string.exit_study_mode));
         }
      } else {
         if (mButtonMap.get(STUDY).isSelected()
               && (mSelectedButton == null || mSelectedButton == button)) {
            button.setSelected(!button.isSelected());
            button.setTextColor(button.isSelected() ? 0xffff0000 : 0xffffffff);
            if (button.isSelected()) {
               mSelectedButton = button;
               sendCommand(device,
                     getCommandByDevice(device.config, CMD_IR_STUDY),
                     HEX_RESULTS);
            } else {
               mSelectedButton = null;
            }
         }
      }
      return true;
   }

   private boolean exitStudyMode() {
      Button studyBtn = mButtonMap.get(STUDY);
      if (studyBtn.isSelected()) {
         if (mSelectedButton != null) {
            mSelectedButton.setSelected(false);
            mSelectedButton.setTextColor(0xffffffff);
         }
         studyBtn.setSelected(false);
         studyBtn.setTextColor(0xffffffff);
         showToast(getResources().getString(R.string.exit_study_mode));
         return true;
      }
      return false;
   }

   @Override
   public void onResume() {
      super.onResume();
   }

   @Override
   public void onBackPressed() {
      if (!exitStudyMode()) {
         super.onBackPressed();
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

      cleanup();
   }

   private void showToast(String msg) {
      UIUtils.showToast(this, msg);
   }

   private void showMessageDialog(String msg) {
      UIUtils.getAlertDialogBuilder(this).setMessage(msg)
            .setPositiveButton(R.string.ok, null).show();
   }

   private void showProgressDialog() {
      if (mProgressDialog == null) {
         mProgressDialog = new ProgressDialog(this);
         mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         mProgressDialog.setMessage(getString(R.string.studying));
         mProgressDialog.setMax(100);
         mProgressDialog.setCancelable(false);
      }
      mProgressDialog.show();
   }

   private void hideProgressDialog() {
      if (mProgressDialog != null) {
         mProgressDialog.dismiss();
      }
   }

   private void sendCommand(DeviceItem device, byte[] data,
         HashSet<String> hexResults) {
      device.socket = mDevice.socket;
      mIotManager.requestSendingUdpData(data, device.config.ip,
            device.config.port, device.socket, mHandler, device.listener,
            hexResults);
   }

   private void cleanup() {
      if (mDevice.socket != null && !mDevice.socket.isClosed()) {
         mDevice.socket.close();
      }
      mDevice.socket = null;
   }

   private void analyzeResult(ControllerDataListener listener,
         DatagramPacket packet) {
      byte[] data = packet.getData();
      String resultStr = IotManager.toHexString(data, 0, packet.getLength());
      Utils.log(TAG, "analyzeResult: " + resultStr);
      DeviceItem device = listener.device;
      switch (device.config.type) {
      case DEV_BW800IR:
         if (HEX_RESULTS.contains(resultStr)) {
            mStudyActionMap.get(resultStr).action();
         } else if (STUDY_START.equals(resultStr)) {
            showProgressDialog();
         }
         break;
      }
   }

   class Action {
      void action() {
         if (mSelectedButton != null) {
            mSelectedButton.setSelected(false);
            mSelectedButton.setTextColor(0xffffffff);
            mSelectedButton = null;
         }
      }
   }

   class StudySuccessAction extends Action {
      public void action() {
         showToast(getResources().getString(R.string.study_success));
         hideProgressDialog();
         if (mSelectedButton != null) {
            DeviceItem device = (DeviceItem) mSelectedButton.getTag();
            getPrefsManager().setButtonAddr(device.name,
                  mButtonAddrMap.get(device.name));
            mAvailableButtons.add(device.name);
         }
         super.action();
      }
   }

   class StudyFailureAction extends Action {
      public void action() {
         showToast(getResources().getString(R.string.study_failure));
         hideProgressDialog();
         super.action();
      }
   }

   class StudyTimeoutAction extends Action {
      public void action() {
         showToast(getResources().getString(R.string.study_timeout));
         hideProgressDialog();
         super.action();
      }
   }

   class StudySendSuccessAction extends Action {
      public void action() {
         showToast(getResources().getString(R.string.study_send_success));
      }
   }

   class StudySendFailureAction extends Action {
      public void action() {
         showToast(getResources().getString(R.string.study_send_failure));
      }
   }

   class ControllerDataListener extends DeviceDataListener {

      public ControllerDataListener(DeviceItem device) {
         super(device);
      }

      @Override
      public void onDataSent(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramSocket) {
            mDevice.socket = (DatagramSocket) obj;
         }
      }

      @Override
      public void onDataReceived(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramPacket) {
            analyzeResult(this, (DatagramPacket) obj);
         }
      }

   }

   private static final HashSet<String> HEX_RESULTS = new HashSet<String>();
   private static final String STUDY_START = "50fb05010004";
   private static final String STUDY_SUCCESS = "50fc01000001";
   private static final String STUDY_FAILURE = "50fc02000002";
   private static final String STUDY_TIMEOUT = "50fc05000005";
   private static final String STUDY_SEND_START = "50fb01010000";
   private static final String STUDY_SEND_SUCCESS = "50fc03000003";
   private static final String STUDY_SEND_FAILURE = "50fc04000004";

   static {
      HEX_RESULTS.add(STUDY_SUCCESS);
      HEX_RESULTS.add(STUDY_FAILURE);
      HEX_RESULTS.add(STUDY_TIMEOUT);
      HEX_RESULTS.add(STUDY_SEND_SUCCESS);
      HEX_RESULTS.add(STUDY_SEND_FAILURE);
   }

   private static final String POWER = "power";
   private static final String SIGNAL = "signal";
   private static final String SUBTITLE = "subtitle";
   private static final String PLAY = "play";
   private static final String SOUND_TRACK = "sound_track";
   private static final String PREV = "prev";
   private static final String STOP = "stop";
   private static final String NEXT = "next";
   private static final String MENU = "menu";
   private static final String BACK = "back";
   private static final String UP = "up";
   private static final String LEFT = "left";
   private static final String ENTER = "enter";
   private static final String RIGHT = "right";
   private static final String DOWN = "down";
   private static final String MUTE = "mute";
   private static final String FAVOR = "favor";

   private static final String VOLUP = "volume_up";
   private static final String NEXT_CHANNEL = "next_channel";
   private static final String VOLDN = "volume_down";
   private static final String PREV_CHANNEL = "prev_channel";
   private static final String NUM_1 = "one";
   private static final String NUM_2 = "two";
   private static final String NUM_3 = "three";
   private static final String NUM_4 = "four";
   private static final String NUM_5 = "five";
   private static final String NUM_6 = "six";
   private static final String NUM_7 = "seven";
   private static final String NUM_8 = "eight";
   private static final String NUM_9 = "nine";
   private static final String SLEEP = "sleep";
   private static final String NUM_0 = "zero";
   private static final String STUDY = "study";

   private static final List<ButtonItem> BUTTONS = new ArrayList<ButtonItem>();

   static {
      BUTTONS.add(new ButtonItem(POWER, R.id.btn_power));
      BUTTONS.add(new ButtonItem(SIGNAL, R.id.btn_signal));
      BUTTONS.add(new ButtonItem(SUBTITLE, R.id.btn_subtitle));
      BUTTONS.add(new ButtonItem(PLAY, R.id.btn_play));
      BUTTONS.add(new ButtonItem(SOUND_TRACK, R.id.btn_sound_track));
      BUTTONS.add(new ButtonItem(PREV, R.id.btn_prev));
      BUTTONS.add(new ButtonItem(STOP, R.id.btn_stop));
      BUTTONS.add(new ButtonItem(NEXT, R.id.btn_next));
      BUTTONS.add(new ButtonItem(MENU, R.id.btn_menu));
      BUTTONS.add(new ButtonItem(BACK, R.id.btn_back));
      BUTTONS.add(new ButtonItem(UP, R.id.btn_up));
      BUTTONS.add(new ButtonItem(LEFT, R.id.btn_left));
      BUTTONS.add(new ButtonItem(ENTER, R.id.btn_enter));
      BUTTONS.add(new ButtonItem(RIGHT, R.id.btn_right));
      BUTTONS.add(new ButtonItem(DOWN, R.id.btn_down));
      BUTTONS.add(new ButtonItem(MUTE, R.id.btn_mute));
      BUTTONS.add(new ButtonItem(FAVOR, R.id.btn_favor));
      BUTTONS.add(new ButtonItem(VOLUP, R.id.btn_vol_up));
      BUTTONS.add(new ButtonItem(NEXT_CHANNEL, R.id.btn_next_channel));
      BUTTONS.add(new ButtonItem(VOLDN, R.id.btn_vol_down));
      BUTTONS.add(new ButtonItem(PREV_CHANNEL, R.id.btn_prev_channel));
      BUTTONS.add(new ButtonItem(NUM_1, R.id.btn_digital_1));
      BUTTONS.add(new ButtonItem(NUM_2, R.id.btn_digital_2));
      BUTTONS.add(new ButtonItem(NUM_3, R.id.btn_digital_3));
      BUTTONS.add(new ButtonItem(NUM_4, R.id.btn_digital_4));
      BUTTONS.add(new ButtonItem(NUM_5, R.id.btn_digital_5));
      BUTTONS.add(new ButtonItem(NUM_6, R.id.btn_digital_6));
      BUTTONS.add(new ButtonItem(NUM_7, R.id.btn_digital_7));
      BUTTONS.add(new ButtonItem(NUM_8, R.id.btn_digital_8));
      BUTTONS.add(new ButtonItem(NUM_9, R.id.btn_digital_9));
      BUTTONS.add(new ButtonItem(SLEEP, R.id.btn_sleep));
      BUTTONS.add(new ButtonItem(NUM_0, R.id.btn_digital_0));
      BUTTONS.add(new ButtonItem(STUDY, R.id.btn_study));
   }

   static class ButtonItem {
      String name;
      int id;

      public ButtonItem(String name, int id) {
         this.name = name;
         this.id = id;
      }
   }

}
