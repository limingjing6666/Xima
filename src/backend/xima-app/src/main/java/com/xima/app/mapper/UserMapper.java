package com.xima.app.mapper;

import com.xima.app.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     */
    User findById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    User findByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(@Param("email") String email);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户信息
     */
    int update(User user);

    /**
     * 更新用户状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询所有用户
     */
    List<User> findAll();

    /**
     * 分页查询用户
     */
    List<User> findByPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户总数
     */
    long count();

    /**
     * 为用户添加角色
     */
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据角色名查询角色ID
     */
    Long findRoleIdByName(@Param("name") String name);

    /**
     * 更新用户头像
     */
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);

    /**
     * 更新聊天背景
     */
    int updateChatBackground(@Param("id") Long id, @Param("chatBackground") String chatBackground);
    
    /**
     * 重置所有用户状态为离线
     */
    int resetAllUsersStatus();
}
