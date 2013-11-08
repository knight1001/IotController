package com.oosic.iot.controller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.oosic.iot.controller.library.IotManager;
import com.oosic.iot.controller.library.IotResult;
import com.oosic.iot.controller.R;
import com.oosic.iot.controller.utils.UIUtils;
import com.oosic.iot.controller.utils.Utils;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SensorControlActivity extends IotBaseActivity {

   private static final String TAG = "SensorControlActivity";

   private static final String TEMPERATURE_SENSOR = "temperature";
   private static final String HUMIDITY_SENSOR = "humidity";
   private static final String LIGHT_SENSOR = "light";
   private static final String FORMOL_SENSOR = "formol";
   private static final String FAN_SWITCHER = "fan_switcher";
   private static final String LIGHT_SWITCHER = "light_switcher";

   private TextView mTempView, mHumidityView, mLightView, mFormolView;
   private TextView mFanSwitcherView, mLightSwitcherView;
   private Handler mHandler = new Handler();
   private IotManager mIotManager;
   private Map<String, DeviceItem> mDeviceMap = new HashMap<String, DeviceItem>();
   private Map<String, DatagramSocket> mDeviceSocketMap = new HashMap<String, DatagramSocket>();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_sensor_control);

      findViews();
      init();
      mHandler.post(new Runnable() {
         public void run() {
            relayoutViews();
         }
      });
   }

   private void findViews() {
      mTempView = (TextView) findViewById(R.id.temp_sensor);
      mHumidityView = (TextView) findViewById(R.id.humidity_sensor);
      mLightView = (TextView) findViewById(R.id.light_sensor);
      mFormolView = (TextView) findViewById(R.id.formol_sensor);
      mFanSwitcherView = (TextView) findViewById(R.id.fan_switcher);
      mLightSwitcherView = (TextView) findViewById(R.id.light_switcher);
   }

   private void relayoutViews() {

   }

   private void init() {
      mIotManager = getIotManager();
      configDevices();
      initViews();
   }

   private void configDevices() {
      // temperature sensor
      DeviceItem device = new DeviceItem();
      device.name = TEMPERATURE_SENSOR;
      device.config = new DeviceConfig(DEV_BW8001SW, 210, "192.168.1.210",
            5000, COMP_ADC, 1);
      device.listener = new SensorDataListener(device);
      mTempView.setTag(device);
      mDeviceMap.put(device.name, device);

      // fan switcher
      device = new DeviceItem();
      device.name = FAN_SWITCHER;
      device.config = new DeviceConfig(DEV_BW8001SW, 210, "192.168.1.210",
            5000, COMP_RELEAY, 1);
      device.listener = new SensorDataListener(device);
      mFanSwitcherView.setTag(device);
      mDeviceMap.put(device.name, device);

      // humidity sensor
      device = new DeviceItem();
      device.name = HUMIDITY_SENSOR;
      device.config = new DeviceConfig(DEV_BW8001SW, 211, "192.168.1.211",
            5000, COMP_ADC, 1);
      device.listener = new SensorDataListener(device);
      mHumidityView.setTag(device);
      mDeviceMap.put(device.name, device);

      // light sensor
      device = new DeviceItem();
      device.name = LIGHT_SENSOR;
      device.config = new DeviceConfig(DEV_BW8001SW, 212, "192.168.1.212",
            5000, COMP_ADC, 1);
      device.listener = new SensorDataListener(device);
      mLightView.setTag(device);
      mDeviceMap.put(device.name, device);

      // light switcher
      device = new DeviceItem();
      device.name = LIGHT_SWITCHER;
      device.config = new DeviceConfig(DEV_BW8001SW, 212, "192.168.1.212",
            5000, COMP_RELEAY, 1);
      device.listener = new SensorDataListener(device);
      mLightSwitcherView.setTag(device);
      mDeviceMap.put(device.name, device);

      device = new DeviceItem();
      device.name = FORMOL_SENSOR;
      device.config = new DeviceConfig(DEV_BW8001SW, 213, "192.168.1.213",
            5000, COMP_ADC, 1);
      device.listener = new SensorDataListener(device);
      mFormolView.setTag(device);
      mDeviceMap.put(device.name, device);
   }

   private void initViews() {
      mFanSwitcherView.setText(R.string.off);
      mFanSwitcherView.setSelected(false);
      mFanSwitcherView.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            DeviceItem device = (DeviceItem) v.getTag();
            byte[] data = getCommandByDevice(device.config,
                  v.isSelected() ? CMD_RELAY_OFF : CMD_RELAY_ON);
            sendCommand(device, data);
         }
      });

      mLightSwitcherView.setText(R.string.off);
      mLightSwitcherView.setSelected(false);
      mLightSwitcherView.setOnClickListener(new OnClickListener() {
         public void onClick(View v) {
            DeviceItem device = (DeviceItem) v.getTag();
            byte[] data = getCommandByDevice(device.config,
                  v.isSelected() ? CMD_RELAY_OFF : CMD_RELAY_ON);
            sendCommand(device, data);
         }
      });
   }

   private void queryDevices() {
      DeviceItem device = mDeviceMap.get(TEMPERATURE_SENSOR);
      byte[] data = getCommandByDevice(device.config, CMD_GET_ADC);
      sendCommand(device, data);

      device = mDeviceMap.get(HUMIDITY_SENSOR);
      data = getCommandByDevice(device.config, CMD_GET_ADC);
      sendCommand(device, data);

      device = mDeviceMap.get(LIGHT_SENSOR);
      data = getCommandByDevice(device.config, CMD_GET_ADC);
      sendCommand(device, data);

      device = mDeviceMap.get(FORMOL_SENSOR);
      data = getCommandByDevice(device.config, CMD_GET_ADC);
      sendCommand(device, data);
   }

   private void scheduleQueryDeivces() {
      mHandler.removeCallbacks(mQueryDevicesRunnable);
      mHandler.postDelayed(mQueryDevicesRunnable, 60000);
   }

   private QueryDevicesRunnable mQueryDevicesRunnable = new QueryDevicesRunnable();

   class QueryDevicesRunnable implements Runnable {

      @Override
      public void run() {
         queryDevices();
         scheduleQueryDeivces();
      }

   }

   @Override
   public void onResume() {
      super.onResume();

      queryDevices();
      scheduleQueryDeivces();
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

   private void sendCommand(DeviceItem device, byte[] data) {
      StringBuilder builder = new StringBuilder(device.config.ip);
      builder.append('.').append(device.config.port);
      device.socket = mDeviceSocketMap.get(builder.toString());
      mIotManager.requestSendingUdpData(data, device.config.ip,
            device.config.port, device.socket, mHandler, device.listener);
   }

   private void cleanup() {
      mHandler.removeCallbacks(mQueryDevicesRunnable);

      for (Entry<String, DeviceItem> entry : mDeviceMap.entrySet()) {
         DeviceItem device = entry.getValue();
         if (device.socket != null && !device.socket.isClosed()) {
            device.socket.close();
         }
         device.socket = null;
      }
      mDeviceSocketMap.clear();
   }

   private void analyzeResult(SensorDataListener listener, DatagramPacket packet) {
      byte[] data = packet.getData();
      Utils.log(
            TAG,
            "analyzeResult: "
                  + IotManager.toHexString(data, 0, packet.getLength()));
      DeviceItem device = listener.device;
      switch (device.config.type) {
      case DEV_BW8001SW:
         if (packet.getLength() == 6) {
            if (device.config.component == COMP_ADC) {
               int adcValue = ((data[4] & 0xff) << 8) & 0xff00 | data[3] & 0xff;
               analyzeAdcValue(device, adcValue);
            } else if (device.config.component == COMP_RELEAY) {
               boolean status = (data[3] & 0x01) != 0;
               if (FAN_SWITCHER.equals(device.name)) {
                  mFanSwitcherView.setSelected(status);
                  mFanSwitcherView.setText(status ? R.string.on : R.string.off);
               } else if (LIGHT_SWITCHER.equals(device.name)) {
                  mLightSwitcherView.setSelected(status);
                  mLightSwitcherView.setText(status ? R.string.on
                        : R.string.off);
               }
            }
         }
         break;
      }
   }

   private void analyzeAdcValue(DeviceItem device, int adcValue) {
      if (TEMPERATURE_SENSOR.equals(device.name)) {
         int temperature = getTemperatureByResistence(getTemperatureResistenceByAdcValue(adcValue));
         mTempView.setText(String.valueOf(temperature) + "℃");
         int command = 0;
         if (temperature > 28 && !mFanSwitcherView.isSelected()) {
            command = CMD_RELAY_ON;
         } else if (temperature <= 28 && mFanSwitcherView.isSelected()) {
            command = CMD_RELAY_OFF;
         }
         if (command > 0) {
            DeviceItem switcher = mDeviceMap.get(FAN_SWITCHER);
            byte[] data = getCommandByDevice(device.config, command);
            sendCommand(switcher, data);
         }
      } else if (HUMIDITY_SENSOR.equals(device.name)) {
         int humidity = getHumidityByVoltage(getHumidityVoltageByAdcValue(adcValue));
         mHumidityView.setText(String.valueOf(humidity) + '%');
      } else if (LIGHT_SENSOR.equals(device.name)) {
         mLightView.setText(String.valueOf(((double) adcValue) / 1000));
         int command = 0;
         if (adcValue >= 2000 && mLightSwitcherView.isSelected()) {
            command = CMD_RELAY_OFF;
         } else if (adcValue < 2000 && !mLightSwitcherView.isSelected()) {
            command = CMD_RELAY_ON;
         }
         if (command > 0) {
            DeviceItem switcher = mDeviceMap.get(LIGHT_SWITCHER);
            byte[] data = getCommandByDevice(device.config, command);
            sendCommand(switcher, data);
         }
      } else if (FORMOL_SENSOR.equals(device.name)) {
         mFormolView.setText(String.valueOf(((double) adcValue) / 1000));
      }
   }

   class SensorDataListener extends DeviceDataListener {

      public SensorDataListener(DeviceItem device) {
         super(device);
      }

      @Override
      public void onDataSent(IotResult result, Object obj) {
         if (obj != null && obj instanceof DatagramSocket) {
            synchronized (mDeviceSocketMap) {
               StringBuilder builder = new StringBuilder(this.device.config.ip);
               builder.append('.').append(this.device.config.port);
               mDeviceSocketMap.put(builder.toString(), (DatagramSocket) obj);
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

   static double getHumidityVoltageByAdcValue(int adcValue) {
      return ((double) adcValue) * 1.5 / (double) 1000;
   }

   static double getTemperatureResistenceByAdcValue(int adcValue) {
      return (94.5 - ((double) adcValue) * 30 / (double) 1000)
            / ((double) adcValue / 1000);
   }

   static int getHumidityByVoltage(double voltage) {
      for (int i = 0; i < HUMIDITY_TABLE.size(); i++) {
         HumidityValue value = HUMIDITY_TABLE.get(i);
         int result = Double.compare(voltage, value.voltage);
         if (result == 0) {
            return value.humidity;
         } else if (result < 0) {
            if (i == 0) {
               return value.humidity;
            } else if (i > 0) {
               HumidityValue prevValue = HUMIDITY_TABLE.get(i - 1);
               return (int) ((voltage - prevValue.voltage)
                     * (value.humidity - prevValue.humidity)
                     / (value.voltage - prevValue.voltage) + prevValue.humidity);
            }
         }
      }
      return HUMIDITY_TABLE.get(HUMIDITY_TABLE.size() - 1).humidity;
   }

   static int getTemperatureByResistence(double resistence) {
      for (int i = 0; i < TEMPERATURE_TABLE.size(); i++) {
         TemperatureValue value = TEMPERATURE_TABLE.get(i);
         int result = Double.compare(resistence, value.resistence);
         if (result == 0) {
            return value.temperature;
         } else if (result > 0) {
            if (i == 0) {
               return value.temperature;
            } else if (i > 0) {
               return TEMPERATURE_TABLE.get(i - 1).temperature;
            }
         }
      }
      return TEMPERATURE_TABLE.get(TEMPERATURE_TABLE.size() - 1).temperature;
   }

   static final List<HumidityValue> HUMIDITY_TABLE = new ArrayList<HumidityValue>();
   static final List<TemperatureValue> TEMPERATURE_TABLE = new ArrayList<TemperatureValue>();

   static {
      // humidity table
      HUMIDITY_TABLE.add(new HumidityValue(0.60, 20));
      HUMIDITY_TABLE.add(new HumidityValue(0.90, 30));
      HUMIDITY_TABLE.add(new HumidityValue(1.20, 40));
      HUMIDITY_TABLE.add(new HumidityValue(1.50, 50));
      HUMIDITY_TABLE.add(new HumidityValue(1.80, 60));
      HUMIDITY_TABLE.add(new HumidityValue(2.10, 70));
      HUMIDITY_TABLE.add(new HumidityValue(2.40, 80));
      HUMIDITY_TABLE.add(new HumidityValue(2.70, 90));
      HUMIDITY_TABLE.add(new HumidityValue(2.82, 95));

      // temperature table
      TEMPERATURE_TABLE.add(new TemperatureValue(17.9310, 10));
      TEMPERATURE_TABLE.add(new TemperatureValue(17.2641, 11));
      TEMPERATURE_TABLE.add(new TemperatureValue(16.5796, 12));
      TEMPERATURE_TABLE.add(new TemperatureValue(15.9260, 13));
      TEMPERATURE_TABLE.add(new TemperatureValue(15.3016, 14));
      TEMPERATURE_TABLE.add(new TemperatureValue(14.7050, 15));
      TEMPERATURE_TABLE.add(new TemperatureValue(14.1319, 16));
      TEMPERATURE_TABLE.add(new TemperatureValue(13.5899, 17));
      TEMPERATURE_TABLE.add(new TemperatureValue(13.0688, 18));
      TEMPERATURE_TABLE.add(new TemperatureValue(12.5705, 19));
      TEMPERATURE_TABLE.add(new TemperatureValue(12.0935, 20));
      TEMPERATURE_TABLE.add(new TemperatureValue(11.6377, 21));
      TEMPERATURE_TABLE.add(new TemperatureValue(11.2011, 22));
      TEMPERATURE_TABLE.add(new TemperatureValue(10.7833, 23));
      TEMPERATURE_TABLE.add(new TemperatureValue(10.3832, 24));
      TEMPERATURE_TABLE.add(new TemperatureValue(10.0000, 25));
      TEMPERATURE_TABLE.add(new TemperatureValue(9.6330, 26));
      TEMPERATURE_TABLE.add(new TemperatureValue(9.2813, 27));
      TEMPERATURE_TABLE.add(new TemperatureValue(8.9443, 28));
      TEMPERATURE_TABLE.add(new TemperatureValue(8.6212, 29));
      TEMPERATURE_TABLE.add(new TemperatureValue(8.3115, 30));
      TEMPERATURE_TABLE.add(new TemperatureValue(8.0145, 31));
      TEMPERATURE_TABLE.add(new TemperatureValue(7.7297, 32));
      TEMPERATURE_TABLE.add(new TemperatureValue(7.4564, 33));
      TEMPERATURE_TABLE.add(new TemperatureValue(7.1941, 34));
      TEMPERATURE_TABLE.add(new TemperatureValue(6.9424, 35));
      TEMPERATURE_TABLE.add(new TemperatureValue(6.7008, 36));
      TEMPERATURE_TABLE.add(new TemperatureValue(6.4689, 37));
      TEMPERATURE_TABLE.add(new TemperatureValue(6.2401, 38));
      TEMPERATURE_TABLE.add(new TemperatureValue(6.0321, 39));
   }

   static class HumidityValue {
      double voltage;
      int humidity;

      public HumidityValue(double voltage, int humidity) {
         this.voltage = voltage;
         this.humidity = humidity;
      }
   }

   static class TemperatureValue {
      double resistence;
      int temperature;

      public TemperatureValue(double resistence, int temperature) {
         this.resistence = resistence;
         this.temperature = temperature;
      }
   }

}
