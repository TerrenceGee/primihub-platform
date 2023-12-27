package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author terrence
 * @date 2023/11/10
 **/
@Data
public class DataResourceAssignmentListVo {
    /**
     * 目标资源
     */
    private String resourceFusionId;
    /**
     * 1.user   2.organ
     */
    private String assignType;
    /**
     * id，可能是用户授权id，也有可能是机构授权id
     */
    private Long id;
    /**
     * 授权的用户Id
     */
    private Long userId;
    /**
     * 授权的用户Id
     */
    private String userName;
    /**
     * 授权的机构Id
     */
    private String organId;
    /**
     * 授权机构名称
     */
    private String organName;
    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;
    /**
     * 授权通过时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date assignTime;
    /**
     * 授权状态
     * 0.申请 1.申请通过，由所有者直接授予 2.申请拒绝
     */
    private Integer auditStatus;
}
