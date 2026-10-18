package com.estudo.kafka_cluster.service;

import com.estudo.kafka_cluster.entity.Item;
import com.estudo.kafka_cluster.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ItemService(ItemRepository itemRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.itemRepository = itemRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processAndSendItems() {
        int pageSize = 1000;
        int pageNumber = 0;
        int totalProcessed = 0;
        
        Page<Item> page;
        do {
            page = itemRepository.findAll(PageRequest.of(pageNumber, pageSize));
            
            page.getContent().forEach(item -> {
                String message = "ID: " + item.getId() + ", Nome: " + item.getName();
                try {
                    kafkaTemplate.send("item-topic2", message).get();
                    System.out.println("Enviado: " + message);
                } catch (Exception e) {
                    System.err.println("Erro ao enviar mensagem: " + e.getMessage());
                }
            });
            
            totalProcessed += page.getNumberOfElements();
            pageNumber++;
            
        } while (page.hasNext());
        
        System.out.println("Processados " + totalProcessed + " registros");
    }
}