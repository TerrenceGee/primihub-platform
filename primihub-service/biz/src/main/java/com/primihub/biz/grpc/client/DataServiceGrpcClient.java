package com.primihub.biz.grpc.client;

import io.grpc.Channel;
import java_data_service.DataServiceGrpc;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class DataServiceGrpcClient {

    @Resource(name="grpcClientChannel")
    private Channel channel;

    public <Result> Result run(Functional<DataServiceGrpc.DataServiceBlockingStub,Result> functional){
        DataServiceGrpc.DataServiceBlockingStub dataServiceBlockingStub = DataServiceGrpc.newBlockingStub(channel).withDeadlineAfter(3, TimeUnit.SECONDS);
        return functional.run(dataServiceBlockingStub);
    }

    public <Result> Result run(Functional<DataServiceGrpc.DataServiceBlockingStub,Result> functional, Channel channel){
        DataServiceGrpc.DataServiceBlockingStub dataServiceBlockingStub = DataServiceGrpc.newBlockingStub(channel).withDeadlineAfter(3, TimeUnit.SECONDS);
        return functional.run(dataServiceBlockingStub);
    }
}
