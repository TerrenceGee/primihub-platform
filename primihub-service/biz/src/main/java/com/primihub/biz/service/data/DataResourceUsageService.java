package com.primihub.biz.service.data;
import com.primihub.biz.entity.event.DataResourceUsageSaveEvent;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataResourceUsageService {

    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;

    @EventListener(DataResourceUsageSaveEvent.class)
    public void processDataResourceUsageSaveEvent(DataResourceUsageSaveEvent event) {
        if (event.getDataResourceUsageList() == null || event.getDataResourceUsageList().isEmpty()) {
            log.error("要添加的资源使用记录为空");
        }
        dataResourcePrRepository.saveDataResourceUsage(event.getDataResourceUsageList());
        log.info("添加资源使用记录条数：{}", event.getDataResourceUsageList().size());
    }
}
