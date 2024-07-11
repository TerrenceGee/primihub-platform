package com.primihub.biz.service.data;


import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.convert.DataTaskConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.base.DataPirKeyQuery;
import com.primihub.biz.entity.data.dataenum.PirPhase1Enum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataPirCopyReq;
import com.primihub.biz.entity.data.req.DataPirReq;
import com.primihub.biz.entity.data.req.DataPirTaskReq;
import com.primihub.biz.entity.data.req.ScoreModelReq;
import com.primihub.biz.entity.data.vo.DataPirTaskDetailVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.primarydb.data.RecordPrRepository;
import com.primihub.biz.repository.primarydb.data.ResultPrRepository;
import com.primihub.biz.repository.primarydb.data.ScoreModelPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.repository.secondarydb.data.RecordRepository;
import com.primihub.biz.repository.secondarydb.data.ScoreModelRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.data.pirphase1.PirPhase1Execute;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PirService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private DataAsyncService dataAsyncService;
    @Qualifier("recordRepository")
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private DataPsiRepository dataPsiRepository;
    @Autowired
    private SysOrganSecondarydbRepository organSecondaryDbRepository;
    @Autowired
    private RecordPrRepository recordPrRepository;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private ScoreModelRepository scoreModelRepository;
    @Autowired
    private ScoreModelPrRepository scoreModelPrRepository;
    @Autowired
    private ResultPrRepository resultPrRepository;
    @Autowired
    private ThreadPoolTaskExecutor primaryThreadPool;

    public String getResultFilePath(String taskId, String taskDate) {
        return new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(taskDate).append("/").append(taskId).append(".csv").toString();
    }

    public BaseResultEntity pirSubmitTask(DataPirReq req, String pirParam) {
        BaseResultEntity dataResource = otherBusinessesService.getDataResource(req.getResourceId());
        if (dataResource.getCode() != 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "资源查询失败");
        }
        Map<String, Object> pirDataResource = (LinkedHashMap) dataResource.getResult();
        int available = Integer.parseInt(pirDataResource.getOrDefault("available", "1").toString());
        if (available == 1) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "资源不可用");
        }
        // dataResource columnName list
        String resourceColumnNames = pirDataResource.getOrDefault("resourceColumnNameList", "").toString();
        if (StringUtils.isBlank(resourceColumnNames)) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表失败");
        }
        String[] resourceColumnNameArray = resourceColumnNames.split(",");
        if (resourceColumnNameArray.length == 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表为空");
        }

        String[] queryColumnNames = {resourceColumnNameArray[0]};
        // convert pirparam to query array
        List<DataPirKeyQuery> dataPirKeyQueries = convertPirParamToQueryArray(pirParam, queryColumnNames);

        DataTask dataTask = new DataTask();
