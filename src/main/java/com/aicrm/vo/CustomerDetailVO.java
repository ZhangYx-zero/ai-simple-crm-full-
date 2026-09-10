package com.aicrm.vo;

import com.aicrm.entity.CrmContact;
import com.aicrm.entity.CrmCustomer;
import lombok.Data;

import java.util.List;

/**
 * 客户详情：客户本身 + 其下联系人列表（一个客户对应多个联系人）。
 */
@Data
public class CustomerDetailVO {

    private CrmCustomer customer;
    private List<CrmContact> contacts;
}
