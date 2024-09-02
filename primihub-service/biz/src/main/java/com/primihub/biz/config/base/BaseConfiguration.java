package com.primihub.biz.config.base;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import com.primihub.biz.entity.sys.config.BaseAuthConfig;
import com.primihub.biz.entity.sys.config.LokiConfig;
import com.primihub.biz.entity.sys.config.LpyProperties;
import com.primihub.sdk.config.GrpcClientConfig;
import com.primihub.sdk.config.GrpcProxyConfig;
import com.primihub.sdk.config.LpyGrpcConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Component
@NacosConfigurationProperties(dataId = "base.json", autoRefreshed = true)
public class BaseConfiguration {
    private Set<String> tokenValidateUriBlackList;
    private Set<String> needSignUriList;
    private Set<Long> adminUserIds;
    private String primihubOfficalService;
    private String defaultPassword;
    private String defaultPasswordVector;
    private GrpcClientConfig grpcClient;
    private GrpcProxyConfig grpcProxy;
    private LpyGrpcConfig lpyGrpcConfig;
    private Integer grpcServerPort;
    private String uploadUrlDirPrefix;
    private String resultUrlDirPrefix;
    private String runModelFileUrlDirPrefix;
    private String usefulToken;
    private String taskEmailSubject;
    /**
     * resource
     */
    private boolean displayDatabaseSourceType = false;
    /**
     * auth
     */
    private Map<String, BaseAuthConfig> authConfigs;
    /**
     * mail
     */
    private MailProperties mailProperties;
    /**
     * Use in mail text content
     */
    private String systemDomainName;
    /**
     * loki
     */
    private LokiConfig lokiConfig;
    /**
     * Open the nacos template for debugging
     */
    private Boolean openDynamicTuning = false;

    private Integer uploadSize = 10;
    /**
     * 注水开关
     */
    private Boolean waterSwitch = true;
    /**
     * 令牌云配置项
     */
    private LpyProperties lpyProperties;
}
