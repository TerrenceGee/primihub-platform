package com.primihub.biz.service.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.po.DataResourceOrganAssign;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.vo.DataResourceAssignmentListVo;
import com.primihub.biz.entity.data.vo.DataResourceCopyVo;
import com.primihub.biz.entity.fusion.param.OrganResourceParam;
import com.primihub.biz.entity.fusion.param.ResourceParam;
import com.primihub.biz.entity.fusion.vo.FusionResourceVo;
import com.primihub.biz.entity.sys.po.DataSet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "fusion", contextId = "resource")
public interface FusionResourceService {
    @RequestMapping("/fusionResource/getResourceList")
    BaseResultEntity getResourceList(@RequestBody ResourceParam resourceParam);

    @RequestMapping("/fusionResource/getResourceListById")
    BaseResultEntity getResourceListById(@RequestParam("resourceIdArray") List<String> resourceIdArray, @RequestParam("globalId") String globalId);
    @RequestMapping("/fusionResource/getResourceListByIdList")
    void getDataResourceByIdList(@RequestParam("resourceIdList")List<String> resourceFusionIds);

    @RequestMapping("/fusionResource/getResourceTagList")
    BaseResultEntity getResourceTagList();

    @RequestMapping("/fusionResource/getDataResource")
    BaseResultEntity getDataResource(@RequestParam("resourceId") String resourceId, @RequestParam("globalId") String globalId);

    @RequestMapping("/fusionResource/saveOrganResourceAuth")
    BaseResultEntity saveOrganResourceAuth(@RequestParam("organId") String organId, @RequestParam("resourceId") String resourceId, @RequestParam("projectId") String projectId, @RequestParam("auditStatus") Integer auditStatus);

    @RequestMapping("/fusionResource/getOrganResourceList")
    BaseResultEntity getOrganResourceList(OrganResourceParam param);

    @PostMapping("/copy/batchSave")
    BaseResultEntity batchSave(@RequestParam("globalId") String globalId, @RequestParam("copyPart") String copyPart);

    @PostMapping("/fusionResource/saveResource")
    BaseResultEntity saveResource(@RequestParam("globalId") String globalId, @RequestBody List<DataResourceCopyVo> copyResourceDtoList);

    @RequestMapping("/fusionResource/getCopyResource")
    BaseResultEntity getCopyResource(@RequestParam("resourceIds") Set<String> resourceIds);

    @RequestMapping("/fusionResource/getTestDataSet")
    BaseResultEntity getTestDataSet(@RequestParam("id") String id);

    @RequestMapping("/fusionResource/getDataSets")
    BaseResultEntity getDataSets(@RequestBody Set<String> id);

    @PostMapping("/fusionResource/batchSaveTestDataSet")
    BaseResultEntity batchSaveTestDataSet(@RequestBody List<DataSet> dataSets);

//    @GetMapping("/fusionResource/getDataResourceOrganAssignment")
//    BaseResultEntity getDataResourceOrganAssignment(@RequestParam("organGlobalId") String organGlobalId, Integer pageNo, Integer pageSize);

    @GetMapping("/fusionResource/getDataResourceOrganAssignment")
    BaseResultEntity getDataResourceOrganAssignment(Map<String, Object> param);

    @GetMapping("/fusionResource/getDataResourceToApply")
    BaseResultEntity getDataResourceToApply(@RequestParam("organGlobalId") String sysLocalOrganId, @RequestParam("organGlobalId")Integer pageNo, @RequestParam("organGlobalId")Integer pageSize);

    @RequestMapping("/fusionResource/getResourceListUser")
    BaseResultEntity getResourceListUser(@RequestBody ResourceParam resourceParam);

    @RequestMapping("/fusionResource/getResourceListOrgan")
    BaseResultEntity getResourceListOrgan(@RequestBody ResourceParam resourceParam);

    @PostMapping("/fusionResource/saveResourceOrganAssignList")
    BaseResultEntity saveDataResourceOrganAssignList(@RequestParam("globalId") String globalId, @RequestBody List<DataResourceOrganAssign> dataResourceOrganAssigns);

    @GetMapping("/fusionResource/getDataResourceOrganAssignmentByResourceId")
    List<DataResourceAssignmentListVo> getDataResourceOrganAssignmentByResourceId(@RequestParam("resourceFusionId") String resourceFusionId);

    // ----------------------------------------------------------------------------------

    @GetMapping("/fusionResource/getDataResourceAvailableOfOrgan")
    BaseResultEntity<PageDataEntity> getDataResourceAvailableOfOrgan(ResourceParam param);

    /**
     * 用户可用
     */
    @GetMapping("/fusionResource/getDataResourceAvailableOfUser")
    List getDataResourceAvailableOfUser(@RequestParam("resourceIdList") List<String> resourceIdList);

    /**
     * 机构已获得授权
     */
    @GetMapping("/fusionResource/getDataResourceAssignmentOfOrgan")
    List getDataResourceAssignmentOfOrgan(@RequestParam("organId") String organId);

    /**
     * 用户已获得授权
     */
    @GetMapping("/fusionResource/getDataResourceAssignmentOfUser")
    List getDataResourceAssignmentOfUser(@RequestParam("resourceIdList") List<String> resourceIdList);
    /**
     * 机构可申请的
     */
    @GetMapping("/fusionResource/getDataResourceToApplyOfOrgan")
    List getDataResourceToApplyOfOrgan(@RequestParam("organId") String organId);
}
