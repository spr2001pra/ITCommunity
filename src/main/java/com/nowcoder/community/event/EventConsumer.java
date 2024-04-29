package com.nowcoder.community.event;

import com.alibaba.fastjson2.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    // 方法和主题时多对多的关系，可以一个方法消费多个主题，由于三种情景下发的通知很相似，所以一个方法即可
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!"); // 没有取到消息
            return;
        }

        // 事件对象从消息对象ConsumerRecord record的JSON中还原出来(Event event)，
        // 它是事件触发时，封装了的相关数据，消费者在消费数据时得到的是原始的数据，
        // 但是消费者需要把原始的数据变成系统发送的文字通知，所以需要做一些转换
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) { // JSON还原事件对象失败，说明有格式错误
            logger.error("消息格式错误!");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID); // 假设系统是userId为1的虚拟用户，这种状况下Message表中部分字段失去作用，可复用
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic()); // ConversationId字段在系统发通知时改存消息的topic
        message.setCreateTime(new Date()); // state字段默认值为0，有效，所以不需要设置

        Map<String, Object> content = new HashMap<>(); // content字段存储通知需要动态显示的量，如用户名等，以JSON形式存储，此外还要存储其他无处存放的量
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        // 继续往content字段存储其他无处存放的额外的数据
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}
