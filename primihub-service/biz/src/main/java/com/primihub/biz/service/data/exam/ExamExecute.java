package com.primihub.biz.service.data.exam;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class ExamExecute {
    @Autowired
    private BaseConfiguration baseConfiguration;

    public void processExam(DataExamReq req) {
        try {
            log.info("raw exam field: {}", req.getFieldValueMap().keySet());
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
        // keySet
        Set<String> keySet = fieldValueMap.keySet();
        List<String> targetFieldValueList = req.getFieldValueMap().get(req.getTargetField());
        List<Map> metaData = new ArrayList<>();

        for (int i = 0; i < targetFieldValueList.size(); i++) {
            Map<String, String> map = new HashMap<>();
            for (String key : keySet) {
                map.put(key, fieldValueMap.get(key).get(i));
            }
            metaData.add(map);
        }
        log.info("metaData 1st row field: {}", metaData.get(0).entrySet());
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