//        dataTask.setTaskIdName(UUID.randomUUID().toString());
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskName(req.getTaskName());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PIR.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        DataPirTask dataPirTask = new DataPirTask();
        dataPirTask.setTaskId(dataTask.getTaskId());
        // retrievalId will rent in web ,need to be readable
        dataPirTask.setRetrievalId(pirParam);
        dataPirTask.setProviderOrganName(pirDataResource.get("organName").toString());
        dataPirTask.setResourceName(pirDataResource.get("resourceName").toString());
        dataPirTask.setResourceId(req.getResourceId());
        dataTaskPrRepository.saveDataPirTask(dataPirTask);
        dataAsyncService.pirGrpcTask(dataTask, dataPirTask, resourceColumnNames, dataPirKeyQueries);
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", dataTask.getTaskId());
        return BaseResultEntity.success(map);
    }

    private static List<DataPirKeyQuery> convertPirParamToQueryArray(String pirParam, String[] resourceColumnNameArray) {
        DataPirKeyQuery dataPirKeyQuery = new DataPirKeyQuery();
        dataPirKeyQuery.setKey(resourceColumnNameArray);
        String[] array = {
                pirParam
        };
        List<String[]> queries = new ArrayList<>(resourceColumnNameArray.length);
        for (int i = 0; i < resourceColumnNameArray.length; i++) {
            queries.add(i, array);
        }
        dataPirKeyQuery.setQuery(queries);
        return Collections.singletonList(dataPirKeyQuery);
    }

    private static List<DataPirKeyQuery> convertPirParamToQueryArray(String[] pirParamArray, String[] resourceColumnNameArray) {
        DataPirKeyQuery dataPirKeyQuery = new DataPirKeyQuery();
        dataPirKeyQuery.setKey(resourceColumnNameArray);
        String[] split = pirParamArray;
        List<String[]> singleValueQuery = Arrays.stream(split).map(String::trim).filter(StringUtils::isNotBlank)
                .map(s -> new String[]{s}).collect(Collectors.toList());
        dataPirKeyQuery.setQuery(singleValueQuery);
        return Collections.singletonList(dataPirKeyQuery);
    }

    public BaseResultEntity getPirTaskList(DataPirTaskReq req) {
        List<DataPirTaskVo> dataPirTaskVos = dataTaskRepository.selectDataPirTaskPage(req);
        if (dataPirTaskVos.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(), new ArrayList()));
        }
        Integer tolal = dataTaskRepository.selectDataPirTaskCount(req);
        Map<String, LinkedHashMap<String, Object>> resourceMap = new HashMap<>();
        List<String> ids = dataPirTaskVos.stream().map(DataPirTaskVo::getResourceId).collect(Collectors.toList());
        BaseResultEntity baseResult = otherBusinessesService.getResourceListById(ids);
        if (baseResult.getCode() == 0) {
            List<LinkedHashMap<String, Object>> voList = (List<LinkedHashMap<String, Object>>) baseResult.getResult();
            if (voList != null && voList.size() != 0) {
                resourceMap.putAll(voList.stream().collect(Collectors.toMap(data -> data.get("resourceId").toString(), Function.identity())));
            }
        }
        for (DataPirTaskVo dataPirTaskVo : dataPirTaskVos) {
            if (resourceMap.containsKey(dataPirTaskVo.getResourceId())) {
                DataTaskConvert.dataPirTaskPoConvertDataPirTaskVo(dataPirTaskVo, resourceMap.get(dataPirTaskVo.getResourceId()));
            }
        }
        return BaseResultEntity.success(new PageDataEntity(tolal, req.getPageSize(), req.getPageNo(), dataPirTaskVos));
    }

    public BaseResultEntity getPirTaskDetail(Long taskId) {
        DataPirTask task = dataTaskRepository.selectPirTaskById(taskId);
        if (task == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "未查询到任务信息");
        }
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "未查询到任务详情");
        }
        DataPirTaskDetailVo vo = new DataPirTaskDetailVo();
        List<LinkedHashMap<String, Object>> list = null;
        if (StringUtils.isNotEmpty(dataTask.getTaskResultPath())) {
            vo.setList(FileUtil.getCsvData(dataTask.getTaskResultPath(), 50));
        }
        vo.setTaskName(dataTask.getTaskName());
        vo.setTaskIdName(dataTask.getTaskIdName());
        vo.setTaskState(dataTask.getTaskState());
        vo.setOrganName(task.getProviderOrganName());
        vo.setResourceName(task.getResourceName());
        vo.setResourceId(task.getResourceId());
        vo.setRetrievalId(task.getRetrievalId());
        vo.setTaskError(dataTask.getTaskErrorMsg());
        vo.setCreateDate(dataTask.getCreateDate());
        vo.setTaskStartTime(dataTask.getTaskStartTime());
        vo.setTaskEndTime(dataTask.getTaskEndTime());
        return BaseResultEntity.success(vo);
    }


    /**
     * pir phase1 #1
     *
     * @param req
     * @return
     */
    public BaseResultEntity submitPirPhase1(DataPirCopyReq req) {
        PsiRecord psiRecord = recordRepository.selectPsiRecordByRecordId(req.getPsiRecordId());
        String psiTaskId = psiRecord.getPsiTaskId();

        DataPsiTask task = dataPsiRepository.selectPsiTaskByTaskId(psiTaskId);

        List<LinkedHashMap<String, Object>> list = null;
        if (StringUtils.isEmpty(task.getFilePath())) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "psi结果为空");
        }
        list = FileUtil.getAllCsvData(task.getFilePath());
        Set<String> idNumSet = list.stream().map(map -> String.valueOf(map.getOrDefault(psiRecord.getTargetField(), "")))
                .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        req.setTargetValueSet(idNumSet);

        req.setOriginOrganId(organConfiguration.getSysLocalOrganId());
        String targetOrganId = psiRecord.getTargetOrganId();

        String recordId = Long.toString(SnowflakeId.getInstance().nextId());
        PirRecord record = new PirRecord();
        record.setRecordId(recordId);
        record.setPirName(req.getTaskName());
        record.setTaskState(0);
        record.setOriginOrganId(organConfiguration.getSysLocalOrganId());
        record.setTargetOrganId(psiRecord.getTargetOrganId());
        record.setStartTime(new Date());
        record.setCommitRowsNum(idNumSet.size());
        record.setResultRowsNum(0);
        recordPrRepository.savePirRecord(record);

        req.setPirRecordId(recordId);
        req.setTargetOrganId(psiRecord.getTargetOrganId());

        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(targetOrganId);
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", targetOrganId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }
        for (SysOrgan organ : sysOrgans) {
            otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/submitPirRecord", organ.getPublicKey());
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/processPirPhase1", organ.getPublicKey());
        }
        return null;
    }

    /**
     * 协作方
     * pir phase1 #2
     *
     * @param req
     * @return
     */
    public BaseResultEntity processPirPhase1(DataPirCopyReq req) {
        log.info("processPirPhase1:");
        log.info(JSON.toJSONString(req));

        String scoreModelType = req.getScoreModelType();
        ScoreModel scoreModel = scoreModelRepository.selectScoreModelByScoreTypeValue(scoreModelType);
        if (scoreModel == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "scoreModelType");
        }

        req.setTaskState(TaskStateEnum.PREPARING.getStateType());
        sendFinishPirTask(req);

        // futureTask
        startFuturePirPhase1Task(req);

        return BaseResultEntity.success();
    }

    private void startFuturePirPhase1Task(DataPirCopyReq req) {
        // 进行预处理，使用异步
        FutureTask<Object> task = new FutureTask<>(() -> {
            try {
                PirPhase1Execute bean = (PirPhase1Execute) DataCopyService.context.getBean(PirPhase1Enum.PIR_PHASE1_TYPE_MAP.get(req.getTargetField()));
                bean.processPirPhase1(req);
            } catch (Exception e) {
                log.error("异步执行异常", e);
                req.setTaskState(TaskStateEnum.FAIL.getStateType());
                sendFinishPirTask(req);
            }
            return null;
        });
        primaryThreadPool.submit(task);
    }

    /**
     * pir phase2 #3
     * 发起方
     *
     * @param req
     * @return
     */
    public BaseResultEntity submitPirPhase2(DataPirReq param, DataPirCopyReq req) {
        BaseResultEntity dataResource = otherBusinessesService.getDataResource(param.getResourceId());
        if (dataResource.getCode() != 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "资源查询失败");
        }
        Map<String, Object> pirDataResource = (LinkedHashMap) dataResource.getResult();
        int available = Integer.parseInt(pirDataResource.getOrDefault("available", "1").toString());
        if (available == 1) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "资源不可用");
        }
        // dataResource columnName list
        String resourceColumnNames = pirDataResource.getOrDefault("resourceColumnNameList", "").toString();
        if (StringUtils.isBlank(resourceColumnNames)) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表失败");
        }
        String[] resourceColumnNameArray = Arrays.stream(resourceColumnNames.split(",")).map(String::trim).toArray(String[]::new);
        ;
        log.info("pir 提交数据特征: {}", Arrays.toString(resourceColumnNameArray));
        if (resourceColumnNameArray.length == 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表为空");
        }
        String pirRecordId = req.getPirRecordId();
        PirRecord record = recordRepository.selectPirRecordByRecordId(pirRecordId);

        boolean containedTargetFieldFlag = Arrays.asList(resourceColumnNameArray).contains(record.getTargetField());
        if (!containedTargetFieldFlag) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表不包含目标字段");
        }

        String[] targetValueArray = req.getTargetValueSet().toArray(new String[0]);
        String[] queryColumnNames = {record.getTargetField()};
        List<DataPirKeyQuery> dataPirKeyQueries = convertPirParamToQueryArray(targetValueArray, queryColumnNames);

        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskName(req.getTaskName());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PIR.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        DataPirTask dataPirTask = new DataPirTask();
        dataPirTask.setTaskId(dataTask.getTaskId());
        // retrievalId will rent in web ,need to be readable
        dataPirTask.setRetrievalId(String.join(",", req.getTargetValueSet()));
        dataPirTask.setProviderOrganName(pirDataResource.get("organName").toString());
        dataPirTask.setResourceName(pirDataResource.get("resourceName").toString());
        dataPirTask.setResourceId(param.getResourceId());
        dataTaskPrRepository.saveDataPirTask(dataPirTask);
        dataAsyncService.pirGrpcTask(dataTask, dataPirTask, resourceColumnNames, dataPirKeyQueries);


        record.setPirTaskId(dataPirTask.getTaskId());
        record.setTaskState(dataTask.getTaskState());

        List<LinkedHashMap<String, Object>> list = new ArrayList<>();
        if (Objects.equals(dataTask.getTaskState(), TaskStateEnum.SUCCESS.getStateType())) {
            if (org.apache.commons.lang.StringUtils.isNotEmpty(dataTask.getTaskResultPath())) {
                list = FileUtil.getAllCsvData(dataTask.getTaskResultPath());
            }
            record.setResultRowsNum(list.size());
            record.setEndTime(new Date());
        }
        recordPrRepository.updatePirRecord(record);

        if (com.alibaba.nacos.common.utils.CollectionUtils.isNotEmpty(list)) {
            list.forEach(map -> {
                map.put("pirTaskId", dataPirTask.getTaskId());
            });
            resultPrRepository.savePirResultList(list);
        }

        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(req.getTargetOrganId());
        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(record, organ.getOrganGateway() + "/share/shareData/submitPirRecord", organ.getPublicKey());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("taskId", dataTask.getTaskId());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getScoreModelList() {
        return BaseResultEntity.success(scoreModelRepository.selectAll());
    }

    public BaseResultEntity submitScoreModel(ScoreModelReq req) {
        ScoreModel scoreModel = new ScoreModel(req);
        scoreModelPrRepository.saveScoreModel(scoreModel);

        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(req.getOrganId());
        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(scoreModel, organ.getOrganGateway() + "/share/shareData/submitScoreModelType", organ.getPublicKey());
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity sendFinishPirTask(DataPirCopyReq req) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(req.getOriginOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", req.getOriginOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/finishPirTask", organ.getPublicKey());
        }
        return null;
    }
}
