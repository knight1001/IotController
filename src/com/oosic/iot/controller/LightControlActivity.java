package com.oosic.iot.controller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oosic.iot.controller.library.IotAdapter;
import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.library.IotResult;
import com.oosic.iot.controller.utils.UIUtils;
import com.oosic.iot.controller.views.LightView;
import com.oosic.iot.controller.views.ThemeView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

public class LightControlActivity extends IotBaseActivity {

   private static final String TAG = "IotControlActivity";

   private static final String DOOR_LIGHT = "door_light";
   private static final String PORCH_LIGHT = "porch_light";
   private static final String DINING_ROOM_LIGHT = "dining_light";
   private static final String LIVING_ROOM_MAIN_LIGHT = "living_main_light";
   private static final String LIVING_ROOM_SURROUND_LIGHT = "living_surround_light";
   private static final String BATHROOM_LIGHT = "bathing_light";
   private static final String READING_ROOM_LIGHT = "reading_light";
   private static final String MASTER_BEDROOM_LIGHT = "master_bedroom_light";
   private static final String SENCONDARY_BEDROOM_LIGHT = "secondary_bedroom_light";

   private List<ThemeItem> mThemeList = new ArrayList<ThemeItem>();
   private List<LightItem> mLightList = new ArrayList<LightItem>();
   private Map<String, LightItem> mLightMap = new HashMap<String, LightItem>();
   private Map<String, DeviceConfig> mLightConfigMap = new HashMap<String, DeviceConfig>();

