package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author terrence
 * @date 2023/11/08
 **/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataResourceUserAssign {
    private Long id;
    private Long resourceId;
    private String resourceFusionId;
    /**
     * 资源所属机构Id
     */
    private String resourceOrganId;
    private Long userId;
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
    /**
     * 创建时间
     */
    private Date cTime;
    /**
     * 修改时间
     */
    private Date uTime;

    /**
     * 授权人
     * @param resourceId
     * @param resourceGlobalId
     * @param resourceOrganId
     * @param userId
     */
    private Long operateUserId;

    public DataResourceUserAssign(Long resourceId, String resourceGlobalId, String resourceOrganId, Long userId) {
        this.resourceId = resourceId;
        this.resourceFusionId = resourceGlobalId;
        this.resourceOrganId = resourceOrganId;
        this.userId = userId;
    }

}
