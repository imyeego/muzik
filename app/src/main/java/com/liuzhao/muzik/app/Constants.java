package com.liuzhao.muzik.app;

import android.os.Environment;

import java.io.File;

/**
 *
 * @author liuzhao
 */
public class Constants {
    public static String APP_NAME = "muzik";
    public static String README = "readme.txt";

    //聚合数据基地址
    public static String JUHE_BASEURL = "http://v.juhe.cn";

    //聚合数据API_KEY
    public static String JUHE_APIKEY = "54fa4938258096bf67c09099e89acf3d";

    //豆瓣top250 api
    public static String DOUBAN_TOP250 = "https://api.douban.com/v2/movie/";

    public static final String MY_URL = "http://192.168.43.114:8088/";

    public static final String PATH = Environment.getExternalStorageDirectory() + File.separator + APP_NAME;

    public static final String DATA_PATH = Constants.PATH + File.separator + "school.db";



}
