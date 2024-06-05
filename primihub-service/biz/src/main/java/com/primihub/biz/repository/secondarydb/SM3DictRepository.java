package com.primihub.biz.repository.secondarydb;

import com.primihub.biz.entity.SM3Dict;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SM3DictRepository {

    List<SM3Dict> selectSm3DictList(@Param("list") List<String> queryValueList);
}