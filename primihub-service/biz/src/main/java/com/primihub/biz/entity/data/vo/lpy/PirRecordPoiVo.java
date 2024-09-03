package com.primihub.biz.entity.data.vo.lpy;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.primihub.biz.entity.data.po.PirRecord;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@ExcelTarget("隐匿查询执行记录表行")
public class PirRecordPoiVo {
    private Long id;
    @Excel(name = "记录id", orderNum = "1", width = 25, needMerge = true)
    private String recordId;
    @Excel(name = "隐匿查询任务名称")
    private String pirName;
    @Excel(name = "任务状态(0未开始 1成功 2运行中 3失败 4取消)")
    private Integer taskState;
    @Excel(name = "发起方机构id")
    private String originOrganId;
    @Excel(name = "协作方机构id")
    private String targetOrganId;
    @Excel(name = "开始时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @Excel(name = "结束时间", format = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    @Excel(name = "隐匿查询任务提交样本数")
    private Integer commitRowsNum;
    @Excel(name = "隐匿查询任务结果数")
    private Integer resultRowsNum;
    @Excel(name = "隐匿查询目标字段")
    private String targetField;
    @Excel(name = "隐匿查询模型分类型[yhhhwd_score(用户号码稳定性评分),yhxf_score(用户消费意愿评分),yhgm_score(用户购买力评分)]")
    private String scoreModelType;

    public PirRecordPoiVo() {
    }

    public PirRecordPoiVo(PirRecord po) {
        this.id = po.getId();
        this.recordId = po.getRecordId();
        this.pirName = po.getPirName();
        this.taskState = po.getTaskState();
        this.originOrganId = po.getOriginOrganId();
        this.targetOrganId = po.getTargetOrganId();
        this.startTime = po.getStartTime();
        this.endTime = po.getEndTime();
        this.commitRowsNum = po.getCommitRowsNum();
        this.resultRowsNum = po.getResultRowsNum();
        this.targetField = po.getTargetField();
        this.scoreModelType = po.getScoreModelType();
    }
}
