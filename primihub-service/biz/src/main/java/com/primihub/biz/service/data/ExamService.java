package com.primihub.biz.service.data;


import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.convert.DataExamConvert;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.dataenum.ExamEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.po.lpy.CTCCExamTask;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.req.DataExamTaskReq;
import com.primihub.biz.entity.data.req.lpy.CtccExamReq;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.data.vo.lpy.CtccExamTaskVo;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataCTCCPrimarydbRepository;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.primarydb.sys.SysFilePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataCTCCRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.data.exam.ExamExecute;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.primihub.biz.constant.RemoteConstant.INPUT_FIELD_ARRAY;

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
    private BaseConfiguration baseConfiguration;
    @Autowired
    private SysFilePrimarydbRepository sysFilePrimarydbRepository;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private DataCTCCRepository ctccRepository;
    @Autowired
    private DataCTCCPrimarydbRepository ctccPrimaryDbRepository;
    @Autowired
    private SysFileSecondarydbRepository sysFileSecondarydbRepository;
    @Autowired
    private ExamService examService;

    public BaseResultEntity<PageDataEntity<DataPirTaskVo>> getExamTaskList(DataExamTaskReq req) {
        List<DataExamTaskVo> dataExamTaskVos = dataTaskRepository.selectDataExamTaskPage(req);
        if (dataExamTaskVos.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(), Collections.emptyList()));
        }
        Integer total = dataTaskRepository.selectDataExamTaskCount(req);
        return BaseResultEntity.success(new PageDataEntity(total, req.getPageSize(), req.getPageNo(), dataExamTaskVos));
    }

    private BaseResultEntity sendExamTask(DataExamReq param) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(param.getTargetOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", param.getTargetOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(param, organ.getOrganGateway() + "/share/shareData/processExamTask", organ.getPublicKey());
        }
        return null;
    }

    public BaseResultEntity processExamTask(DataExamReq req) {
        if (Arrays.stream(INPUT_FIELD_ARRAY).noneMatch(str -> Objects.equals(str, req.getTargetField()))) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION, Arrays.toString(INPUT_FIELD_ARRAY));
        }
        if (CollectionUtils.isEmpty(req.getFieldValueSet())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "fieldValue");
        }

        req.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        sendEndExamTask(req);

        // futureTask
        startFutureExamTask(req);

        return BaseResultEntity.success();
    }

    //    private DataResource generateTargetResource(Map returnMap) {
    public DataResource generateTargetResource(List<Map> metaData, String resourceName) {
        log.info("开始生成数据源===========================");

        SysFile sysFile = new SysFile();
        sysFile.setFileSource(1);
        sysFile.setFileSuffix("csv");
        sysFile.setFileName(UUID.randomUUID().toString());
        Date date = new Date();
        StringBuilder sb = new StringBuilder().append(baseConfiguration.getUploadUrlDirPrefix()).append(1)
                .append("/").append(DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/");
        sysFile.setFileArea("local");
        sysFile.setFileSize(0L);
        sysFile.setFileCurrentSize(0L);
        sysFile.setIsDel(0);

        try {
            File tempFile = new File(sb.toString());
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            FileUtil.convertToCsv(metaData, sb.append(sysFile.getFileName()).append(".").append(sysFile.getFileSuffix()).toString());
            log.info("写入csv文件===========================");
            sysFile.setFileUrl(sb.toString());
        } catch (IOException e) {
            log.error("upload", e);
            return null;
//            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"写硬盘失败");
        }

        log.info("sysFile: {}", JSON.toJSONString(sysFile));
        sysFilePrimarydbRepository.insertSysFile(sysFile);
        log.info("sysFile: {}", JSON.toJSONString(sysFile));

        // resourceFilePreview
        BaseResultEntity resultEntity = dataResourceService.getDataResourceCsvVo(sysFile);
        // 这里最多50行，所以不用考虑传输和日志消耗
        log.info("resultEntity: {}", JSON.toJSONString(resultEntity));
        DataResourceCsvVo csvVo = (DataResourceCsvVo) resultEntity.getResult();

        try {
            DataResource po = new DataResource();
            po.setResourceName(resourceName);
            po.setResourceDesc(resourceName);
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
            log.info("{}", JSON.toJSONString(handleDataResourceFileResult));
            if (handleDataResourceFileResult.getCode() != 0) {
                log.info("{}", JSON.toJSONString(handleDataResourceFileResult));
                // todo 错误处理
                return null;
            }

            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            if (sysLocalOrganInfo != null && sysLocalOrganInfo.getOrganId() != null && !"".equals(sysLocalOrganInfo.getOrganId().trim())) {
                po.setResourceFusionId(organConfiguration.generateUniqueCode());
            }
            List<DataFileField> dataFileFieldList = new ArrayList<>();
            for (DataFileFieldVo field : fieldList) {
                dataFileFieldList.add(DataResourceConvert.DataFileFieldVoConvertPo(field, 0L, po.getResourceId()));
            }
            TaskParam taskParam = dataResourceService.resourceSynGRPCDataSet(dataSource, po, dataFileFieldList);
            log.info("{}", JSON.toJSONString(taskParam));
            if (!taskParam.getSuccess()) {
                log.info("{}", JSON.toJSONString(taskParam));
                return null;
            }
            if (dataSource != null) {
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
                dataResourcePrRepository.saveResourceTagRelation(dataResourceTag.getTagId(), po.getResourceId());
            }
            log.info("存入数据库成功======================");
            fusionResourceService.saveResource(organConfiguration.getSysLocalOrganId(), dataResourceService.findCopyResourceList(po.getResourceId(), po.getResourceId()));
            singleTaskChannel.output().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(), po))).build());
            return po;
        } catch (Exception e) {
            log.info("save DataResource Exception：{}", e.getMessage());
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private void startFutureExamTask(DataExamReq req) {
        // 进行预处理，使用异步
        FutureTask<Object> task = new FutureTask<>(() -> {
            try {
                ExamExecute bean = (ExamExecute) DataCopyService.context.getBean(ExamEnum.EXAM_TYPE_MAP.get(req.getTargetField()));
                bean.processExam(req);
            } catch (Exception e) {
                log.error("异步执行异常", e);
                req.setTaskState(TaskStateEnum.FAIL.getStateType());
                sendEndExamTask(req);
            }
            return null;
        });
        primaryThreadPool.submit(task);
    }


    private BaseResultEntity getTargetResource(String resourceId, String organId) {
        BaseResultEntity fusionResult = fusionResourceService.getDataResource(resourceId, organId);
        if (fusionResult.getCode() != 0 || fusionResult.getResult() == null) {
            log.info("未找到预处理源数据 resourceId: [{}]", resourceId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "resourceId");
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
        if (task == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "未查询到任务信息");
        }
        DataExamTaskVo vo = DataExamConvert.convertPoToVo(task);
        return BaseResultEntity.success(vo);
    }


    /**
     * @param req
     * @return
     */
    public BaseResultEntity examTaskList(DataExamTaskReq req) {
        List<DataExamTaskVo> dataExamTaskVos = dataTaskRepository.selectDataExamTaskList(req);
        return BaseResultEntity.success(dataExamTaskVos);
    }

    public BaseResultEntity sendEndExamTask(DataExamReq req) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(req.getOriginOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", req.getOriginOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/finishExamTask", organ.getPublicKey());
        }
        return null;
    }

    /**
     * 获取电信列表
     */
    public BaseResultEntity<PageDataEntity<CtccExamTaskVo>> getCtccExamTaskList(DataExamTaskReq req) {
        List<CtccExamTaskVo> ctccExamTaskVoList = ctccRepository.selectCtccExamTaskPage(req);
        if (ctccExamTaskVoList.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(), Collections.emptyList()));
        }
        Integer total = ctccRepository.selectCtccExamTaskCount(req);
        return BaseResultEntity.success(new PageDataEntity(total, req.getPageSize(), req.getPageNo(), ctccExamTaskVoList));
    }

    /**
     * 下载电信上传的csv文件
     */
    public BaseResultEntity downloadCtccExamFile(Long taskId, HttpServletResponse response) throws Exception {
        CTCCExamTask ctccExamTask = ctccRepository.selectCtccExamTaskById(taskId);
        // 下载电信csv文件到本地
        download(response, ctccExamTask);
        // 修改记录的状态
        /**
         * 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0
         */
        ctccExamTask.setTaskState(2);
        ctccPrimaryDbRepository.updateCtccExamTask(ctccExamTask);
        return null;
    }

    // 等待串一下
    private void download(HttpServletResponse response, CTCCExamTask ctccExamTask) throws Exception {
        File file = new File(ctccExamTask.getFileUrl());
        if (file != null && file.exists()) {
            String fileName = ctccExamTask.getFileName() + ".csv";
            FileInputStream inputStream = new FileInputStream(file);
            response.setHeader("content-Type", "application/vnd.ms-excel");
            response.setHeader("content-disposition", "attachment; fileName=" + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            ServletOutputStream outputStream = response.getOutputStream();
            int len = 0;
            byte[] data = new byte[1024];
            while ((len = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } else {
            String content = "no data";
            String fileName = null;
            if (ctccExamTask != null) {
                fileName = ctccExamTask.getFileName() + ".csv";
            } else {
                fileName = UUID.randomUUID().toString() + ".csv";
            }
            OutputStream outputStream = null;
            //将字符串转化为文件
            byte[] currentLogByte = content.getBytes();
            try {
                // 告诉浏览器用什么软件可以打开此文件
                response.setHeader("content-Type", "application/vnd.ms-excel");
                // 下载文件的默认名称
                response.setHeader("Content-disposition", "attachment;filename=" + new String(fileName.getBytes("UTF-8"), "iso-8859-1"));
                response.setCharacterEncoding("UTF-8");
                outputStream = response.getOutputStream();
                outputStream.write(currentLogByte);
                outputStream.close();
                outputStream.flush();
            } catch (Exception e) {
                log.info("downloadPsiTask -- fileName:{} -- fileContent:{} -- e:{}", fileName, content, e.getMessage());
            }
        }
    }

    /**
     * 上传电信返回的csv文件
     */
    public BaseResultEntity uploadCtccExamFile(CtccExamReq req) {
        CTCCExamTask ctccExamTask = ctccRepository.selectCtccExamTaskById(req.getTaskId());
        // 修改记录的状态
        /**
         * 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0
         */
        ctccExamTask.setTaskState(1);
        ctccExamTask.setTargetResourceId(req.getResourceId());
        ctccPrimaryDbRepository.updateCtccExamTask(ctccExamTask);
        // 返回给对方
        DataExamReq examReq = DataExamConvert.convertCtccToReq(ctccExamTask);
        examReq.setTaskState(TaskStateEnum.SUCCESS.getStateType());
        examReq.setTargetResourceId(req.getResourceId());
        examService.sendEndExamTask(examReq);
        log.info("====================== SUCCESS ======================");
        return BaseResultEntity.success();
    }

    public BaseResultEntity endCtccExamFile(CtccExamReq req) {
        CTCCExamTask ctccExamTask = ctccRepository.selectCtccExamTaskById(req.getTaskId());
        /**
         * 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0
         */
        ctccExamTask.setTaskState(3);
        ctccPrimaryDbRepository.updateCtccExamTask(ctccExamTask);
        // 返回给对方
        DataExamReq examReq = DataExamConvert.convertCtccToReq(ctccExamTask);
        examReq.setTaskState(TaskStateEnum.FAIL.getStateType());
        examService.sendEndExamTask(examReq);
        log.info("====================== FAIL ======================");
        log.error("generate target resource failed!");
        return BaseResultEntity.success();
    }

    public void generateCtccFile(List<Map> metaData, String resourceName) {
        log.info("开始生成 ctcc file ===========================");

        SysFile sysFile = new SysFile();
        sysFile.setFileSource(1);
        sysFile.setFileSuffix("csv");
        sysFile.setFileName(UUID.randomUUID().toString());
        Date date = new Date();
        StringBuilder sb = new StringBuilder().append(baseConfiguration.getUploadUrlDirPrefix()).append(1)
                .append("/").append(DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/");
        sysFile.setFileArea("local");
        sysFile.setFileSize(0L);
        sysFile.setFileCurrentSize(0L);
        sysFile.setIsDel(0);

        try {
            File tempFile = new File(sb.toString());
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            String filePath = sb.append(sysFile.getFileName()).append(".").append(sysFile.getFileSuffix()).toString();
            FileUtil.convertToCsv(metaData, filePath);
            log.info("写入csv文件成功=========================== {}", filePath);
            sysFile.setFileUrl(sb.toString());
        } catch (IOException e) {
            log.error("upload", e);
        }
        BaseResultEntity resultEntity = dataResourceService.getDataResourceCsvVo(sysFile);
        log.info("resultEntity: {}", JSON.toJSONString(resultEntity));
    }
}
