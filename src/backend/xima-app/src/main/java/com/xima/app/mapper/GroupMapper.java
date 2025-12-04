package com.xima.app.mapper;

import com.xima.app.entity.ChatGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群组Mapper
 */
@Mapper
public interface GroupMapper {
    
    // 创建群组
    int insert(ChatGroup group);
    
    // 根据ID查询群组
    ChatGroup findById(@Param("id") Long id);
    
    // 更新群组信息
    int update(ChatGroup group);
    
    // 删除群组
    int deleteById(@Param("id") Long id);
    
    // 查询用户加入的所有群组
    List<ChatGroup> findByUserId(@Param("userId") Long userId);
    
    // 更新成员数量
    int updateMemberCount(@Param("id") Long id, @Param("count") Integer count);
    
    // 增加成员数量
    int incrementMemberCount(@Param("id") Long id);
    
    // 减少成员数量
    int decrementMemberCount(@Param("id") Long id);
    
    // 更新群主
    int updateOwner(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
