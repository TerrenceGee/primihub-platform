package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseJsonParam;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.base.DataPirKeyQuery;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.req.DataModelAndComponentReq;
import com.primihub.biz.entity.data.req.DataPirReq;
import com.primihub.biz.entity.data.req.DataPirTaskReq;
import com.primihub.biz.entity.data.vo.DataPirTaskDetailVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.service.data.PirService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Arrays;

/**
 * pir 管理
 */
@Api(value = "匿踪查询接口",tags = "匿踪查询接口")
@RequestMapping("pir")
@RestController
@Slf4j
public class PirController {

    @Autowired
    private PirService pirService;

    @ApiOperation(value = "提交匿踪查询任务",httpMethod = "POST",consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping("pirSubmitTask")
    public BaseResultEntity pirSubmitTask(String resourceId,String pirParam,String taskName,
                                          @RequestHeader Long userId){
        if (StringUtils.isBlank(resourceId)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (StringUtils.isBlank(pirParam)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"pirParam");
        }
        if (StringUtils.isBlank(taskName)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskName");
        }
        return pirService.pirSubmitTask(resourceId,pirParam,taskName, userId);
    }

    /**
     * 查询隐匿查询任务列表
     * @param req
     * @param userId 用户id
     * @param roleType 角色类型
     * @return
     */
    @ApiOperation(value = "匿踪查询任务列表")
    @GetMapping("getPirTaskList")
    public BaseResultEntity getPirTaskList(DataPirTaskReq req,
                                           @RequestHeader("userId") Long userId,
                                           @RequestHeader("roleType")Integer roleType
                                           ){
        if (req.getTaskState()!=null){
            if(!TaskStateEnum.TASK_STATE_MAP.containsKey(req.getTaskState())) {
                return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"taskState");
            }
        }
        if (req.getResourceName()!=null && req.getResourceName().contains("_")){
            // Cancel mysql like _ Wildcard action
            req.setResourceName(req.getResourceName().replace("_","\\_"));
        }
        // 0用户查询 1机构查询
        if (req.getQueryType() == null || !Arrays.asList(0, 1).contains(req.getQueryType())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"queryType");
        }
        return pirService.getPirTaskList(req, userId, roleType);
    }

    /**
     * 查询隐匿查询任务详情
     * @param taskId
     * @return
     */
    @ApiOperation(value = "匿踪任务详情",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name = "taskId", value = "任务ID", dataType = "Long", paramType = "query")
    @GetMapping(value = "getPirTaskDetail")
    public BaseResultEntity<DataPirTaskDetailVo> getPirTaskDetail(Long taskId){
        if (taskId==null||taskId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return pirService.getPirTaskDetail(taskId);
    }

    @ApiOperation(value = "匿踪查询任务结果文件下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "taskId", value = "任务ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "taskDate", value = "任务创建日期", dataType = "String", paramType = "query")
    })
    @GetMapping("downloadPirTask")
    public void downloadPirTask(HttpServletResponse response,String taskId,String taskDate) {
        if (StringUtils.isBlank(taskId)||StringUtils.isBlank(taskDate)) {
            return;
        }
        String resultFilePath = pirService.getResultFilePath(taskId,taskDate);
        String fileName = taskId+".csv";
        OutputStream outputStream = null;
        try {
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type","application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-disposition","attachment;filename="+ URLEncoder.encode(new String(fileName.getBytes("UTF-8"),"iso-8859-1"),"utf-8"));
            outputStream = response.getOutputStream();
            File file = new File(resultFilePath);
            if (!file.exists()){
                log.info("{}-不存在",resultFilePath);
                //将字符串转化为文件
                byte[] currentLogByte = "no data".getBytes();
                outputStream.write(currentLogByte);
            }else {
                InputStream stream = new FileInputStream(file);
                byte buff[] = new byte[1024];
                int length = 0;
                while ((length = stream.read(buff)) > 0) {
                    outputStream.write(buff,0,length);
                }
                stream.close();
            }
            outputStream.close();
            outputStream.flush();
        }catch (Exception e) {
            log.info("downloadPirTask -- fileName:{} -- resultFilePath:{} -- e:{}",fileName,resultFilePath,e.getMessage());
        }
    }
}
