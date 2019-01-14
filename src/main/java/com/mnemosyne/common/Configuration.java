package com.mnemosyne.common;

import com.mnemosyne.common.httpClient.HTTPClient;
import com.mnemosyne.election.ElectionService;
import com.mnemosyne.master.MasterNodeService;
import com.mnemosyne.master.assign.AssignHandler;
import com.mnemosyne.master.assign.AssignTaskThreadPool;
import com.mnemosyne.master.assign.LoadStrategy;
import com.mnemosyne.master.assign.LoadStrategy.DefaultLoadStrategy;
import com.mnemosyne.master.receive.ReceiveTaskThreadPool;
import com.mnemosyne.slave.ExecuteTaskThreadPool;
import com.mnemosyne.slave.SlaveNodeService;
import com.mnemosyne.task.TaskHandler;

/**
 * Created by Mr.Luo on 2018/5/9
 */
public class Configuration {

    private static HTTPClient httpClient;

    private static TaskHandler taskHandler;

    private static ElectionService electionService;

    private static SlaveNodeService slaveNodeService;

    private static MasterNodeService masterNodeService;

    private static LoadStrategy loadStrategy = new DefaultLoadStrategy();

    private static AssignTaskThreadPool assignTaskThreadPool;

    private static ReceiveTaskThreadPool receiveTaskThreadPool;

    private static ExecuteTaskThreadPool executeTaskThreadPool;

    private static AssignHandler assignHandler;

    public Configuration() {

    }

    public static SlaveNodeService getSlaveNodeService() {
        return slaveNodeService;
    }

    public static void setSlaveNodeService(SlaveNodeService slaveNodeService) {
        Configuration.slaveNodeService = slaveNodeService;
    }

    public static MasterNodeService getMasterNodeService() {
        return masterNodeService;
    }

    public static void setMasterNodeService(MasterNodeService masterNodeService) {
        Configuration.masterNodeService = masterNodeService;
    }

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
