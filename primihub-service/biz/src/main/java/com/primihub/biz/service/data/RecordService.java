package com.primihub.biz.service.data;


import com.anji.captcha.util.StringUtils;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.po.PirRecord;
import com.primihub.biz.entity.data.po.PsiRecord;
import com.primihub.biz.entity.data.req.RecordReq;
import com.primihub.biz.repository.primarydb.data.RecordPrRepository;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.data.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RecordService {
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private RecordPrRepository recordPrRepository;
    @Autowired
    private SysCommonPrimaryRedisRepository redisRepository;

    public BaseResultEntity savePsiRecord(PsiRecord record) {
        PsiRecord psiRecord = recordRepository.selectPsiRecordByRecordId(record.getRecordId());
        if (psiRecord == null) {
            recordPrRepository.savePsiRecord(record);
        } else {
            record.setId(psiRecord.getId());
            recordPrRepository.updatePsiRecord(record);
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity savePirRecord(PirRecord record) {
        PirRecord pirRecord = recordRepository.selectPirRecordByRecordId(record.getRecordId());
        if (pirRecord == null) {
            String scoreModelType = redisRepository.getKey(record.getRecordId());
            if (StringUtils.isNotBlank(scoreModelType)) {
                record.setScoreModelType(scoreModelType);
                redisRepository.deleteKey(record.getRecordId());
            }
            recordPrRepository.savePirRecord(record);
        } else {
            record.setId(pirRecord.getId());
            recordPrRepository.updatePirRecord(record);
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity getPsiRecordList(RecordReq req) {
        return BaseResultEntity.success(recordRepository.selectPsiRecordList(req));
    }

    public BaseResultEntity getPsiRecordPage(RecordReq req) {
        List<PsiRecord> psiRecordList = recordRepository.selectPsiRecordPage(req);
        if (psiRecordList.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(), Collections.emptyList()));
        }
        Integer total = recordRepository.selectPsiRecordCount(req);
        return BaseResultEntity.success(new PageDataEntity(total, req.getPageSize(), req.getPageNo(), psiRecordList));
    }

    public BaseResultEntity getPirRecordPage(RecordReq req) {
        List<PirRecord> pirRecordList = recordRepository.selectPirRecordPage(req);
        if (pirRecordList.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(), Collections.emptyList()));
        }
        Integer total = recordRepository.selectPirRecordCount(req);
        return BaseResultEntity.success(new PageDataEntity(total, req.getPageSize(), req.getPageNo(), pirRecordList));
    }
}
