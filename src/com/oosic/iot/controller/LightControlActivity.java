package com.oosic.iot.controller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.oosic.iot.controller.library.IotAdapter;
import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.library.IotResult;
import com.oosic.iot.controller.R;
import com.oosic.iot.controller.utils.UIUtils;
import com.oosic.iot.controller.utils.Utils;
import com.oosic.iot.controller.views.LightView;
import com.oosic.iot.controller.views.ThemeView;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class LightControlActivity extends IotBaseActivity {

   private static final String TAG = "LightControlActivity";

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
   private Map<String, DatagramSocket> mLightSocketMap = new HashMap<String, DatagramSocket>();
   private Map<String, List<LightItem>> mLightDeviceMap = new HashMap<String, List<LightItem>>();

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

      setContentView(R.layout.activity_light_control);

      findViews();
      init();
      mHandler.post(new Runnable() {
         public void run() {
            relayoutViews();
         }
      });
   }

   private void findViews() {
      mThemeGridView = (GridView) findViewById(R.id.theme_grid);
      mLightListView = (ListView) findViewById(R.id.light_list);
      mLightOnView = (ImageView) findViewById(R.id.btn_light_on);
      mLightOffView = (ImageView) findViewById(R.id.btn_light_off);
   }

   private void relayoutViews() {
      float scrWidth = (float) IotApp.getScreenWidth(this);
      float scrHeight = (float) IotApp.getScreenHeight(this);
      float wRatio = scrWidth / IotApp.DEFAULT_SCREEN_WIDTH;
      float hRatio = scrHeight / IotApp.DEFAULT_SCREEN_HEIGHT;
      float ratio = (Float.compare(wRatio, hRatio) < 0) ? wRatio : hRatio;

      Resources resources = getResources();
      ImageView logoView = (ImageView) findViewById(R.id.light_logo);
      if (logoView != null) {
         RelativeLayout.LayoutParams params = (LayoutParams) logoView
               .getLayoutParams();
         params.leftMargin = (int) (resources
               .getDimension(R.dimen.logo_margin_h) * wRatio);
         params.topMargin = (int) (resources
               .getDimension(R.dimen.logo_margin_v) * hRatio);
         logoView.setLayoutParams(params);
      }

      ViewGroup bodyLayout = (ViewGroup) findViewById(R.id.light_body_layout);
      if (bodyLayout != null) {
         RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bodyLayout
               .getLayoutParams();
         params.leftMargin = (int) (resources
               .getDimension(R.dimen.logo_margin_h) * wRatio);
         params.rightMargin = (int) (resources
               .getDimension(R.dimen.logo_margin_h) * wRatio);
         params.topMargin = (int) (resources
               .getDimension(R.dimen.light_body_margin_t) * hRatio);
         // params.height = (int) (resources.getDimension(R.dimen.body_height) *
         // wRatio);
         bodyLayout.setLayoutParams(params);
      }

      if (mLightOnView != null) {
         RelativeLayout.LayoutParams params = (LayoutParams) mLightOnView
               .getLayoutParams();
         params.width = (int) (resources
               .getDimension(R.dimen.light_action_ico_size) * ratio);
         params.height = (int) (resources
               .getDimension(R.dimen.light_action_ico_size) * ratio);
         mLightOnView.setLayoutParams(params);
      }

      if (mLightOffView != null) {
         RelativeLayout.LayoutParams params = (LayoutParams) mLightOffView
               .getLayoutParams();
         params.width = (int) (resources
               .getDimension(R.dimen.light_action_ico_size) * ratio);
         params.height = (int) (resources
               .getDimension(R.dimen.light_action_ico_size) * ratio);
         params.leftMargin = (int) (resources
               .getDimension(R.dimen.light_action_gap) * wRatio);
         mLightOffView.setLayoutParams(params);
      }

      ViewGroup lightListLayout = (ViewGroup) findViewById(R.id.light_list_layout);
      if (lightListLayout != null) {
         LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lightListLayout
               .getLayoutParams();
         params.leftMargin = (int) (resources.getDimension(R.dimen.middle_gap) * wRatio);
         lightListLayout.setLayoutParams(params);
      }
   }

   private void init() {
      mIotManager = getIotManager();
      configDevices();
      initViews();
   }

   private void initViews() {
      mThemeAdapter = new ThemeAdapter(this, mThemeList);
      mThemeGridView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
            if (position >= mThemeAdapter.getCount() - 1) {
               return;
            }

            Map<String, LightItem> map = new HashMap<String, LightItem>();
            for (Entry<String, LightItem> entry : mLightMap.entrySet()) {
               map.put(entry.getKey(), entry.getValue());
            }

            List<String> names = (List<String>) view.getTag();
            for (int i = 0; i < names.size(); i++) {
               LightItem light = map.remove(names.get(i));
               if (light != null) {
                  byte[] data = getCommandByDevice(light.config, CMD_RELAY_ON);
                  sendCommand(light, data);
               }
            }

            if (!map.isEmpty()) {
               for (Entry<String, LightItem> entry : map.entrySet()) {
                  byte[] data = getCommandByDevice(entry.getValue().config,
                        CMD_RELAY_OFF);
                  sendCommand(entry.getValue(), data);
               }
            }
         }
      });
      mThemeGridView.setOnItemSelectedListener(new OnItemSelectedListener() {
         public void onItemSelected(AdapterView<?> parent, View view,
               int position, long id) {
            if (view != null) {
               view.setSelected(true);
            }
         }

         @Override
         public void onNothingSelected(AdapterView<?> parent) {

         }
      });
      mThemeGridView.setAdapter(mThemeAdapter);
      mLightAdapter = new LightAdapter(this, mLightList);
      mLightListView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
            LightItem light = (LightItem) view.getTag();
            int command = light.status == LightItem.ON ? CMD_RELAY_OFF
                  : CMD_RELAY_ON;
            byte[] data = getCommandByDevice(light.config, command);
            sendCommand(light, data);
         }
      });
      mLightListView.setAdapter(mLightAdapter);
      mLightOnView.setSelected(false);
      mLightOnView.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            mLightOnView.setSelected(true);
            mLightOffView.setSelected(false);
            turnOnAllLights();
         }
      });
      mLightOnView.setSelected(false);
      mLightOffView.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            mLightOnView.setSelected(false);
            mLightOffView.setSelected(true);
            turnOffAllLights();
         }
      });
      mThemeGridView.requestFocus();
      mThemeGridView.requestFocusFromTouch();
   }

   private void turnOnAllLights() {
      for (Entry<String, List<LightItem>> entry : mLightDeviceMap.entrySet()) {
         List<LightItem> lights = entry.getValue();
         if (lights != null && lights.size() > 0) {
            byte[] data = getCommandByDevice(lights.get(0).config,
                  CMD_RELAY_ALL_ON);
            sendCommand(lights.get(0), data);
         }
      }
   }

   private void turnOffAllLights() {
      for (Entry<String, List<LightItem>> entry : mLightDeviceMap.entrySet()) {
         List<LightItem> lights = entry.getValue();
         if (lights != null && lights.size() > 0) {
            byte[] data = getCommandByDevice(lights.get(0).config,
                  CMD_RELAY_ALL_OFF);
            sendCommand(lights.get(0), data);
         }
      }
   }

   private void sendCommand(LightItem light, byte[] data) {
      StringBuilder builder = new StringBuilder(light.config.ip);
      builder.append('.').append(light.config.port);
      light.socket = mLightSocketMap.get(builder.toString());
      mIotManager.requestSendingUdpData(data, light.config.ip,
            light.config.port, light.socket, mHandler, light.listener);
   }

   private void analyzeResult(LightDataListener listener, DatagramPacket packet) {
      byte[] data = packet.getData();
      Utils.log(
            TAG,
            "analyzeResult: "
                  + IotManager.toHexString(data, 0, packet.getLength()));
      LightItem light = (LightItem) listener.device;
      switch (listener.device.config.type) {
      case DEV_BW8001SW:
         if (packet.getLength() == 6) {
            StringBuilder builder = new StringBuilder(light.config.ip);
            String key = builder.append('.').append(light.config.addr)
                  .toString();
            boolean status = (data[3] & 0x01) != 0;
            if (light.status != status) {
               light.status = status;

               List<LightItem> lights = mLightDeviceMap.get(key);
               if (lights != null && lights.size() > 0) {
                  for (LightItem item : lights) {
                     item.status = status;
                  }
               }
               mLightAdapter.notifyDataSetChanged();
            }
         }
         break;
      case DEV_BW800R3:
         if (packet.getLength() == 6) {
            StringBuilder builder = new StringBuilder(light.config.ip);
            String key = builder.append('.').append(light.config.addr)
                  .toString();
            List<LightItem> lights = mLightDeviceMap.get(key);
            if (lights != null && lights.size() > 0) {
               boolean changed = false;
               for (LightItem item : lights) {
                  boolean status = (data[3] & (0x01 << (item.config.channel - 1))) != 0;
                  if (item.status != status) {
                     item.status = status;
                     changed = true;
                  }
               }

               if (changed) {
                  mLightAdapter.notifyDataSetChanged();
               }
            }
         }
         break;
      }
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
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

   private void cleanup() {
      for (LightItem light : mLightList) {
         if (light.socket != null && !light.socket.isClosed()) {
            light.socket.close();
         }
         light.socket = null;
      }
      mLightSocketMap.clear();
   }

   private void configDevices() {
      mLightConfigMap.put(PORCH_LIGHT, new DeviceConfig(DEV_BW800R3, 201,
            "192.168.1.201", 5000, COMP_RELEAY, 1));

      mLightConfigMap.put(DINING_ROOM_LIGHT, new DeviceConfig(DEV_BW800R3, 201,
            "192.168.1.201", 5000, COMP_RELEAY, 2));

      mLightConfigMap.put(LIVING_ROOM_MAIN_LIGHT, new DeviceConfig(DEV_BW800R3,
            201, "192.168.1.201", 5000, COMP_RELEAY, 3));

      mLightConfigMap.put(LIVING_ROOM_SURROUND_LIGHT, new DeviceConfig(
            DEV_BW8001SW, 210, "192.168.1.210", 5000, COMP_RELEAY, 1));

      mLightConfigMap.put(BATHROOM_LIGHT, new DeviceConfig(DEV_BW8001SW, 211,
            "192.168.1.211", 5000, COMP_RELEAY, 1));

      mLightConfigMap.put(MASTER_BEDROOM_LIGHT, new DeviceConfig(DEV_BW8001SW,
            212, "192.168.1.212", 5000, COMP_RELEAY, 1));

      mLightConfigMap.put(SENCONDARY_BEDROOM_LIGHT, new DeviceConfig(
            DEV_BW8001SW, 213, "192.168.1.213", 5000, COMP_RELEAY, 1));

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
      theme.lights.add(LIVING_ROOM_MAIN_LIGHT);
      mThemeList.add(theme);

      theme = new ThemeItem();
      theme.ico = R.drawable.ico_bathroom;
      theme.title = R.string.bathroom;
      theme.lights.add(LIVING_ROOM_SURROUND_LIGHT);
      theme.lights.add(BATHROOM_LIGHT);
      mThemeList.add(theme);

      theme = new ThemeItem();
      theme.ico = R.drawable.ico_bedroom;
      theme.title = R.string.bedroom;
      theme.lights.add(MASTER_BEDROOM_LIGHT);
      theme.lights.add(SENCONDARY_BEDROOM_LIGHT);
      mThemeList.add(theme);

      theme = new ThemeItem();
      theme.ico = R.drawable.ico_custom;
      theme.title = R.string.custom;
      mThemeList.add(theme);

      // light control
      LightItem light = new LightItem();
      light.name = PORCH_LIGHT;
      light.title = R.string.porch_light;
      mLightList.add(light);

      light = new LightItem();
      light.name = DINING_ROOM_LIGHT;
      light.title = R.string.dining_room_light;
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
      light.name = BATHROOM_LIGHT;
      light.title = R.string.bathroom_light;
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
         item.config = mLightConfigMap.get(item.name);
         item.listener = new LightDataListener(item);
         mLightMap.put(item.name, item);

         StringBuilder builder = new StringBuilder(item.config.ip);
         String key = builder.append('.').append(item.config.addr).toString();
         if (mLightDeviceMap.containsKey(key)) {
            mLightDeviceMap.get(key).add(item);
         } else {
            List<LightItem> list = new ArrayList<LightItem>();
            list.add(item);
            mLightDeviceMap.put(key, list);
         }
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
         lightView.getLeftArrowView().setBackgroundResource(
               item.status == LightItem.ON ? R.drawable.ico_arrow_l_hl
                     : R.drawable.ico_arrow_l_normal);
         lightView.getRightArrowView().setBackgroundResource(
               item.status == LightItem.ON ? R.drawable.ico_arrow_r_hl
                     : R.drawable.ico_arrow_r_normal);
         lightView.setSelected(item.status);

         convertView.setTag(item);
         return convertView;
      }

   }

   class LightDataListener extends DeviceDataListener {

      public LightDataListener(LightItem light) {
         super(light);
      }

      @Override
      public void onDataSent(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramSocket) {
            synchronized (mLightSocketMap) {
               StringBuilder builder = new StringBuilder(this.device.config.ip);
               builder.append('.').append(this.device.config.port);
               mLightSocketMap.put(builder.toString(), (DatagramSocket) obj);
            }
         }
      }

      @Override
      public void onDataReceived(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramPacket) {
            analyzeResult(this, (DatagramPacket) obj);
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
