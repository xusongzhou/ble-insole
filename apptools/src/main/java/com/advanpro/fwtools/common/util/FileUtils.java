package com.advanpro.fwtools.common.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.*;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.UUID;

public class FileUtils {
	
    /**
     * 格式化文件大小，根据文件大小不同使用不同单位
     * @param size 文件大小
     * @return 字符串形式的大小，包含单位(B,KB,MB,GB,TB,PB)
     */
	public static String formatFileSize(long size) {
		DecimalFormat formater = new DecimalFormat("####.00");
		if (size < 1024L) {
			return size + " B";
		} else if (size < 1048576L) {
			return formater.format(size / 1024f) + " KB";
		} else if (size < 1073741824L) {
			return formater.format(size / 1048576f) + " MB";
		} else if (size < 1099511627776L) {
			return formater.format(size / 1073741824f) + " GB";
		} else if (size < 1125899906842624L) {
			return formater.format(size / 1099511627776f) + " TB";
		} else if (size < 1152921504606846976L) {
			return formater.format(size / 1125899906842624f) + " PB";
		} 
		return "size: out of range";
	}
    
	/**
	 * 从路径中获取文件名，包含扩展名
	 * @param path 路径
	 * @return 如果所传参数是合法路径，截取文件名，如果不是返回原值
	 */
	public static String getFileName(String path) {
		if (path != null && (path.contains("/")||path.contains("\\"))) {
			String fileName = path.trim();
			int beginIndex;
			if ((beginIndex=fileName.lastIndexOf("\\")) != -1) {
			    fileName = fileName.substring(beginIndex+1);
			}
			if ((beginIndex=fileName.lastIndexOf("/")) != -1) {
				fileName = fileName.substring(beginIndex+1);
			}
			return fileName;
		} 
		return path;
	}
	
	/**
	 * 从路径中获取文件名，不包含扩展名
	 * @param path 路径
	 * @return 如果所传参数是合法路径，截取文件名，如果不是返回原值
	 */
	public static String getFileNameWithoutSuffix(String path) {
		if (path != null && (path.contains("/")||path.contains("\\"))) {
			String fileName = path.trim();
			int beginIndex;
			if ((beginIndex=fileName.lastIndexOf("\\")) != -1) {
			    fileName = fileName.substring(beginIndex+1);
			}
			if ((beginIndex=fileName.lastIndexOf("/")) != -1) {
				fileName = fileName.substring(beginIndex+1);
			}
			return deleteSuffix(fileName);
		} 
		return path;
	}

	/**
	 * 返回去掉扩展名的文件名
	 */
	public static String deleteSuffix(String fileName) {
		if (fileName.contains(".")) {
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}
		return fileName;
	}
	
	/**
	 * 检查是否有同名文件，有则在自动在文件名后加当前时间的毫秒值
	 * @param fileName 要检查的文件名
	 * @param targetDir 检查文件名重名所在的目录
	 * @return 有重名返回更改后的文件名，没有则返回原名
	 */
	public static String checkAndRename(String fileName, String targetDir) {
		File file = new File(targetDir,getFileName(fileName));
		if (file.exists()) {
			if (fileName.contains(".")) {
				String sub = fileName.substring(0, fileName.lastIndexOf(".")); 
				fileName = fileName.replace(sub, sub+"_"+System.currentTimeMillis());
			} else {
				fileName = fileName+"_"+System.currentTimeMillis();
			}			
		}
		return fileName;
	} 
	
	/**
	 * 移动文件，当有重名文件时，自动在原文件名后加上当前时间的毫秒值
	 * @param file 要移动的文件
	 * @param targetDir 目标目录
	 * @return 移动成功返回true,否则返回false
	 * @throws IOException
	 */
	public static boolean moveFile(File file, String targetDir) throws IOException {
		File dir = new File(targetDir);
		String fileName = checkAndRename(file.getName(), targetDir);
		//写入文件
		File f = new File(dir, fileName);
		//读取源文件到输入流
		InputStream in = new FileInputStream(file);		
		OutputStream out = new FileOutputStream(f);

		byte[] buf = new byte[1024];
		int len = -1;
		while ((len=in.read(buf)) != -1) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		
		//如果文件存在，并且大小与源文件相等，则写入成功，删除源文件
		if (f.exists() && f.length()==file.length()) {
			file.delete();
			return true;
		}
		return false;
	}
	
