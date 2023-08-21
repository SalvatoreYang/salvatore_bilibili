package com.salvatore.bilibili.service.config;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.util.StringUtils;
import com.salvatore.bilibili.domain.UserFollowing;
import com.salvatore.bilibili.domain.UserMoment;
import com.salvatore.bilibili.domain.constant.UserMomentsConstant;
import com.salvatore.bilibili.service.UserFollowingService;
import com.salvatore.bilibili.service.websocket.WebSocketService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RocketMQConfig {

//    private final String nameServerAddr = "localhost:9378";

    @Value("${rocketmq.name.server.address}")
    private String nameServerAddr;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private UserFollowingService userFollowingService;

    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws Exception{
        // 实例化消息生产者Produce
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
        // 设置nameserverd的地址
        producer.setNamesrvAddr(nameServerAddr);
        // 启动produce实例
        producer.start();
        return producer;
    }

    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws Exception{
        // 实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
        // 设置nameserverd的地址
        consumer.setNamesrvAddr(nameServerAddr);
        // 订阅一个或多个Topic，以及Tag来过滤需要消费的消息
        consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS, "*");
        // 注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                MessageExt msg = msgs.get(0);

                if (msg == null)
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                String bodyStr = new String(msg.getBody());
                UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);
                Long userId = userMoment.getUserId();
                List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                for (UserFollowing fan : fanList) {
                    String key = "subscribed-" + fan.getUserId();
                    String subscribedListStr = redisTemplate.opsForValue().get(key);
                    List<UserMoment> subscribedList;
                    if (StringUtils.isNullOrEmpty(subscribedListStr)){
                        subscribedList = new ArrayList<>();
                    }else {
                        subscribedList = JSONArray.parseArray(subscribedListStr, UserMoment.class);
                    }
                    subscribedList.add(userMoment);
                    redisTemplate.opsForValue().set(key, JSONObject.toJSONString(subscribedList));
                }
                // 标记该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws Exception{
        // 实例化消息生产者Produce
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_DANMUS);
        // 设置nameserverd的地址
        producer.setNamesrvAddr(nameServerAddr);
        // 启动produce实例
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws Exception{
        // 实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_DANMUS);
        // 设置nameserverd的地址
        consumer.setNamesrvAddr(nameServerAddr);
        // 订阅一个或多个Topic，以及Tag来过滤需要消费的消息
        consumer.subscribe(UserMomentsConstant.TOPIC_DANMUS, "*");
        // 注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                MessageExt msg = msgs.get(0);

                if (msg == null)
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;

                String bodyStr = new String(msg.getBody());
                JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                String sessionId = jsonObject.getString("sessionId");
                String message = jsonObject.getString("message");
                WebSocketService webSocketService = WebSocketService.WEBSOCKET_MAP.get("sessionId");
                if (webSocketService.getSession().isOpen()){
                    try {
                        webSocketService.sendMessage(message);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                // 标记该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

}
