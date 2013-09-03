package com.oosic.iot.controller.library;

import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.text.TextUtils;

public class SsidEncryption {

   public static final int FLAG_START = 1399;
   public static final int FLAG_SEPARATOR = 1459;

   private String ssid;
   private String password;
   private String encryptionKey;
   private ArrayList<Integer> encryptedData;

   public SsidEncryption(String ssid, String password, String encryptionKey)
         throws Exception {
      this.ssid = ssid;
      this.password = password;
      this.encryptionKey = encryptionKey;

      encryptData();
   }

   private void encryptData() throws Exception {
      this.encryptedData = new ArrayList<Integer>();
      this.encryptedData.add(Integer.valueOf(FLAG_START));
      constructSsid();
      this.encryptedData.add(Integer.valueOf(FLAG_SEPARATOR));
      constructPassord();
   }

   private byte[] encryptPassword() throws Exception {
      if (TextUtils.isEmpty(this.encryptionKey)) {
         return this.password.getBytes();
      }

      final int AES_LENGTH = 16;
      final int DATA_LENGTH = 32;
      byte[] data = this.password.getBytes();
      byte[] key = this.encryptionKey.getBytes();
      byte[] paddedData = new byte[DATA_LENGTH];
      byte[] aesKey = new byte[AES_LENGTH];

      for (int x = 0; x < AES_LENGTH; x++) {
         if (x < key.length) {
            aesKey[x] = key[x];
         } else {
            aesKey[x] = 0;
         }
      }

      int dataOffset = 0;
      if (data.length < DATA_LENGTH) {
         paddedData[dataOffset] = (byte) data.length;
         dataOffset++;
      }

      System.arraycopy(data, 0, paddedData, dataOffset, data.length);
      dataOffset += data.length;

      while (dataOffset < DATA_LENGTH) {
         paddedData[dataOffset] = 0;
         dataOffset++;
      }

      Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
      return cipher.doFinal(paddedData);
   }

   private void constructSsid() throws Exception {
      int length = this.ssid.length() + 1 + 27;
      this.encryptedData.add(Integer.valueOf(length));

      encodeSsid(this.ssid.getBytes());
   }

   private void encodeSsid(byte[] data) throws Exception {
      encodeData(data);
   }

   private void constructPassord() throws Exception {
      byte[] data = encryptPassword();
      int length = data.length + 1 + 27;
      this.encryptedData.add(Integer.valueOf(length));

      encodePassword(data);
   }

   private void encodePassword(byte[] data) throws Exception {
      encodeData(data);
   }

   private void encodeData(byte[] data) {
      final int DATA_OFFSET = 593;
      byte prevNibble = 0;
      int index = 0;
      for (int i = 0; i < data.length; i++) {
         byte currChar = data[i];

         int lowNibble = currChar & 0x0f;
         int highNibble = currChar >> 4;

         this.encryptedData.add(Integer
               .valueOf(((prevNibble ^ index++) << 4 | highNibble)
                     + DATA_OFFSET));
         prevNibble = (byte) highNibble;
         this.encryptedData
               .add(Integer.valueOf(((prevNibble ^ index++) << 4 | lowNibble)
                     + DATA_OFFSET));
         prevNibble = (byte) lowNibble;

         index &= 15;
      }
   }

   public ArrayList<Integer> getEncryptedData() {
      return this.encryptedData;
   }

}
