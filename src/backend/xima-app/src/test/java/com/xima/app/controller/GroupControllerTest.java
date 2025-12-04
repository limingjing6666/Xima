package com.xima.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xima.app.dto.group.*;
import com.xima.app.entity.MessageContentType;
import com.xima.app.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.xima.app.config.TestSecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GroupController 单元测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GroupService groupService;

    private GroupDTO testGroup;
    private GroupMessageDTO testMessage;
    private GroupMemberDTO testMember;

    @BeforeEach
    void setUp() {
        testGroup = new GroupDTO();
        testGroup.setId(1L);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setOwnerId(1L);
        testGroup.setMemberCount(5);
        testGroup.setMyRole("OWNER");

        testMessage = new GroupMessageDTO();
        testMessage.setId(1L);
        testMessage.setGroupId(1L);
        testMessage.setSenderId(1L);
        testMessage.setSenderName("Sender");
        testMessage.setContent("Hello Group!");
        testMessage.setContentType("TEXT");
        testMessage.setCreateTime(LocalDateTime.now());

        testMember = new GroupMemberDTO();
        testMember.setUserId(1L);
        testMember.setUsername("member");
        testMember.setNickname("Member User");
        testMember.setRole("MEMBER");
    }

    @Test
    @DisplayName("创建群组 - 成功")
    @WithUserDetails("testuser")
    void createGroup_Success() throws Exception {
        // Given
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("New Group");
        request.setDescription("New Description");
        request.setMemberIds(Arrays.asList(2L, 3L));

        when(groupService.createGroup(anyLong(), any(CreateGroupRequest.class))).thenReturn(testGroup);

        // When & Then
        mockMvc.perform(post("/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Test Group"));
    }

    @Test
    @DisplayName("获取我的群组列表 - 成功")
    @WithUserDetails("testuser")
    void getMyGroups_Success() throws Exception {
        // Given
        List<GroupDTO> groups = Arrays.asList(testGroup);
        when(groupService.getUserGroups(anyLong())).thenReturn(groups);

        // When & Then
        mockMvc.perform(get("/v1/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Test Group"));
    }

    @Test
    @DisplayName("获取群组详情 - 成功")
    @WithUserDetails("testuser")
    void getGroupDetail_Success() throws Exception {
        // Given
        when(groupService.getGroupDTO(eq(1L), anyLong())).thenReturn(testGroup);

        // When & Then
        mockMvc.perform(get("/v1/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Test Group"));
    }

    @Test
    @DisplayName("获取群组详情 - 群组不存在")
    @WithUserDetails("testuser")
    void getGroupDetail_NotFound() throws Exception {
        // Given
        when(groupService.getGroupDTO(eq(999L), anyLong())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/v1/groups/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("获取群成员列表 - 成功")
    @WithUserDetails("testuser")
    void getGroupMembers_Success() throws Exception {
        // Given
        List<GroupMemberDTO> members = Arrays.asList(testMember);
        when(groupService.isMember(eq(1L), anyLong())).thenReturn(true);
        when(groupService.getGroupMembers(1L)).thenReturn(members);

        // When & Then
        mockMvc.perform(get("/v1/groups/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取群成员列表 - 非群成员")
    @WithUserDetails("testuser")
    void getGroupMembers_NotMember() throws Exception {
        // Given
        when(groupService.isMember(eq(1L), anyLong())).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/v1/groups/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("添加群成员 - 成功")
    @WithUserDetails("testuser")
    void addMember_Success() throws Exception {
        // Given
        Map<String, Long> request = new HashMap<>();
        request.put("userId", 3L);
        when(groupService.addMember(eq(1L), eq(3L), anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/v1/groups/1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("移除群成员 - 成功")
    @WithUserDetails("testuser")
    void removeMember_Success() throws Exception {
        // Given
        when(groupService.removeMember(eq(1L), eq(2L), anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/v1/groups/1/members/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("退出群组 - 成功")
    @WithUserDetails("testuser")
    void leaveGroup_Success() throws Exception {
        // Given
        when(groupService.leaveGroup(eq(1L), anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/v1/groups/1/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("退出群组 - 群主不能退出")
    @WithUserDetails("testuser")
    void leaveGroup_OwnerCannotLeave() throws Exception {
        // Given
        when(groupService.leaveGroup(eq(1L), anyLong())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/v1/groups/1/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("解散群组 - 成功")
    @WithUserDetails("testuser")
    void dissolveGroup_Success() throws Exception {
        // Given
        when(groupService.dissolveGroup(eq(1L), anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/v1/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取群聊天记录 - 成功")
    @WithUserDetails("testuser")
    void getMessages_Success() throws Exception {
        // Given
        List<GroupMessageDTO> messages = Arrays.asList(testMessage);
        when(groupService.getMessages(eq(1L), anyLong(), eq(0), eq(50))).thenReturn(messages);

        // When & Then
        mockMvc.perform(get("/v1/groups/1/messages")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].content").value("Hello Group!"));
    }

    @Test
    @DisplayName("发送群消息 - 成功")
    @WithUserDetails("testuser")
    void sendMessage_Success() throws Exception {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("content", "Hello!");
        request.put("contentType", "TEXT");
        when(groupService.sendMessage(eq(1L), anyLong(), eq("Hello!"), eq(MessageContentType.TEXT)))
                .thenReturn(testMessage);

        // When & Then
        mockMvc.perform(post("/v1/groups/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("设置管理员 - 成功")
    @WithUserDetails("testuser")
    void setAdmin_Success() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 2L);
        request.put("isAdmin", true);
        when(groupService.setAdmin(eq(1L), eq(2L), eq(true), anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/v1/groups/1/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("转让群主 - 成功")
    @WithUserDetails("testuser")
    void transferOwner_Success() throws Exception {
        // Given
        Map<String, Long> request = new HashMap<>();
        request.put("newOwnerId", 2L);
        when(groupService.transferOwner(eq(1L), eq(2L), anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/v1/groups/1/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("禁言成员 - 成功")
    @WithUserDetails("testuser")
    void setMuted_Success() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("userId", 2L);
        request.put("muted", true);
        when(groupService.setMuted(eq(1L), eq(2L), eq(true), anyLong())).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/v1/groups/1/mute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("邀请好友加入群聊 - 成功")
    @WithUserDetails("testuser")
    void inviteMembers_Success() throws Exception {
        // Given
        InviteMembersRequest request = new InviteMembersRequest();
        request.setGroupId(1L);
        request.setUserIds(Arrays.asList(3L, 4L));

        InviteMembersResult result = new InviteMembersResult();
        result.setSuccessCount(2);
        result.setFailCount(0);
        result.setMessage("成功邀请 2 位好友加入群聊");

        when(groupService.inviteMembers(eq(1L), anyList(), anyLong())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/v1/groups/1/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.successCount").value(2));
    }

    @Test
    @DisplayName("获取群聊未读消息数 - 成功")
    @WithUserDetails("testuser")
    void getUnreadCounts_Success() throws Exception {
        // Given
        Map<Long, Integer> unreadCounts = new HashMap<>();
        unreadCounts.put(1L, 5);
        unreadCounts.put(2L, 3);
        when(groupService.getUnreadCounts(anyLong())).thenReturn(unreadCounts);

        // When & Then
        mockMvc.perform(get("/v1/groups/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    @DisplayName("标记群消息已读 - 成功")
    @WithUserDetails("testuser")
    void markAsRead_Success() throws Exception {
        // Given
        doNothing().when(groupService).markAsRead(eq(1L), anyLong());

        // When & Then
        mockMvc.perform(post("/v1/groups/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(groupService).markAsRead(eq(1L), anyLong());
    }

    @Test
    @DisplayName("未认证访问 - 拒绝")
    void unauthenticated_AccessDenied() throws Exception {
        // When & Then
        mockMvc.perform(get("/v1/groups"))
                .andExpect(status().isForbidden());
    }
}
