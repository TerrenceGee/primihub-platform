package com.primihub.biz.entity.data.dataenum;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum DataResourceAuthTypeEnum {
    PUBLIC(1,"公开"),
    PRIVATE(2,"私有"),
    ASSIGN(3,"指定机构"),
    ;
    private Integer authType;
    private String desc;

    public static Map<Integer, DataResourceAuthTypeEnum> AUTH_TYPE_MAP=new HashMap(){
        {
            for (DataResourceAuthTypeEnum e: DataResourceAuthTypeEnum.values()){
                put(e.authType,e);
            }
        }
    };

    DataResourceAuthTypeEnum(Integer authType, String desc) {
        this.authType = authType;
        this.desc = desc;
    }
}
