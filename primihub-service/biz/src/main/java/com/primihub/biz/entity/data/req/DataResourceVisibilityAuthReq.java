package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.Date;

@Data
public class DataResourceVisibilityAuthReq {
    private String resourceFusionId;
    private String organGlobalId;
    private String organName;
    /** 0申请中 1通过 2拒绝 */
    private Integer auditStatus;
    /** 申请时间 */
    private Date applyTime;
    /** 授权时间 */
    private Date assignTime;

    public DataResourceVisibilityAuthReq() {
    }
}
