package com.advanpro.fwtools.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by zeng on 2016/5/5.
 * 解压缩工具
 */
public class ZipUtils {
    /**
     * 多文件压缩文件
     * @param zipName 压缩包名
     * @param zipPaths 待压缩文件路径
     * @return 压缩包
     */
    public static File zip(String zipName, String... zipPaths) {
        File zipFile = null;
        if (zipPaths != null && zipPaths.length > 0) {
            if (zipName == null) zipName = System.currentTimeMillis() + ".zip";
            ZipOutputStream zos = null;            
            try {
                boolean first = true;
                for (String path : zipPaths) {
                    File file = new File(path);
                    if (file.exists()) {
                        if (zipFile == null) {
                            zipFile = new File(file.getParent(), zipName);
                        }
                        //如果已存在同名压缩包
                        if (first && zipFile.exists()) {
                            return zipFile;
                        }
                        if (zos == null) {
                            zos = new ZipOutputStream(new FileOutputStream(zipFile));
                        }
                        addEntry("/", file, zos);
                        first = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(zos);
            }            
        }
        return zipFile;
    }
    
    /**
     * 扫描添加文件Entry
     * @param base 基路径
     * @param source 源文件
     * @param zos Zip文件输出流
     */
    private static void addEntry(String base, File source, ZipOutputStream zos)
            throws IOException {
        // 按目录分级，形如：/aaa/bbb.txt
        String entry = base + source.getName();
        if (source.isDirectory()) {
            for (File file : source.listFiles()) {
                // 递归列出目录下的所有文件，添加文件Entry
                addEntry(entry + "/", file, zos);
            }
        } else {
            BufferedInputStream bis = null;
            try {
                byte[] buffer = new byte[1024 * 10];
                bis = new BufferedInputStream(new FileInputStream(source), buffer.length);
                int read;
                zos.putNextEntry(new ZipEntry(entry));
                while ((read = bis.read(buffer, 0, buffer.length)) != -1) {
                    zos.write(buffer, 0, read);
                }
                zos.closeEntry();
            } finally {
                IOUtils.closeQuietly(bis);
            }
        }
    }

    /**
     * 解压文件到当前文件夹
     * @param filePath 压缩文件路径
     */
    public static void unZip(String filePath) {
        unZip(filePath, null);
    }

    /**
     * 解压文件到指定文件夹
     * @param sourcePath 压缩文件路径
     * @param targetDir 解压的文件夹
     */
    public static void unZip(String sourcePath, String targetDir) {
        File source = new File(sourcePath);
        if (source.exists()) {
            ZipInputStream zis = null;
            BufferedOutputStream bos = null;
            try {
                zis = new ZipInputStream(new FileInputStream(source));
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null && !entry.isDirectory()) {
                    File target;
                    if (targetDir == null) {
                        target = new File(source.getParent(), entry.getName());
                    } else {
                        target = new File(targetDir, entry.getName());
                    }
                    if (!target.getParentFile().exists()) {
                        // 创建文件父目录
                        target.getParentFile().mkdirs();
                    }
                    // 写入文件
                    bos = new BufferedOutputStream(new FileOutputStream(target));
                    int read;
                    byte[] buffer = new byte[1024 * 10];
                    while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, read);
                    }
                    bos.flush();
                }
                zis.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(zis, bos);
            }
        }
    }
}
