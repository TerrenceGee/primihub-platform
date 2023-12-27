package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分页数据
 */
@Data
@ApiModel("列表分页参数")
public class PageReq {
    /**
     * 页码
     */
    @ApiModelProperty(value = "第几页",example = "1")
    private Integer pageNo = 1;
    /**
     * 页大小
     */
    @ApiModelProperty(value = "每页条数",example = "20")
    private Integer pageSize = 5;
    /**
     * 偏移量
     */

    private Integer offset;

    public Integer getOffset() {
        return (pageNo-1)*pageSize;
    }
}
