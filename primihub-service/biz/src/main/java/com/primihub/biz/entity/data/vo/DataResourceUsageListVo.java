package com.primihub.biz.entity.data.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author terrence
 * @date 2023/11/10
 **/
@Data
public class DataResourceUsageListVo {
    private String taskName;
    private Long taskId;
    private Long productId;
    private String productName;
    // 使用时间
    private Date useTime;
    private Long taskStartTime;
}
