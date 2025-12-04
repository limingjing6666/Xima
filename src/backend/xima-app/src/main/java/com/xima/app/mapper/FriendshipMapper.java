package com.xima.app.mapper;

import com.xima.app.entity.Friendship;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 好友关系数据访问接口
 */
@Mapper
public interface FriendshipMapper {

    /**
     * 根据ID查询好友关系
     */
    Friendship findById(@Param("id") Long id);

    /**
     * 查询两个用户之间的好友关系
     */
    Friendship findByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 查询用户的所有好友关系（已接受）
     */
    List<Friendship> findFriendsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户收到的好友请求（待处理）
     */
    List<Friendship> findPendingRequestsByFriendId(@Param("friendId") Long friendId);

    /**
     * 查询用户发出的好友请求
     */
    List<Friendship> findSentRequestsByUserId(@Param("userId") Long userId);

    /**
     * 插入好友关系
     */
    int insert(Friendship friendship);

    /**
     * 更新好友关系状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 删除好友关系
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除两个用户之间的好友关系
     */
    int deleteByUserIdAndFriendId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 检查是否已经是好友
     */
    boolean existsFriendship(@Param("userId") Long userId, @Param("friendId") Long friendId);

    /**
     * 统计用户好友数量
     */
    long countFriends(@Param("userId") Long userId);

    /**
     * 查询用户所有好友的ID列表
     */
    List<Long> findFriendIdsByUserId(@Param("userId") Long userId);

    /**
     * 更新好友备注
     */
    int updateRemark(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("remark") String remark);

    /**
     * 获取好友备注
     */
    String getRemark(@Param("userId") Long userId, @Param("friendId") Long friendId);
}
