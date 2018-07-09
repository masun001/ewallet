package com.morningtech.eth.server.util;

import java.io.*;
import java.util.Properties;

/**
 * @author xuchunlin
 * @version V1.0
 * @Title: PropertyUtils
 * @Package com.hucheng.util
 * @Description: TODO
 * @date 2017/12/19 13:09
 */
public class PropertyUtils {

    public static Properties readRelative(String file) throws IOException {
        Properties prop = new Properties();
        //读取属性文件a.properties
        InputStream in =PropertyUtils.class.getResourceAsStream(file);
        prop.load(in);     ///加载属性列表
        in.close();
        return prop;
    }

    public static Properties read(String file) throws IOException {
        Properties prop = new Properties();
        //读取属性文件a.properties
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        prop.load(in);     ///加载属性列表
        in.close();
        return prop;
    }

    public static void write(String file, Properties properties){
        Properties prop = new Properties();
        try{
            ///保存属性到b.properties文件
            FileOutputStream oFile = new FileOutputStream(file, false);//true表示追加打开
            properties.store(oFile, "The New properties file");
            oFile.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }


    public static void main(String[] args) {
        Properties p=new Properties();
        p.put("lastBlockNumber","1323123123");
        PropertyUtils.write("D:/a.properties",p);
    }
}
