package com.primihub.biz.service.data.exam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.lpy.CTCCExamTask;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.vo.lpy.CTCCPsiVo;
import com.primihub.biz.repository.secondarydb.data.DataCTCCRepository;
import com.primihub.biz.service.data.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 注意，这个时候还没有模型分呢
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExamExecuteCTCC implements ExamExecute {
    private final DataCTCCRepository ctccRepository;
    private final ExamService examService;

    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : ctcc");

        // rawSet
        Set<String> rawSet = req.getFieldValueSet();

        Set<CTCCPsiVo> psiResult = rawSet.stream().map(CTCCPsiVo::new).collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(psiResult)) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            examService.sendEndExamTask(req);
            log.info("====================== FAIL ======================");
            log.error("samples size after exam is zero!");
        } else {
            String jsonArrayStr = JSON.toJSONString(psiResult);
            List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
            // 生成数据源
            String resourceName = "预处理生成资源" + SysConstant.HYPHEN_DELIMITER + req.getTaskId();
            DataResource dataResource = examService.generateTargetResource(maps, resourceName);
            // todo 这里生成资源后，要给用户下载

            // 这里创建一张表用来存储，CTCC的任务
            CTCCExamTask ctccTask = new CTCCExamTask();
            ctccTask.setTaskId(req.getTaskId());
            ctccTask.setTaskState());
            ctccTask.setOriginOrganId(req.getOriginOrganId());
            ctccTask.setOriginResourceId(req.getResourceId());
            ctccTask.setState(req.getTaskId());
            ctccTask.setTargetOrganId(req.getTargetOrganId());
            ctccTask.setTargetResourceId(req.getTargetResourceId());
            ctccTask.setTaskName(req.getTaskName());

            // 自动提醒运营人员来下载文件，并线下发送给电信

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
    }
}
