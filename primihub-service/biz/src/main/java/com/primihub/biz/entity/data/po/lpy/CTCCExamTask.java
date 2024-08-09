package com.primihub.biz.entity.data.po.lpy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class CTCCExamTask {

    private Long id;

    /** 对应对方的任务id */
    private String taskId;

    /** 对应对方的任务id */
    private String taskName;

    /** 对应对方的任务id */
    private String originOrganId;

    /** 对应对方的任务id */
    private String targetOrganId;

    /** 对应对方的任务id */
    private String originResourceId;

    /** 对应对方的任务id */
    private String targetResourceId;
    /**
     * 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0
     */
    private Integer taskState;
    /**
     * state 0刚创建，待上传 1已上传，待运行 2已成功，已无效
     */
    private Integer state;

    @JsonIgnore
    private Integer isDel;

    @JsonIgnore
    private Date createDate;

    @JsonIgnore
    private Date updateDate;

}
