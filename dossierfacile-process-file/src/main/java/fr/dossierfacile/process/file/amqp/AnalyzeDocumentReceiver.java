package fr.dossierfacile.process.file.amqp;

import fr.dossierfacile.common.entity.messaging.QueueName;
import fr.dossierfacile.common.service.interfaces.QueueMessageService;
import fr.dossierfacile.process.file.log.LogAggregator;
import fr.dossierfacile.process.file.service.AnalyzeDocumentService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyzeDocumentReceiver {
    private final AnalyzeDocumentService analyzeDocumentService;
    private final QueueMessageService queueMessageService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final LogAggregator logAggregator;
    @Value("${document.analysis.delay.ms}")
    private Long documentAnalysisDelay;
    @Value("${document.analysis.timeout.ms}")
    private Long documentAnalysisTimeout;

    @PostConstruct
    public void startConsumer() {
        scheduler.scheduleAtFixedRate(this::receiveDocument, 0, 2, TimeUnit.SECONDS);
    }

    private void receiveDocument() {
        try {
            queueMessageService.consume(QueueName.QUEUE_DOCUMENT_ANALYSIS,
                    documentAnalysisDelay,
                    documentAnalysisTimeout,
                    (message) -> {
                        log.info("Received {} to process : {}", ActionType.ANALYZE_DOCUMENT.name(), message.getFileId());
                        analyzeDocumentService.processDocument(message.getDocumentId());
                    }, (jobContext) -> {
                        log.info("Ending processing");
                        logAggregator.sendWorkerLogs(jobContext, ActionType.ANALYZE_DOCUMENT);
                    });
        } catch (Exception e) {
            log.error("Unable to consume the message queue");
        }
    }

}