   private GridView mThemeGridView;
   private ListView mLightListView;
   private ImageView mLightOnView, mLightOffView;
   private ThemeAdapter mThemeAdapter;
   private LightAdapter mLightAdapter;
   private Handler mHandler = new Handler();
   private IotManager mIotManager;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_lightcontrol);

      findViews();
      init();
   }

   private void findViews() {
      mThemeGridView = (GridView) findViewById(R.id.theme_grid);
      mLightListView = (ListView) findViewById(R.id.light_list);
      mLightOnView = (ImageView) findViewById(R.id.btn_light_on);
      mLightOffView = (ImageView) findViewById(R.id.btn_light_off);
   }

   private void init() {
      mIotManager = getIotManager();
      initDevices();
      initViews();
   }

   private void initViews() {
      mThemeAdapter = new ThemeAdapter(this, mThemeList);
      mThemeGridView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
            List<String> names = (List<String>) view.getTag();
            for (int i = 0; i < names.size(); i++) {
               LightItem light = mLightMap.get(names.get(i));
               if (light.socket == null && light.socket.isClosed()
                     && !light.socket.isConnected()) {
                  light.socket = mIotManager.genUdpSocket(light.config.ip,
                        light.config.port);
               }
               if (light.socket != null) {
                  byte[] data = null;
                  DatagramPacket packet = mIotManager.genUdpPacket(data,
                        light.config.ip, light.config.port);
                  getIotManager().requestSendingUdpPacket(light.socket, packet,
                        mHandler, light.listener);
               }
            }
         }
      });
      mThemeGridView.setAdapter(mThemeAdapter);
      mLightAdapter = new LightAdapter(this, mLightList);
      mLightListView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
            LightItem light = (LightItem) view.getTag();
            if (light.socket == null && light.socket.isClosed()
                  && !light.socket.isConnected()) {
               light.socket = mIotManager.genUdpSocket(light.config.ip,
                     light.config.port);
            }
            if (light.socket != null) {
               byte[] data = getCommandByDevice(light.config,
                     light.status == LightItem.ON ? CMD_TURN_OFF : CMD_TURN_ON,
                     0);
               DatagramPacket packet = mIotManager.genUdpPacket(data,
                     light.config.ip, light.config.port);
               getIotManager().requestSendingUdpPacket(light.socket, packet,
                     mHandler, light.listener);
            }
         }
      });
      mLightListView.setAdapter(mLightAdapter);
      mLightOnView.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            mLightOnView.setSelected(true);
            mLightOffView.setSelected(false);
            turnOnAllLights();
         }
      });
      mLightOffView.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            mLightOnView.setSelected(false);
            mLightOffView.setSelected(true);
            turnOffAllLights();
         }
      });
   }

   private void turnOnAllLights() {

   }

   private void turnOffAllLights() {

   }

   private void sendCommand2AllLights(byte[] data) {
      for (LightItem light : mLightList) {
         if (light.socket == null && light.socket.isClosed()
               && !light.socket.isConnected()) {
            light.socket = mIotManager.genUdpSocket(light.config.ip,
                  light.config.port);
         }
         if (light.socket != null) {
            DatagramPacket packet = mIotManager.genUdpPacket(data,
                  light.config.ip, light.config.port);
            getIotManager().requestSendingUdpPacket(light.socket, packet,
                  mHandler, light.listener);
         }
      }
   }

   private void analyzeResult(DatagramPacket packet) {
      byte[] data = packet.getData();
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();

   }

   private void showToast(String msg) {
      UIUtils.showToast(this, msg);
   }

   private void showMessageDialog(String msg) {
      UIUtils.getAlertDialogBuilder(this).setMessage(msg)
            .setPositiveButton(R.string.ok, null).show();
   }

   private void initDevices() {
      mLightConfigMap.put(DOOR_LIGHT, new DeviceConfig(DEV_BW800R3, 201,
            "192.168.10.201", 5000));
      mLightConfigMap.put(PORCH_LIGHT, new DeviceConfig(DEV_BW800R3, 201,
            "192.168.10.201", 5000));
      mLightConfigMap.put(DINING_ROOM_LIGHT, new DeviceConfig(DEV_BW800R3, 201,
            "192.168.10.201", 5000));
      mLightConfigMap.put(LIVING_ROOM_MAIN_LIGHT, new DeviceConfig(DEV_BW800R3,
            211, "192.168.10.211", 5000));
      mLightConfigMap.put(LIVING_ROOM_SURROUND_LIGHT, new DeviceConfig(
            DEV_BW800R3, 212, "192.168.10.212", 5000));
      mLightConfigMap.put(BATHROOM_LIGHT, new DeviceConfig(DEV_BW800R3, 201,
            "192.168.10.201", 5000));
      mLightConfigMap.put(READING_ROOM_LIGHT, new DeviceConfig(DEV_BW800R3,
            201, "192.168.10.201", 5000));
      mLightConfigMap.put(MASTER_BEDROOM_LIGHT, new DeviceConfig(DEV_BW800R3,
            211, "192.168.10.211", 5000));
      mLightConfigMap.put(SENCONDARY_BEDROOM_LIGHT, new DeviceConfig(
            DEV_BW800R3, 212, "192.168.10.212", 5000));

      // theme control
      ThemeItem theme = new ThemeItem();
      theme.ico = R.drawable.ico_porch;
      theme.title = R.string.porch;
      theme.lights.add(PORCH_LIGHT);
      mThemeList.add(theme);

      theme = new ThemeItem();
      theme.ico = R.drawable.ico_diningroom;
      theme.title = R.string.dining_room;
      theme.lights.add(DINING_ROOM_LIGHT);
      mThemeList.add(theme);

      theme = new ThemeItem();
      theme.ico = R.drawable.ico_theater;
      theme.title = R.string.theater;
      theme.lights.add(LIVING_ROOM_SURROUND_LIGHT);
      mThemeList.add(theme);

      theme = new ThemeItem();
      theme.ico = R.drawable.ico_bathroom;
      theme.title = R.string.bathroom;
      theme.lights.add(BATHROOM_LIGHT);
      mThemeList.add(theme);

      theme = new ThemeItem();
      theme.ico = R.drawable.ico_bedroom;
      theme.title = R.string.bedroom;
      theme.lights.add(MASTER_BEDROOM_LIGHT);
      mThemeList.add(theme);

      theme = new ThemeItem();
      theme.ico = R.drawable.ico_custom;
      theme.title = R.string.custom;
      mThemeList.add(theme);

      // light control
      LightItem light = new LightItem();
      light.name = DOOR_LIGHT;
      light.title = R.string.door_light;
      mLightList.add(light);

      light = new LightItem();
      light.name = PORCH_LIGHT;
      light.title = R.string.porch_light;
      mLightList.add(light);

      light = new LightItem();
      light.name = LIVING_ROOM_MAIN_LIGHT;
      light.title = R.string.living_room_main_light;
      mLightList.add(light);

      light = new LightItem();
      light.name = LIVING_ROOM_SURROUND_LIGHT;
      light.title = R.string.living_room_surround_light;
      mLightList.add(light);

      light = new LightItem();
      light.name = READING_ROOM_LIGHT;
      light.title = R.string.reading_room_light;
      mLightList.add(light);

      light = new LightItem();
      light.name = MASTER_BEDROOM_LIGHT;
      light.title = R.string.master_bedroom_light;
      mLightList.add(light);

      light = new LightItem();
      light.name = SENCONDARY_BEDROOM_LIGHT;
      light.title = R.string.secondary_bedroom_light;
      mLightList.add(light);

      for (LightItem item : mLightList) {
         light.config = mLightConfigMap.get(item.name);
         item.listener = new LightDataListener(item.name);
         mLightMap.put(item.name, item);
      }
   }

   class ThemeAdapter extends IotAdapter<ThemeItem> {

      public ThemeAdapter(Context context) {
         super(context);
      }

      public ThemeAdapter(Context context, List<ThemeItem> data) {
         super(context, data);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
            convertView = new ThemeView(this.context);
         }

         if (this.data.size() <= 0 || position < 0
               || position >= this.data.size()) {
            return convertView;
         }

         ThemeItem item = this.data.get(position);
         ThemeView themeView = (ThemeView) convertView;
         themeView.getIcoView().setImageResource(item.ico);
         themeView.getTitleView().setText(item.title);

         convertView.setTag(item.lights);
         return convertView;
      }

   }

   class LightAdapter extends IotAdapter<LightItem> {

      public LightAdapter(Context context) {
         super(context);
      }

      public LightAdapter(Context context, List<LightItem> data) {
         super(context, data);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
            convertView = new LightView(this.context);
         }

         if (this.data.size() <= 0 || position < 0
               || position >= this.data.size()) {
            return convertView;
         }

         LightItem item = this.data.get(position);
         LightView lightView = (LightView) convertView;
         lightView.getTitleView().setText(item.title);
         lightView.getStatusView().setText(
               (item.status == LightItem.ON) ? R.string.on : R.string.off);

         convertView.setTag(item);
         return convertView;
      }

   }

   class LightDataListener extends DeviceDataListener {

      public LightDataListener(String name) {
         super(name);
      }

      @Override
      public void onDataSent(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramSocket) {
            synchronized (mLightMap) {
               LightItem item = mLightMap.get(this.name);
               if (item != null) {
                  item.socket = (DatagramSocket) obj;
               }
            }
         }
      }

      @Override
      public void onDataReceived(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramPacket) {
            analyzeResult((DatagramPacket) obj);
         }
      }

   }

   class ThemeItem {
      int ico = 0;
      int title = 0;
      List<String> lights = new ArrayList<String>();
   }

   class LightItem extends DeviceItem {
      static final boolean ON = true;
      static final boolean OFF = false;

      int title = 0;
      boolean status = OFF;
   }

}
