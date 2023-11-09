package com.primihub.biz.entity.sys.param;

import lombok.Data;

/**
 * 忘记密码参数
 */
@Data
public class ForgetPasswordParam {
    private String password;
    private String passwordAgain;
    private String verificationCode;
    private String userAccount;
}
