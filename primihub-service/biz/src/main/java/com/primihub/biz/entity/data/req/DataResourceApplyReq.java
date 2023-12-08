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
     * 对应的操作，1是同意，2是拒绝
     */
    private Integer auditStatus;
    /**
     * 资源Id
     */
    private Long resourceId;
    /**
     * 用户Id
     */
    private Long userId;
    /**
     * 机构Id
     */
    private String organId;
    /**
     * 0用户 1机构
     */
    private Integer queryType;
}
