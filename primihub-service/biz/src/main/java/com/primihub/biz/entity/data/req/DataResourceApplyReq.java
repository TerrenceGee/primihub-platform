package com.primihub.biz.entity.data.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * 数据资源申请
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataResourceApplyReq {
    /**
     * 1.user   2.organ
     */
    private Integer assignType;
    /**
     * 相对应的id
     */
    private Long id;
    /**
     * 对应的操作，1是同意，2是拒绝
     */
    private Integer auditStatus;
    /**
     * 授权通过时间
     */
    private Date assignTime;
    /**
     * 资源Id
     */
    private Long resourceId;
}
