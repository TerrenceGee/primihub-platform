package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.List;

@Data
public class AssignReq {
    private Long resourceId;
    private String resourceFusionId;
    private List<DataSourceOrganReq> fusionOrganList;
    private List<DataResourceAssignUserReq> userAssignList;
}
