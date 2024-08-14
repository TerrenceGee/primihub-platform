package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.lpy.CTCCExamTask;
import org.springframework.stereotype.Repository;

@Repository
public interface DataCTCCPrimarydbRepository {

    void saveCtccExamTask(CTCCExamTask ctccTask);

    void updateCtccExamTask(CTCCExamTask ctccExamTask);
}
