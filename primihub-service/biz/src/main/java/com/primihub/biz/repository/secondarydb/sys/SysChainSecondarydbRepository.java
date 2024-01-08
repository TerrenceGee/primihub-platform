package com.primihub.biz.repository.secondarydb.sys;

import com.primihub.biz.entity.sys.po.SysChainInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysChainSecondarydbRepository {

    List<SysChainInfo> selectChainInfo();
}
