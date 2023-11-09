package com.primihub.biz.entity.data.req;

import lombok.Data;

/**
 * 分页数据
 */
@Data
public class PageReq {
    /**
     * 页码
     */
    private Integer pageNo = 1;
    /**
     * 页大小
     */
    private Integer pageSize = 5;
    /**
     * 偏移量
     */
    private Integer offset;

    public Integer getOffset() {
        return (pageNo-1)*pageSize;
    }
}
