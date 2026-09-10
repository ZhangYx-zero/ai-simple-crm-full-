package com.aicrm.mapper;

import com.aicrm.entity.CrmOpportunity;
import com.aicrm.vo.FunnelRow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商机 Mapper。
 * 漏斗是手写聚合 SQL：注意必须手动加 deleted=0 ——
 * @TableLogic 只作用于 MyBatis-Plus 自动生成的 SQL，对手写 @Select 不生效。
 */
public interface CrmOpportunityMapper extends BaseMapper<CrmOpportunity> {

    /** 全部商机的漏斗统计（管理员/主管用） */
    @Select("SELECT stage AS stage, COUNT(*) AS cnt, COALESCE(SUM(amount), 0) AS total_amount " +
            "FROM crm_opportunity WHERE deleted = 0 " +
            "GROUP BY stage ORDER BY stage")
    List<FunnelRow> funnelAll();

    /** 某销售名下商机的漏斗统计（普通销售用，强制 owner_id 过滤） */
    @Select("SELECT stage AS stage, COUNT(*) AS cnt, COALESCE(SUM(amount), 0) AS total_amount " +
            "FROM crm_opportunity WHERE deleted = 0 AND owner_id = #{ownerId} " +
            "GROUP BY stage ORDER BY stage")
    List<FunnelRow> funnelByOwner(@Param("ownerId") Long ownerId);
}
