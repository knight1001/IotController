package com.oosic.iot.controller.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {
   
   private static final String TAG = "FileHelper";
   
   public static List<String> getSubFilePaths(String dirPath) {
      File dir = new File(dirPath);
      
      if (!(dir.exists()) || !(dir.isDirectory())) {
         Utils.log(TAG, "Error: " + dirPath + " does not exists!!!");
         return null;
      }
      
      List<String> list = new ArrayList<String>();
      File[] files = dir.listFiles();
      
      for (File file : files) {
         String path = file.getAbsolutePath();
         
         if (file.isFile()) {
            list.add(path);
         } else if (file.isDirectory()) {
            list.add(path);
            list.addAll(getSubFilePaths(path));
         }
      }
      
      return list;
   }
   
   public static List<File> getSubFiles(String dirPath) {
      File dir = new File(dirPath);
      
      if (!(dir.exists()) || !(dir.isDirectory())) {
         Utils.log(TAG, "Error: " + dirPath + " does not exists!!!");
         return null;
      }
      
      List<File> list = new ArrayList<File>();
      File[] files = dir.listFiles();
      
      for (File file : files) {
         Utils.log(TAG, "LIST: " + file.getAbsolutePath());
         if (file.isFile()) {
            list.add(file);
         } else if (file.isDirectory()) {
            list.add(file);
            list.addAll(getSubFiles(file.getAbsolutePath()));
         }
      }
      
      return list;
   }
   
   public static File createFile(String absolutePath) {
      if (absolutePath.endsWith("/")) {
         Utils.log(TAG, "Error: " + absolutePath + " is a directory!!!");
         return null;
      }
      
      Utils.log(TAG, "FILE: " + absolutePath);
      File file = new File(absolutePath);
      try {
         if (!file.exists()) {
            int index = absolutePath.lastIndexOf("/");
            if (index > 0) {
               String dirPath = absolutePath.substring(0, index + 1);
               File dir = new File(dirPath);
               dir.mkdirs();
            }
         } else {
            if (!file.delete()) {
               return null;
            }
         }
         
         if (file.createNewFile()) {
            return file;
         }
      } catch (IOException e) {
         e.printStackTrace();
      } catch (SecurityException e) {
         e.printStackTrace();
      }
         
      return null;
   }
   
   public static File createDir(String absolutePath) {
      if (!absolutePath.endsWith("/")) {
         absolutePath += "/";
      }
      
      Utils.log(TAG, "DIR: " + absolutePath);
      File dir = new File(absolutePath);
      try {
         if (!dir.exists()) {
            dir.mkdirs();
         }
         
         return dir;
      } catch (SecurityException e) {
         e.printStackTrace();
      }
         
      return null;
   }
   
   public static void deleteFile(String absolutePath) {
      File file = new File(absolutePath);
      
      if (file.exists()) {
         deleteFile(file);
      }
   }
   
   public static void deleteFile(File file) {
      if (file.isDirectory()) {
         File[] files = file.listFiles();
         if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; ++i) {
               deleteFile(files[i]);
            }
         }
      }
      file.delete();
   }
   
   public static void copyStream(InputStream is, OutputStream os) throws IOException {
      byte[] buffer = new byte[4096];
      int length = 0;
      while ((length = is.read(buffer, 0, buffer.length)) != -1) {
         os.write(buffer, 0, length);
      }
   }
   
}
