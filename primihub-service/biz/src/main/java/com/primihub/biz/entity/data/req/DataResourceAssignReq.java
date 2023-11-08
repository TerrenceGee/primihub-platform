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
    private List<DataResourceAssignOrganReq> assignOrganList;
    private List<DataResourceAssignUserReq> assignUserList;
}
