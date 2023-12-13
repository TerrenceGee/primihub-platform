package com.primihub.application.controller.data;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.DataResourceAuthTypeEnum;
import com.primihub.biz.entity.data.dataenum.ResourceStateEnum;
import com.primihub.biz.entity.data.dataenum.SourceEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.sdk.task.dataenum.FieldTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 数据资源管理
 */
@RequestMapping("resource")
@RestController
@Slf4j
public class ResourceController {

    @Autowired
    private DataResourceService dataResourceService;

    /**
     * 获取资源标签列表
     * @return
     */
    @GetMapping("getResourceTags")
    public BaseResultEntity getResourceTags(){
        return dataResourceService.getResourceTags();
    }

    /**
     * 资源概览列表信息接口
     * @param userId    用户id
     * @param req       分页、条件信息
     * @return
     */
    @GetMapping("getdataresourcelist")
    public BaseResultEntity getDataResourceList(@RequestHeader("userId") Long userId,
                                                @RequestHeader("roleType") Integer roleType,
                                                DataResourceReq req){
        return dataResourceService.getDataResourceList(req,userId, roleType);
    }


    /**
     * 获取衍生数据列表
     * @param req 分页、条件信息
     * @return
     */
    @GetMapping("getDerivationResourceList")
    public BaseResultEntity getDerivationResourceList(DerivationResourceReq req,
                                                      @RequestHeader("userId") Long userId,
                                                      @RequestHeader("roleType") Integer roleType
                                                      ){
        // 查询机构
        if (req.getQueryType() == 1) {
            // 需要有管理员权限
            if (roleType != 1) {
                return BaseResultEntity.failure(BaseResultEnum.NO_AUTH);
            }
        }
        return dataResourceService.getDerivationResourceList(req, userId);
    }

