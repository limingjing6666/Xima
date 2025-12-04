package com.xima.app.service.impl;

import com.xima.app.dto.friend.FriendDTO;
import com.xima.app.dto.friend.FriendRequestInfoDTO;
import com.xima.app.entity.Friendship;
import com.xima.app.entity.FriendshipStatus;
import com.xima.app.entity.User;
import com.xima.app.exception.BusinessException;
import com.xima.app.exception.ErrorCode;
import com.xima.app.mapper.FriendshipMapper;
import com.xima.app.mapper.UserMapper;
import com.xima.app.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipMapper friendshipMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void sendFriendRequest(Long userId, Long friendId) {
        // 不能添加自己
        if (userId.equals(friendId)) {
            throw new BusinessException(ErrorCode.CANNOT_ADD_SELF);
        }

        // 检查目标用户是否存在
        User friend = userMapper.findById(friendId);
        if (friend == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查是否已经是好友或有待处理请求
        if (friendshipMapper.existsFriendship(userId, friendId)) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_EXISTS);
        }

        // 创建好友请求
        Friendship friendship = new Friendship(userId, friendId, FriendshipStatus.PENDING);
        friendshipMapper.insert(friendship);

        log.info("用户 {} 向用户 {} 发送好友请求", userId, friendId);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long userId, Long requestId) {
        Friendship friendship = friendshipMapper.findById(requestId);
        if (friendship == null) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        // 验证是否是发给当前用户的请求
        if (!friendship.getFriendId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 验证状态
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BusinessException("该请求已被处理");
        }

        // 更新原请求状态为已接受
        friendshipMapper.updateStatus(requestId, FriendshipStatus.ACCEPTED.name());
        
        // 创建反向好友记录（让被添加方也有自己的记录，可以独立设置备注）
        Friendship reverseFriendship = new Friendship(userId, friendship.getUserId(), FriendshipStatus.ACCEPTED);
        friendshipMapper.insert(reverseFriendship);
        
        log.info("用户 {} 接受了用户 {} 的好友请求", userId, friendship.getUserId());
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long userId, Long requestId) {
        Friendship friendship = friendshipMapper.findById(requestId);
        if (friendship == null) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        // 验证是否是发给当前用户的请求
        if (!friendship.getFriendId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 验证状态
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new BusinessException("该请求已被处理");
        }

        friendshipMapper.updateStatus(requestId, FriendshipStatus.REJECTED.name());
        log.info("用户 {} 拒绝了用户 {} 的好友请求", userId, friendship.getUserId());
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        friendshipMapper.deleteByUserIdAndFriendId(userId, friendId);
        log.info("用户 {} 删除了好友 {}", userId, friendId);
    }

    @Override
    public List<FriendDTO> getFriendList(Long userId) {
        List<Friendship> friendships = friendshipMapper.findFriendsByUserId(userId);
        List<FriendDTO> friends = new ArrayList<>();

        for (Friendship friendship : friendships) {
            // 现在每个用户有自己的记录，user_id是当前用户，friend_id是好友
            Long friendUserId = friendship.getFriendId();

            User friendUser = userMapper.findById(friendUserId);
            if (friendUser != null) {
                // 优先使用备注，没有备注则使用昵称
                String displayName = friendship.getRemark() != null && !friendship.getRemark().isEmpty()
                        ? friendship.getRemark()
                        : friendUser.getNickname();
                friends.add(FriendDTO.builder()
                        .friendshipId(friendship.getId())
                        .userId(friendUser.getId())
                        .username(friendUser.getUsername())
                        .nickname(displayName)
                        .remark(friendship.getRemark())
                        .avatar(friendUser.getAvatar())
                        .status(friendUser.getStatus())
                        .friendshipStatus(friendship.getStatus())
                        .createTime(friendship.getCreateTime())
                        .build());
            }
        }

        return friends;
    }

    @Override
    public List<FriendRequestInfoDTO> getPendingRequests(Long userId) {
        List<Friendship> requests = friendshipMapper.findPendingRequestsByFriendId(userId);
        List<FriendRequestInfoDTO> result = new ArrayList<>();

        for (Friendship request : requests) {
            User fromUser = userMapper.findById(request.getUserId());
            if (fromUser != null) {
                result.add(FriendRequestInfoDTO.builder()
                        .requestId(request.getId())
                        .fromUserId(fromUser.getId())
                        .fromUsername(fromUser.getUsername())
                        .fromNickname(fromUser.getNickname())
                        .fromAvatar(fromUser.getAvatar())
                        .status(request.getStatus())
                        .createTime(request.getCreateTime())
                        .build());
            }
        }

        return result;
    }

    @Override
    public List<FriendRequestInfoDTO> getSentRequests(Long userId) {
        List<Friendship> requests = friendshipMapper.findSentRequestsByUserId(userId);
        List<FriendRequestInfoDTO> result = new ArrayList<>();

        for (Friendship request : requests) {
            User toUser = userMapper.findById(request.getFriendId());
            if (toUser != null) {
                result.add(FriendRequestInfoDTO.builder()
                        .requestId(request.getId())
                        .fromUserId(toUser.getId())
                        .fromUsername(toUser.getUsername())
                        .fromNickname(toUser.getNickname())
                        .fromAvatar(toUser.getAvatar())
                        .status(request.getStatus())
                        .createTime(request.getCreateTime())
                        .build());
            }
        }

        return result;
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipMapper.findByUserIdAndFriendId(userId, friendId);
        if (friendship != null && friendship.getStatus() == FriendshipStatus.ACCEPTED) {
            return true;
        }
        // 检查反向关系
        friendship = friendshipMapper.findByUserIdAndFriendId(friendId, userId);
        return friendship != null && friendship.getStatus() == FriendshipStatus.ACCEPTED;
    }

    @Override
    public long getFriendCount(Long userId) {
        return friendshipMapper.countFriends(userId);
    }

    @Override
    @Transactional
    public void setFriendRemark(Long userId, Long friendId, String remark) {
        friendshipMapper.updateRemark(userId, friendId, remark);
        log.info("用户 {} 设置好友 {} 的备注为: {}", userId, friendId, remark);
    }
}
