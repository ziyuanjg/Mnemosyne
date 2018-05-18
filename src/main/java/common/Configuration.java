package common;

import common.httpClient.HTTPClient;
import master.assign.AssignTaskThreadPool;
import master.assign.ChooseNode;
import master.assign.ChooseNode.DefaultChooseNode;
import master.receive.ReceiveTaskThreadPool;
import slave.ExecuteTaskThreadPool;
import slave.TaskService;
import task.TaskHandler;

/**
 * Created by Mr.Luo on 2018/5/9
 */
public class Configuration {

    private static HTTPClient httpClient;

    private static TaskHandler taskHandler;

    private static TaskService taskService;

    private static ChooseNode chooseNode = new DefaultChooseNode();

    private static AssignTaskThreadPool assignTaskThreadPool;

    private static ReceiveTaskThreadPool receiveTaskThreadPool;

    private static ExecuteTaskThreadPool executeTaskThreadPool;

    public static ReceiveTaskThreadPool getReceiveTaskThreadPool() {
        return receiveTaskThreadPool;
    }

    public static void setReceiveTaskThreadPool(ReceiveTaskThreadPool receiveTaskThreadPool) {
        Configuration.receiveTaskThreadPool = receiveTaskThreadPool;
    }

    public static AssignTaskThreadPool getAssignTaskThreadPool() {
        return assignTaskThreadPool;
    }

    public static void setAssignTaskThreadPool(AssignTaskThreadPool assignTaskThreadPool) {
        Configuration.assignTaskThreadPool = assignTaskThreadPool;
    }

    public static ChooseNode getChooseNode() {
        return chooseNode;
    }

    public static void setChooseNode(ChooseNode chooseNode) {
        Configuration.chooseNode = chooseNode;
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

    public static ExecuteTaskThreadPool getExecuteTaskThreadPool() {
        return executeTaskThreadPool;
    }

    public static void setExecuteTaskThreadPool(ExecuteTaskThreadPool executeTaskThreadPool) {
        Configuration.executeTaskThreadPool = executeTaskThreadPool;
    }

    public static HTTPClient getHttpClient() {
        return httpClient;
    }

    public static void setHttpClient(HTTPClient httpClient) {
        Configuration.httpClient = httpClient;
    }
}
