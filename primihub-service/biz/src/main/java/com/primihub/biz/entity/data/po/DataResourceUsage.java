package com.primihub.biz.entity.data.po;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 资源使用情况
 */
@Getter
@Setter
public class DataResourceUsage {
    private Long id;
    private Long resourceId;
    private String resourceFusionId;
    private Integer isDel = 0;
    private Date createDate;
    private Date updateDate;
    private String taskName;
    private Long taskId;
    private String dataProjectName;
    private Long dataProjectId;
    private Date usageTime;
    private String usageType;

    public DataResourceUsage() {
    }

    public DataResourceUsage(Long resourceId, String resourceFusionId, String taskName, Long taskId, String dataProjectName, Long dataProjectId, Date usageTime, String usageType) {
        this.resourceId = resourceId;
        this.resourceFusionId = resourceFusionId;
        this.isDel = 0;
        this.createDate = new Date();
        this.taskName = taskName;
        this.taskId = taskId;
        this.dataProjectName = dataProjectName;
        this.dataProjectId = dataProjectId;
        this.usageTime = usageTime;
        this.usageType = usageType;
    }
}
