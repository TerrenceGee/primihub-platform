package com.primihub.biz.entity.data.req;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataResourceUsageReq extends PageReq{
    private Long resourceId;
    private String resourceFusionId;
}
