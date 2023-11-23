package com.primihub.biz.entity.data.req;

import lombok.Data;

/**
 * 衍生资源查询条件、分页信息
 */
@Data
public class DerivationResourceReq extends PageReq {
    /**
     * 资源Id
     */
    private Long resourceId;
    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 资源标签
     */
    private String tag;
    /**
     * 任务名称
     */
    private String taskIdName;
    /**
     * 项目Id
     */
    private Long projectId;
    /**
     * 开始时间
     */
    private String startDate;
    /**
     * 结尾时间
     */
    private String endDate;
    /**
     * 0 个人资源列表
     * 1 机构资源列表
     */
    private Integer queryType;
    /**
     * userId
     */
    private Long userId;
}
