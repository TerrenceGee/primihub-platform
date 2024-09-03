package com.primihub.biz.entity.sys.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LpyProperties {
    private List<String> mailAccount;
    private String mailSubject;
    private String mailText;
}
