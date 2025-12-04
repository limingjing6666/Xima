package com.xima.app.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 群消息已读记录Mapper
 */
@Mapper
public interface GroupMessageReadMapper {
    
    // 获取用户在某群的最后已读消息ID
    Long getLastReadMessageId(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    // 更新或插入已读记录
    int upsertLastRead(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("lastReadMessageId") Long lastReadMessageId);
}
