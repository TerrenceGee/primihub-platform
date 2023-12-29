package com.primihub.biz.entity.data.req;

import lombok.Getter;
import lombok.Setter;

/**
 * @author terrence
 * @date 2023/11/08
 **/
@Getter
@Setter
public class DataResourceAssignReq {
    private Long resourceId;
    private String resourceFusionId;
    private String organId;
    /** 授权用户id */
    private Long userId;
    /** 审核结果 */
    private Integer auditStatus;
    /** 1机构 2用户 */
    private Integer type;
    private Long id;

    private String timestamp;
    private String nonce;
    private String token;
}
