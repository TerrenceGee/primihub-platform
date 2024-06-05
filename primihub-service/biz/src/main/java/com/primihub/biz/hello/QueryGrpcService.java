package com.primihub.biz.hello;

import com.primihub.biz.entity.SM3Dict;
import com.primihub.biz.repository.secondarydb.SM3DictRepository;
import com.primihub.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class QueryGrpcService extends QueryGrpc.QueryImplBase {
    private final static Logger log = LoggerFactory.getLogger(QueryGrpcService.class);
    @Resource
    private SM3DictRepository sm3DictRepository;
    @Override
    public void queryData(QueryRequest request, StreamObserver<QueryReply> responseObserver) {
        log.info("[query][param size] --- [{}]", request.getRequestListCount());
        List<String> queryValueList = request.getRequestListList().stream().map(InputObject::getIdNum).collect(Collectors.toList());

        List<SM3Dict> sm3Dicts = sm3DictRepository.selectSm3DictList(queryValueList);

        QueryReply.Builder builder = QueryReply.newBuilder();
        List<OutputObject> collect = sm3Dicts.stream().map(QueryGrpcService::convertSM3DictToVo).collect(Collectors.toList());
        builder.addAllReplyList(collect);

        log.info("[query][reply size] --- [{}]", collect.size());

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    public static OutputObject convertSM3DictToVo(SM3Dict sm3Dict){
        OutputObject.Builder builder = OutputObject.newBuilder()
                .setIdNum(sm3Dict.getIdNum())
                .setPhoneNum(sm3Dict.getPhoneNum());
        return builder.build();
    }
}
