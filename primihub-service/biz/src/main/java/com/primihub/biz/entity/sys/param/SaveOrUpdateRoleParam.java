package com.primihub.biz.entity.sys.param;

import lombok.Data;

/**
 * 添加或者修改角色参数
 */
@Data
public class SaveOrUpdateRoleParam {
    /**
     * 角色id
     */
    private Long roleId;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 授权的权限id
     */
    private Long[] grantAuthArray;
    /**
     * 取消授权的权限id
     */
    private Long[] cancelAuthArray;
    /**
     * 角色类型 1管理员 2普通用户
     */
    private Integer roleType;
}
