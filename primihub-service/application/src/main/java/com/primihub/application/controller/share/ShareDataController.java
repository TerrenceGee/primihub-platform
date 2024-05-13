package com.primihub.application.controller.share;


import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dto.DataFusionCopyDto;
import com.primihub.biz.entity.data.po.PirRecord;
import com.primihub.biz.entity.data.po.PsiRecord;
import com.primihub.biz.entity.data.po.ScoreModel;
import com.primihub.biz.entity.data.req.DataPirCopyReq;
import com.primihub.biz.entity.data.req.DataPirReq;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.entity.sys.po.DataSet;
import com.primihub.biz.service.data.*;
import com.primihub.biz.service.share.ShareService;
import com.primihub.biz.service.sys.SysOrganService;
import com.primihub.biz.service.test.TestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Api(value = "数据同步接口 - 多节点可用,单节点不可用",tags = "数据同步接口")
@RequestMapping("shareData")
@RestController
@Slf4j
public class ShareDataController {

    @Autowired
    private DataProjectService dataProjectService;
    @Autowired
    private DataModelService dataModelService;
    @Autowired
    private SysOrganService sysOrganService;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private TestService testService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private PirService pirService;
    @Autowired
    private RemoteClient remoteClient;
    @Autowired
    private RecordService recordService;

    @ApiOperation(value = "通信检测",httpMethod = "POST",consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/healthConnection")
    public BaseResultEntity healthConnection(@RequestBody Object time){
        log.info("healthConnection - {}",time);
        return BaseResultEntity.success(shareService.getServiceState());
    }

    /**
     * 创建编辑项目接口
     * @return
     */
    @PostMapping("syncProject")
    public BaseResultEntity syncProject(@RequestBody ShareProjectVo vo){
        return dataProjectService.syncProject(vo);
    }

    /**
     * 创建编辑项目接口
     * @return
     */
    @PostMapping("syncModel")
    public BaseResultEntity syncModel(@RequestBody ShareModelVo vo){
        return dataModelService.syncModel(vo);
    }

    @PostMapping("apply")
    public BaseResultEntity applyForJoinNode(@RequestBody Map<String,Object> info){
        if (info==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"info");
        }
        if (!info.containsKey("applyId")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"applyId");
        }
        if (!info.containsKey("organId")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (!info.containsKey("organName")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organName");
        }
        if (!info.containsKey("gateway")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"gateway");
        }
        if (!info.containsKey("publicKey")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"publicKey");
        }
        return sysOrganService.applyForJoinNode(info);
    }

    @PostMapping("saveFusionResource")
    public BaseResultEntity saveFusionResource(@RequestBody DataFusionCopyDto dto){
        if (dto==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dto null");
        }
        if (StringUtils.isEmpty(dto.getOrganId())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (StringUtils.isEmpty(dto.getCopyPart())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"CopyPart");
        }
        return dataResourceService.saveFusionResource(dto);
    }

    @PostMapping("batchSaveTestDataSet")
    public BaseResultEntity batchSaveTestDataSet(@RequestBody List<DataSet> dataSets){
        if (dataSets == null || dataSets.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"shareData - dataSets");
        }
        return testService.batchSaveTestDataSet(dataSets);
    }

    @ApiOperation(value = "网关通信检测",httpMethod = "POST",consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("verifyGateway")
    public BaseResultEntity verifyGatewayConnection(@RequestBody String uniqueIdentification){
        if (org.apache.commons.lang3.StringUtils.isBlank(uniqueIdentification)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"uniqueIdentification");
        }
        return sysOrganService.verifyGatewayConnection(uniqueIdentification);
    }

    /**
     * #2 处理
     */
    @PostMapping(value = "processPirPhase1")
    public BaseResultEntity processPirPhase1(@RequestBody DataPirCopyReq req) {
        return pirService.processPirPhase1(req);
    }

    /**
     * #3 提交phase2
     */
    @PostMapping(value = "submitPirPhase2")
    public BaseResultEntity submitPirPhase2(@RequestBody DataPirCopyReq req) {
        // 查询条件
        DataPirReq param = new DataPirReq();
        if (org.apache.commons.lang.StringUtils.isBlank(req.getTargetResourceId())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "resourceId");
        }
        if (org.apache.commons.lang.StringUtils.isBlank(req.getTaskName())) {
            req.setTaskName("PIR任务-" + req.getPsiRecordId());
        }
        param.setResourceId(req.getTargetResourceId());
        param.setTaskName(req.getTaskName());
        return pirService.submitPirPhase2(param, req);
    }

    /**
     * 发起方
     * @param req
     * @return
     */
    @PostMapping(value = "submitScoreModelType")
    public BaseResultEntity submitScoreModelType(@RequestBody ScoreModel req) {
        if (org.apache.commons.lang.StringUtils.isBlank(req.getScoreModelCode())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "ScoreModelCode");
        }
        if (org.apache.commons.lang.StringUtils.isBlank(req.getScoreModelName())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "ScoreModelName");
        }
        if (org.apache.commons.lang.StringUtils.isBlank(req.getScoreModelType())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "ScoreModelType");
        }
        if (org.apache.commons.lang.StringUtils.isBlank(req.getScoreKey())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "ScoreKey");
        }
        return remoteClient.submitScoreModelType(req);
    }

    /**
     * 协作方
     */
    @PostMapping(value = "submitPsiRecord")
    public BaseResultEntity submitPsiRecord(@RequestBody PsiRecord record) {
        return recordService.savePsiRecord(record);
    }

    /**
     * 协作方
     */
    @PostMapping(value = "submitPirRecord")
    public BaseResultEntity submitPirRecord(@RequestBody PirRecord record) {
        return recordService.savePirRecord(record);
    }
}
