package com.xima.app.controller;

import com.xima.app.common.Result;
import com.xima.app.dto.friend.FriendDTO;
import com.xima.app.dto.friend.FriendRequestDTO;
import com.xima.app.dto.friend.FriendRequestInfoDTO;
import com.xima.app.security.UserDetailsImpl;
import com.xima.app.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 好友控制器
 */
@Tag(name = "好友管理", description = "好友请求、好友列表等接口")
@RestController
@RequestMapping("/v1/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Operation(summary = "发送好友请求", description = "向指定用户发送好友请求")
    @PostMapping("/request")
    public Result<String> sendFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody FriendRequestDTO request) {
        friendshipService.sendFriendRequest(userDetails.getId(), request.getFriendId());
        return Result.success("好友请求已发送", null);
    }

    @Operation(summary = "接受好友请求", description = "接受指定的好友请求")
    @PostMapping("/request/{requestId}/accept")
    public Result<String> acceptFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long requestId) {
        friendshipService.acceptFriendRequest(userDetails.getId(), requestId);
        return Result.success("已接受好友请求", null);
    }

    @Operation(summary = "拒绝好友请求", description = "拒绝指定的好友请求")
    @PostMapping("/request/{requestId}/reject")
    public Result<String> rejectFriendRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long requestId) {
        friendshipService.rejectFriendRequest(userDetails.getId(), requestId);
        return Result.success("已拒绝好友请求", null);
    }

    @Operation(summary = "删除好友", description = "删除指定的好友关系")
    @DeleteMapping("/{friendId}")
    public Result<String> deleteFriend(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId) {
        friendshipService.deleteFriend(userDetails.getId(), friendId);
        return Result.success("已删除好友", null);
    }

    @Operation(summary = "获取好友列表", description = "获取当前用户的好友列表")
    @GetMapping
    public Result<List<FriendDTO>> getFriendList(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<FriendDTO> friends = friendshipService.getFriendList(userDetails.getId());
        return Result.success(friends);
    }

    @Operation(summary = "获取待处理请求", description = "获取收到的待处理好友请求")
    @GetMapping("/requests/pending")
    public Result<List<FriendRequestInfoDTO>> getPendingRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<FriendRequestInfoDTO> requests = friendshipService.getPendingRequests(userDetails.getId());
        return Result.success(requests);
    }

    @Operation(summary = "获取已发送请求", description = "获取已发送的好友请求")
    @GetMapping("/requests/sent")
    public Result<List<FriendRequestInfoDTO>> getSentRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<FriendRequestInfoDTO> requests = friendshipService.getSentRequests(userDetails.getId());
        return Result.success(requests);
    }

    @Operation(summary = "检查好友关系", description = "检查与指定用户是否是好友")
    @GetMapping("/check/{friendId}")
    public Result<Boolean> checkFriendship(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId) {
        boolean isFriend = friendshipService.isFriend(userDetails.getId(), friendId);
        return Result.success(isFriend);
    }

    @Operation(summary = "获取好友数量", description = "获取当前用户的好友数量")
    @GetMapping("/count")
    public Result<Long> getFriendCount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        long count = friendshipService.getFriendCount(userDetails.getId());
        return Result.success(count);
    }

    @Operation(summary = "设置好友备注", description = "设置好友的备注名称")
    @PutMapping("/{friendId}/remark")
    public Result<String> setFriendRemark(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long friendId,
            @RequestBody java.util.Map<String, String> body) {
        String remark = body.get("remark");
        friendshipService.setFriendRemark(userDetails.getId(), friendId, remark);
        return Result.success("备注设置成功", null);
    }
}
