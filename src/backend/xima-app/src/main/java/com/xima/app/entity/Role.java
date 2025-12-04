package com.xima.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    private Long id;
    private RoleType name;
    
    public Role(RoleType name) {
        this.name = name;
    }
}