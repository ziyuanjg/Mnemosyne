package com.mnemosyne.election;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;

/**
 * Created by Mr.Luo on 2018/5/10
 */
@Builder
public class ElectionConfig {

    /**
     * 从节点列表
     */
    private static Set<ServiceNode> serviceNodeList = new HashSet<>();

    /**
     * 主节点
     */
    private static ServiceNode masterNode;

    /**
     * 本机节点
     */
    private static ServiceNode localNode;

    public static ServiceNode getLocalNode() {
        return localNode;
    }

    public static void setLocalNode(ServiceNode localNode) {
        ElectionConfig.localNode = localNode;
    }

    public static Set<ServiceNode> getServiceNodeList() {
        return serviceNodeList;
    }

    public static void setServiceNodeList(Set<ServiceNode> serviceNodeList) {
        ElectionConfig.serviceNodeList = serviceNodeList;
    }

    public static ServiceNode getMasterNode() {
        return masterNode;
    }

    public static void setMasterNode(ServiceNode masterNode) {
        ElectionConfig.masterNode = masterNode;
    }

    public static void addServiceNode(ServiceNode serviceNode){
        serviceNodeList.add(serviceNode);
    }

    public static void removeServiceNode(ServiceNode serviceNode){
        serviceNodeList.remove(serviceNode);
    }

    public static ServiceNode getServiceNodeByUrl(String url){
        for (ServiceNode serviceNode : serviceNodeList) {
            if(serviceNode.getUrl().equals(url)){
                return serviceNode;
            }
        }
        return null;
    }

    public static Boolean removeNode(ServiceNode serviceNode){
        return serviceNodeList.remove(serviceNode);
    }
}
