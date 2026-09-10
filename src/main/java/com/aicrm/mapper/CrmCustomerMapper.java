package com.aicrm.mapper;

import com.aicrm.entity.CrmCustomer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 客户 Mapper。继承 BaseMapper 后自带单表 CRUD；复杂聚合 SQL 手写在这里。
 */
public interface CrmCustomerMapper extends BaseMapper<CrmCustomer> {
}
