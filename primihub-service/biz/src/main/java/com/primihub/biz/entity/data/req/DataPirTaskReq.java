package com.primihub.biz.entity.data.req;

import lombok.Data;

/**
 * pir任务参数
 */
@Data
public class DataPirTaskReq extends PageReq {

    private String serverAddress;

    private String organName;

    private String resourceName;

    private String retrievalId;

    /**
     * 任务状态(0未开始 1成功 2查询中 3失败)
     */
    private Integer taskState;

    private String taskId;

    private String taskName;

    private String startDate;
    private String endDate;
    private Long userId;

    /**
     * USER:个人上传数据列表
     * ORGAN:整个机构数据列表
     */
    private String queryType = "USER";
}
