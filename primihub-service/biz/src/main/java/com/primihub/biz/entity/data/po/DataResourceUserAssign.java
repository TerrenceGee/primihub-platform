package com.primihub.biz.entity.data.po;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author terrence
 * @date 2023/11/08
 **/
@Getter
@Setter
public class DataResourceUserAssign {
    private Long id;
    private Long resourceId;
    private String organGlobalId;
    private String organName;
    private Long userId;
    private String userName;
    private String userAccount;
    private Integer isDel = 0;
    /**
     * 创建时间
     */
    private Date cTime;
    /**
     * 修改时间
     */
    private Date uTime;

    public DataResourceUserAssign(Long resourceId, String organGlobalId, String organName, Long userId, String userName, String userAccount) {
        this.isDel = 0;
        this.resourceId = resourceId;
        this.organGlobalId = organGlobalId;
        this.organName = organName;
        this.userId = userId;
        this.userName = userName;
        this.userAccount = userAccount;
    }
}
