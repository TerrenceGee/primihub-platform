package com.primihub.biz.entity.data.vo.lpy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CtccExamTaskVo {
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
    /** 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0 */
    private Integer taskState;
    private String targetField;
    /** filePath */
    private String fileUrl;
    private String fileName;
    @JsonIgnore
    private Integer isDel;
    @JsonIgnore
    private Date createDate;
    @JsonIgnore
    private Date updateDate;
}
