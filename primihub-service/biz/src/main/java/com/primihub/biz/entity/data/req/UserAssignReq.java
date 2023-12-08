package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.List;

@Data
public class UserAssignReq {
    private String resourceFusionId;
    private List<Long> userId;
}
