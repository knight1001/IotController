package com.oosic.iot.controller.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class InputStreamHelper {

   private String path;
   private URL url;
   private InputStream is;
   
   public InputStreamHelper(String path) {
      this.path = path;
   }
   
   public InputStreamHelper(URL url) {
      this.url = url;
   }
   
   public InputStreamHelper(InputStream stream) {
      this.is = stream;
   }
   
   public InputStream getInputStream() {
      if (this.is != null) {
         return this.is;
      }
      
      try {
         if (this.path != null) {
            this.is = new FileInputStream(this.path);
         }
         
         if (this.url != null) {
            this.is = this.url.openConnection().getInputStream();
         }
      } catch (FileNotFoundException e) {
//         e.printStackTrace();
         this.is = null;
      } catch (IOException e) {
         this.is = null;
         e.printStackTrace();
      } finally {
         
      }
      
      return this.is;
   }
   
   public void close() {
      if (this.is != null) {
         try {
            this.is.close();
            this.is = null;
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
   
   public boolean writeToFile(String path) {
      OutputStream out = new OutputStreamHelper(path).getOutputStream();
      
      return writeToFile(getInputStream(), out);
   }
   
   public boolean writeToFile(OutputStream out) {
      return writeToFile(getInputStream(), out);
   }
   
   public static boolean copy(String dst, String src) {
      InputStream in = new InputStreamHelper(src).getInputStream();
      OutputStream out = new OutputStreamHelper(dst).getOutputStream();
      
      return writeToFile(in, out);
   }
   
   public static boolean writeToFile(InputStream in, String path) {
      OutputStream out = new OutputStreamHelper(path).getOutputStream();
      
      return writeToFile(in, out);
   }
   
   public static boolean writeToFile(InputStream in, OutputStream out) {
      boolean result = false;
      
      if ((in == null) || (out == null)) {
         return result;
      }
      
      try {
         byte[] buffer = new byte[4096];
         int length = 0;
         
         while ((length = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, length);
         }
         out.close();
//         in.close();
         
         result = true;
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            out.close();
//            in.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      
      return result;
   }

}
