package common;

import common.httpClient.HTTPClient;
import election.ElectionService;
import master.assign.AssignHandler;
import master.assign.AssignTaskThreadPool;
import master.assign.LoadStrategy;
import master.assign.LoadStrategy.DefaultLoadStrategy;
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

    private static ElectionService electionService;

    private static LoadStrategy loadStrategy = new DefaultLoadStrategy();

    private static AssignTaskThreadPool assignTaskThreadPool;

    private static ReceiveTaskThreadPool receiveTaskThreadPool;

    private static ExecuteTaskThreadPool executeTaskThreadPool;

    private static AssignHandler assignHandler;

    public static ElectionService getElectionService() {
        return electionService;
    }

    public static void setElectionService(ElectionService electionService) {
        Configuration.electionService = electionService;
    }

    public static AssignHandler getAssignHandler() {
        return assignHandler;
    }

    public static void setAssignHandler(AssignHandler assignHandler) {
        Configuration.assignHandler = assignHandler;
    }

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


    public static LoadStrategy getLoadStrategy() {
        return loadStrategy;
    }

    public static void setLoadStrategy(LoadStrategy loadStrategy) {
        Configuration.loadStrategy = loadStrategy;
    }
}
