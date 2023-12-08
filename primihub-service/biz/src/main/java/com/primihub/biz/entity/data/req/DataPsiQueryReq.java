package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataPsiQueryReq extends PageReq{
    private String resultName;
    private String organId;
    private String taskName;
    private Integer taskState;
    private String startDate;
    private String endDate;
    /**
     * 0: 个人上传数据列表
     * 1: 整个机构数据列表
     */
    private Integer queryType = 0;
    private Long userId;
}
