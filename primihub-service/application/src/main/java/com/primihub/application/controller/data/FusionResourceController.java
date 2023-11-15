package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.service.data.OtherBusinessesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("fusionResource")
@RestController
public class FusionResourceController {

    @Autowired
    private OtherBusinessesService otherBusinessesService;

    /**
     * Pir选择合作方的资源列表
     *
     * @param req
     * @return
     */
    @RequestMapping("getResourceList")
    public BaseResultEntity getResourceList(DataFResourceReq req,
                                            @RequestHeader("userId") Long userId,
                                            @RequestHeader("roleType") Integer roleType // 1.管理员 2.普通用户
                                            ) {
        return otherBusinessesService.getResourceList(req, userId, roleType);
    }

    @RequestMapping("getDataResource")
    public BaseResultEntity getDataResource(String resourceId) {
        if (StringUtils.isBlank(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "resourceId");
        }
        return otherBusinessesService.getDataResource(resourceId);
    }

    @RequestMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList() {
        return otherBusinessesService.getResourceTagList();
    }
}
