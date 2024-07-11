package com.primihub.biz.repository.primaryredis.sys;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.entity.data.req.DataPirCopyReq;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Repository
public class TaskPrimaryRedisRepository {
    @Resource(name = "primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;

    public DataPirCopyReq getCopyPirReq(Long taskId) {
        String result = primaryStringRedisTemplate.opsForValue()
                .get(RedisKeyConstant.PIR_PHASE1_TASK.replace("<taskId>", String.valueOf(taskId)));
        return JSON.parseObject(result, DataPirCopyReq.class);
    }

    public void setCopyPirReq(DataPirCopyReq req) {
        primaryStringRedisTemplate.opsForValue().set(
                RedisKeyConstant.PIR_PHASE1_TASK.replace("<taskId>", String.valueOf(req.getDataPirTaskId())),
                JSON.toJSONString(req),
                3L,
                TimeUnit.DAYS
        );
    }

    public void deleteCopyPirReq(Long taskId) {
        primaryStringRedisTemplate.delete(RedisKeyConstant.PIR_PHASE1_TASK.replace("<taskId>", String.valueOf(taskId)));
    }
}
