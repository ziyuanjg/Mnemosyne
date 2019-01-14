package com.mnemosyne.common;

import cn.hutool.setting.dialect.Props;
import com.mnemosyne.common.httpClient.OkHTTPClient;
import com.mnemosyne.common.utils.EnumUtil;
import com.mnemosyne.common.utils.SystemUtil;
import com.mnemosyne.election.ElectionConfig;
import com.mnemosyne.election.ElectionService;
import com.mnemosyne.election.ServiceNode;
import com.mnemosyne.master.MasterConfig;
import com.mnemosyne.master.MasterNodeService;
import com.mnemosyne.master.assign.AssignHandler;
import com.mnemosyne.master.assign.AssignTaskThreadPool;
import com.mnemosyne.master.receive.ReceiveTaskThreadPool;
import com.mnemosyne.slave.ExecuteTaskThreadPool;
import com.mnemosyne.slave.SlaveConfig;
import com.mnemosyne.slave.SlaveNodeService;
import com.mnemosyne.task.SaveConfig;
import com.mnemosyne.task.SaveTypeEnum;
import com.mnemosyne.task.disk.DiskTaskHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Mr.Luo on 2018/5/22
 */
@Slf4j
public class MnemosyneProperties {

    /**
     * 读取配置文件
     */
    private final Props props = new Props("mnemosyne.properties");

    /**
     * 初始化上下文
     */
    public void initContext() {

        // httpClient
        Configuration.setHttpClient(new OkHTTPClient());

        // 持久化配置
        saveConfig();
        log.info("持久化配置完毕");

        // 子节点配置
        slaveConfig();
        log.info("子节点配置完毕");

        // 主节点配置
        masterConfig();
        log.info("主节点配置完毕");

        // 选举配置
        Configuration.setElectionService(new ElectionService());
    }

    /**
     * 启动mnemosyne
     */
    public void start() {

        // 将本节点加入节点集合
        ServiceNode localhostNode = new ServiceNode(SystemUtil.getLocalHostLANAddress().getHostAddress());
        ElectionConfig.addServiceNode(localhostNode);
        ElectionConfig.setLocalNode(localhostNode);

        String masterUrl = props.getStr("com.mnemosyne.master.url");
        if (masterUrl == null) {

            log.info("本节点担任主节点");
            // 本节点担任主节点职责
            ElectionConfig.getLocalNode().setIsMaster(Boolean.TRUE);
            ElectionConfig.setMasterNode(ElectionConfig.getLocalNode());

            // 开启任务接收模块
            Configuration.setReceiveTaskThreadPool(new ReceiveTaskThreadPool());
            // 开启任务分配模块
            Configuration.setAssignTaskThreadPool(new AssignTaskThreadPool());
            // 开启主节点主线程
            Configuration.getMasterNodeService().startHeartThread();
            // 开启分配任务线程
            Configuration.getAssignHandler().startAssignThread();

            log.info("主节点线程池开启完毕");
        } else {

            log.info("本节点担任子节点");
            // 本节点担任子节点职责
            ServiceNode masterNode = new ServiceNode(masterUrl);
            ElectionConfig.setMasterNode(masterNode);

            // 开启子节点主线程
            Configuration.getSlaveNodeService().startSlaveService();
            log.info("子节点线程池开启完毕");
        }
    }

    /**
     * 主节点配置
     */
    private void masterConfig() {
        Integer assignCorePoolSize = props.getInt("master.assignCorePoolSize");
        if (assignCorePoolSize != null) {
            MasterConfig.setAssignCorePoolSize(assignCorePoolSize);
        }

        Integer assignMaxPoolSize = props.getInt("master.assignMaxPoolSize");
        if (assignMaxPoolSize != null) {
            MasterConfig.setAssignMaxPoolSize(assignMaxPoolSize);
        }

        Long assignKeepAliveTime = props.getLong("master.assignKeepAliveTime");
        if (assignKeepAliveTime != null) {
            MasterConfig.setAssignKeepAliveTime(assignKeepAliveTime);
        }

        Integer receveCorePoolSize = props.getInt("master.receveCorePoolSize");
        if (assignCorePoolSize != null) {
            MasterConfig.setReceveCorePoolSize(receveCorePoolSize);
        }

        Integer receveMaxPoolSize = props.getInt("master.receveMaxPoolSize");
        if (assignMaxPoolSize != null) {
            MasterConfig.setReceveMaxPoolSize(receveMaxPoolSize);
        }

        Long receveKeepAliveTime = props.getLong("master.receveKeepAliveTime");
        if (assignKeepAliveTime != null) {
            MasterConfig.setReceveKeepAliveTime(receveKeepAliveTime);
        }

        Configuration.setAssignHandler(new AssignHandler());

        Configuration.setMasterNodeService(new MasterNodeService());

        String loadStrategy = props.getStr("com.mnemosyne.master.loadStrategy");
        if (loadStrategy != null) {
            // TODO 分配策略
        }
    }

    /**
     * 子节点配置
     */
    private void slaveConfig() {
        Integer corePoolSize = props.getInt("slave.corePoolSize");
        if (corePoolSize != null) {
            SlaveConfig.setCorePoolSize(corePoolSize);
        }

        Integer maxPoolSize = props.getInt("slave.maxPoolSize");
        if (maxPoolSize != null) {
            SlaveConfig.setMaxPoolSize(maxPoolSize);
        }

        Long keepAliveTime = props.getLong("slave.keepAliveTime");
        if (keepAliveTime != null) {
            SlaveConfig.setKeepAliveTime(keepAliveTime);
        }

        Long heartTime = props.getLong("slave.heartTime");
        if (heartTime != null) {
            SlaveConfig.setHeartTime(heartTime);
        }

        Integer failMaxNum = props.getInt("slave.failMaxNum");
        if (failMaxNum != null) {
            SlaveConfig.setFailMaxNum(failMaxNum);
        }

        Configuration.setExecuteTaskThreadPool(new ExecuteTaskThreadPool());

        Configuration.setSlaveNodeService(new SlaveNodeService());
    }

    /**
     * 持久化配置
     */
    private void saveConfig() {
        String saveType = props.getStr("save.type");

        switch (EnumUtil.findByMsg(SaveTypeEnum.class, saveType)) {
            case DISK:

                String filePath = props.getStr("save.filePath");
                if (filePath != null) {
                    SaveConfig.setFilePath(filePath);
                }

                Integer corePoolSize = props.getInt("save.corePoolSize");
                if (corePoolSize != null) {
                    SaveConfig.setCorePoolSize(corePoolSize);
                }

                Integer maxPoolSize = props.getInt("save.maxPoolSize");
                if (maxPoolSize != null) {
                    SaveConfig.setMaxPoolSize(maxPoolSize);
                }

                Long keepAliveTime = props.getLong("save.keepAliveTime");
                if (keepAliveTime != null) {
                    SaveConfig.setKeepAliveTime(keepAliveTime);
                }

                Integer taskMAXLength = props.getInt("save.taskMAXLength");
                if (taskMAXLength != null) {
                    SaveConfig.setTaskMAXLength(taskMAXLength);
                }

                Integer taskNumOfPartition = props.getInt("save.taskNumOfPartition");
                if (taskNumOfPartition != null) {
                    SaveConfig.setTaskNumOfPartition(taskNumOfPartition);
                }

                // 任务处理器
                Configuration.setTaskHandler(new DiskTaskHandler());
                break;
            case MYSQL:
                break;
            case REDIS:
                break;
            default:
                break;
        }
    }


}
