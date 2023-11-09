package com.primihub.biz.entity.sys.param;

import lombok.Data;

/**
 * 查询用户列表参数
 */
@Data
public class FindUserPageParam {
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 角色id
     */
    private Long roleId;
}
