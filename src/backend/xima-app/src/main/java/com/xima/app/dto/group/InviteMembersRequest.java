package com.xima.app.dto.group;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 邀请好友加入群聊请求
 */
@Data
public class InviteMembersRequest {
    
    @NotNull(message = "群组ID不能为空")
    private Long groupId;
    
    @NotEmpty(message = "邀请成员列表不能为空")
    private List<Long> userIds;
}
