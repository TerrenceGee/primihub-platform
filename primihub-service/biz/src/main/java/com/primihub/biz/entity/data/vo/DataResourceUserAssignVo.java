package com.primihub.biz.entity.data.vo;

import com.primihub.biz.entity.data.po.DataResourceUserAssign;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataResourceUserAssignVo extends DataResourceUserAssign {
    private String userName;
    private String organName;

    public DataResourceUserAssignVo(DataResourceUserAssign po) {
        this.setApplyTime(po.getApplyTime());
        this.setAssignTime(po.getAssignTime());
        this.setAuditStatus(po.getAuditStatus());
        this.setCTime(po.getCTime());
        this.setId(po.getId());
        this.setOperateUserId(po.getOperateUserId());
        this.setResourceFusionId(po.getResourceFusionId());
        this.setResourceId(po.getResourceId());
        this.setResourceOrganId(po.getResourceOrganId());
        this.setUserId(po.getUserId());
        this.setUTime(po.getUTime());
    }
}
