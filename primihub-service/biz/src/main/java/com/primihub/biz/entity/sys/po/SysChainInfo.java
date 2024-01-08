package com.primihub.biz.entity.sys.po;

import com.primihub.biz.entity.sys.param.ChainInfoParam;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SysChainInfo {
    private Long id;
    private String username;
    private String address;
    private Integer isDel;
    private Date cTime;
    private Date uTime;

    public SysChainInfo() {
    }

    public SysChainInfo(ChainInfoParam param) {
        this.address = param.getAddress();
        this.username = param.getUsername();
    }
}
