package com.primihub.biz.service.data.exam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.RemoteConstant;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.lpy.CTCCExamTask;
import com.primihub.biz.entity.data.po.lpy.DataImei;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.entity.data.vo.lpy.CTCCPsiVo;
import com.primihub.biz.entity.data.vo.lpy.ImeiPsiVo;
import com.primihub.biz.repository.primarydb.data.DataCTCCPrimarydbRepository;
import com.primihub.biz.repository.primarydb.data.DataImeiPrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataImeiRepository;
import com.primihub.biz.service.data.ExamService;
import com.primihub.biz.service.data.RemoteClient;
import com.primihub.biz.util.crypt.SM3Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.primihub.biz.constant.RemoteConstant.UNDEFINED;

@Service
@Slf4j
public class ExamExecuteImei implements ExamExecute {
    @Autowired
    private DataImeiRepository imeiRepository;
    @Autowired
    private DataImeiPrimarydbRepository imeiPrimaryDbRepository;
    @Autowired
    private ExamService examService;
    @Autowired
    private RemoteClient remoteClient;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataCTCCPrimarydbRepository ctccPrimaryDbRepository;

    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : imei, taskName: {}", StringUtils.isNotBlank(req.getTaskName()) ? req.getTaskName() : "null");
        // 从taskName区分是cmcc还是ctcc
        String taskName = req.getTaskName();
        if (StringUtils.isNotBlank(taskName) && taskName.endsWith(RemoteConstant.CTCC_FLAG)) {
            // ctcc
            imeiCtccProcess(req);
        } else {
            // cmcc
            imeiCmccProcess(req);
        }

    }

    private void imeiCmccProcess(DataExamReq req) {
        Set<String> rawSet = req.getFieldValueSet();

        /*
        rawSet
        oldSet, newSet => query
        oldSet, newExistSet, newNoExistSet
        oldSet, newExistSet, waterSet, noSet
         */
        // 已经存在的数据
        Set<DataImei> dataImeiSet = imeiRepository.selectImei(rawSet);
        Set<String> oldSet = dataImeiSet.stream().map(DataImei::getImei).collect(Collectors.toSet());
        Collection<String> newSet = CollectionUtils.subtract(rawSet, oldSet);

        // 先过滤出存在手机号的数据
        log.info("process exam query imei, count: {}", newSet.size());

        // 预处理使用模型分
        List<DataImei> newExistDataSet = new ArrayList<>();
        for (String imei : newSet) {
            RemoteRespVo respVo = remoteClient.queryFromRemote(imei, "AME000818");
            if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                DataImei dataImei = new DataImei();
                dataImei.setImei(imei);
                dataImei.setScore(Double.valueOf((String) (respVo.getRespBody().get("yhhhwd_score"))));
                dataImei.setY(dataImei.getY());
                dataImei.setScoreModelType("yhhhwd_score");
                newExistDataSet.add(dataImei);
            }
        }
        Set<String> newExistSet = newExistDataSet.stream().map(DataImei::getImei).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(newExistDataSet)) {
            imeiPrimaryDbRepository.saveImeiSet(newExistDataSet);
            oldSet.addAll(newExistSet);
        }

        if (baseConfiguration.getWaterSwitch()) {
            Collection<String> newNoExistSet = CollectionUtils.subtract(newSet, newExistSet);
            if (CollectionUtils.isNotEmpty(newNoExistSet)) {
                // water
                List<String> waterList = new ArrayList<>(newNoExistSet);
                int halfSize = (int) Math.ceil((waterList.size() * 0.7));
                Set<String> waterSet = new HashSet<>();
                Random random = new Random();
                for (int i = 0; i < halfSize; i++) {
                    int randomIndex = random.nextInt(waterList.size());
                    String s = waterList.get(randomIndex);
                    waterSet.add(s);
                    waterList.remove(randomIndex);
                }

                if (CollectionUtils.isNotEmpty(waterSet)) {
                    List<DataImei> collect = waterSet.stream().map(imei -> {
                        DataImei data = new DataImei();
                        data.setPhoneNum(UNDEFINED);
                        data.setImei(imei);
                        data.setScore(Double.parseDouble(RemoteClient.getRandomScore()));
                        data.setScoreModelType("yhhhwd_score");
                        return data;
                    }).collect(Collectors.toList());
                    imeiPrimaryDbRepository.saveImeiSet(collect);
                    oldSet.addAll(waterSet);
                }
            }
        }

        Set<ImeiPsiVo> existResult = oldSet.stream().map(ImeiPsiVo::new).collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(existResult)) {
            log.error("samples size after exam is zero!");
            existResult.add(new ImeiPsiVo(SM3Util.encrypt(UUID.randomUUID().toString().replace("-", ""))));
        }
        String jsonArrayStr = JSON.toJSONString(existResult);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        // 生成数据源
        String resourceName = "预处理生成资源" + SysConstant.HYPHEN_DELIMITER + req.getTaskId();
        DataResource dataResource = examService.generateTargetResource(maps, resourceName);
        if (dataResource == null) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            examService.sendEndExamTask(req);
            log.info("====================== FAIL ======================");
            log.error("generate target resource failed!");
        } else {
            req.setTaskState(TaskStateEnum.SUCCESS.getStateType());
            req.setTargetResourceId(dataResource.getResourceFusionId());
            examService.sendEndExamTask(req);
            log.info("====================== SUCCESS ======================");
        }
    }

    private void imeiCtccProcess(DataExamReq req) {
        // rawSet
        Set<String> rawSet = req.getFieldValueSet();

        Set<CTCCPsiVo> psiResult = rawSet.stream().map(CTCCPsiVo::new).collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(psiResult)) {
//            req.setTaskState(TaskStateEnum.FAIL.getStateType());
//            examService.sendEndExamTask(req);
//            log.info("====================== FAIL ======================");
            log.error("samples size after exam is zero!");
            psiResult.add(new CTCCPsiVo(SM3Util.encrypt(UUID.randomUUID().toString().replace("-", ""))));
        }
        String jsonArrayStr = JSON.toJSONString(psiResult);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        String resourceName = "预处理中间资源" + SysConstant.HYPHEN_DELIMITER + req.getTaskId();
        examService.generateCtccFile(maps, resourceName);



        /*if (CollectionUtils.isEmpty(psiResult)) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            examService.sendEndExamTask(req);
            log.info("====================== FAIL ======================");
            log.error("samples size after exam is zero!");
        } else {
            CTCCExamTask ctccTask = new CTCCExamTask();
            ctccTask.setOriginResourceId(req.getResourceId());
            ctccTask.setTaskName(req.getTaskName());
            ctccTask.setTargetOrganId(req.getTargetOrganId());
            ctccTask.setOriginOrganId(req.getOriginOrganId());
            ctccTask.setTaskId(req.getTaskId());
            *//**
             * 运行状态 0未运行 1完成 2运行中 3失败 4取消 默认0
             *//*
            ctccTask.setTaskState(0);
            ctccTask.setOriginResourceId(req.getResourceId());
//            ctccTask.setTargetResourceId(req.getTargetResourceId());
            ctccTask.setTargetField(req.getTargetField());
            ctccTask.setFileUrl(dataResource.getUrl());
            ctccTask.setFileName(UUID.randomUUID().toString());
            ctccPrimaryDbRepository.saveCtccExamTask(ctccTask);
        }*/
    }
}
