package election;

import common.EnumInterface;

/**
 * Created by Mr.Luo on 2018/5/23
 */
public enum ElectionStatusEnum implements EnumInterface {

    LAUNCH(0, "发起选举"),
    VOTE(1, "投票阶段"),
    ELECTION(2, "选举阶段");

    private Integer code;
    private String message;

    ElectionStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    @Override
    public Integer getCode() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }
}