	/**
	 * 去掉字符串中重复部分字符串
	 * @param dup 重复部分字符串
	 * @param strs 要去重的字符串
	 * @return 按参数先后顺序返回一个字符串数组
	 */
	public static String[] removeDuplicate(String dup, String... strs) {
		for (int i = 0; i < strs.length; i++) {
			if (strs[i] != null) {
				strs[i] = strs[i].replaceAll(dup+"+", "");				
			}
		}
		return strs;
	}
	
	/**
	 * 获取随机UUID文件名
	 * @param fileName 原文件名
	 * @return 生成的文件名
	 */
	public static String generateRandonFileName(String fileName) {
		// 获得扩展名
		int beginIndex = fileName.lastIndexOf(".");
		String ext = "";
		if (beginIndex != -1) {
			ext = fileName.substring(beginIndex);
		}
		return UUID.randomUUID().toString() + ext;
	}	
	
	/**
	 * 删除文件夹
	 * @param file 文件夹
	 * @return 删除成功返回true,否则返回false
	 */
	public static boolean delelteDirectory(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					delelteDirectory(f);
				} else {
					f.delete();
				}
			}
		}
			
		return file.delete();			
	}
	
	/**
	 * 获取文件夹的大小
	 * @param dir 目录
	 * @return 所传参数是目录且存在，则返回文件夹大小，否则返回-1
	 */
	public static long getDirectorySize(File dir) {
		if (dir.isDirectory() && dir.exists()) {
			long size = 0;
			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						size += getDirectorySize(file);
					} else {
						size += file.length();
					}
				}
				return size;				
			}
			return 0;
		}
		return -1;
	}

	/**
	 * 根据文件路径加载bitmap
	 * @param path 文件绝对路径
	 */
	public static Bitmap getBitmap(String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			return BitmapFactory.decodeStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        return null;
	}

	/**
	 * 根据文件路径加载bitmap
	 * @param path 文件绝对路径
	 * @param w 宽
	 * @param h 高
	 */
	public static Bitmap getBitmap(String path, int w, int h) {
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			// 设置为ture只获取图片大小
			opts.inJustDecodeBounds = true;
			opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
			// 返回为空
			BitmapFactory.decodeFile(path, opts);
			int width = opts.outWidth;
			int height = opts.outHeight;
			float scaleWidth = 0.f, scaleHeight = 0.f;
			if (width > w || height > h) {
				// 缩放
				scaleWidth = ((float) width) / w;
				scaleHeight = ((float) height) / h;
			}
			opts.inJustDecodeBounds = false;
			float scale = Math.max(scaleWidth, scaleHeight);
			opts.inSampleSize = (int) scale;
			WeakReference<Bitmap> weak = new WeakReference<>(BitmapFactory.decodeFile(path, opts));
			Bitmap bMapRotate = Bitmap.createBitmap(weak.get(), 0, 0, weak.get().getWidth(), weak.get().getHeight(), null, true);
			if (bMapRotate != null) {
				return bMapRotate;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        return null;
	}

	/**
	 * 保存bitmap到文件
	 * @param photoFile 文件
	 */
	public static void saveBitmapToFile(Bitmap bitmap, File photoFile){		
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(photoFile);
			if (bitmap != null) {
				if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)) {
					fileOutputStream.flush();
				}
			}
		} catch (Exception e) {
			photoFile.delete();
			e.printStackTrace();
		} finally{
			try {
				if (fileOutputStream != null) fileOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
