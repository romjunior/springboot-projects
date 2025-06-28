package com.exemplo.rabbit_cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ClusterConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ClusterConsumer.class);

    @RabbitListener(queues = "${spring.rabbitmq.queues.cluster-queue}")
    public void receiveMessage(Event message) {
        logger.info("Message received={}", message);
    }
}
