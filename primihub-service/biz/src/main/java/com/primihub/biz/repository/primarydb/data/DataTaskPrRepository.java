package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataExamTask;
import com.primihub.biz.entity.data.po.DataPirTask;
import com.primihub.biz.entity.data.po.DataTask;
import org.springframework.stereotype.Repository;

@Repository
public interface DataTaskPrRepository {

    void saveDataTask(DataTask dataTask);

    void updateDataTask(DataTask dataTask);

    void deleteDataTask(Long taskId);

    void saveDataPirTask(DataPirTask DataPirTask);

    void deleteDataPirTask(Long taskId);

    void updateDataPirTask(DataPirTask dataPirTask);

    void saveDataExamTask(DataExamTask dataExamTask);
    // todo 添加字段 containY
    void updateDataExamTask(DataExamTask dataExamTask);

}
