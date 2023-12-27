package com.primihub.biz.entity.data.req;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    /** 授权时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date assignTime;

    public DataResourceVisibilityAuthReq() {
    }
}
