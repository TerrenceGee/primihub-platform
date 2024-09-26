package com.primihub.biz.service.data.exam;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public abstract class ExamExecute {
    @Autowired
    private BaseConfiguration baseConfiguration;

    public void processExam(DataExamReq req) {
        try {
            this.downloadRawExamFile(req);
        } catch (IOException e) {
            log.error("write raw exam file error", e);
            return;
        }
        this.execute(req);
    }

    public void downloadRawExamFile(DataExamReq req) throws IOException {
        String sb = baseConfiguration.getUploadUrlDirPrefix() +
                "raw-exam-file" + "/" +
                req.getTaskName() + "-" + req.getResourceId() +
                "." + "csv";

        Map<String, List<String>> fieldValueMap = req.getFieldValueMap();
        List<Map> metaData = fieldValueMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> {
                            Map<String, String> map = new HashMap<>();
                            map.put(entry.getKey(), value);
                            return map;
                        })
                )
                .collect(Collectors.toList());
        FileUtil.convertToCsv(metaData, sb);
        log.info("write raw exam file success, path: {}", sb);
    }

    public abstract void execute(DataExamReq req);
}
