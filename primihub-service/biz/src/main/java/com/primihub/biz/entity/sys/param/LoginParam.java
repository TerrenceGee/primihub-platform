package com.primihub.biz.entity.sys.param;

import lombok.Data;

/**
 * 用户登录参数
 */
@Data
public class LoginParam extends BaseCaptchaParam {
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 用户验证Key
     */
    private String validateKeyName;
    /**
     * 授权公钥
     */
    private String authPublicKey;
}
