package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.PirRecord;
import com.primihub.biz.entity.data.po.PsiRecord;
import com.primihub.biz.entity.data.req.RecordReq;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface RecordRepository {

    PsiRecord selectPsiRecordByRecordId(@Param("recordId") String psiRecordId);

    PirRecord selectPirRecordByRecordId(@Param("recordId") String recordId);

    Set<PsiRecord> selectPsiRecordList(RecordReq req);

    List<PsiRecord> selectPsiRecordPage(RecordReq req);

    Integer selectPsiRecordCount(RecordReq req);

    List<PirRecord> selectPirRecordPage(RecordReq req);

    Integer selectPirRecordCount(RecordReq req);

    List<PsiRecord> selectPsiRecordWeekly(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    List<PirRecord> selectPirRecordWeekly(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
