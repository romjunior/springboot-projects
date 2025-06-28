package com.exemplo.rabbit_cluster;

import org.slf4j.Logger;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClusterProducer {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ClusterProducer.class);

    private final RabbitTemplate rabbitTemplate;

    private final DirectExchange directExchange;

    public ClusterProducer(RabbitTemplate rabbitTemplate, DirectExchange directExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.directExchange = directExchange;
    }

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(
                directExchange.getName(),
                RabbitConfig.ROUTING_KEY,
                new Event(1, message));
        logger.info("Message sent={}", message);
    }
}
