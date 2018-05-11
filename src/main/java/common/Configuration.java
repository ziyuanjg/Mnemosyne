package common;

import common.httpClient.HTTPClient;
import master.MasterConfig;
import slave.SlaveConfig;
import slave.TaskService;
import slave.TaskThreadPool;
import task.SaveConfig;
import task.TaskHandler;

/**
 * Created by Mr.Luo on 2018/5/9
 */
public class Configuration {

    private static SlaveConfig slaveConfig;

    private static SaveConfig saveConfig;

    private static MasterConfig masterConfig;

    private static HTTPClient httpClient;

    private static TaskThreadPool taskThreadPool;

    private static TaskHandler taskHandler;

    private static TaskService taskService;

    public static MasterConfig getMasterConfig() {
        return masterConfig;
    }

    public static void setMasterConfig(MasterConfig masterConfig) {
        Configuration.masterConfig = masterConfig;
    }

    public static TaskService getTaskService() {
        return taskService;
    }

    public static void setTaskService(TaskService taskService) {
        Configuration.taskService = taskService;
    }

    public static TaskHandler getTaskHandler() {
        return taskHandler;
    }

    public static void setTaskHandler(TaskHandler taskHandler) {
        Configuration.taskHandler = taskHandler;
    }

    public static TaskThreadPool getTaskThreadPool() {
        return taskThreadPool;
    }

    public static void setTaskThreadPool(TaskThreadPool taskThreadPool) {
        Configuration.taskThreadPool = taskThreadPool;
    }

    public static SlaveConfig getSlaveConfig() {
        return slaveConfig;
    }

    public static void setSlaveConfig(SlaveConfig slaveConfig) {
        Configuration.slaveConfig = slaveConfig;
    }

    public static SaveConfig getSaveConfig() {
        return saveConfig;
    }

    public static void setSaveConfig(SaveConfig saveConfig) {
        Configuration.saveConfig = saveConfig;
    }

    public static HTTPClient getHttpClient() {
        return httpClient;
    }

    public static void setHttpClient(HTTPClient httpClient) {
        Configuration.httpClient = httpClient;
    }
}
