package com.estudo.spring_ia_practice.ingest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProceduresIngestion {

    private static final Log logger = LogFactory.getLog(ProceduresIngestion.class);

    private final ProceduresReader proceduresReader;
    private final VectorStore vectorStore;

    public ProceduresIngestion(ProceduresReader proceduresReader, VectorStore vectorStore) {
        this.proceduresReader = proceduresReader;
        this.vectorStore = vectorStore;
    }

    public synchronized int ingest() {
        List<Document> procedures = this.proceduresReader.loadProcedures();

        if (procedures.isEmpty()) {
            logger.warn("No procedure documents were found for ingestion");
            return 0;
        }

        this.vectorStore.write(procedures);
        logger.info("Ingested " + procedures.size() + " procedure documents into the vector store");
        return procedures.size();
    }
}
