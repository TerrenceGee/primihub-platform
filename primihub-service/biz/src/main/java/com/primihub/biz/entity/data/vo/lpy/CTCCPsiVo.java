package com.primihub.biz.entity.data.vo.lpy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CTCCPsiVo {
    private String imei;

    public CTCCPsiVo() {
    }

    public CTCCPsiVo(String imei) {
        this.imei = imei;
    }
}
