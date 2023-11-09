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
     * 1.user   2.organ
     */
    private String assignType;
    /**
     * 授权的用户Id
     */
    private Long dataResourceAssignUserId;
    /**
     * 授权的机构Id
     */
    private Long dataResourceAssignOrganId;
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
    private Integer assignStatus;
}
