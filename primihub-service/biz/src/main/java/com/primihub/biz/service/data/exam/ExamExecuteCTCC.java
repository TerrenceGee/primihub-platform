package com.primihub.biz.service.data.exam;

import com.primihub.biz.entity.data.req.DataExamReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 注意，这个时候还没有模型分呢
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ExamExecuteCTCC implements ExamExecute {

    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : ctcc");


    }
}
