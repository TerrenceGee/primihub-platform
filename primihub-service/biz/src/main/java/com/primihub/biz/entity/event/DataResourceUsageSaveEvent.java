package com.primihub.biz.entity.event;

import com.primihub.biz.entity.data.po.DataResourceUsage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DataResourceUsageSaveEvent {
    private List<DataResourceUsage> dataResourceUsageList;
}
