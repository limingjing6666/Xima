package com.xima.app.dto.group;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 创建群组请求
 */
@Data
public class CreateGroupRequest {
    @NotBlank(message = "群名称不能为空")
    @Size(max = 100, message = "群名称最多100个字符")
    private String name;
    
    @Size(max = 500, message = "群描述最多500个字符")
    private String description;
    
    // 初始成员ID列表（不包含创建者）
    private List<Long> memberIds;
}
