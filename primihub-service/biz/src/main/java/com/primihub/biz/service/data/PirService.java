package com.primihub.biz.service.data;


import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.convert.DataTaskConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.base.DataPirKeyQuery;
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
import com.primihub.biz.repository.primarydb.data.ScoreModelPrRepository;
import com.primihub.biz.repository.primaryredis.sys.TaskPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.data.*;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PirService {
    private final Lock lock = new ReentrantLock();
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
    private ExamService examService;
    @Autowired
    private RecordPrRepository recordPrRepository;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private ScoreModelRepository scoreModelRepository;
    @Autowired
    private ScoreModelPrRepository scoreModelPrRepository;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private TaskPrimaryRedisRepository taskPrimaryRedisRepository;

    public String getResultFilePath(String taskId, String taskDate) {
        return new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(taskDate).append("/").append(taskId).append(".csv").toString();
    }

    private static List<DataPirKeyQuery> convertPirParamToQueryArray(String pirParam, String[] resourceColumnNameArray) {
        DataPirKeyQuery dataPirKeyQuery = new DataPirKeyQuery();
        dataPirKeyQuery.setKey(resourceColumnNameArray);
        String[] array = {pirParam};
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
        List<String[]> singleValueQuery = Arrays.stream(split).map(String::trim).filter(StringUtils::isNotBlank).map(s -> new String[]{s}).collect(Collectors.toList());
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
     * 创建一个PIR任务但是不发起
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
        Set<String> idNumSet = list.stream().map(map -> String.valueOf(map.getOrDefault(psiRecord.getTargetField(), ""))).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        req.setTargetValueSet(idNumSet);

        req.setOriginOrganId(organConfiguration.getSysLocalOrganId());
        String targetOrganId = psiRecord.getTargetOrganId();


        List<Map> mapList = idNumSet.stream().map(idNum -> {
            Map map = new HashMap();
            map.put(psiRecord.getTargetField(), idNum);
            return map;
        }).collect(Collectors.toList());
        // 生成数据源
        String resourceName = "PIR处理资源" + SysConstant.HYPHEN_DELIMITER + req.getTaskName();
        DataResource dataResource = examService.generateTargetResource(mapList, resourceName);
        req.setOriginResourceId(dataResource.getResourceFusionId());

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
        record.setTargetField(psiRecord.getTargetField());
        recordPrRepository.savePirRecord(record);

        req.setPirRecordId(recordId);
        req.setTargetOrganId(psiRecord.getTargetOrganId());

        // 这里应该开始创建pir任务了
        String[] targetValueArray = req.getTargetValueSet().toArray(new String[0]);
        String[] queryColumnNames = {psiRecord.getTargetField()};
        List<DataPirKeyQuery> dataPirKeyQueries = convertPirParamToQueryArray(targetValueArray, queryColumnNames);

        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskName(req.getTaskName());
        dataTask.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PIR.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        DataPirTask dataPirTask = new DataPirTask();
        dataPirTask.setTaskId(dataTask.getTaskId());
        // retrievalId will rent in web ,need to be readable
        dataPirTask.setRetrievalId(String.valueOf(req.getTargetValueSet().size()));

        SysOrgan sysOrgan = organSecondaryDbRepository.selectSysOrganByOrganId(psiRecord.getTargetOrganId());
        // 协作方机构名称
        dataPirTask.setProviderOrganName(sysOrgan.getOrganName());
        dataPirTask.setResourceName("wait");
        dataPirTask.setResourceId("wait");
        dataTaskPrRepository.saveDataPirTask(dataPirTask);

        req.setTargetField(psiRecord.getTargetField());
        req.setDataTaskId(dataTask.getTaskId());
        req.setDataPirTaskId(dataPirTask.getId());
        req.setResourceColumnNames("wait");
        req.setDataPirKeyQueries(dataPirKeyQueries);

        Map<String, Object> map = new HashMap<>();
        map.put("taskId", dataTask.getTaskId());

        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(targetOrganId);
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", targetOrganId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }

        taskPrimaryRedisRepository.setCopyPirReq(req);
        for (SysOrgan organ : sysOrgans) {
            otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/submitPirRecord", organ.getPublicKey());
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/processPirPhase1", organ.getPublicKey());
        }
        return null;
    }

    /*
        String originResourceId = req.getOriginResourceId();
        DataResource resource = dataResourceRepository.queryDataResourceByResourceFusionId(originResourceId);

        String[] targetValueArray = req.getTargetValueSet().toArray(new String[0]);
        String[] queryColumnNames = {pirRecord.getTargetField()};
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
        dataPirTask.setRetrievalId(String.valueOf(req.getTargetValueSet().size()));
        dataPirTask.setProviderOrganName(pirDataResource.get("organName").toString());
        dataPirTask.setResourceName(pirDataResource.get("resourceName").toString());
        dataPirTask.setResourceId(param.getResourceId());
        dataTaskPrRepository.saveDataPirTask(dataPirTask);
        dataAsyncService.pirGrpcTask(dataTask, dataPirTask, resourceColumnNames, dataPirKeyQueries, req, resource);


        Map<String, Object> map = new HashMap<>();
        map.put("taskId", dataTask.getTaskId());
     */

    /**
     * 发起方
     */
    public BaseResultEntity submitPirPhase2(Long pirTaskId) {
        DataPirCopyReq req = taskPrimaryRedisRepository.getCopyPirReq(pirTaskId);
        if (req == null) {
            log.error("{}: {}", BaseResultEnum.DATA_QUERY_NULL.getMessage(), "任务已超时");
            // todo 超时任务直接失败
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "任务已超时");
        }

        String pirRecordId = req.getPirRecordId();
        PirRecord pirRecord = recordRepository.selectPirRecordByRecordId(pirRecordId);
        BaseResultEntity dataResource = otherBusinessesService.getDataResource(req.getTargetResourceId());
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
        boolean containedTargetFieldFlag = Arrays.asList(resourceColumnNameArray).contains(pirRecord.getTargetField());
        if (!containedTargetFieldFlag) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表不包含目标字段");
        }

        List<DataPirKeyQuery> dataPirKeyQueries = req.getDataPirKeyQueries();
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(req.getDataTaskId());
        dataTask.setTaskStartTime(System.currentTimeMillis());

        DataPirTask dataPirTask = dataTaskRepository.selectPirTaskById(req.getDataPirTaskId());
        // retrievalId will rent in web ,need to be readable

        String originResourceId = req.getOriginResourceId();
        DataResource resource = dataResourceRepository.queryDataResourceByResourceFusionId(originResourceId);
        dataAsyncService.pirGrpcTask(dataTask, dataPirTask, resourceColumnNames, dataPirKeyQueries, req, resource);


        Map<String, Object> map = new HashMap<>();
        map.put("taskId", dataTask.getTaskId());
        return BaseResultEntity.success(map);

    }

    /**
     * pir phase2 #3
     * 发起方
     *
     * @param req
     * @return
     */
    public BaseResultEntity submitPirPhase2(DataPirReq param, DataPirCopyReq req) {
        String pirRecordId = req.getPirRecordId();
        PirRecord pirRecord = recordRepository.selectPirRecordByRecordId(pirRecordId);
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
        boolean containedTargetFieldFlag = Arrays.asList(resourceColumnNameArray).contains(pirRecord.getTargetField());
        if (!containedTargetFieldFlag) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表不包含目标字段");
        }

        String originResourceId = req.getOriginResourceId();
        DataResource resource = dataResourceRepository.queryDataResourceByResourceFusionId(originResourceId);

        String[] targetValueArray = req.getTargetValueSet().toArray(new String[0]);
        String[] queryColumnNames = {pirRecord.getTargetField()};
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
        dataPirTask.setRetrievalId(String.valueOf(req.getTargetValueSet().size()));
        dataPirTask.setProviderOrganName(pirDataResource.get("organName").toString());
        dataPirTask.setResourceName(pirDataResource.get("resourceName").toString());
        dataPirTask.setResourceId(param.getResourceId());
        dataTaskPrRepository.saveDataPirTask(dataPirTask);
        dataAsyncService.pirGrpcTask(dataTask, dataPirTask, resourceColumnNames, dataPirKeyQueries, req, resource);


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

    public BaseResultEntity finishPirTask(DataPirCopyReq req) {
        try {
            lock.lock();
            updatePirTaskAfterSelect(req);
        } finally {
            lock.unlock();
        }
        return BaseResultEntity.success();
    }

    @Transactional
    public void updatePirTaskAfterSelect(DataPirCopyReq req) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(req.getDataTaskId());
        dataTask.setTaskState(req.getTaskState());
        dataTaskPrRepository.updateDataTask(dataTask);
        if (Objects.equals(req.getTaskState(), TaskStateEnum.READY.getStateType())) {
            Long dataPirTaskId = req.getDataPirTaskId();
            DataPirTask task = dataTaskRepository.selectPirTaskById(dataPirTaskId);
            if (task == null) {
                log.error("{}: {}", BaseResultEnum.DATA_QUERY_NULL, "pirTask");
                return;
            }
            task.setResourceId(req.getTargetResourceId());
            dataTaskPrRepository.updateDataPirTask(task);

            taskPrimaryRedisRepository.setCopyPirReq(req);
        }

        if (Objects.equals(req.getTaskState(), TaskStateEnum.FAIL.getStateType())) {
            taskPrimaryRedisRepository.deleteCopyPirReq(req.getDataPirTaskId());
        }
    }
}
