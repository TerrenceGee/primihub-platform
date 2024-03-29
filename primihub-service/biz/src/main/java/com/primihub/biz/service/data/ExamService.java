package com.primihub.biz.service.data;


import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.convert.DataExamConvert;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.req.DataExamTaskReq;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.primarydb.sys.SysFilePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamService {

    private final Lock lock = new ReentrantLock();

    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private SysOrganSecondarydbRepository organSecondaryDbRepository;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private ThreadPoolTaskExecutor primaryThreadPool;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private SysFileSecondarydbRepository fileRepository;
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private SysFilePrimarydbRepository sysFilePrimarydbRepository;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private SingleTaskChannel singleTaskChannel;

    public BaseResultEntity<PageDataEntity<DataPirTaskVo>> getExamTaskList(DataExamTaskReq req) {
        List<DataExamTaskVo> dataExamTaskVos = dataTaskRepository.selectDataExamTaskPage(req);
        if (dataExamTaskVos.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),Collections.emptyList()));
        }
        Integer total = dataTaskRepository.selectDataExamTaskCount(req);
        return BaseResultEntity.success(new PageDataEntity(total,req.getPageSize(),req.getPageNo(),dataExamTaskVos));
    }

    public BaseResultEntity submitExamTask(DataExamReq param) {
        // getTargetData
        DataExamTask po = dataTaskRepository.selectDataExamByTaskId(param.getTaskId());
        String resourceId = po.getOriginResourceId();
        DataResource dataResource = dataResourceRepository.queryDataResourceByResourceFusionId(resourceId);
        if (dataResource == null) {
            log.info("预处理的资源查询为空 resourceId: [{}]", resourceId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "dataResource");
        }
        SysFile sysFile = fileRepository.selectSysFileByFileId(Optional.ofNullable(dataResource.getFileId()).orElse(0L));
        if (sysFile == null) {
            log.info("预处理的资源查询为空 sysFileId: [{}]", dataResource.getFileId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "sysFile");
        }
        BaseResultEntity<Set<String>> result = getDataResourceCsvTargetFieldList(sysFile);
        if (!BaseResultEntity.isSuccess(result)) {
            log.info("文件解析失败 sysFileId: [{}]", dataResource.getFileId());
            return result;
        }
        Set<String> targetFieldValueSet = result.getResult();

        DataExamReq req = DataExamConvert.convertPoToReq(po);
        req.setFieldValueSet(targetFieldValueSet);
        // 发送给对方机构
        return sendExamTask(param);
    }

    private  BaseResultEntity<Set<String>> getDataResourceCsvTargetFieldList(SysFile sysFile) {
        try {
            List<String> fileContent = FileUtil.getFileContent(sysFile.getFileUrl(), 1);
            if (fileContent==null|| fileContent.isEmpty()) {
                log.info("csv文件解析失败");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL);
            }
            String headersStr = fileContent.get(0);
            if (StringUtils.isBlank(headersStr)) {
                log.info("csv文件解析失败: 文件字段为空");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL);
            }
            String[] headers = headersStr.split(",");
            if (headers[0].startsWith(DataConstant.UTF8_BOM)) {
                headers[0] = headers[0].substring(1);
            }
            if (!Arrays.asList(headers).contains(DataConstant.INPUT_FIELD_NAME)) {
                log.info("该资源字段不包括目的字段: [{}]", DataConstant.INPUT_FIELD_NAME);
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL, DataConstant.INPUT_FIELD_NAME);
            }
            List<LinkedHashMap<String, Object>> csvData = FileUtil.getCsvData(sysFile.getFileUrl(), Math.toIntExact(sysFile.getFileSize()));
            // stream.filter 结果为ture的元素留下
            Set<String> targetFieldValueSet = csvData.stream().map(stringObjectLinkedHashMap -> stringObjectLinkedHashMap.getOrDefault(DataConstant.INPUT_FIELD_NAME, StringUtils.EMPTY)).map(String::valueOf).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            return BaseResultEntity.success(targetFieldValueSet);
        }catch (Exception e){
            log.info("fileUrl:[{}] Exception Message : {}",sysFile.getFileUrl(),e);
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL,"请检查文件编码格式");
        }
    }

    private BaseResultEntity sendExamTask(DataExamReq param) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(param.getTargetOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", param.getTargetOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(param, organ.getOrganGateway() + "/data/exam/processExamTask", organ.getPublicKey());
        }
        return null;
    }

    private DataExamTask saveExamTaskReq(DataExamReq param) {
        DataExamTask task = DataExamConvert.convertReqToPo(param, organConfiguration.getSysLocalOrganInfo());
        dataTaskPrRepository.saveDataExamTask(task);
        return task;
    }

    public BaseResultEntity processExamTask(DataExamReq req) {
        if (CollectionUtils.isEmpty(req.getFieldValueSet())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "fieldValue");
        }

        req.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        sendEndExamTask(req);

        // futureTask
        startFutureExamTask(req);

        return BaseResultEntity.success();
    }

    private DataResource generateTargetResource(Map returnMap) {
        List<HashMap<String, Object>> metaData = new ArrayList<>();

        SysFile sysFile = new SysFile();
        sysFile.setFileSource(1);
        sysFile.setFileSuffix("csv");
        sysFile.setFileName(UUID.randomUUID().toString());
        Date date=new Date();
        StringBuilder sb =new StringBuilder().append(baseConfiguration.getUploadUrlDirPrefix()).append(1)
                .append("/").append(DateUtil.formatDate(date,DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/");
        sysFile.setFileArea("local");
//        sysFile.setFileSize();
//        sysFile.setFileCurrentSize();
        sysFile.setIsDel(0);

        try {
            File tempFile=new File(sb.toString());
            if(!tempFile.exists()) {
                tempFile.mkdirs();
            }
            FileUtil.convertToCsv(metaData, sb.append(sysFile.getFileName()).append(".").append(sysFile.getFileSuffix()).toString());
            sysFile.setFileUrl(sb.append(sysFile.getFileName()).append(".").append(sysFile.getFileSuffix()).toString());
        } catch (IOException e) {
            log.error("upload",e);
            return null;
//            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"写硬盘失败");
        }

        sysFilePrimarydbRepository.insertSysFile(sysFile);

        // resourceFilePreview
        BaseResultEntity resultEntity = dataResourceService.getDataResourceCsvVo(sysFile);
        DataResourceCsvVo csvVo = (DataResourceCsvVo) resultEntity.getResult();

        Map<String,Object> map = new HashMap<>();
        try {
            DataResource po = new DataResource();
            po.setResourceName("生成资源");
            po.setResourceDesc("生成资源");
            po.setResourceAuthType(1);  // 公开
            po.setResourceSource(1);    // 文件
//            po.setUserId();
//            po.setOrganId();
            po.setFileId(sysFile.getFileId());
            po.setFileSize(0);
            po.setFileSuffix("");
            po.setFileColumns(0);
            po.setFileRows(0);
            po.setFileHandleStatus(0);
            po.setResourceNum(0);
            po.setDbId(0L);
            po.setUrl("");
//            po.setPublicOrganId();    // 可见机构列表
            po.setResourceState(0);
            List<DataFileFieldVo> fieldList = csvVo.getFieldList();
            BaseResultEntity handleDataResourceFileResult = null;
            DataSource dataSource = null;
            po.setFileId(sysFile.getFileId());
            po.setFileSize(sysFile.getFileSize().intValue());
            po.setFileSuffix(sysFile.getFileSuffix());
            po.setFileColumns(0);
            po.setFileRows(0);
            po.setFileHandleStatus(0);
            po.setResourceNum(0);
            po.setDbId(0L);
            po.setUrl(sysFile.getFileUrl());
//            po.setPublicOrganId();
            po.setResourceState(0);

            handleDataResourceFileResult = dataResourceService.handleDataResourceFile(po, sysFile.getFileUrl());
            if (handleDataResourceFileResult.getCode()!=0) {
                // todo 错误处理
//                return handleDataResourceFileResult;
                return null;
            }

            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            if (sysLocalOrganInfo!=null&&sysLocalOrganInfo.getOrganId()!=null&&!"".equals(sysLocalOrganInfo.getOrganId().trim())){
                po.setResourceFusionId(organConfiguration.generateUniqueCode());
            }
            List<DataFileField> dataFileFieldList = new ArrayList<>();
            for (DataFileFieldVo field : fieldList) {
                dataFileFieldList.add(DataResourceConvert.DataFileFieldVoConvertPo(field, 0L, po.getResourceId()));
            }
            TaskParam taskParam = dataResourceService.resourceSynGRPCDataSet(dataSource, po, dataFileFieldList);
            if (!taskParam.getSuccess()){
                // todo 错误处理
//                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"无法将资源注册到数据集中:"+taskParam.getError());
                return null;
            }
            if (dataSource!=null){
                dataResourcePrRepository.saveSource(dataSource);
                po.setDbId(dataSource.getId());
            }
            dataResourcePrRepository.saveResource(po);
            for (DataFileField field : dataFileFieldList) {
                field.setResourceId(po.getResourceId());
            }
            dataResourcePrRepository.saveResourceFileFieldBatch(dataFileFieldList);
            List<String> tags = new ArrayList<String>() {
                {
                    add("examine");
                }
            };
            for (String tagName : tags) {
                DataResourceTag dataResourceTag = new DataResourceTag(tagName);
                dataResourcePrRepository.saveResourceTag(dataResourceTag);
                dataResourcePrRepository.saveResourceTagRelation(dataResourceTag.getTagId(),po.getResourceId());
            }
            fusionResourceService.saveResource(organConfiguration.getSysLocalOrganId(),dataResourceService.findCopyResourceList(po.getResourceId(), po.getResourceId()));
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),po))).build());

            return po;
        }catch (Exception e){
            // todo
            log.info("save DataResource Exception：{}",e.getMessage());
            e.printStackTrace();
//            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
            return null;
        }
    }

    private Map getDataFromCMCCSource(String cmccScoreUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HashMap<String, Object>> request = new HttpEntity(new Object(), headers);
        ResponseEntity<Map> exchange = restTemplate.exchange(cmccScoreUrl, HttpMethod.POST, request, Map.class);
        return exchange.getBody();
    }

    private Map getDataFromFirstSource(String firstUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HashMap<String, Object>> request = new HttpEntity(new Object(), headers);
        ResponseEntity<Map> exchange = restTemplate.exchange(firstUrl, HttpMethod.POST, request, Map.class);
        return exchange.getBody();
    }

    private void startFutureExamTask(DataExamReq req) {
        // 进行预处理，使用异步
        FutureTask<Object> task = new FutureTask<>(() -> {
            Set<String> fieldValueSet = req.getFieldValueSet();
            Map resultMap = null;
            Map returnMap = null;
            try {
                resultMap = getDataFromFirstSource(DataConstant.FIRST_URL);
                returnMap = getDataFromCMCCSource(DataConstant.CMCC_SCORE_URL);
            } catch (Exception e) {
                log.info("处理预审核处理出错: [{}]", e.getMessage());
                req.setTaskState(TaskStateEnum.FAIL.getStateType());
                sendEndExamTask(req);
            }
            // 生成数据源
            DataResource dataResource = generateTargetResource(returnMap);

            req.setTaskState(TaskStateEnum.SUCCESS.getStateType());
            req.setTargetResourceId(dataResource.getResourceFusionId());
            sendEndExamTask(req);
            return null;
        });
        primaryThreadPool.submit(task);
    }

    private BaseResultEntity sendEndExamTask(DataExamReq req) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(req.getOriginOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", req.getOriginOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/data/exam/finishExamTask", organ.getPublicKey());
        }
        return null;
    }

    private BaseResultEntity getTargetResource(String resourceId, String organId) {
        BaseResultEntity fusionResult = fusionResourceService.getDataResource(resourceId, organId);
        if (fusionResult.getCode() != 0 || fusionResult.getResult() == null ) {
            log.info("未找到预处理源数据 resourceId: [{}]", resourceId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"resourceId");
        }
        return fusionResult;
    }

    @Transactional
    public BaseResultEntity finishExamTask(DataExamReq req) {
        try {
            lock.lock();
            updateExamTaskAfterSelect(req);
        } finally {
            lock.unlock();
        }
        return BaseResultEntity.success();
    }

    private void updateExamTaskAfterSelect(DataExamReq req) {
        DataExamTask task = dataTaskRepository.selectDataExamByTaskId(req.getTaskId());
        task.setTaskState(req.getTaskState());
        task.setTargetResourceId(req.getTargetResourceId());
        dataTaskPrRepository.updateDataExamTask(task);
    }

    public BaseResultEntity<DataPirTaskDetailVo> getExamTaskDetail(String taskId) {
        DataExamTask task = dataTaskRepository.selectDataExamByTaskId(taskId);
        if (task==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到任务信息");
        }
        DataExamTaskVo vo = DataExamConvert.convertPoToVo(task);
        return BaseResultEntity.success(vo);
    }

    public BaseResultEntity saveExamTask(DataExamReq req) {
        // 检查是否有目标字段
        DataResource dataResourcePo = dataResourceRepository.queryDataResourceByResourceFusionId(req.getResourceId());
        if (dataResourcePo == null) {
            log.info("资源查询失败, resourceId: [{}]", req.getResourceId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "dataResource");
        }
        List<DataFileField> dataFileFields = dataResourceRepository.queryDataFileFieldByResourceId(dataResourcePo.getResourceId());
        if (CollectionUtils.isEmpty(dataFileFields)) {
            log.info("资源查询失败, resourceId: [{}]", dataResourcePo.getResourceId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "dataFileFields");
        }
        Set<String> fieldNameSet = dataFileFields.stream().map(DataFileField::getFieldName).collect(Collectors.toSet());
        if (!fieldNameSet.contains(DataConstant.INPUT_FIELD_NAME)) {
            log.info("该数据资源缺乏目的字段, [{}]", DataConstant.INPUT_FIELD_NAME);
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, DataConstant.INPUT_FIELD_NAME);
        }

        req.setTaskId(String.valueOf(SnowflakeId.getInstance().nextId()));
        DataExamTask po = DataExamConvert.convertReqToPo(req, organConfiguration.getSysLocalOrganInfo());
        po.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTaskPrRepository.saveDataExamTask(po);
        return BaseResultEntity.success();
    }
}
