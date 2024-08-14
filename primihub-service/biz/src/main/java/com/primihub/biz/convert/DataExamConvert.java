package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataExamTask;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.lpy.CTCCExamTask;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.vo.DataExamTaskVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;

import java.util.Collections;

public class DataExamConvert {

    public static DataExamTask convertReqToPo(DataExamReq req, SysLocalOrganInfo localOrganInfo, DataResource dataResource) {
        DataExamTask po = new DataExamTask();
        po.setTaskId(req.getTaskId());
        po.setTaskName(req.getTaskName());
        po.setOriginResourceId(req.getResourceId());
        po.setOriginOrganId(localOrganInfo.getOrganId());
        po.setTargetOrganId(req.getTargetOrganId());
        po.setContainsY(dataResource.getFileContainsY());
        return po;
    }

    public static DataExamTaskVo convertPoToVo(DataExamTask task) {
        DataExamTaskVo vo = new DataExamTaskVo();
        return vo;
    }

    public static DataExamReq convertPoToReq(DataExamTask po) {
        DataExamReq req = new DataExamReq();
        req.setTaskId(po.getTaskId());
        req.setTaskState(po.getTaskState());
        req.setResourceId(po.getOriginResourceId());
        req.setTaskName(po.getTaskName());
        req.setTargetResourceId(po.getTargetResourceId());
        req.setOriginOrganId(po.getOriginOrganId());
        req.setTargetOrganId(po.getTargetOrganId());
        return req;
    }

    public static DataExamReq convertCtccToReq(CTCCExamTask task) {
        DataExamReq req = new DataExamReq();
        req.setTargetResourceId(task.getTargetResourceId());
        req.setTargetField(task.getTargetField());
        req.setTaskName(task.getTaskName());
        req.setResourceId(task.getOriginResourceId());
        req.setOriginOrganId(task.getOriginOrganId());
        req.setTaskState(task.getTaskState());
        req.setTargetOrganId(task.getTargetOrganId());
        req.setTaskId(task.getTaskId());
        req.setFieldValueSet(Collections.emptySet());
        return req;
    }
}
