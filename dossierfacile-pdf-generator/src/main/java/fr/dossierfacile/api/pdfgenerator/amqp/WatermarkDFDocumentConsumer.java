package fr.dossierfacile.api.pdfgenerator.amqp;


import fr.dossierfacile.api.pdfgenerator.service.interfaces.DocumentService;
import fr.dossierfacile.api.pdfgenerator.service.interfaces.PdfGeneratorService;
import fr.dossierfacile.common.entity.StorageFile;
import fr.dossierfacile.common.entity.messaging.QueueName;
import fr.dossierfacile.common.service.interfaces.QueueMessageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatermarkDFDocumentConsumer {
    private final PdfGeneratorService pdfGeneratorService;
    private final DocumentService documentService;
    private final QueueMessageService queueMessageService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Value("${document.pdf-generation.delay.ms}")
    private Long documentPdfGenerationDelay;
    @Value("${document.pdf-generation.timeout.ms}")
    private Long documentPdfGenerationTimeout;

    @PostConstruct
    public void startConsumer() {
        scheduler.scheduleAtFixedRate(this::receiveDocument, 0, 2, TimeUnit.SECONDS);
    }

    private void receiveDocument() {
        try {
            queueMessageService.consume(
                    QueueName.QUEUE_DOCUMENT_WATERMARK_PDF,
                    documentPdfGenerationDelay,
                    documentPdfGenerationTimeout,
                    (msg) -> {
                        long executionTimestamp = System.currentTimeMillis();
                        StorageFile watermarkFile = pdfGeneratorService.generateBOPdfDocument(documentService.getDocument(msg.getDocumentId()));
                        documentService.saveWatermarkFileAt(executionTimestamp, watermarkFile, msg.getDocumentId());
                    }, null);
        } catch (Exception e) {
            log.error("Unable to consume the message queue");
        }
    }
}
