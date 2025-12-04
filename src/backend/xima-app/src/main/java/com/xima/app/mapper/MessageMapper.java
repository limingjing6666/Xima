package com.xima.app.mapper;

import com.xima.app.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息数据访问接口
 */
@Mapper
public interface MessageMapper {

    /**
     * 根据ID查询消息
     */
    Message findById(@Param("id") Long id);

    /**
     * 插入消息
     */
    int insert(Message message);

    /**
     * 更新消息状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 查询两个用户之间的聊天记录
     */
    List<Message> findChatHistory(@Param("userId1") Long userId1, 
                                   @Param("userId2") Long userId2,
                                   @Param("offset") int offset, 
                                   @Param("limit") int limit);

    /**
     * 查询用户的离线消息
     */
    List<Message> findOfflineMessages(@Param("receiverId") Long receiverId);

    /**
     * 批量更新消息状态为已送达
     */
    int updateStatusToDelivered(@Param("receiverId") Long receiverId);

    /**
     * 统计未读消息数
     */
    long countUnreadMessages(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

    /**
     * 删除消息
     */
    int deleteById(@Param("id") Long id);

    /**
     * 撤回消息
     */
    int recallMessage(@Param("id") Long id);

    /**
     * 搜索消息
     */
    List<Message> searchMessages(@Param("userId") Long userId, 
                                  @Param("keyword") String keyword,
                                  @Param("offset") int offset, 
                                  @Param("limit") int limit);
}
