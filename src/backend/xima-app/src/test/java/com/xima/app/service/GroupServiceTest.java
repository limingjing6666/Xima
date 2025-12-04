package com.xima.app.service;

import com.xima.app.dto.group.CreateGroupRequest;
import com.xima.app.dto.group.GroupDTO;
import com.xima.app.dto.group.GroupMemberDTO;
import com.xima.app.dto.group.GroupMessageDTO;
import com.xima.app.dto.group.InviteMembersResult;
import com.xima.app.entity.ChatGroup;
import com.xima.app.entity.GroupMember;
import com.xima.app.entity.MessageContentType;
import com.xima.app.entity.User;
import com.xima.app.mapper.FriendshipMapper;
import com.xima.app.mapper.GroupMapper;
import com.xima.app.mapper.GroupMemberMapper;
import com.xima.app.mapper.GroupMessageMapper;
import com.xima.app.mapper.GroupMessageReadMapper;
import com.xima.app.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GroupService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupMemberMapper groupMemberMapper;

    @Mock
    private GroupMessageMapper groupMessageMapper;

    @Mock
    private GroupMessageReadMapper groupMessageReadMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FriendshipMapper friendshipMapper;

    @InjectMocks
    private GroupService groupService;

    private User owner;
    private User member;
    private ChatGroup testGroup;
    private GroupMember ownerMember;
    private GroupMember normalMember;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setNickname("Group Owner");

        member = new User();
        member.setId(2L);
        member.setUsername("member");
        member.setNickname("Group Member");

        testGroup = new ChatGroup();
        testGroup.setId(1L);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setOwnerId(1L);
        testGroup.setMaxMembers(200);
        testGroup.setMemberCount(2);
        testGroup.setCreateTime(LocalDateTime.now());

        ownerMember = new GroupMember();
        ownerMember.setId(1L);
        ownerMember.setGroupId(1L);
        ownerMember.setUserId(1L);
        ownerMember.setRole(GroupMember.MemberRole.OWNER);
        ownerMember.setMuted(false);

        normalMember = new GroupMember();
        normalMember.setId(2L);
        normalMember.setGroupId(1L);
        normalMember.setUserId(2L);
        normalMember.setRole(GroupMember.MemberRole.MEMBER);
        normalMember.setMuted(false);
    }

    @Test
    @DisplayName("创建群组成功")
    void createGroup_Success() {
        // Given
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("New Group");
        request.setDescription("New Description");
        request.setMemberIds(Arrays.asList(2L, 3L));

        when(groupMapper.insert(any(ChatGroup.class))).thenAnswer(invocation -> {
            ChatGroup group = invocation.getArgument(0);
            group.setId(1L);
            return 1;
        });
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);

        // When
        GroupDTO result = groupService.createGroup(1L, request);

        // Then
        assertNotNull(result);
        verify(groupMapper).insert(any(ChatGroup.class));
        verify(groupMemberMapper).insert(any(GroupMember.class));
    }

    @Test
    @DisplayName("获取群组详情成功")
    void getGroupDTO_Success() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(groupMemberMapper.findTopMemberAvatars(1L, 4)).thenReturn(Collections.emptyList());

        // When
        GroupDTO result = groupService.getGroupDTO(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        assertEquals("OWNER", result.getMyRole());
    }

    @Test
    @DisplayName("获取群组详情 - 群组不存在")
    void getGroupDTO_NotFound() {
        // Given
        when(groupMapper.findById(anyLong())).thenReturn(null);

        // When
        GroupDTO result = groupService.getGroupDTO(999L, 1L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("获取用户群组列表成功")
    void getUserGroups_Success() {
        // Given
        when(groupMapper.findByUserId(1L)).thenReturn(Arrays.asList(testGroup));
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(groupMemberMapper.findTopMemberAvatars(1L, 4)).thenReturn(Collections.emptyList());

        // When
        List<GroupDTO> result = groupService.getUserGroups(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("添加群成员成功")
    void addMember_Success() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(groupMemberMapper.isMember(1L, 3L)).thenReturn(false);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(userMapper.findById(3L)).thenReturn(member);

        // When
        boolean result = groupService.addMember(1L, 3L, 1L);

        // Then
        assertTrue(result);
        verify(groupMemberMapper).insert(any(GroupMember.class));
        verify(groupMapper).incrementMemberCount(1L);
    }

    @Test
    @DisplayName("添加群成员失败 - 无权限")
    void addMember_NoPermission() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 2L)).thenReturn(normalMember);

        // When
        boolean result = groupService.addMember(1L, 3L, 2L);

        // Then
        assertFalse(result);
        verify(groupMemberMapper, never()).insert(any(GroupMember.class));
    }

    @Test
    @DisplayName("添加群成员失败 - 已是成员")
    void addMember_AlreadyMember() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(groupMemberMapper.isMember(1L, 2L)).thenReturn(true);

        // When
        boolean result = groupService.addMember(1L, 2L, 1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("移除群成员成功")
    void removeMember_Success() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(userMapper.findById(2L)).thenReturn(member);

        // When
        boolean result = groupService.removeMember(1L, 2L, 1L);

        // Then
        assertTrue(result);
        verify(groupMemberMapper).deleteByGroupIdAndUserId(1L, 2L);
        verify(groupMapper).decrementMemberCount(1L);
    }

    @Test
    @DisplayName("移除群成员失败 - 不能移除群主")
    void removeMember_CannotRemoveOwner() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(groupMapper.findById(1L)).thenReturn(testGroup);

        // When
        boolean result = groupService.removeMember(1L, 1L, 1L);

        // Then
        assertFalse(result);
        verify(groupMemberMapper, never()).deleteByGroupIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("退出群组成功")
    void leaveGroup_Success() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(userMapper.findById(2L)).thenReturn(member);

        // When
        boolean result = groupService.leaveGroup(1L, 2L);

        // Then
        assertTrue(result);
        verify(groupMemberMapper).deleteByGroupIdAndUserId(1L, 2L);
    }

    @Test
    @DisplayName("退出群组失败 - 群主不能退出")
    void leaveGroup_OwnerCannotLeave() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);

        // When
        boolean result = groupService.leaveGroup(1L, 1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("解散群组成功")
    void dissolveGroup_Success() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);

        // When
        boolean result = groupService.dissolveGroup(1L, 1L);

        // Then
        assertTrue(result);
        verify(groupMessageMapper).deleteByGroupId(1L);
        verify(groupMemberMapper).deleteByGroupId(1L);
        verify(groupMapper).deleteById(1L);
    }

    @Test
    @DisplayName("解散群组失败 - 非群主")
    void dissolveGroup_NotOwner() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);

        // When
        boolean result = groupService.dissolveGroup(1L, 2L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("发送群消息成功")
    void sendMessage_Success() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(userMapper.findById(1L)).thenReturn(owner);

        // When
        GroupMessageDTO result = groupService.sendMessage(1L, 1L, "Hello Group!", MessageContentType.TEXT);

        // Then
        assertNotNull(result);
        assertEquals("Hello Group!", result.getContent());
        verify(groupMessageMapper).insert(any());
    }

    @Test
    @DisplayName("发送群消息失败 - 非群成员")
    void sendMessage_NotMember() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 3L)).thenReturn(null);

        // When
        GroupMessageDTO result = groupService.sendMessage(1L, 3L, "Hello!", MessageContentType.TEXT);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("发送群消息失败 - 被禁言")
    void sendMessage_Muted() {
        // Given
        GroupMember mutedMember = new GroupMember();
        mutedMember.setMuted(true);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 2L)).thenReturn(mutedMember);

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> groupService.sendMessage(1L, 2L, "Hello!", MessageContentType.TEXT));
    }

    @Test
    @DisplayName("检查是否是群成员")
    void isMember_Success() {
        // Given
        when(groupMemberMapper.isMember(1L, 1L)).thenReturn(true);
        when(groupMemberMapper.isMember(1L, 999L)).thenReturn(false);

        // When & Then
        assertTrue(groupService.isMember(1L, 1L));
        assertFalse(groupService.isMember(1L, 999L));
    }

    @Test
    @DisplayName("设置管理员成功")
    void setAdmin_Success() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 2L)).thenReturn(normalMember);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(userMapper.findById(2L)).thenReturn(member);

        // When
        boolean result = groupService.setAdmin(1L, 2L, true, 1L);

        // Then
        assertTrue(result);
        verify(groupMemberMapper).updateRole(1L, 2L, GroupMember.MemberRole.ADMIN.name());
    }

    @Test
    @DisplayName("设置管理员失败 - 非群主操作")
    void setAdmin_NotOwner() {
        // Given
        ChatGroup group = new ChatGroup();
        group.setId(1L);
        group.setOwnerId(1L);
        when(groupMapper.findById(1L)).thenReturn(group);

        // When
        boolean result = groupService.setAdmin(1L, 3L, true, 2L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("转让群主成功")
    void transferOwner_Success() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 2L)).thenReturn(normalMember);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(userMapper.findById(2L)).thenReturn(member);

        // When
        boolean result = groupService.transferOwner(1L, 2L, 1L);

        // Then
        assertTrue(result);
        verify(groupMapper).updateOwner(1L, 2L);
    }

    @Test
    @DisplayName("禁言成员成功")
    void setMuted_Success() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 2L)).thenReturn(normalMember);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(userMapper.findById(2L)).thenReturn(member);

        // When
        boolean result = groupService.setMuted(1L, 2L, true, 1L);

        // Then
        assertTrue(result);
        verify(groupMemberMapper).updateMuted(1L, 2L, true);
    }

    @Test
    @DisplayName("禁言成员失败 - 不能禁言群主")
    void setMuted_CannotMuteOwner() {
        // Given
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(groupMapper.findById(1L)).thenReturn(testGroup);

        // When
        boolean result = groupService.setMuted(1L, 1L, true, 1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("批量邀请成员成功")
    void inviteMembers_Success() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(userMapper.findById(3L)).thenReturn(member);
        when(groupMemberMapper.isMember(1L, 3L)).thenReturn(false);

        // When
        InviteMembersResult result = groupService.inviteMembers(1L, Arrays.asList(3L), 1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailCount());
    }

    @Test
    @DisplayName("批量邀请成员 - 部分失败")
    void inviteMembers_PartialFail() {
        // Given
        when(groupMapper.findById(1L)).thenReturn(testGroup);
        when(groupMemberMapper.findByGroupIdAndUserId(1L, 1L)).thenReturn(ownerMember);
        when(userMapper.findById(1L)).thenReturn(owner);
        when(userMapper.findById(3L)).thenReturn(member);
        when(userMapper.findById(4L)).thenReturn(null); // 用户不存在
        when(groupMemberMapper.isMember(1L, 3L)).thenReturn(false);

        // When
        InviteMembersResult result = groupService.inviteMembers(1L, Arrays.asList(3L, 4L), 1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailCount());
    }
}
