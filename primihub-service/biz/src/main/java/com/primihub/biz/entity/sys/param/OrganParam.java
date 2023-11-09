package com.primihub.biz.entity.sys.param;

import com.primihub.biz.entity.data.req.PageReq;
import lombok.Data;

/**
 * 查询合作列表参数
 */
@Data
public class OrganParam extends PageReq {
    /**
     * 机构Id
     */
    private String organId;
    /**
     * 机构名称
     */
    private String organName;
    /**
     * 0待审批 1同意 2拒绝
     */
    private Integer examineState;
}
