package com.aicrm.common;

/**
 * 商机阶段常量（数据库 crm_opportunity.stage 的取值）。
 * 用常量类而不是到处写魔法数字：改一处、不容易敲错、好读。
 */
public final class StageConstants {

    /** 1 初步接触 */
    public static final int CONTACT = 1;
    /** 2 需求确认 */
    public static final int CONFIRM = 2;
    /** 3 方案报价 */
    public static final int QUOTE = 3;
    /** 4 商务谈判 */
    public static final int NEGOTIATE = 4;
    /** 5 赢单（终态） */
    public static final int WIN = 5;
    /** 6 输单（终态） */
    public static final int LOSE = 6;

    private StageConstants() {
    }

    /** 是否终态（赢单/输单）。终态商机不能再变更阶段。 */
    public static boolean isEnd(int stage) {
        return stage == WIN || stage == LOSE;
    }

    /** 阶段中文名，用于展示与"商机阶段变更"系统跟进记录 */
    public static String name(int stage) {
        return switch (stage) {
            case CONTACT -> "初步接触";
            case CONFIRM -> "需求确认";
            case QUOTE -> "方案报价";
            case NEGOTIATE -> "商务谈判";
            case WIN -> "赢单";
            case LOSE -> "输单";
            default -> "未知";
        };
    }
}
