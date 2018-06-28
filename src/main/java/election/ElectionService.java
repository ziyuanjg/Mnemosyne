package election;

import com.alibaba.fastjson.JSON;
import common.BizResult;
import common.Configuration;
import common.httpClient.RequestTypeEnum;
import common.utils.EnumUtil;
import election.ElectionDTO.ElectionDTOBuilder;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Response;
import slave.SlaveNodeService;

/**
 * Created by Mr.Luo on 2018/5/23
 */
@Slf4j(topic = "election")
@Path("election")
public class ElectionService {


    private final String receiveMsgUrl = "election/receiveMsg";

    /**
     * 活跃节点列表
     */
    private final Set<ServiceNode> availableNodeSet = new HashSet<>();

    /**
     * 已校验的节点数
     */
    private final AtomicInteger checkNodeNum = new AtomicInteger(0);

    /**
     * 选举节点
     */
    private ServiceNode electionNode = null;

    /**
     * 投票节点
     */
    private ServiceNode electionMasterNode = null;

    /**
     * 节点投票信息
     */
    private Map<ServiceNode, ServiceNode> electionNodeMap;

    /**
     * 投票数
     */
    private Map<ServiceNode, AtomicInteger> electionNumMap;

    /**
     * 当前的节点状态
     */
    private ElectionStatusEnum electionStatus;

    /**
     * 投票轮数
     */
    private Integer voteNum;

    /**
     * 本轮接收到选票的节点数
     */
    private Integer voteNodeNum;

    /**
     * 每轮投票秒数
     */
    private final Long voteSecond = 5L * 1000 * 1000000;

    /**
     * 拒绝选举结果的节点数
     */
    private final Set<ServiceNode> refuseNodeSet = new HashSet<>();

    /**
     * 收到确认结果的节点数
     */
    private final AtomicInteger responseNum = new AtomicInteger(0);

    /**
     * 选举线程
     */
    private Thread electionThread;

    /**
     * 是否为支持选举状态
     */
    private Boolean supportLaunch = Boolean.FALSE;

    /**
     * 发起选举
     */
    public void launchElection() {

        electionStatus = ElectionStatusEnum.LAUNCH;

        sendLaunchMsg();

        while (checkNodeNum.get() < ElectionConfig.getServiceNodeList().size()) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        electionNodeMap = new HashMap<>(availableNodeSet.size());
        electionNumMap = new HashMap<>(availableNodeSet.size());

        for (ServiceNode serviceNode : availableNodeSet) {
            electionNumMap.put(serviceNode, new AtomicInteger(0));
        }
    }

    private void init() {
        electionNode = null;
        electionMasterNode = null;
        availableNodeSet.clear();
        checkNodeNum.set(0);
        voteNum = 0;
        electionStatus = null;
        electionNodeMap = null;
        electionNumMap = null;
        refuseNodeSet.clear();
        responseNum.set(0);
    }

    @Path("receiveMsg")
    @POST
    public synchronized BizResult<ElectionDTO> receiveMsg(ElectionDTO electionDTO,
            @Context HttpServletRequest request) {

        String url = request.getHeader("url");
        ServiceNode serviceNode = ElectionConfig.getServiceNodeByUrl(url);

        ElectionDTO.ElectionDTOBuilder builder = ElectionDTO.builder();
        switch (EnumUtil.findByCode(ElectionStatusEnum.class, electionDTO.getStatus())) {
            case LAUNCH:
                launchExecute(url, serviceNode, builder);
                break;
            case VOTE:
                voteExecute(electionDTO, url);
                break;
            case ELECTION:
                electionExecute(electionDTO, url);
                break;
            case FINISH:
                finishExecute(electionDTO, url);
                break;
            case COMPARE:
                compareExecute(builder);
                break;
            case FIND:
                findExecute(builder);
                break;
            case FINDRESULT:
                findResultExecute(electionDTO, builder);
                break;
            default:
                // 记录异常
                log.error("无法解析的选举消息类型,status:{}", electionDTO.getStatus());
                break;
        }
        return BizResult.createSuccessResult(builder.build());
    }

