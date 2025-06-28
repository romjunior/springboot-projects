package com.exemplo.rabbit_cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClusterController {

    private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

    private final ClusterProducer clusterProducer;

    public ClusterController(ClusterProducer clusterProducer) {
        this.clusterProducer = clusterProducer;
    }

    @GetMapping(value = "/cluster")
    public String clusterQueue(@RequestParam("message") String message) {
        logger.info("Message received={}", message);
        clusterProducer.sendMessage(message);
        return message;
    }

}
