package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class DataResourceVisibilityAuth {
    private Long id;
    private Long resourceId;
    private String resourceFusionId;
    private String organGlobalId;
    private String organName;
    private Integer isDel;
    private Date cTime;
    private Date uTime;
    /** 0申请中 1通过 2拒绝 */
    private Integer auditStatus;
    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    /** 授权时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date assignTime;

    public DataResourceVisibilityAuth() {
    }

    public DataResourceVisibilityAuth(Long resourceId, String organGlobalId, String organName) {
        this.resourceId = resourceId;
        this.organGlobalId = organGlobalId;
        this.organName = organName;
        this.isDel = 0;
    }
}
