package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.ChainInfoParam;
import com.primihub.biz.service.sys.SysChainService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import scala.annotation.meta.param;

@Api(value = "链信息接口", tags = "链信息接口")
@RequestMapping("chain")
@RestController
public class ChainController {

    @Autowired
    private SysChainService sysChainService;

    @GetMapping("getChainInfo")
    public BaseResultEntity getChainInfo(@RequestHeader("userId") Long userId, @RequestHeader("roleType") Integer roleType) {
        return sysChainService.getChainInfo();
    }

    /**
     * 添加修改链信息
     * @param param 链信息
     * @return
     */
    @PostMapping(value = "saveOrUpdateChainInfo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResultEntity saveOrUpdateChainInfo(@RequestBody ChainInfoParam param) {
        if (param.getUsername() == null || param.getUsername().isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "username");
        }
        if (param.getAddress() == null || param.getAddress().isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "address");
        }
        return sysChainService.saveOrUpdateChainInfo(param);
    }

    /**
     * 此逻辑不提供接口，但封装上链的逻辑
     */
    @PostMapping("saveChainDataResource")
    public BaseResultEntity saveChainDataResource(Long resourceId) {
        if (resourceId == null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "resourceId");
        }
        return sysChainService.saveChainDataResource(resourceId);
    }

    @GetMapping("getChainDataResource")
    public BaseResultEntity getChainDataResource(String tradeHashCode) {
        if (tradeHashCode == null || tradeHashCode.isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "tradeHashCode");
        }
        return sysChainService.getChainDataResource(tradeHashCode);
    }
}
