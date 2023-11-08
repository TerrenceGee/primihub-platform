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
    ORGAN_ADMIN("机构管理员"),
    PLAIN_USER("普通用户");

    private final String roleName;

    RoleTypeEnum(String roleName) {
        this.roleName = roleName;
    }

    public static final List<String> ROLE_TYPE_LIST = new ArrayList<String>(){
        {
            for (RoleTypeEnum e:RoleTypeEnum.values()){
                add(e.name());
            }
        }
    };

}
