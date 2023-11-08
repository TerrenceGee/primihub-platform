package com.primihub.biz.entity.data.po;

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
@AllArgsConstructor
@NoArgsConstructor
public class DataResourceOrganAssign {
    private Long id;
    private Long resourceId;
    private String organGlobalId;
    private String organName;
    private Integer isDel = 0;
    /**
     * 创建时间
     */
    private Date cTime;
    /**
     * 修改时间
     */
    private Date uTime;

    public DataResourceOrganAssign(Long resourceId, String organGlobalId, String organName) {
        this.isDel = 0;
        this.resourceId = resourceId;
        this.organGlobalId = organGlobalId;
        this.organName = organName;
    }
}
