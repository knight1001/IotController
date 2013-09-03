package com.oosic.iot.controller.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

public class OutputStreamHelper {

   private String path;
   private URL url;
   private OutputStream os;
   
   public OutputStreamHelper(String path) {
      this.path = path;
   }
   
   public OutputStreamHelper(URL url) {
      this.url = url;
   }
   
   public OutputStreamHelper(OutputStream stream) {
      this.os = stream;
   }
   
   public void close() {
      if (this.os != null) {
         try {
            this.os.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
   
   public OutputStream getOutputStream() {
      if (this.os != null) {
         return this.os;
      }
      
      try {
         if (this.path != null) {
            File file = FileHelper.createFile(this.path);
            this.os = new FileOutputStream(file);
         }
         
         if (this.url != null) {
            this.os = this.url.openConnection().getOutputStream();
         }
      } catch (FileNotFoundException e) {
//         e.printStackTrace();
         this.os = null;
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      return this.os;
   }

}
