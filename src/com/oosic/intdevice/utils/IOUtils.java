package com.oosic.intdevice.utils;

import java.io.File;
import java.io.IOException;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

public final class IOUtils {
   
	public static final String ROOT_DIR = ".IntDevice";
	private static final String CACHE_DIR = "cache";
	private static final String DOWNLOAD_DIR = "download";
   
	public static final long RESERVED_STORAGE_SPACE = 1; //MB
	
	public static boolean hasExternalStorage() {
		return Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public static File getExternalFile(String file) {
		return new File(Environment.getExternalStorageDirectory(), file);
	}

	private static File getRootDir() throws IOException {
		File dir = getExternalFile(ROOT_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
			new File(dir, ".nomedia").createNewFile();
		}
		return dir;
	}

	public static File getCacheDir() throws IOException {
		File dir = new File(getRootDir(), CACHE_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
   
	public static File getDownloadDir() throws IOException {
		File dir = new File(getRootDir(), DOWNLOAD_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
   
   public static String getFileNameFromUrl(String url) {
      if (TextUtils.isEmpty(url)) {
         return "";
      }
      
      return url.substring(url.lastIndexOf('/') + 1);
   }
   
   public static long gb2Bytes(int gb) {
      return (long) gb * 1024 * 1024 * 1024;
   }
   
   public static long mb2Bytes(int mb) {
      return (long) mb * 1024 * 1024;
   }
   
   public static long kb2Bytes(int kb) {
      return (long) kb * 1024;
   }
   
	public static boolean isInternalStorageAvailable() {
	   return Environment.MEDIA_MOUNTED.equals(
	         Environment.getExternalStorageState());
	}
	
	public static boolean hasEnoughInternalStorageSpace() {
	   return getAvailableInternalStorageSpace()
	         > RESERVED_STORAGE_SPACE * 1024 * 1024;
	}
	
	public static boolean hasInternalStorageSpace() {
	   return getAvailableInternalStorageSpace() > 0;
	}
	
	public static long getAvailableInternalStorageSpace() {
	   return getAvailableStorageSpace(
	         Environment.getExternalStorageDirectory().getPath());
	}
	
	public static boolean hasStorageSpace(String path) {
	   return getAvailableStorageSpace(path) > 0;
	}
	
	public static long getAvailableStorageSpace(String path) {
	   StatFs fs = new StatFs(path);
	   long blockSize = fs.getBlockSize();
	   long blocks = fs.getAvailableBlocks();
	   return (long) blockSize * blocks;
	}
	
}