    /**
     * 确认本节点是否支持发起选举
     */
    private void findExecute(ElectionDTOBuilder builder) {

        // 返回结果的节点数
        AtomicInteger returnNodeNum = new AtomicInteger(0);
        // 同意选举的节点数
        AtomicInteger voteNodeNum = new AtomicInteger(0);
        // 收集集群中其他子节点的意见
        sendFindResultMsg(returnNodeNum, voteNodeNum);

        while (returnNodeNum.get() < (availableNodeSet.size() - 1)) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (voteNodeNum.get() > ((availableNodeSet.size() - 1) / 2)) {
            // 超过一半节点同意进行选举
            builder.voteElection(Boolean.TRUE);
            supportLaunch = Boolean.TRUE;
        } else {
            // 不到一半节点同意进行选举
            builder.voteElection(Boolean.FALSE);
            supportLaunch = Boolean.FALSE;
        }
    }

    /**
     * 确认本节点的主节点心跳情况
     */
    private void findResultExecute(ElectionDTO electionDTO, ElectionDTOBuilder builder) {

        // 返回本节点是否支持本次选举
        if(ElectionConfig.getMasterNode().equals(electionDTO.getVoteServiceNode()) && Configuration.getSlaveNodeService().getFailNum() == 0){
            builder.voteElection(Boolean.FALSE);
        }else{
            builder.voteElection(Boolean.TRUE);
        }
    }

    /**
     * 比较选举结果
     */
    private void compareExecute(ElectionDTOBuilder builder) {
        // 返回本节点的选举结果
        builder.voteServiceNode(electionMasterNode);
        log.info("本节点支持节点:{}", electionMasterNode.getUrl());
    }

    /**
     * 接收结束选举消息
     */
    private void finishExecute(ElectionDTO electionDTO, String url) {

        if (!electionNode.getUrl().equals(url)) {
            // 非主持节点发出的消息，不予执行
            log.error("非主持节点发出的结束消息,不予执行.url:{}", url);
        }

        electionStatus = ElectionStatusEnum.FINISH;

        if (!electionMasterNode.equals(electionDTO.getVoteServiceNode())) {
            refuseNodeSet.add(ElectionConfig.getLocalNode());
        }

        // 广播本节点是否支持主持节点的投票结果
        sendCompareMsg(electionMasterNode, url);
        log.info("本节点{}主持节点的选举结果", electionMasterNode.equals(electionDTO.getVoteServiceNode()) ? "支持" : "不支持");

        while (responseNum.get() < availableNodeSet.size()) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 如果超过一半节点拒绝本次选举结果则将本次的主持节点踢出集群（叛徒节点）。
        if (refuseNodeSet.size() > (availableNodeSet.size() / 2)) {

            ElectionConfig.setServiceNodeList(availableNodeSet);
            log.info("超过一半节点不支持本次选举结果,将主持节点{}踢出集群。", electionNode.getUrl());
            ElectionConfig.removeNode(electionNode);

            // 启动选举线程，发起新一轮选举
            startElection();

            return;
        }

        // 如果超过一半节点支持本次选举结果则成功选举出master。
        ElectionConfig.setMasterNode(electionDTO.getVoteServiceNode());
        log.info("成功选举出master节点:{}", electionDTO.getVoteServiceNode().getUrl());

        init();

        ElectionConfig.setServiceNodeList(availableNodeSet);
        if (ElectionConfig.getLocalNode().equals(electionDTO.getVoteServiceNode())) {
            Configuration.getSlaveNodeService().stopSlaveService();
            Configuration.getMasterNodeService().startHeartThread();
        } else {
            Configuration.getSlaveNodeService().startSlaveService();
        }

    }

    /**
     * 开启选举线程
     */
    public void startElection() {
        if (electionThread == null) {
            Thread thread = new Thread(new ElectionThread());
            electionThread = thread;
            thread.start();
        }
    }

    /**
     * 接收发起选举消息
     */
    private void launchExecute(String url, ServiceNode serviceNode, ElectionDTOBuilder builder) {

        if (electionStatus == null) {
            log.info("收到发起选举信息,进入选举状态");
            init();
            // 允许进入选举状态
            electionStatus = ElectionStatusEnum.LAUNCH;
            // 记录选举执行节点
            electionNode = serviceNode;
        } else if (ElectionStatusEnum.LAUNCH.equals(electionStatus)) {
            if (electionNode.getUrl().hashCode() < url.hashCode()) {
                log.info("接收到更高优先级节点发出的发起选举信息,将主持节点从{}更新为{}", electionNode.getUrl(), serviceNode.getUrl());
                // 如果出现多个节点同时发起选举，则根据hash选择一个节点作为选举执行节点
                electionNode = serviceNode;
                builder.serviceNode(electionNode);
            }
        } else if (ElectionStatusEnum.FINISH.equals(electionStatus)) {
            // 完成阶段如出现了重新投票的请求，则需根据本机收到的反对节点数是否超过一半决定是否同意重新选举
            if (refuseNodeSet.size() > (availableNodeSet.size() - 1) / 2) {
                log.info("接收到重新发起选举的请求,且超过一半节点同意,将上次的主持节点踢出集群,重新选举主节点");
                // 如果重新执行选举，则上次的主持节点被提出集群
                availableNodeSet.remove(electionNode);
                init();
                // 允许进入选举状态
                electionStatus = ElectionStatusEnum.LAUNCH;
                // 记录选举执行节点
                electionNode = serviceNode;
            }
        }
    }

