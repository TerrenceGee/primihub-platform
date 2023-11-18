package com.primihub.biz.entity.data.req;

import lombok.Data;


/**
 * 中心节点数据集请求参数
 */
@Data
public class DataFResourceReq extends PageReq {
    private String resourceId;
    private String resourceName;
    private Integer resourceSource;
    private String organId;
    private String tagName;
    private Integer fileContainsY;
}
