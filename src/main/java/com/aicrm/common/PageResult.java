package com.aicrm.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 统一分页返回体，方便前端/文档展示
 */
@Data
public class PageResult<T> {

    private long total;      // 总记录数
    private long pageNum;    // 当前页码
    private long pageSize;   // 每页条数
    private List<T> records; // 当前页数据

    /** 由 MyBatis-Plus 的 IPage 转换而来 */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(page.getTotal());
        r.setPageNum(page.getCurrent());
        r.setPageSize(page.getSize());
        r.setRecords(page.getRecords());
        return r;
    }
}
