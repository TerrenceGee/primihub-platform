package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.lpy.CTCCExamTask;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.vo.lpy.CtccExamTaskVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataCTCCRepository {
    List<CtccExamTaskVo> selectCtccExamTaskPage(PageReq req);

    Integer selectCtccExamTaskCount(PageReq req);

    CTCCExamTask selectCtccExamTaskById(@Param("id") Long taskId);
}
