package com.primihub.biz.entity.data.vo.lpy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CTCCPsiVo {
    private String ctcc;

    public CTCCPsiVo() {
    }

    public CTCCPsiVo(String ctcc) {
        this.ctcc = ctcc;
    }
}
