package com.primihub.biz.entity.sys.param;

import com.primihub.biz.entity.data.po.DataResource;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChainDataResourceParam {
    private String fileHash;
    private String resourceName;
    private String resourceDesc;
    private String tags;
    private Integer fileSize;
    private Integer fileColumns;
    private Integer fileRows;

    public ChainDataResourceParam() {
    }

    public ChainDataResourceParam(String fileHash, String resourceName, String resourceDesc, String tags, Integer fileSize, Integer fileColumns, Integer fileRows) {
        this.fileHash = fileHash;
        this.resourceName = resourceName;
        this.resourceDesc = resourceDesc;
        this.tags = tags;
        this.fileSize = fileSize;
        this.fileColumns = fileColumns;
        this.fileRows = fileRows;
    }

    public ChainDataResourceParam(DataResource dataResource, String tags) {
        this.fileHash = dataResource.getResourceHashCode();
        this.resourceName = dataResource.getResourceName();
        this.resourceDesc = dataResource.getResourceDesc();
        this.tags = tags;
        this.fileSize = dataResource.getFileSize();
        this.fileColumns = dataResource.getFileColumns();
        this.fileRows = dataResource.getFileRows();
    }
}
