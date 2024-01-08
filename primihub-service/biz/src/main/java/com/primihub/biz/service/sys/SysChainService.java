package com.primihub.biz.service.sys;


import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.DataResourceTag;
import com.primihub.biz.entity.sys.param.ChainDataResourceParam;
import com.primihub.biz.entity.sys.param.ChainInfoParam;
import com.primihub.biz.entity.sys.po.SysChainInfo;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.primarydb.sys.SysChainPrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysChainSecondarydbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class SysChainService {
    @Autowired
    private SysChainSecondarydbRepository sysChainSecondarydbRepository;
    @Autowired
    private SysChainPrimarydbRepository sysChainPrimarydbRepository;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    @Qualifier("soaRestTemplate")
    private RestTemplate restTemplate;
    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;
    public BaseResultEntity getChainInfo() {
        List<SysChainInfo> sysChainInfos = sysChainSecondarydbRepository.selectChainInfo();
        if (sysChainInfos.isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        return BaseResultEntity.success(sysChainInfos.get(0));
    }

    public BaseResultEntity saveOrUpdateChainInfo(ChainInfoParam param) {
        // 信息不会做删除，这里不用考虑先查后写的问题
        List<SysChainInfo> sysChainInfos = sysChainSecondarydbRepository.selectChainInfo();
        SysChainInfo sysChainInfo = new SysChainInfo(param);
        if (sysChainInfos.isEmpty()) {
            sysChainPrimarydbRepository.insertChainInfo(sysChainInfo);
        } else {
            sysChainPrimarydbRepository.updateChainInfo(sysChainInfo);
        }
        return BaseResultEntity.success(param);
    }

    public BaseResultEntity saveChainDataResource(Long resourceId) {
        List<SysChainInfo> sysChainInfos = sysChainSecondarydbRepository.selectChainInfo();
        if (sysChainInfos.isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_CONFIGURATION);
        } else {
            // 这里应当校验每一项字段
        }
        DataResource dataResource = dataResourceRepository.queryDataResourceById(resourceId);
        if (dataResource == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        List<DataResourceTag> dataResourceTags = dataResourceRepository.queryTagsByResourceId(dataResource.getResourceId());
        String tags = dataResourceTags.stream().map(DataResourceTag::getTagName).collect(Collectors.joining(","));
        ChainDataResourceParam param = new ChainDataResourceParam(dataResource, tags);
        String chainCenterService = baseConfiguration.getChainCenterService();
        if (chainCenterService == null || chainCenterService.isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_CONFIGURATION);
        }

        String chainToken = getChainToken(sysChainInfos);
        if (chainToken == null || chainToken.isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.CHAIN_SERVICE_NOT_AVAILABLE);
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", chainToken);
        HttpEntity httpEntity = new HttpEntity(param, httpHeaders);
        BaseResultEntity entity = restTemplate.postForObject(chainCenterService + SysConstant.SYS_CHAIN_SAVE_URI, httpEntity, BaseResultEntity.class);
        if (entity != null && entity.getCode() == 0 && entity.getResult() != null) {
            String tradeHashCode = (String) entity.getResult();
            dataResource.setTradeHashCode(tradeHashCode);
            dataResourcePrRepository.editResource(dataResource);
            return BaseResultEntity.success(entity.getResult());
        } else {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RESOURCE_EVIDENCE_FAILURE, entity.getMsg());
        }
        // 如果鉴定成功，后续 dataResource便不再支持修改
        // 要把返回的交易hash存到表记录中
    }

    public BaseResultEntity getChainDataResource(String tradeHashCode) {
        List<SysChainInfo> sysChainInfos = sysChainSecondarydbRepository.selectChainInfo();
//        DataResource dataResource = dataResourceRepository.queryDataResourceByTradeHashCode(tradeHashCode);
//        if (dataResource == null) {
//            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
//        }
//        if (dataResource.getTradeHashCode() == null || dataResource.getTradeHashCode().isEmpty()) {
//            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "tradeHashCode");
//        }
        String chainToken = getChainToken(sysChainInfos);
        if (chainToken == null || chainToken.isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.CHAIN_SERVICE_NOT_AVAILABLE);
        }

        String chainCenterService = baseConfiguration.getChainCenterService();
        String address = chainCenterService + SysConstant.SYS_CHAIN_VERIFY_URI.replace("<tradeHashCode>", tradeHashCode);
        // http请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", primaryStringRedisTemplate.opsForValue().get(RedisKeyConstant.CHAIN_TOKEN_KEY.replace("<username>", sysChainInfos.get(0).getUsername())));
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(address));
        ResponseEntity<BaseResultEntity> responseEntity = restTemplate.exchange(requestEntity, BaseResultEntity.class);
        return responseEntity.getBody();
    }

    private String getChainToken(List<SysChainInfo> sysChainInfos) {
        String token = primaryStringRedisTemplate.opsForValue().get(RedisKeyConstant.CHAIN_TOKEN_KEY.replace("<username>", sysChainInfos.get(0).getUsername()));
        if (token == null || token.isEmpty()) {
            return getChainTokenFromLogin(sysChainInfos);
        } else {
            return token;
        }
    }

    private String getChainTokenFromLogin(List<SysChainInfo> sysChainInfos) {
        Map map = new TreeMap();
        map.put("userName", sysChainInfos.get(0).getUsername());
        map.put("address",  sysChainInfos.get(0).getAddress());
        // 使用http协议发送
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HashMap<String, Object>> request = new HttpEntity(map,headers);
        String chainCenterService = baseConfiguration.getChainCenterService();
        String address = chainCenterService + SysConstant.SYS_CHAIN_LOGIN;
        BaseResultEntity resultEntity = restTemplate.postForObject(address, request, BaseResultEntity.class);
        if (resultEntity != null && resultEntity.getCode() == 0 && resultEntity.getResult() != null) {
            String result = (String) resultEntity.getResult();
            primaryStringRedisTemplate.opsForValue().set(RedisKeyConstant.CHAIN_TOKEN_KEY.replace("<username>", sysChainInfos.get(0).getUsername()), result, 1800, TimeUnit.SECONDS);
            return result;
        } else {
            return null;
        }
    }
}
