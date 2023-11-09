package com.primihub.biz.entity.sys.enumeration;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


/**
 * @author terrence
 * @date 2023/11/07
 **/
@Getter
public enum RoleTypeEnum {
    ORGAN_ADMIN(1, "机构管理员"),
    PLAIN_USER(2, "普通用户");

    private final Integer roleCode;
    private final String roleName;

    RoleTypeEnum(Integer roleCode, String roleName) {
        this.roleCode = roleCode;
        this.roleName = roleName;
    }

    public static final List<Integer> ROLE_TYPE_CODE_LIST = new ArrayList<Integer>(){
        {
            for (RoleTypeEnum e:RoleTypeEnum.values()){
                add(e.getRoleCode());
            }
        }
    };

}
