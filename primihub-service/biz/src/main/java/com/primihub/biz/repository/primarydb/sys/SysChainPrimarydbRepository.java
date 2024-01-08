package com.primihub.biz.repository.primarydb.sys;

import com.primihub.biz.entity.sys.po.SysChainInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import org.springframework.stereotype.Repository;

@Repository
public interface SysChainPrimarydbRepository {

    void insertChainInfo(SysChainInfo sysChainInfo);

    void updateChainInfo(SysChainInfo sysChainInfo);
}
