package com.primihub.biz.service.data.exam;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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
            throw new RuntimeException(e);
        }
        this.execute(req);
    }

    public void downloadRawExamFile(DataExamReq req) throws IOException {
        String dirFileName = baseConfiguration.getUploadUrlDirPrefix() +
                "raw-exam-file" + "/";

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
        File dirFile = new File(dirFileName);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        String fileName = dirFileName + req.getTaskName() + "-" + req.getResourceId() + DateUtil.formatDate(new Date(), DateUtil.DateStyle.TIME_FORMAT_SHORT.getFormat()) + "." + "csv";
        FileUtil.convertToCsv(metaData, fileName);
        log.info("write raw exam file success, path: {}", fileName);
    }

    public abstract void execute(DataExamReq req);
}
