package com.xima.app.service;

import com.xima.app.dto.friend.FriendDTO;
import com.xima.app.dto.friend.FriendRequestInfoDTO;

import java.util.List;

/**
 * 好友服务接口
 */
public interface FriendshipService {

    /**
     * 发送好友请求
     */
    void sendFriendRequest(Long userId, Long friendId);

    /**
     * 接受好友请求
     */
    void acceptFriendRequest(Long userId, Long requestId);

    /**
     * 拒绝好友请求
     */
    void rejectFriendRequest(Long userId, Long requestId);

    /**
     * 删除好友
     */
    void deleteFriend(Long userId, Long friendId);

    /**
     * 获取好友列表
     */
    List<FriendDTO> getFriendList(Long userId);

    /**
     * 获取收到的好友请求列表
     */
    List<FriendRequestInfoDTO> getPendingRequests(Long userId);

    /**
     * 获取发出的好友请求列表
     */
    List<FriendRequestInfoDTO> getSentRequests(Long userId);

    /**
     * 检查是否是好友
     */
    boolean isFriend(Long userId, Long friendId);

    /**
     * 获取好友数量
     */
    long getFriendCount(Long userId);

    /**
     * 设置好友备注
     */
    void setFriendRemark(Long userId, Long friendId, String remark);
}
