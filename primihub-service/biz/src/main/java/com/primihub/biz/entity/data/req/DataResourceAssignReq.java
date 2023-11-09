package com.primihub.biz.entity.data.req;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author terrence
 * @date 2023/11/08
 **/
@Getter
@Setter
public class DataResourceAssignReq {
    // 对机构授权要同步到 fusion
    private List<DataResourceAssignOrganReq> assignOrganList;
    // 用户授权在 platform
    private List<DataResourceAssignUserReq> assignUserList;
}
