package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.req.DataExamTaskReq;
import com.primihub.biz.entity.data.vo.DataPirTaskDetailVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.service.data.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 预审核管理
 */
@RequestMapping
@RestController
@Slf4j
public class ExamController {

    @Autowired
    private ExamService examService;
    @GetMapping("/examine/getExamTaskList")
    public BaseResultEntity<PageDataEntity<DataPirTaskVo>> getExamTaskList(DataExamTaskReq req){
        return examService.getExamTaskList(req);
    }

    /**
     * 一，创建审核任务
     */
    @PostMapping(value = "/examine/saveExamTask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResultEntity saveExamTask(@RequestBody DataExamReq dataExamReq) {
        if (StringUtils.isBlank(dataExamReq.getResourceId())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (StringUtils.isBlank(dataExamReq.getTaskName())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskName");
        }
        if (StringUtils.isBlank(dataExamReq.getTargetOrganId())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"targetOrganId");
        }
        return examService.saveExamTask(dataExamReq);
    }

    /**
     * 二：发起任务
     * @param dataExamReq
     * @return
     */
    @PostMapping(value = "/examine/submitExamTask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResultEntity submitExamTask(@RequestBody DataExamReq dataExamReq) {
        if (StringUtils.isBlank(dataExamReq.getTaskId())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return examService.submitExamTask(dataExamReq);
    }

    @GetMapping(value = "/examine/getExamTaskDetail")
    public BaseResultEntity<DataPirTaskDetailVo> getExamTaskDetail(String taskId){
        if (StringUtils.isBlank(taskId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return examService.getExamTaskDetail(taskId);
    }

    /**
     * 三：处理任务
     * 这里要 mock 两个数据源
     */
    @PostMapping(value = "/shareData/processExamTask")
    public BaseResultEntity processExamTask(@RequestBody DataExamReq dataExamReq) {
        return examService.processExamTask(dataExamReq);
    }

    /**
     * 第四步：结束任务
     */
    @PostMapping(value = "/shareData/finishExamTask")
    public BaseResultEntity finishExamTask(@RequestBody DataExamReq dataExamReq) {
        return examService.finishExamTask(dataExamReq);
    }

}
