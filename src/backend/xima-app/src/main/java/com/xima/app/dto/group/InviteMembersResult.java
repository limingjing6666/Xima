package com.xima.app.dto.group;

import lombok.Data;
import java.util.List;

/**
 * 邀请好友加入群聊结果
 */
@Data
public class InviteMembersResult {
    
    private int successCount;           // 成功邀请人数
    private int failCount;              // 失败人数
    private List<Long> successUserIds;  // 成功邀请的用户ID
    private List<Long> failUserIds;     // 失败的用户ID（已是群成员等）
    private String message;             // 结果消息
}
