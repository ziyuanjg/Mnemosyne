package com.mnemosyne.election;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Mr.Luo on 2018/5/30
 */
@Data
@Builder
public class ElectionDTO {

    /**
     * 选举状态
     *
     * @see ElectionStatusEnum
     */
    private Integer status;

    /**
     * 选举执行者节点
     */
    private ServiceNode serviceNode;

    /**
     * 投票节点
     */
    private ServiceNode voteServiceNode;

    /**
     * 投票轮数
     */
    private Integer voteNum;

    /**
     * 是否支持开始选举
     */
    private Boolean voteElection;


}
