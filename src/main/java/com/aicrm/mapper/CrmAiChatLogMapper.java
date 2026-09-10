package com.aicrm.mapper;

import com.aicrm.entity.CrmAiChatLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * AI 问答日志 Mapper。注意表没有 deleted 列，不能写逻辑删除。
 */
public interface CrmAiChatLogMapper extends BaseMapper<CrmAiChatLog> {
}
