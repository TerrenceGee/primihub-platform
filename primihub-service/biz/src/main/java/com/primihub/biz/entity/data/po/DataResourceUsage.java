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
    private Integer isDel;
    private Date createDate;
    private Date updateDate;
    private String taskName;
    private Long taskId;
    private String dataProjectName;
    private Long dataProjectId;
    private Date usageTime;
    private String usageType;
}
