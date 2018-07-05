package com.mnemosyne.task.disk;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件锁 Created by Mr.Luo on 2018/4/28
 */
public class FileLockHandler {


    /**
     * 文件锁
     */
    private static Map<String, Object> fileLockMap = new HashMap();

    /**
     * 获取文件锁
     */
    public static Boolean getFileLock(String fileName) {

        Object lock = fileLockMap.putIfAbsent(fileName, new Object());

        if (lock != null) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    /**
     * 释放文件锁
     */
    public static void releaseFileLock(String fileName) {

        fileLockMap.remove(fileName);
    }

}
