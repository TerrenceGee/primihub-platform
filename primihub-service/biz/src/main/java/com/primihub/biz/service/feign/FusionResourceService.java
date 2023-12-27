package com.primihub.biz.service.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.vo.DataResourceCopyVo;
import com.primihub.biz.entity.fusion.param.OrganResourceParam;
import com.primihub.biz.entity.fusion.param.ResourceParam;
import com.primihub.biz.entity.sys.po.DataSet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "fusion", contextId = "resource")
//@FeignClient(name = "fusion",contextId = "resource",url = "http://192.168.99.13:32132")
@FeignClient(name = "fusion",contextId = "resource")
public interface FusionResourceService {
    @RequestMapping("/fusionResource/getResourceList")
    BaseResultEntity getResourceList(@RequestBody ResourceParam resourceParam);

    @RequestMapping("/fusionResource/getResourceListById")
    BaseResultEntity getResourceListById(@RequestParam("resourceIdArray") List<String> resourceIdArray, @RequestParam("globalId") String globalId);

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
//    @GetMapping("/fusionResource/getDataResourceOrganAssignmentByResourceId")
//    BaseResultEntity<PageDataEntity> getDataResourceOrganAssignmentByResourceId(String resourceFusionId, PageReq pageReq);

    // ----------------------------------------------------------------------------------
    /** 用户可使用 */
    @RequestMapping("/fusionResource/getResourceListUser")
    BaseResultEntity getResourceListUser(@RequestBody ResourceParam resourceParam);
    /** 机构可使用 */
    @RequestMapping("/fusionResource/getResourceListOrgan")
    BaseResultEntity getResourceListOrgan(@RequestBody ResourceParam resourceParam);
    /**
     * 机构已获得授权
     */
    @GetMapping("/fusionResource/getDataResourceAssignmentOfOrgan")
    BaseResultEntity<PageDataEntity> getDataResourceAssignmentOfOrgan(ResourceParam param);
    /**
     * 用户已获得授权
     */
    @GetMapping("/fusionResource/getDataResourceAssignmentOfUser")
    BaseResultEntity<PageDataEntity> getDataResourceAssignmentOfUser(ResourceParam param);
    /**
     * 机构可申请的
     */
//    @GetMapping("/fusionResource/getDataResourceToApplyOfOrgan")
//    List getDataResourceToApplyOfOrgan(@RequestParam("organId") String organId);

    @RequestMapping("/fusionResource/getCoopResourceListOrgan")
    BaseResultEntity getCoopResourceListOrgan(@RequestBody ResourceParam resourceParam);
}
