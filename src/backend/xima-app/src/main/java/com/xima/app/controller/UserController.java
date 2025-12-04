package com.xima.app.controller;

import com.xima.app.common.Result;
import com.xima.app.dto.user.UpdateUserRequest;
import com.xima.app.dto.user.UserDTO;
import com.xima.app.entity.UserStatus;
import com.xima.app.security.UserDetailsImpl;
import com.xima.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息查询、修改等接口")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.upload.avatar-dir:C:/Users/23725/Desktop/Xima/uploads/avatars}")
    private String avatarDir;

    @Value("${app.upload.background-dir:C:/Users/23725/Desktop/Xima/uploads/backgrounds}")
    private String backgroundDir;

    @Value("${file.upload-dir:C:/Users/23725/Desktop/Xima/uploads/}")
    private String baseUploadDir;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserDTO user = userService.getUserById(userDetails.getId());
        return Result.success(user);
    }

    @Operation(summary = "更新当前用户信息", description = "更新当前登录用户的个人信息")
    @PutMapping("/me")
    public Result<UserDTO> updateCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUser(userDetails.getId(), request);
        return Result.success("更新成功", user);
    }

    @Operation(summary = "更新用户状态", description = "更新当前用户的在线状态")
    @PutMapping("/me/status")
    public Result<Void> updateStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam UserStatus status) {
        userService.updateUserStatus(userDetails.getId(), status);
        return Result.success();
    }

    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户信息")
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "搜索用户", description = "根据关键字搜索用户")
    @GetMapping("/search")
    public Result<List<UserDTO>> searchUsers(@RequestParam String keyword) {
        List<UserDTO> users = userService.searchUsers(keyword);
        return Result.success(users);
    }

    @Operation(summary = "获取所有用户", description = "获取所有用户列表（需要管理员权限）")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return Result.success(users);
    }

    @Operation(summary = "删除用户", description = "根据ID删除用户（需要管理员权限）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "上传头像", description = "上传用户头像图片")
    @PostMapping("/me/avatar")
    public Result<String> uploadAvatar(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // 验证文件
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }
        
        // 限制文件大小 2MB
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("文件大小不能超过2MB");
        }
        
        // 用户ID作为子目录
        Long userId = userDetails.getId();
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // 创建目录 uploads/avatars/{userId}/
        Path uploadPath = Paths.get(avatarDir, String.valueOf(userId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        // 生成访问URL
        String avatarUrl = "/api/uploads/avatars/" + userId + "/" + filename;
        
        // 更新用户头像
        userService.updateAvatar(userDetails.getId(), avatarUrl);
        
        return Result.success("头像上传成功", avatarUrl);
    }

    @Operation(summary = "上传聊天背景", description = "上传用户聊天背景图片")
    @PostMapping("/me/background")
    public Result<String> uploadBackground(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // 验证文件
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }
        
        // 限制文件大小 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }
        
        // 用户ID作为子目录
        Long userId = userDetails.getId();
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // 创建目录 {backgroundDir}/{userId}/
        Path uploadPath = Paths.get(backgroundDir, String.valueOf(userId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        // 生成访问URL
        String backgroundUrl = "/api/uploads/backgrounds/" + userId + "/" + filename;
        
        return Result.success("背景上传成功", backgroundUrl);
    }

    @Operation(summary = "上传聊天图片", description = "上传聊天中发送的图片")
    @PostMapping("/chat/image")
    public Result<String> uploadChatImage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片文件");
        }
        
        // 限制文件大小 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }
        
        // 用户ID作为子目录
        Long userId = userDetails.getId();
        
        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // 创建目录 {baseUploadDir}/chat/images/{userId}/
        Path uploadPath = Paths.get(baseUploadDir, "chat/images", String.valueOf(userId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        // 生成访问URL
        String imageUrl = "/api/uploads/chat/images/" + userId + "/" + filename;
        
        return Result.success("图片上传成功", imageUrl);
    }

    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    @PutMapping("/me/password")
    public Result<String> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody java.util.Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("请输入原密码");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }
        
        boolean success = userService.changePassword(userDetails.getId(), oldPassword, newPassword);
        if (success) {
            return Result.success("密码修改成功", null);
        } else {
            return Result.error("原密码错误");
        }
    }

    @Operation(summary = "上传聊天文件", description = "上传聊天中发送的文件")
    @PostMapping("/chat/file")
    public Result<java.util.Map<String, String>> uploadChatFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }
        
        // 限制文件大小 20MB
        if (file.getSize() > 20 * 1024 * 1024) {
            return Result.error("文件大小不能超过20MB");
        }
        
        // 用户ID作为子目录
        Long userId = userDetails.getId();
        
        // 生成文件名（保留原始文件名用于显示）
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String filename = UUID.randomUUID().toString() + extension;
        
        // 创建目录 {baseUploadDir}/chat/files/{userId}/
        Path uploadPath = Paths.get(baseUploadDir, "chat/files", String.valueOf(userId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 保存文件
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        // 生成访问URL
        String fileUrl = "/api/uploads/chat/files/" + userId + "/" + filename;
        
        // 返回文件信息
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("url", fileUrl);
        result.put("name", originalFilename);
        result.put("size", String.valueOf(file.getSize()));
        
        return Result.success("文件上传成功", result);
    }

    @Operation(summary = "更新聊天背景", description = "更新当前用户的聊天背景设置")
    @PutMapping("/me/chat-background")
    public Result<String> updateChatBackground(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody java.util.Map<String, String> request) {
        String chatBackground = request.get("chatBackground");
        userService.updateChatBackground(userDetails.getId(), chatBackground);
        return Result.success("聊天背景更新成功", chatBackground);
    }

    @Operation(summary = "获取聊天背景", description = "获取当前用户的聊天背景设置")
    @GetMapping("/me/chat-background")
    public Result<String> getChatBackground(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String chatBackground = userService.getChatBackground(userDetails.getId());
        return Result.success(chatBackground);
    }
}
