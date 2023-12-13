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
    private String resourceFusionId;
    private String organId;
    private String timestamp;
    private String nonce;
}
