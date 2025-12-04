package com.xima.app.mapper;

import com.xima.app.entity.GroupMember;
import com.xima.app.dto.group.GroupMemberDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 群成员Mapper
 */
@Mapper
public interface GroupMemberMapper {
    
    // 添加群成员
    int insert(GroupMember member);
    
    // 批量添加群成员
    int batchInsert(@Param("members") List<GroupMember> members);
    
    // 根据群ID和用户ID查询成员
    GroupMember findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    // 查询群的所有成员
    List<GroupMemberDTO> findMembersByGroupId(@Param("groupId") Long groupId);
    
    // 查询群成员ID列表
    List<Long> findMemberIdsByGroupId(@Param("groupId") Long groupId);
    
    // 更新成员信息
    int update(GroupMember member);
    
    // 删除群成员
    int deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    // 删除群的所有成员
    int deleteByGroupId(@Param("groupId") Long groupId);
    
    // 统计群成员数量
    int countByGroupId(@Param("groupId") Long groupId);
    
    // 检查用户是否是群成员
    boolean isMember(@Param("groupId") Long groupId, @Param("userId") Long userId);
    
    // 获取群前N个成员的头像
    List<String> findTopMemberAvatars(@Param("groupId") Long groupId, @Param("limit") int limit);
    
    // 更新成员角色
    int updateRole(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("role") String role);
    
    // 更新禁言状态
    int updateMuted(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("muted") boolean muted);
    
    // 查询用户所在的所有群ID
    List<Long> findGroupIdsByUserId(@Param("userId") Long userId);
}
