package com.tlang.xsjc.jkdk.utils;

public class StrUtils {
    /**
     * 对象转字符串输出
     * @param object
     * @return
     */
    public static String  objToStr(Object object){
        return object == null ? "":object.toString();
    }
}
