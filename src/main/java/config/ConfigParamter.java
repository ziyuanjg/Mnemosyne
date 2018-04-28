package config;

import lombok.Builder;
import timeWheel.task.SaveTypeEnum;

/**
 * Created by 希罗 on 2018/4/28
 */
@Builder
public class ConfigParamter {

    /**
     * 持久化方式
     */
    private static SaveTypeEnum saveTypeEnum;

    /**
     * 磁盘持久化目录
     */
    private static String filePath;


    public static SaveTypeEnum getSaveTypeEnum() {
        return saveTypeEnum;
    }

    public static void setSaveTypeEnum(SaveTypeEnum saveTypeEnum) {
        ConfigParamter.saveTypeEnum = saveTypeEnum;
    }

    public static String getFilePath() {
        return filePath;
    }

    public static void setFilePath(String filePath) {
        if(!filePath.endsWith("/")){
            filePath = filePath + "/";
        }
        ConfigParamter.filePath = filePath;
    }
}
