package com.xima.app.mapper;

import com.xima.app.entity.GroupMessage;
import com.xima.app.dto.group.GroupMessageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群消息Mapper
 */
@Mapper
public interface GroupMessageMapper {
    
    // 插入群消息
    int insert(GroupMessage message);
    
    // 根据ID查询消息
    GroupMessage findById(@Param("id") Long id);
    
    // 查询群聊天记录
    List<GroupMessageDTO> findByGroupId(@Param("groupId") Long groupId, 
                                        @Param("offset") int offset, 
                                        @Param("limit") int limit);
    
    // 查询群最新消息
    GroupMessageDTO findLatestByGroupId(@Param("groupId") Long groupId);
    
    // 统计群未读消息数（根据最后已读消息ID）
    int countUnreadMessages(@Param("groupId") Long groupId, @Param("lastReadMessageId") Long lastReadMessageId);
    
    // 删除群消息
    int deleteByGroupId(@Param("groupId") Long groupId);
    
    // 撤回群消息
    int recallMessage(@Param("id") Long id);
    
    // 搜索群消息
    List<GroupMessageDTO> searchMessages(@Param("groupId") Long groupId,
                                         @Param("keyword") String keyword,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);
}
