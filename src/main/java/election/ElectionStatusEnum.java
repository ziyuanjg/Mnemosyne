package election;

import common.EnumInterface;

/**
 * Created by Mr.Luo on 2018/5/23
 */
public enum ElectionStatusEnum implements EnumInterface {

    LAUNCH(0, "发起选举"),
    VOTE(1, "发起投票"),
    ELECTION(2, "选举"),
    FINISH(3, "完成投票"),
    COMPARE(4, "同步投票结果"),
    FIND(5, "搜寻其他丢失主节点的子节点"),
    FINDRESULT(6, "确认其他节点是否支持主节点离线"),
    CANCEL(7, "取消选举")

    ;

    private Integer code;
    private String message;

    ElectionStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
