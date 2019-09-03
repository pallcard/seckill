package cn.wishhust.enums;

/**
 *
 * 使用枚举类将常量枚举类封装起来，方便重复利用，也易于维护。
 */
public enum SeckillStatEnum {
    SUCCESS(1,"秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1,"你已秒杀成功，不能重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATA_REWRITE(-3,"数据篡改");
    private int state;
    private String stateInfo;

    SeckillStatEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public static SeckillStatEnum stateOf(int index) {
        for(SeckillStatEnum state: values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }
}
