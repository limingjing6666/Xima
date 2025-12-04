package com.xima.app.service;

import com.xima.app.dto.group.*;
import com.xima.app.dto.group.InviteMembersResult;
import com.xima.app.entity.ChatGroup;
import com.xima.app.entity.GroupMember;
import com.xima.app.entity.GroupMessage;
import com.xima.app.entity.MessageContentType;
import com.xima.app.mapper.GroupMapper;
import com.xima.app.mapper.GroupMemberMapper;
import com.xima.app.mapper.GroupMessageMapper;
import com.xima.app.mapper.GroupMessageReadMapper;
import com.xima.app.mapper.UserMapper;
import com.xima.app.mapper.FriendshipMapper;
import com.xima.app.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群组服务
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final GroupMessageMapper groupMessageMapper;
    private final GroupMessageReadMapper groupMessageReadMapper;
    private final UserMapper userMapper;
    private final FriendshipMapper friendshipMapper;

    /**
     * 创建群组
     */
    @Transactional
    public GroupDTO createGroup(Long ownerId, CreateGroupRequest request) {
        // 创建群组
        ChatGroup group = new ChatGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setOwnerId(ownerId);
        group.setMaxMembers(200);
        group.setMemberCount(1);
        groupMapper.insert(group);

        // 添加群主为成员
        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(group.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole(GroupMember.MemberRole.OWNER);
        ownerMember.setMuted(false);
        groupMemberMapper.insert(ownerMember);

        // 添加初始成员
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<GroupMember> members = new ArrayList<>();
            List<Long> addedMemberIds = new ArrayList<>();
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(ownerId)) {
                    GroupMember member = new GroupMember();
                    member.setGroupId(group.getId());
                    member.setUserId(memberId);
                    member.setRole(GroupMember.MemberRole.MEMBER);
                    member.setMuted(false);
                    members.add(member);
                    addedMemberIds.add(memberId);
                }
            }
            if (!members.isEmpty()) {
                groupMemberMapper.batchInsert(members);
                groupMapper.updateMemberCount(group.getId(), 1 + members.size());
                
                // 为新成员初始化已读状态（避免历史消息显示为未读）
                for (Long memberId : addedMemberIds) {
                    groupMessageReadMapper.upsertLastRead(group.getId(), memberId, 0L);
                }
            }
        }
        
        // 为群主初始化已读状态
        groupMessageReadMapper.upsertLastRead(group.getId(), ownerId, 0L);

        return getGroupDTO(group.getId(), ownerId);
    }

    /**
     * 获取群组详情
     */
    public GroupDTO getGroupDTO(Long groupId, Long userId) {
        ChatGroup group = groupMapper.findById(groupId);
        if (group == null) {
            return null;
        }

        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setAvatar(group.getAvatar());
        dto.setDescription(group.getDescription());
        dto.setOwnerId(group.getOwnerId());
        dto.setMaxMembers(group.getMaxMembers());
        dto.setMemberCount(group.getMemberCount());
        dto.setCreateTime(group.getCreateTime());

        // 获取群主信息
        User owner = userMapper.findById(group.getOwnerId());
        if (owner != null) {
            dto.setOwnerName(owner.getNickname());
            dto.setOwnerAvatar(owner.getAvatar());
        }

        // 获取当前用户角色
        GroupMember member = groupMemberMapper.findByGroupIdAndUserId(groupId, userId);
        if (member != null) {
            dto.setMyRole(member.getRole().name());
        }
        
        // 获取前4个成员头像（用于组合头像）
        List<String> memberAvatars = groupMemberMapper.findTopMemberAvatars(groupId, 4);
        dto.setMemberAvatars(memberAvatars);

        return dto;
    }

    /**
     * 获取用户的群组列表
     */
    public List<GroupDTO> getUserGroups(Long userId) {
        List<ChatGroup> groups = groupMapper.findByUserId(userId);
        List<GroupDTO> result = new ArrayList<>();
        for (ChatGroup group : groups) {
            result.add(getGroupDTO(group.getId(), userId));
        }
        return result;
    }

    /**
     * 获取群成员列表
     */
    public List<GroupMemberDTO> getGroupMembers(Long groupId) {
        return groupMemberMapper.findMembersByGroupId(groupId);
    }

    /**
     * 添加群成员
     */
    @Transactional
    @CacheEvict(value = "groupMembers", key = "#groupId")
    public boolean addMember(Long groupId, Long userId, Long operatorId) {
        // 检查操作者权限
        GroupMember operator = groupMemberMapper.findByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || operator.getRole() == GroupMember.MemberRole.MEMBER) {
            return false;
        }

        // 检查是否已是成员
        if (groupMemberMapper.isMember(groupId, userId)) {
            return false;
        }

        // 添加成员
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(GroupMember.MemberRole.MEMBER);
        member.setMuted(false);
        groupMemberMapper.insert(member);
        groupMapper.incrementMemberCount(groupId);

        // 发送系统消息
        User operatorUser = userMapper.findById(operatorId);
        User newMember = userMapper.findById(userId);
        if (operatorUser != null && newMember != null) {
            GroupMessage systemMsg = new GroupMessage();
            systemMsg.setGroupId(groupId);
            systemMsg.setSenderId(operatorId);
            systemMsg.setContent(operatorUser.getNickname() + " 邀请 " + newMember.getNickname() + " 加入了群聊");
            systemMsg.setContentType(MessageContentType.SYSTEM);
            groupMessageMapper.insert(systemMsg);
            
            // 新成员加入时，将系统消息标记为已读（避免历史消息显示为未读）
            groupMessageReadMapper.upsertLastRead(groupId, userId, systemMsg.getId());
        }

        return true;
    }

    /**
     * 移除群成员
     */
    @Transactional
    @CacheEvict(value = "groupMembers", key = "#groupId")
    public boolean removeMember(Long groupId, Long userId, Long operatorId) {
        // 检查操作者权限
        GroupMember operator = groupMemberMapper.findByGroupIdAndUserId(groupId, operatorId);
        if (operator == null) {
            return false;
        }

        // 群主不能被移除
        ChatGroup group = groupMapper.findById(groupId);
        if (group.getOwnerId().equals(userId)) {
            return false;
        }

        // 只有群主和管理员可以移除成员
        if (operator.getRole() == GroupMember.MemberRole.MEMBER) {
            return false;
        }

        // 移除成员
        groupMemberMapper.deleteByGroupIdAndUserId(groupId, userId);
        groupMapper.decrementMemberCount(groupId);

        // 发送系统消息
        User operatorUser = userMapper.findById(operatorId);
        User targetUser = userMapper.findById(userId);
        if (operatorUser != null && targetUser != null) {
            GroupMessage systemMsg = new GroupMessage();
            systemMsg.setGroupId(groupId);
            systemMsg.setSenderId(operatorId);
            systemMsg.setContent(operatorUser.getNickname() + " 将 " + targetUser.getNickname() + " 移出群聊");
            systemMsg.setContentType(MessageContentType.SYSTEM);
            groupMessageMapper.insert(systemMsg);
        }

        return true;
    }

    /**
     * 退出群组
     */
    @Transactional
    public boolean leaveGroup(Long groupId, Long userId) {
        ChatGroup group = groupMapper.findById(groupId);
        if (group == null) {
            return false;
        }

        // 群主不能退出，只能解散
        if (group.getOwnerId().equals(userId)) {
            return false;
        }

        groupMemberMapper.deleteByGroupIdAndUserId(groupId, userId);
        groupMapper.decrementMemberCount(groupId);

        // 发送系统消息
        User user = userMapper.findById(userId);
        GroupMessage systemMsg = new GroupMessage();
        systemMsg.setGroupId(groupId);
        systemMsg.setSenderId(userId);
        systemMsg.setContent(user.getNickname() + " 退出了群聊");
        systemMsg.setContentType(MessageContentType.SYSTEM);
        groupMessageMapper.insert(systemMsg);

        return true;
    }

    /**
     * 解散群组
     */
    @Transactional
    public boolean dissolveGroup(Long groupId, Long ownerId) {
        ChatGroup group = groupMapper.findById(groupId);
        if (group == null || !group.getOwnerId().equals(ownerId)) {
            return false;
        }

        // 删除群消息
        groupMessageMapper.deleteByGroupId(groupId);
        // 删除群成员
        groupMemberMapper.deleteByGroupId(groupId);
        // 删除群组
        groupMapper.deleteById(groupId);

        return true;
    }

    /**
     * 发送群消息
     */
    public GroupMessageDTO sendMessage(Long groupId, Long senderId, String content, MessageContentType contentType) {
        // 检查是否是群成员
        GroupMember member = groupMemberMapper.findByGroupIdAndUserId(groupId, senderId);
        if (member == null) {
            return null;
        }
        
        // 检查是否被禁言
        if (member.getMuted() != null && member.getMuted()) {
            throw new RuntimeException("您已被禁言，无法发送消息");
        }

        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setContentType(contentType);
        groupMessageMapper.insert(message);

        // 返回消息DTO
        User sender = userMapper.findById(senderId);
        GroupMessageDTO dto = new GroupMessageDTO();
        dto.setId(message.getId());
        dto.setGroupId(groupId);
        dto.setSenderId(senderId);
        if (sender != null) {
            dto.setSenderName(sender.getNickname());
            dto.setSenderAvatar(sender.getAvatar());
        }
        dto.setContent(content);
        dto.setContentType(contentType.name());
        dto.setCreateTime(message.getCreateTime());

        return dto;
    }

    /**
     * 获取群聊天记录
     */
    public List<GroupMessageDTO> getMessages(Long groupId, Long userId, int page, int size) {
        // 检查是否是群成员
        if (!groupMemberMapper.isMember(groupId, userId)) {
            return new ArrayList<>();
        }

        int offset = page * size;
        List<GroupMessageDTO> messages = groupMessageMapper.findByGroupId(groupId, offset, size);
        
        // 填充好友备注
        for (GroupMessageDTO msg : messages) {
            if (msg.getSenderId() != null && !msg.getSenderId().equals(userId)) {
                String remark = friendshipMapper.getRemark(userId, msg.getSenderId());
                msg.setSenderRemark(remark);
            }
        }
        
        return messages;
    }

    /**
     * 获取群成员ID列表（带缓存）
     */
    @Cacheable(value = "groupMembers", key = "#groupId")
    public List<Long> getMemberIds(Long groupId) {
        return groupMemberMapper.findMemberIdsByGroupId(groupId);
    }

    /**
     * 检查用户是否是群成员
     */
    public boolean isMember(Long groupId, Long userId) {
        return groupMemberMapper.isMember(groupId, userId);
    }

    /**
     * 更新群信息
     */
    public boolean updateGroup(Long groupId, Long userId, String name, String description, String avatar) {
        // 检查权限
        GroupMember member = groupMemberMapper.findByGroupIdAndUserId(groupId, userId);
        if (member == null || member.getRole() == GroupMember.MemberRole.MEMBER) {
            return false;
        }

        ChatGroup group = new ChatGroup();
        group.setId(groupId);
        group.setName(name);
        group.setDescription(description);
        group.setAvatar(avatar);
        groupMapper.update(group);

        return true;
    }

    /**
     * 获取用户所有群的未读消息数
     */
    public Map<Long, Integer> getUnreadCounts(Long userId) {
        Map<Long, Integer> result = new HashMap<>();
        List<ChatGroup> groups = groupMapper.findByUserId(userId);
        
        for (ChatGroup group : groups) {
            Long lastReadId = groupMessageReadMapper.getLastReadMessageId(group.getId(), userId);
            if (lastReadId == null) {
                lastReadId = 0L;
            }
            int unreadCount = groupMessageMapper.countUnreadMessages(group.getId(), lastReadId);
            if (unreadCount > 0) {
                result.put(group.getId(), unreadCount);
            }
        }
        
        return result;
    }

    /**
     * 标记群消息已读
     */
    public void markAsRead(Long groupId, Long userId) {
        // 获取群最新消息ID
        GroupMessageDTO latestMsg = groupMessageMapper.findLatestByGroupId(groupId);
        if (latestMsg != null) {
            groupMessageReadMapper.upsertLastRead(groupId, userId, latestMsg.getId());
        }
    }

    /**
     * 设置/取消管理员
     */
    @Transactional
    public boolean setAdmin(Long groupId, Long targetUserId, boolean isAdmin, Long operatorId) {
        // 只有群主可以设置管理员
        ChatGroup group = groupMapper.findById(groupId);
        if (group == null || !group.getOwnerId().equals(operatorId)) {
            return false;
        }

        // 不能设置群主为管理员
        if (targetUserId.equals(operatorId)) {
            return false;
        }

        // 检查目标用户是否是群成员
        GroupMember member = groupMemberMapper.findByGroupIdAndUserId(groupId, targetUserId);
        if (member == null) {
            return false;
        }

        // 更新角色
        GroupMember.MemberRole newRole = isAdmin ? GroupMember.MemberRole.ADMIN : GroupMember.MemberRole.MEMBER;
        groupMemberMapper.updateRole(groupId, targetUserId, newRole.name());

        // 发送系统消息
        User operatorUser = userMapper.findById(operatorId);
        User targetUser = userMapper.findById(targetUserId);
        if (operatorUser != null && targetUser != null) {
            String action = isAdmin ? "设为管理员" : "取消管理员";
            GroupMessage systemMsg = new GroupMessage();
            systemMsg.setGroupId(groupId);
            systemMsg.setSenderId(operatorId);
            systemMsg.setContent(operatorUser.getNickname() + " 将 " + targetUser.getNickname() + " " + action);
            systemMsg.setContentType(MessageContentType.SYSTEM);
            groupMessageMapper.insert(systemMsg);
        }

        return true;
    }

    /**
     * 转让群主
     */
    @Transactional
    public boolean transferOwner(Long groupId, Long newOwnerId, Long currentOwnerId) {
        // 检查当前用户是否是群主
        ChatGroup group = groupMapper.findById(groupId);
        if (group == null || !group.getOwnerId().equals(currentOwnerId)) {
            return false;
        }

        // 检查新群主是否是群成员
        GroupMember newOwner = groupMemberMapper.findByGroupIdAndUserId(groupId, newOwnerId);
        if (newOwner == null) {
            return false;
        }

        // 更新群主
        groupMapper.updateOwner(groupId, newOwnerId);
        
        // 更新角色：新群主设为OWNER，原群主设为MEMBER
        groupMemberMapper.updateRole(groupId, newOwnerId, GroupMember.MemberRole.OWNER.name());
        groupMemberMapper.updateRole(groupId, currentOwnerId, GroupMember.MemberRole.MEMBER.name());

        // 发送系统消息
        User oldOwner = userMapper.findById(currentOwnerId);
        User newOwnerUser = userMapper.findById(newOwnerId);
        if (oldOwner != null && newOwnerUser != null) {
            GroupMessage systemMsg = new GroupMessage();
            systemMsg.setGroupId(groupId);
            systemMsg.setSenderId(currentOwnerId);
            systemMsg.setContent(oldOwner.getNickname() + " 将群主转让给 " + newOwnerUser.getNickname());
            systemMsg.setContentType(MessageContentType.SYSTEM);
            groupMessageMapper.insert(systemMsg);
        }

        return true;
    }

    /**
     * 禁言/解除禁言成员
     */
    @Transactional
    public boolean setMuted(Long groupId, Long targetUserId, boolean muted, Long operatorId) {
        // 检查操作者权限（群主或管理员）
        GroupMember operator = groupMemberMapper.findByGroupIdAndUserId(groupId, operatorId);
        if (operator == null || operator.getRole() == GroupMember.MemberRole.MEMBER) {
            return false;
        }

        // 不能禁言群主
        ChatGroup group = groupMapper.findById(groupId);
        if (group.getOwnerId().equals(targetUserId)) {
            return false;
        }

        // 管理员不能禁言其他管理员
        GroupMember target = groupMemberMapper.findByGroupIdAndUserId(groupId, targetUserId);
        if (target == null) {
            return false;
        }
        if (operator.getRole() == GroupMember.MemberRole.ADMIN && target.getRole() == GroupMember.MemberRole.ADMIN) {
            return false;
        }

        // 更新禁言状态
        groupMemberMapper.updateMuted(groupId, targetUserId, muted);

        // 发送系统消息
        User operatorUser = userMapper.findById(operatorId);
        User targetUser = userMapper.findById(targetUserId);
        if (operatorUser != null && targetUser != null) {
            String action = muted ? "禁言" : "解除禁言";
            GroupMessage systemMsg = new GroupMessage();
            systemMsg.setGroupId(groupId);
            systemMsg.setSenderId(operatorId);
            systemMsg.setContent(operatorUser.getNickname() + " 将 " + targetUser.getNickname() + " " + action);
            systemMsg.setContentType(MessageContentType.SYSTEM);
            groupMessageMapper.insert(systemMsg);
        }

        return true;
    }

    /**
     * 批量邀请好友加入群聊
     */
    @Transactional
    public InviteMembersResult inviteMembers(Long groupId, List<Long> userIds, Long operatorId) {
        InviteMembersResult result = new InviteMembersResult();
        List<Long> successUserIds = new ArrayList<>();
        List<Long> failUserIds = new ArrayList<>();

        // 检查群组是否存在
        ChatGroup group = groupMapper.findById(groupId);
        if (group == null) {
            result.setMessage("群组不存在");
            result.setFailUserIds(userIds);
            result.setFailCount(userIds.size());
            return result;
        }

        // 检查操作者是否是群成员
        GroupMember operator = groupMemberMapper.findByGroupIdAndUserId(groupId, operatorId);
        if (operator == null) {
            result.setMessage("您不是该群成员，无法邀请好友");
            result.setFailUserIds(userIds);
            result.setFailCount(userIds.size());
            return result;
        }

        // 检查群人数上限
        int currentCount = group.getMemberCount();
        int maxMembers = group.getMaxMembers();

        // 获取操作者信息
        User operatorUser = userMapper.findById(operatorId);
        List<String> invitedNames = new ArrayList<>();

        for (Long userId : userIds) {
            // 检查是否超过人数上限
            if (currentCount >= maxMembers) {
                failUserIds.add(userId);
                continue;
            }

            // 检查用户是否存在
            User user = userMapper.findById(userId);
            if (user == null) {
                failUserIds.add(userId);
                continue;
            }

            // 检查是否已是群成员
            if (groupMemberMapper.isMember(groupId, userId)) {
                failUserIds.add(userId);
                continue;
            }

            // 添加成员
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setRole(GroupMember.MemberRole.MEMBER);
            member.setMuted(false);
            groupMemberMapper.insert(member);
            
            successUserIds.add(userId);
            invitedNames.add(user.getNickname());
            currentCount++;
        }

        // 更新群成员数量
        if (!successUserIds.isEmpty()) {
            groupMapper.updateMemberCount(groupId, currentCount);

            // 发送系统消息
            if (operatorUser != null && !invitedNames.isEmpty()) {
                String namesStr = String.join("、", invitedNames);
                GroupMessage systemMsg = new GroupMessage();
                systemMsg.setGroupId(groupId);
                systemMsg.setSenderId(operatorId);
                systemMsg.setContent(operatorUser.getNickname() + " 邀请 " + namesStr + " 加入了群聊");
                systemMsg.setContentType(MessageContentType.SYSTEM);
                groupMessageMapper.insert(systemMsg);
                
                // 新成员加入时，将系统消息标记为已读（避免历史消息显示为未读）
                for (Long userId : successUserIds) {
                    groupMessageReadMapper.upsertLastRead(groupId, userId, systemMsg.getId());
                }
            }
        }

        result.setSuccessCount(successUserIds.size());
        result.setFailCount(failUserIds.size());
        result.setSuccessUserIds(successUserIds);
        result.setFailUserIds(failUserIds);
        
        if (successUserIds.isEmpty()) {
            result.setMessage("邀请失败，所选用户已是群成员或群人数已满");
        } else if (failUserIds.isEmpty()) {
            result.setMessage("成功邀请 " + successUserIds.size() + " 位好友加入群聊");
        } else {
            result.setMessage("成功邀请 " + successUserIds.size() + " 位好友，" + failUserIds.size() + " 位邀请失败");
        }

        return result;
    }
}
