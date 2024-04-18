package com.primihub.biz.config.mq;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;


public interface SingleTaskChannel {
    String INPUT = "singleTaskInput";
    String OUTPUT = "singleTaskOutput";
    String SEA_TUNNEL_INPUT = "singleSeaTunnelInput";

    @Input(SingleTaskChannel.INPUT)
    SubscribableChannel input();

    @Output(SingleTaskChannel.OUTPUT)
    MessageChannel output();

    @Input(SingleTaskChannel.SEA_TUNNEL_INPUT)
    SubscribableChannel seatunnelInput();
}
