package com.estudo.kafka_cluster;

import com.estudo.kafka_cluster.service.ItemService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaClusterApplication implements CommandLineRunner {

	private final ItemService itemService;

	public KafkaClusterApplication(ItemService itemService) {
		this.itemService = itemService;
	}

	public static void main(String[] args) {
		SpringApplication.run(KafkaClusterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("=== Processando e enviando registros para Kafka ===");
		itemService.processAndSendItems();
	}
}
