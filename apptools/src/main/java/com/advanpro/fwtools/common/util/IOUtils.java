package com.advanpro.fwtools.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class IOUtils {
    /**
     * 将输入流数据写入输出流中
     * @param in 输入流
     * @param out 输出流
     * @throws IOException
     */
    public static void inToOut(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len=in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    /**
     * 关闭一个或多个流对象
     * @param closeables 可关闭的流对象列表
     */
    public static void close(Closeable... closeables) throws IOException {
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        }
    }

    /**
     * 关闭一个或多个流对象
     * @param closeables 可关闭的流对象列表
     */
    public static void closeQuietly(Closeable... closeables) {
        try {
            close(closeables);
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * 从输入流中获取字符串
     * @param in 输入流
     * @param enc 返回的字符串采用的字符集, 如果为null则使用平台默认的字符集
     * @throws IOException
     */
    public static String getString(InputStream in, String enc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len=in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        if (enc == null) {
            return out.toString();
        }
        return out.toString(enc);
    }

    /**
     * 从输入流中获取符合指定正则表达式的字符串
     * @param in 输入流
     * @param regex 用来筛选字符串的正则表达式
     * @param enc 用来解析输入流数据的字符集
     * @return 筛选反的字符串集合
     * @throws IOException
     */
    public static List<String> getStrings(InputStream in, String regex, String enc) throws IOException {
        List<String> strs = new ArrayList<>();
        BufferedReader bfIn;
        if (enc == null) {
            bfIn = new BufferedReader(new InputStreamReader(in));
        } else {
            bfIn = new BufferedReader(new InputStreamReader(in, enc));
        }
        String line;
        Pattern p = Pattern.compile(regex);
        while ((line=bfIn.readLine()) != null) {
            Matcher m = p.matcher(line);
            while (m.find()) {
                strs.add(m.group());
            }
        }
        return strs;
    }
}