    /**
     * 获取衍生数据
     * @param req
     * @return
     */
    @GetMapping("getDerivationResourceData")
    public BaseResultEntity getDerivationResourceData(DerivationResourceReq req){
        if (req.getResourceId() == null || req.getResourceId() ==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return dataResourceService.getDerivationResourceData(req);
    }

    /**
     * 增加或修改资源接口
     * @param req       资源信息
     * @param userId    用户id
     * @return
     */
    @PostMapping("saveorupdateresource")
    public BaseResultEntity saveDataResource(@RequestBody DataResourceReq req,
                                             @RequestHeader("userId") Long userId){
        // update
        if (req.getResourceId()!=null&&req.getResourceId()!=0L){
            if ((req.getResourceName()==null|| "".equals(req.getResourceName().trim()))
                    &&(req.getResourceDesc()==null|| "".equals(req.getResourceDesc().trim()))
                    &&(req.getResourceAuthType()==null || req.getResourceAuthType()==0)
                    &&(req.getPublicOrganId()==null|| "".equals(req.getPublicOrganId().trim()))
                    &&(req.getResourceSource()==null || req.getResourceSource()==0)
                    &&(req.getTags()==null||req.getTags().size()==0)){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"至少需要一个修改的字段");
            }
            if(!DataResourceAuthTypeEnum.AUTH_TYPE_MAP.containsKey(req.getResourceAuthType())) {
                return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"resourceAuthType");
            }
            return dataResourceService.editDataResource(req,userId);
        }
        // save
        else {
            if (req.getResourceName()==null|| "".equals(req.getResourceName().trim())){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceName");
            }
            if (req.getResourceDesc()==null|| "".equals(req.getResourceDesc().trim())){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceDesc");
            }
            if (req.getResourceAuthType()==null || req.getResourceAuthType()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceAuthType");
            }
            if(!DataResourceAuthTypeEnum.AUTH_TYPE_MAP.containsKey(req.getResourceAuthType())) {
                return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"resourceAuthType");
            }
            if (req.getResourceSource()==null || req.getResourceSource()<=0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceSource");
            }
            if (req.getTags()==null||req.getTags().size()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"tags");
            }
            if (req.getResourceSource() == 1){
                if (req.getFileId()==null||req.getFileId()==0L){
                    return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId");
                }
            }
            if (req.getResourceSource() == 2){
                if (req.getDataSource() == null) {
                    return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dataSource");
                }
                if (StringUtils.isBlank(req.getDataSource().getDbUrl())) {
                    return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Url");
                }
                if (StringUtils.isBlank(req.getDataSource().getDbName())) {
                    return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"DbName");
                }
                if (StringUtils.isBlank(req.getDataSource().getDbTableName())) {
                    return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"DbTableName");
                }
                if (req.getDataSource().getDbType()==null || req.getDataSource().getDbType()<=0) {
                    return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Type");
                }
                if (SourceEnum.SOURCE_MAP.get(req.getDataSource().getDbType()) == SourceEnum.mysql){
                    if (StringUtils.isBlank(req.getDataSource().getDbDriver())) {
                        return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Driver");
                    }
                    if (StringUtils.isBlank(req.getDataSource().getDbUsername())) {
                        return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Username");
                    }
                    if (StringUtils.isBlank(req.getDataSource().getDbPassword())) {
                        return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Password");
                    }
                }
            }
            if (req.getFieldList()==null || req.getFieldList().size()==0) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fieldList");
            }
            return dataResourceService.saveDataResource(req,userId);
        }
    }

    /**
     * 查询单个资源信息
     * @param resourceId    资源id
     * @return
     */
    @GetMapping("getdataresource")
    public BaseResultEntity getDataResource(String resourceId){
        if (StringUtils.isBlank(resourceId)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return dataResourceService.getDataResource(resourceId);

    }

    /**
     * 删除一个资源信息
     * @param resourceId 资源Id
     * @return
     */
    @GetMapping("deldataresource")
    public BaseResultEntity deleteDataResource(Long resourceId){
        if (resourceId==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM);
        }
        return dataResourceService.deleteDataResource(resourceId);
    }

    /**
     * 资源文件预览
     * @param fileId 文件Id
     * @param resourceId 资源Id
     * @return
     */
    @RequestMapping("resourceFilePreview")
    public BaseResultEntity resourceFilePreview(Long fileId,String resourceId){
        if (StringUtils.isBlank(resourceId)){
            if(fileId==null||fileId==0L) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId or resourceId");
            }
        }
        return dataResourceService.resourceFilePreview(fileId,resourceId);
    }


    /**
     * 获取资源文件字段
     * @param resourceId 资源Id
     * @return
     */
    @RequestMapping("getDataResourceFieldPage")
    public BaseResultEntity getDataResourceFieldPage(@RequestHeader("userId") Long userId,
                                                     Long resourceId,
                                                     PageReq req){
        if (resourceId==null||resourceId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (userId==null||userId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataResourceService.getDataResourceFieldPage(resourceId,req,userId);
    }

    /**
     * 修改字段信息
     * @return
     */
    @RequestMapping("updateDataResourceField")
    public BaseResultEntity updateDataResourceField(DataResourceFieldReq req){
        if (req.getFieldId()==null||req.getFieldId()==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fieldId");
        }
        FieldTypeEnum fieldTypeEnum = null;
        if (StringUtils.isNotBlank(req.getFieldType())){
            fieldTypeEnum = FieldTypeEnum.getEnumByTypeName(req.getFieldType());
            if (fieldTypeEnum==null) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"类型错误");
            }
        }
        return dataResourceService.updateDataResourceField(req,fieldTypeEnum);
    }


    /**
     * 修改数据资源状态
     * @param resourceId 资源Id
     * @param resourceState 资源状态
     * @return
     */
    @RequestMapping("resourceStatusChange")
    public BaseResultEntity resourceStatusChange(Long resourceId,Integer resourceState){
        if (resourceId==null||resourceId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (resourceState==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceState");
        }
        if (!ResourceStateEnum.RESOURCE_STATE_MAP.containsKey(resourceState)) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"resourceState");
        }
        return dataResourceService.resourceStatusChange(resourceId,resourceState);
    }

    /**
     * 展示的数据源类型
     * @return
     */
    @RequestMapping("displayDatabaseSourceType")
    public BaseResultEntity displayDatabaseSourceType(){
        return dataResourceService.displayDatabaseSourceType();
    }

    /**
     * 注册资源
     * @param resourceId 资源Id
     * @return
     */
    @PostMapping("noticeResource")
    public BaseResultEntity noticeResource(String resourceId){
        if (StringUtils.isBlank(resourceId)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return dataResourceService.noticeResource(resourceId);
    }

    /**
     * 下载文件
     * @param response
     * @param resourceId 资源Id
     * @throws Exception
     */
    @RequestMapping("download")
    public void download(HttpServletResponse response, Long resourceId) throws Exception{
        DataResource dataResource = dataResourceService.getDataResourceUrl(resourceId);
        if (dataResource == null || StringUtils.isBlank(dataResource.getUrl())){
            downloadTaskError(response,"无资源信息");
        }else {
            File file = new File(dataResource.getUrl());
            if (file.exists()){
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    response.setHeader("content-Type","application/vnd.ms-excel");
                    response.setHeader("content-disposition", "attachment; fileName=" + new String((dataResource.getResourceName()+".csv").getBytes("UTF-8"),"iso-8859-1"));
                    ServletOutputStream outputStream = response.getOutputStream();
                    int len = 0;
                    byte[] data = new byte[1024];
                    while ((len = inputStream.read(data)) != -1) {
                        outputStream.write(data, 0, len);
                    }
                    outputStream.close();
                    inputStream.close();
                }catch (Exception e) {
                    downloadTaskError(response,"文件读取失败");
                }
            }else {
                downloadTaskError(response,"无文件信息");
            }
        }

    }

    public void downloadTaskError(HttpServletResponse response,String message) throws IOException {
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(JSONObject.toJSONString(BaseResultEntity.failure(BaseResultEnum.DATA_DOWNLOAD_TASK_ERROR_FAIL,message)));
    }

    /**
     * 修改授权状态，审核
     * @param userId
     * @param roleType
     * @param req
     * @return
     */
    @PostMapping("changeDataResourceAuthStatus")
    public BaseResultEntity changeDataResourceAuthStatus(@RequestHeader("userId") Long userId,
                                                   @RequestHeader("roleType")Integer roleType,
                                                   DataResourceApplyReq req
                                                   ) {
        if (req.getAuditStatus()==null||req.getAuditStatus()==1 || req.getAuditStatus()==2){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"auditStatus");
        }
        return dataResourceService.changeDataResourceAuthStatus(req, userId);
    }


    /**
     * 授权给我的资源
     */
    @GetMapping("getDataResourceAssignedToMe")
    public BaseResultEntity getDataResourceAssignedToMe(@RequestHeader("userId") Long userId,
                                                        @RequestHeader("roleType") Integer roleType,
                                                        Integer queryType,
                                                        PageReq req
    ) {
        return dataResourceService.getDataResourceAssignedToMe(userId, roleType, req, queryType);
    }

    /**
     * 可申请的资源，todo 删除，可申请资源从数据中心获取
     */
    /*@GetMapping("getDataResourceToApply")
    public BaseResultEntity getDataResourceToApply(@RequestHeader("userId") Long userId,
                                                   @RequestHeader("roleType") Integer roleType,
                                                   PageReq req
    ) {
        return dataResourceService.getDataResourceToApply(userId, roleType, req);
    }*/

    /**
     * 给资源授权，包括本方资源和已获得授权的合作方资源
     */
    @PostMapping("saveDataResourceUserAssignment")
    public BaseResultEntity saveUserAssignment(@RequestHeader("userId")Long userId,
                                               @RequestHeader("roleType") Integer roleType,
                                               @RequestBody UserAssignReq req
                                               ) {
        return dataResourceService.saveUserAssignment(req,userId, roleType);
    }
    /**
     * 查看资源的用户授权详情
     */
    @GetMapping("getDataResourceUserAssignment")
    public BaseResultEntity getUserAssignment(
            @RequestHeader("userId")Long userId,
            @RequestHeader("roleType") Integer roleType,
            String resourceFusionId
    ) {
        return dataResourceService.getDataResourceUserAssignment(resourceFusionId, userId, roleType);
    }

    /**
     * 查看资源授权详情
     */
    @GetMapping("getDataResourceAssignmentDetail")
    public BaseResultEntity getDataResourceAssignmentDetail(
            @RequestHeader("userId")Long userId,
            @RequestHeader("roleType") Integer roleType,
            String resourceFusionId,PageReq pageReq,
            Integer queryType   // 1查询机构授权 2查询用户授权
    ) {
        if (queryType == null || !Arrays.asList(1, 2).contains(queryType)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "queryType");
        }
        return dataResourceService.getDataResourceAssignmentDetail(resourceFusionId, userId, pageReq, queryType);
    }

    /**
     * 发起资源申请
     */
    @PostMapping("saveDataResourceAssign")
    public BaseResultEntity saveDataResourceAssign(
            @RequestHeader("userId")Long userId,
            @RequestHeader("roleType") Integer roleType,
            @RequestBody DataResourceAssignReq req
    ) {
        return dataResourceService.saveDataResourceAssign(userId, roleType, req);
    }

    /**
     * 处理资源申请
     */
    @PostMapping("saveDataResourceOrganAssign")
    public BaseResultEntity saveDataResourceOrganAssign(
            @RequestBody DataResourceAssignReq req
    ) {
        log.info("处理资源申请");
        return dataResourceService.saveDataResourceOrganAssign(req);
    }
}
