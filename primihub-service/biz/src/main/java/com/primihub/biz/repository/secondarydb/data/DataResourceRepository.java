package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DerivationResourceReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.vo.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface DataResourceRepository {
    /**
     * 查询所以的资源标签
     * @return
     */
    List<String> queryAllResourceTag();

    List<DataResource> queryDataResource(Map<String,Object> paramMap);

    Integer queryDataResourceCount(Map<String,Object> paramMap);

    List<DataResource> queryDataResourceByResourceIds(@Param("resourceIds")Set<Long> resourceIds,@Param("stringIds")Set<String> stringIds);

    List<DataResourceTag> queryTagsByResourceId(Long resourceId);

    DataResource queryDataResourceById(Long resourceId);

    DataSource queryDataSourceById(Long id);

    DataResource queryDataResourceByResourceFusionId(String resourceFusionId);

    List<DataDerivationResourceVo> queryDerivationResourceList(DerivationResourceReq req);

    Integer queryDerivationResourceListCount(DerivationResourceReq req);

    List<Long> queryResourceTagRelation(Long resourceId);

    List<DataResourceRecordVo> queryDataResourceByIds(@Param("resourceIds") Set<Long> resourceIds);

    List<ResourceTagListVo> queryDataResourceListTags(@Param("resourceIds") List<Long> resourceIds);

    Integer queryResourceProjectRelationCount(String resourceId);

    List<DataFileField> queryDataFileField(Map<String,Object> paramMap);

    List<DataFileField> queryDataFileFieldByFileId(@Param("resourceId")Long resourceId);

    DataFileField queryDataFileFieldById(Long id);

    Integer queryDataFileFieldCount(Map<String,Object> paramMap);

    List<DataResource> findCopyResourceList(@Param("startOffset")Long startOffset, @Param("endOffset")Long endOffset);

    List<Long> queryDataResourceIds(@Param("pageSize") Integer pageSize,@Param("pId")Long pId);

    Long findMaxDataResource();

    List<DataResourceVisibilityAuth> findAuthOrganByResourceId(@Param("resourceIds") List<Long> resourceIds);

    List<DataResourceUsageListVo> queryDataResourceUsageList(@Param("resourceId")Long resourceId);

    Integer queryDataResourceUsageCount(@Param("resourceId")Long resourceId);

    List<DataResourceUserAssign> findUserAssignByParam(Map<String, Object> paramMap);
    Integer findUserAssignCountByParam(Map<String,Object> paramMap);

    // todo
    List<DataResource> queryDataResourceOtherUser(HashMap<String, Object> paramMap);

    // todo
    Integer queryDataResourceOtherUserCount(HashMap<String, Object> paramMap);

    List<DataResourceAssignmentListVo> queryDataResourceUserAssignmentByResourceId(@Param("resourceFusionId") String resourceFusionId);

    // --------------------------------------------------
    DataResourceUserAssign queryDataResourceUserAssignmentById(@Param("id") Long id);
    DataResourceVisibilityAuth queryDataResourceAuthById(@Param("id") Long id);
    List<DataResourceVisibilityAuth> findAuthOrganByParam(Map<String,Object> paramMap);
    Integer findAuthOrganCountByParam(Map<String,Object> paramMap);
}
