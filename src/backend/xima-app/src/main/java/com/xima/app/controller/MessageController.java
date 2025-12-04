package com.xima.app.controller;

import com.xima.app.common.Result;
import com.xima.app.dto.message.ChatMessageDTO;
import com.xima.app.dto.message.SearchMessageDTO;
import com.xima.app.security.UserDetailsImpl;
import com.xima.app.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 */
@Tag(name = "消息管理", description = "聊天消息相关接口")
@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "获取聊天历史", description = "获取与指定用户的聊天历史记录")
    @GetMapping("/history/{friendId}")
    public Result<List<ChatMessageDTO>> getChatHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ChatMessageDTO> messages = messageService.getChatHistory(
                userDetails.getId(), friendId, page, size);
        return Result.success(messages);
    }

    @Operation(summary = "获取离线消息", description = "获取用户的离线消息")
    @GetMapping("/offline")
    public Result<List<ChatMessageDTO>> getOfflineMessages(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<ChatMessageDTO> messages = messageService.getOfflineMessages(userDetails.getId());
        return Result.success(messages);
    }

    @Operation(summary = "标记消息已读", description = "将指定消息标记为已读")
    @PostMapping("/{messageId}/read")
    public Result<String> markAsRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
        return Result.success("已标记为已读", null);
    }

    @Operation(summary = "获取未读消息数", description = "获取与指定用户的未读消息数量")
    @GetMapping("/unread/count/{senderId}")
    public Result<Long> getUnreadCount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long senderId) {
        long count = messageService.getUnreadCount(userDetails.getId(), senderId);
        return Result.success(count);
    }

    @Operation(summary = "删除消息", description = "删除指定的消息")
    @DeleteMapping("/{messageId}")
    public Result<String> deleteMessage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long messageId) {
        messageService.deleteMessage(userDetails.getId(), messageId);
        return Result.success("消息已删除", null);
    }

    @Operation(summary = "搜索消息", description = "搜索聊天记录（私聊+群聊）")
    @GetMapping("/search")
    public Result<List<SearchMessageDTO>> searchMessages(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<SearchMessageDTO> messages = messageService.searchAllMessages(
                userDetails.getId(), keyword, page, size);
        return Result.success(messages);
    }
}