    /**
     * 开始投票消息
     */
    private void voteExecute(ElectionDTO electionDTO, String url) {

        if (!electionNode.getUrl().equals(url)) {
            log.error("非主持节点发出的开发投票消息,不予执行,发出节点为:{}", url);
            return;
        }

        switch (electionStatus) {
            case LAUNCH:
                electionStatus = ElectionStatusEnum.VOTE;
                break;
            case VOTE:
                break;
            default:
                // 错误状态
                return;
        }

        voteNum = electionDTO.getVoteNum();

        if (electionMasterNode == null) {
            electionMasterNode = ElectionConfig.getLocalNode();
        }

        log.info("本次投票给{}节点", electionNode.getUrl());
        // 发送投票消息
        sendElectionMsg();
    }


    /**
     * 接收投票消息
     */
    private void electionExecute(ElectionDTO electionDTO, String url) {

        switch (electionStatus) {
            case LAUNCH:
                electionStatus = ElectionStatusEnum.VOTE;
                break;
            case VOTE:
                break;
            default:
                log.error("本节点状态:{},不支持接收投票消息", electionStatus);
                return;
        }

        if (!voteNum.equals(electionDTO.getVoteNum())) {
            log.info("投票轮数错误,可能是过期消息,本节点轮数:{},接收消息轮数:{}", voteNum, electionDTO.getVoteNum());
            return;
        }

        // 消息发出者
        ServiceNode requestNode = ElectionConfig.getServiceNodeByUrl(url);

        log.info("接收到{}节点投票给{}", requestNode.getUrl(), electionDTO.getVoteServiceNode().getUrl());

        if (electionNodeMap.get(requestNode) != null) {
            electionNumMap.get(electionNodeMap.get(requestNode)).getAndDecrement();
            electionNumMap.get(electionDTO.getVoteServiceNode()).incrementAndGet();
            electionNodeMap.put(requestNode, electionDTO.getVoteServiceNode());
        } else {
            electionNodeMap.put(requestNode, electionDTO.getVoteServiceNode());
            electionNumMap.get(electionDTO.getVoteServiceNode()).incrementAndGet();
        }

        // 判定本节点是否需要更换投票对象
        ServiceNode newElectionNode = updateElectionMasterNode();

        // 修改支持节点
        if (!electionMasterNode.equals(newElectionNode)) {
            log.info("将支持节点{}修改为{}", electionMasterNode.getUrl(), newElectionNode.getUrl());
            electionMasterNode = newElectionNode;
        }
    }

    /**
     * 选举新的投票节点
     */
    private ServiceNode updateElectionMasterNode() {
        ServiceNode newElectionNode = null;
        Integer count = 0;
        for (Entry<ServiceNode, AtomicInteger> entry : electionNumMap.entrySet()) {
            if (newElectionNode == null) {
                newElectionNode = entry.getKey();
                count = entry.getValue().get();
                continue;
            }

            if (count == entry.getValue().get()) {
                if (newElectionNode.getUrl().hashCode() < entry.getKey().getUrl().hashCode()) {
                    newElectionNode = entry.getKey();
                    count = entry.getValue().get();
                    continue;
                }
            }

            if (count < entry.getValue().get()) {
                newElectionNode = entry.getKey();
                count = entry.getValue().get();
            }
        }
        return newElectionNode;
    }

    /**
     * 发送选举消息
     */
    private void sendElectionMsg(ServiceNode serviceNode, ElectionDTO body, Callback callback) {

        Headers headers = new Headers.Builder().add("url", ElectionConfig.getLocalNode().getUrl()).build();
        Configuration.getHttpClient().sendWithCallBack(
                serviceNode.getUrl() + receiveMsgUrl,
                headers,
                body,
                RequestTypeEnum.POST,
                callback);
    }

