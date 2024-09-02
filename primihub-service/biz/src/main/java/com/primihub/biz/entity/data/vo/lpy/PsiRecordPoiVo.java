package com.primihub.biz.entity.data.vo.lpy;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.primihub.biz.entity.data.po.PsiRecord;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@ExcelTarget("隐私求交执行记录表 ")
public class PsiRecordPoiVo implements java.io.Serializable {
    private Long id;
    @Excel(name = "记录id", orderNum = "1", width = 25, needMerge = true)
    private String recordId;
    @Excel(name = "隐私求交任务名称")
    private String psiName;
    @Excel(name = "任务状态(0未开始 1成功 2运行中 3失败 4取消)")
    private Integer taskState;
    @Excel(name = "发起方机构id")
    private String originOrganId;
    @Excel(name = "协作方机构id")
    private String targetOrganId;
    @Excel(name = "开始时间")
    private Date startTime;
    @Excel(name = "结束时间")
    private Date endTime;
    @Excel(name = "隐私求交任务提交样本数")
    private Integer commitRowsNum;
    @Excel(name = "隐私求交任务结果数")
    private Integer resultRowsNum;
    @Excel(name = "隐私求交目标字段")
    private String targetField;

    public PsiRecordPoiVo() {
    }

    public PsiRecordPoiVo(PsiRecord po) {
        this.id = po.getId();
        this.recordId = po.getRecordId();
        this.psiName = po.getPsiName();
        this.taskState = po.getTaskState();
        this.originOrganId = po.getOriginOrganId();
        this.targetOrganId = po.getTargetOrganId();
        this.startTime = po.getStartTime();
        this.endTime = po.getEndTime();
        this.commitRowsNum = po.getCommitRowsNum();
        this.resultRowsNum = po.getResultRowsNum();
        this.targetField = po.getTargetField();
    }
}
