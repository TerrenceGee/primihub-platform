package com.primihub.biz.entity.sys.param;

import lombok.Data;

/**
 * 发送验证码参数
 */
@Data
public class SendVerificationCodeParam {
    /**
     * 邮件地址
     */
    private String email;
    /**
     * 手机号码
     */
    private String cellphone;
    /**
     * 码类型
     */
    private Integer codeType;
}
