package com.xima.app.controller;

import com.xima.app.common.Result;
import com.xima.app.dto.group.*;
import com.xima.app.entity.MessageContentType;
import com.xima.app.security.UserDetailsImpl;
import com.xima.app.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 群组控制器
 */
@Slf4j
@Tag(name = "群组管理", description = "群组创建、管理、消息等接口")
@RestController
@RequestMapping("/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "创建群组")
    @PostMapping
    public Result<GroupDTO> createGroup(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateGroupRequest request) {
        try {
            log.info("创建群组请求: userId={}, name={}, memberIds={}", 
                userDetails.getId(), request.getName(), request.getMemberIds());
            GroupDTO group = groupService.createGroup(userDetails.getId(), request);
            return Result.success("群组创建成功", group);
        } catch (Exception e) {
            log.error("创建群组失败", e);
            return Result.error("创建群组失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取我的群组列表")
    @GetMapping
    public Result<List<GroupDTO>> getMyGroups(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<GroupDTO> groups = groupService.getUserGroups(userDetails.getId());
        return Result.success(groups);
    }

    @Operation(summary = "获取群聊未读消息数")
    @GetMapping("/unread")
    public Result<Map<Long, Integer>> getUnreadCounts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Map<Long, Integer> unreadCounts = groupService.getUnreadCounts(userDetails.getId());
        return Result.success(unreadCounts);
    }

    @Operation(summary = "标记群消息已读")
    @PostMapping("/{groupId}/read")
    public Result<Void> markAsRead(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId) {
        groupService.markAsRead(groupId, userDetails.getId());
        return Result.success("已标记已读", null);
    }

    @Operation(summary = "获取群组详情")
    @GetMapping("/{groupId}")
    public Result<GroupDTO> getGroupDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId) {
        GroupDTO group = groupService.getGroupDTO(groupId, userDetails.getId());
        if (group == null) {
            return Result.error("群组不存在");
        }
        return Result.success(group);
    }

    @Operation(summary = "更新群组信息")
    @PutMapping("/{groupId}")
    public Result<Void> updateGroup(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");
        String avatar = request.get("avatar");
        
        boolean success = groupService.updateGroup(groupId, userDetails.getId(), name, description, avatar);
        if (!success) {
            return Result.error("更新失败，权限不足或群组不存在");
        }
        return Result.success("更新成功", null);
    }

    @Operation(summary = "获取群成员列表")
    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberDTO>> getGroupMembers(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId) {
        if (!groupService.isMember(groupId, userDetails.getId())) {
            return Result.error("您不是该群成员");
        }
        List<GroupMemberDTO> members = groupService.getGroupMembers(groupId);
        return Result.success(members);
    }

    @Operation(summary = "添加群成员")
    @PostMapping("/{groupId}/members")
    public Result<Void> addMember(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @RequestBody Map<String, Long> request) {
        Long userId = request.get("userId");
        boolean success = groupService.addMember(groupId, userId, userDetails.getId());
        if (!success) {
            return Result.error("添加失败，权限不足或用户已是群成员");
        }
        return Result.success("添加成功", null);
    }

    @Operation(summary = "移除群成员")
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeMember(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        boolean success = groupService.removeMember(groupId, userId, userDetails.getId());
        if (!success) {
            return Result.error("移除失败，权限不足");
        }
        return Result.success("移除成功", null);
    }

    @Operation(summary = "退出群组")
    @PostMapping("/{groupId}/leave")
    public Result<Void> leaveGroup(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId) {
        boolean success = groupService.leaveGroup(groupId, userDetails.getId());
        if (!success) {
            return Result.error("退出失败，群主不能退出群组");
        }
        return Result.success("已退出群组", null);
    }

    @Operation(summary = "解散群组")
    @DeleteMapping("/{groupId}")
    public Result<Void> dissolveGroup(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId) {
        boolean success = groupService.dissolveGroup(groupId, userDetails.getId());
        if (!success) {
            return Result.error("解散失败，只有群主可以解散群组");
        }
        return Result.success("群组已解散", null);
    }

    @Operation(summary = "获取群聊天记录")
    @GetMapping("/{groupId}/messages")
    public Result<List<GroupMessageDTO>> getMessages(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<GroupMessageDTO> messages = groupService.getMessages(groupId, userDetails.getId(), page, size);
        return Result.success(messages);
    }

    @Operation(summary = "发送群消息")
    @PostMapping("/{groupId}/messages")
    public Result<GroupMessageDTO> sendMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @RequestBody Map<String, String> request) {
        String content = request.get("content");
        String contentTypeStr = request.getOrDefault("contentType", "TEXT");
        MessageContentType contentType = MessageContentType.valueOf(contentTypeStr);
        
        GroupMessageDTO message = groupService.sendMessage(groupId, userDetails.getId(), content, contentType);
        if (message == null) {
            return Result.error("发送失败，您不是该群成员");
        }
        return Result.success(message);
    }

    @Operation(summary = "设置/取消管理员")
    @PostMapping("/{groupId}/admin")
    public Result<Void> setAdmin(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long targetUserId = Long.valueOf(request.get("userId").toString());
        Boolean isAdmin = (Boolean) request.get("isAdmin");
        
        boolean success = groupService.setAdmin(groupId, targetUserId, isAdmin, userDetails.getId());
        if (!success) {
            return Result.error("操作失败，只有群主可以设置管理员");
        }
        return Result.success(isAdmin ? "已设为管理员" : "已取消管理员", null);
    }

    @Operation(summary = "转让群主")
    @PostMapping("/{groupId}/transfer")
    public Result<Void> transferOwner(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @RequestBody Map<String, Long> request) {
        Long newOwnerId = request.get("newOwnerId");
        
        boolean success = groupService.transferOwner(groupId, newOwnerId, userDetails.getId());
        if (!success) {
            return Result.error("转让失败，只有群主可以转让群主");
        }
        return Result.success("群主已转让", null);
    }

    @Operation(summary = "禁言/解除禁言成员")
    @PostMapping("/{groupId}/mute")
    public Result<Void> setMuted(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        Long targetUserId = Long.valueOf(request.get("userId").toString());
        Boolean muted = (Boolean) request.get("muted");
        
        boolean success = groupService.setMuted(groupId, targetUserId, muted, userDetails.getId());
        if (!success) {
            return Result.error("操作失败，权限不足");
        }
        return Result.success(muted ? "已禁言" : "已解除禁言", null);
    }

    @Operation(summary = "邀请好友加入群聊")
    @PostMapping("/{groupId}/invite")
    public Result<InviteMembersResult> inviteMembers(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody InviteMembersRequest request) {
        try {
            log.info("邀请好友加入群聊: userId={}, groupId={}, inviteUserIds={}", 
                userDetails.getId(), groupId, request.getUserIds());
            InviteMembersResult result = groupService.inviteMembers(groupId, request.getUserIds(), userDetails.getId());
            return Result.success(result.getMessage(), result);
        } catch (Exception e) {
            log.error("邀请好友加入群聊失败", e);
            return Result.error("邀请失败: " + e.getMessage());
        }
    }
}