    /**
     * 发送发起选举消息
     */
    private void sendLaunchMsg() {
        Set<ServiceNode> serviceNodes = ElectionConfig.getServiceNodeList();

        serviceNodes.forEach(serviceNode -> {
            if (!serviceNode.equals(ElectionConfig.getLocalNode())) {
                sendElectionMsg(serviceNode,
                        ElectionDTO.builder().
                                status(ElectionStatusEnum.LAUNCH.getCode()).
                                build(),
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {

                                // 打印异常日志
                                log.error("发送选举信息失败,节点:{}", call.request().header("url"), e);
                                checkNodeNum.incrementAndGet();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                                checkNodeNum.incrementAndGet();

                                if (response.isSuccessful()) {
                                    availableNodeSet
                                            .add(ElectionConfig.getServiceNodeByUrl(response.request().header("url")));
                                    ElectionDTO electionDTO = JSON
                                            .parseObject(response.body().string(), ElectionDTO.class);
                                    if (electionDTO.getServiceNode() != null
                                            && electionDTO.getServiceNode().getUrl().hashCode() > ElectionConfig
                                            .getLocalNode().getUrl().hashCode()) {
                                        // 有其他优先级更高的节点发起了选举，停止本地选举线程，让出主持节点位置
                                        log.info("存在其他优先级更高的节点发起了选举,终止本节点的主持线程.新主持节点为:{}",
                                                electionDTO.getServiceNode().getUrl());
                                        electionThread.stop();
                                    }
                                } else {
                                    log.error("发送选举信息失败,错误信息:{}", response.body().string());
                                }
                            }
                        });
            }
        });
    }

    /**
     * 发送开始投票消息
     */
    private void sendVoteMsg() {

        voteNodeNum = 0;

        if (voteNum == null) {
            voteNum = 1;
        } else {
            voteNum++;
        }

        availableNodeSet.forEach(serviceNode ->
                sendElectionMsg(serviceNode,
                        ElectionDTO.builder()
                                .status(ElectionStatusEnum.VOTE.getCode())
                                .voteNum(voteNum)
                                .build(),
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                voteNodeNum++;
                                log.error("发送开始投票信息失败,节点:{}", call.request().header("url"), e);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                voteNodeNum++;
                            }
                        }
                )
        );
    }

    /**
     * 发送投票消息
     */
    private void sendElectionMsg() {

        ServiceNode tmpNode = electionMasterNode;
        availableNodeSet.forEach(serviceNode -> {
                    if (!serviceNode.equals(ElectionConfig.getLocalNode())) {
                        sendElectionMsg(serviceNode,
                                ElectionDTO.builder().status(ElectionStatusEnum.ELECTION.getCode())
                                        .voteServiceNode(tmpNode)
                                        .voteNum(voteNum)
                                        .build(),
                                new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        log.error("发送投票信息失败,节点:{}", call.request().header("url"), e);
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {

                                    }
                                });
                    }
                }
        );
    }

    /**
     * 发送选举结果消息
     */
    private void sendFinishMsg() {

        availableNodeSet.forEach(serviceNode ->
                sendElectionMsg(serviceNode,
                        ElectionDTO.builder().status(ElectionStatusEnum.FINISH.getCode())
                                .voteServiceNode(electionMasterNode)
                                .build(),
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                log.error("发送选举结果失败,节点:{}", call.request().header("url"), e);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                            }
                        })
        );
    }

    /**
     * 发送确认结果
     */
    private void sendCompareMsg(ServiceNode masterNode, String url) {

        availableNodeSet.forEach(serviceNode -> {
                    if (!serviceNode.equals(ElectionConfig.getLocalNode())) {
                        sendElectionMsg(serviceNode,
                                ElectionDTO.builder().status(ElectionStatusEnum.COMPARE.getCode())
                                        .voteServiceNode(electionMasterNode)
                                        .build(),
                                new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        log.error("发送确认结果失败,节点:{}", call.request().header("url"), e);
                                        responseNum.incrementAndGet();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        responseNum.incrementAndGet();
                                        // 如果结果与选举结果不同则增加记录，如超过一半节点不认可主持节点的选举结果，则将主持节点踢出集群并重新选举
                                        ElectionDTO electionDTO = JSON.parseObject(response.body().string(), ElectionDTO.class);
                                        if (!electionDTO.getVoteServiceNode().equals(masterNode)) {
                                            refuseNodeSet.add(ElectionConfig.getServiceNodeByUrl(url));
                                        }
                                    }
                                });
                    }
                }
        );
    }

    /**
     * 发送确认是否支持选举消息
     */
    private void sendFindMsg(AtomicInteger findFailNodeNum, AtomicInteger returnNodeNum) {

        availableNodeSet.forEach(serviceNode -> {
                    if (!serviceNode.equals(ElectionConfig.getLocalNode()) && !serviceNode
                            .equals(ElectionConfig.getMasterNode())) {
                        sendElectionMsg(serviceNode,
                                ElectionDTO.builder().status(ElectionStatusEnum.FIND.getCode())
                                        .build(),
                                new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        log.error("发送确认是否支持选举失败,节点:{}", call.request().header("url"), e);
                                        returnNodeNum.incrementAndGet();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        ElectionDTO electionDTO = JSON.parseObject(response.body().string(), ElectionDTO.class);
                                        if (electionDTO.getStatus() != null && ElectionStatusEnum.CANCEL
                                                .equals(electionDTO.getStatus())) {
                                            findFailNodeNum.incrementAndGet();
                                        }
                                        returnNodeNum.incrementAndGet();
                                    }
                                });
                    }
                }
        );
    }

    /**
     * 发送确认主节点信息消息
     */
    private void sendFindResultMsg(AtomicInteger returnNodeNum, AtomicInteger voteNodeNum) {

        availableNodeSet.forEach(serviceNode -> {
                    if (!serviceNode.equals(ElectionConfig.getLocalNode())) {
                        sendElectionMsg(serviceNode,
                                ElectionDTO.builder().status(ElectionStatusEnum.FINDRESULT.getCode())
                                        .voteServiceNode(electionMasterNode)
                                        .build(),
                                new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        log.error("发送确认主节点信息消息失败,节点:{}", call.request().header("url"), e);
                                        returnNodeNum.incrementAndGet();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        returnNodeNum.incrementAndGet();
                                        ElectionDTO electionDTO = JSON.parseObject(response.body().string(), ElectionDTO.class);
                                        if (electionDTO.getVoteElection()) {
                                            voteNodeNum.incrementAndGet();
                                        }
                                    }
                                });
                    }
                }
        );
    }

    /**
     * 检查是否有节点已获得超过一半选票
     */
    private Boolean checkElectionNumMap() {

        if (electionNumMap == null || electionNumMap.isEmpty()) {
            return Boolean.FALSE;
        }

        for (AtomicInteger num : electionNumMap.values()) {
            if (num.get() > ((electionNumMap.size() - 1) / 2)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    /**
     * 开始投票阶段
     */
    private void startVote() {

        // 开始投票时间
        Long startTime = System.nanoTime();
        sendVoteMsg();

        // 当收到全部节点的选票或者超过了本轮投票时间才会结束本次投票阶段，进行检查是否结束选举
        while ((availableNodeSet.size() - 1) > voteNodeNum && (System.nanoTime() - startTime) < voteSecond) {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 选举执行者
     */
    class ElectionThread implements Runnable {

        @Override
        public void run() {

            log.info("请求发起选举");
            if (!checkSlaveNodeStatus()) {
                return;
            }

            log.info("发起选举");
            // 初始化
            init();

            // 发起选举
            launchElection();

            // 发起投票，当不存在得票超过一半的节点时，无限投票，每轮投票等待所有节点返回结果时才会结束。
            while (!checkElectionNumMap()) {
                startVote();
            }

            // 当有节点得票超过一半时，推送完成投票消息
            sendFinishMsg();
        }

        /**
         * 检查集群中所有节点的状态，收集是否需要重新选举的信息
         */
        private Boolean checkSlaveNodeStatus() {
            AtomicInteger findFailNodeNum = new AtomicInteger(0);
            AtomicInteger returnNodeNum = new AtomicInteger(0);
            sendFindMsg(findFailNodeNum, returnNodeNum);

            while (returnNodeNum.get() < (ElectionConfig.getServiceNodeList().size() - 2)) {
                try {
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (findFailNodeNum.get() <= ((ElectionConfig.getServiceNodeList().size() - 1) / 2)) {
                Configuration.getSlaveNodeService().startSlaveService();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
    }
}
