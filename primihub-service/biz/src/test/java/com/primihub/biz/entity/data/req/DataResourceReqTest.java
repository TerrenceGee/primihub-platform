package com.primihub.biz.entity.data.req;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.Test;

public class DataResourceReqTest {
    @Test
    public void test() {
        DataResourceReq dataResourceReq = new DataResourceReq();
        String jsonString = JSON.toJSONString(dataResourceReq, SerializerFeature.WriteMapNullValue);
        System.out.println(jsonString);
    }


  
}