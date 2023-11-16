package com.primihub.biz.entity.data.po;

import com.primihub.biz.entity.data.req.DataResourceAssignReq;
import lombok.*;


/**
 * @author terrence
 * @date 2023/11/08
 **/
@Data
public class DataResourceOrganAssign {
    private Long id;
    private String resourceFusionId;
    // 资源所属机构
    private String resourceOrganId;
    // 资源申请机构或者叫资源授予机构
    private String organId;
    /**
     * 授权状态
     * 0.申请 1.申请通过，由所有者直接授予 2.申请拒绝
     */
    private Integer assignStatus;

}
