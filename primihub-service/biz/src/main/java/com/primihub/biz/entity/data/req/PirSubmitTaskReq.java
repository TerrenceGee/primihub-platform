package com.primihub.biz.entity.data.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PirSubmitTaskReq {
    private String resourceId;
    private String pirParam;
    private String taskName;
}
