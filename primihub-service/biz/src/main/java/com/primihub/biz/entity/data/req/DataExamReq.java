package com.primihub.biz.entity.data.req;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DataExamReq {
    /** 资源Id */
    private String resourceId;
    /** 任务名称 */
    private String taskName;
    /** 目标机构Id */
    private String targetOrganId;
    /** 原始机构Id */
    private String originOrganId;
    /** 任务Id */
    private String taskId;
    /** 任务状态 */
    private Integer taskState;
    /** 生成资源Id */
    private String targetResourceId;
    /** 字段及其值列表 */
    private Map<String, List<String>> fieldValueMap;
    /** 端也要使用 */
    private String targetField;
}

