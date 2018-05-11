package electon;

import java.util.List;
import lombok.Builder;

/**
 * Created by Mr.Luo on 2018/5/10
 */
@Builder
public class ElectonConfig {

    /**
     * 从节点列表
     */
    private static List<ServiceNode> serviceNodeList;

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
        ElectonConfig.localNode = localNode;
    }

    public static List<ServiceNode> getServiceNodeList() {
        return serviceNodeList;
    }

    public static void setServiceNodeList(List<ServiceNode> serviceNodeList) {
        ElectonConfig.serviceNodeList = serviceNodeList;
    }

    public static ServiceNode getMasterNode() {
        return masterNode;
    }

    public static void setMasterNode(ServiceNode masterNode) {
        ElectonConfig.masterNode = masterNode;
    }
}
