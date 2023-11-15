package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataResourceUserAssign;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataResourceUserAssignRepository {
    List<DataResourceUserAssign> selectUserAssignRepository(Map<String, Object> paramMap);
}
