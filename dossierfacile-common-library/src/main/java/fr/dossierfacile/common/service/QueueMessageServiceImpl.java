package fr.dossierfacile.common.service;

import fr.dossierfacile.common.entity.messaging.QueueMessage;
import fr.dossierfacile.common.entity.messaging.QueueMessageStatus;
import fr.dossierfacile.common.entity.messaging.QueueName;
import fr.dossierfacile.common.exceptions.RetryableOperationException;
import fr.dossierfacile.common.model.JobContext;
import fr.dossierfacile.common.model.JobStatus;
import fr.dossierfacile.common.repository.QueueMessageRepository;
import fr.dossierfacile.common.service.interfaces.QueueMessageConsumerService;
import fr.dossierfacile.common.service.interfaces.QueueMessageService;
import fr.dossierfacile.common.utils.LoggerUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Service
@Slf4j
@AllArgsConstructor
public class QueueMessageServiceImpl implements QueueMessageService {

    private final QueueMessageRepository queueMessageRepository;
    private final QueueMessageConsumerService queueMessageConsumerService;

    @Override
    public void consume(QueueName queueName, long consumptionDelayInMillis, long consumptionTimeout, Consumer<QueueMessage> consumer, Consumer<JobContext> onFinish) {
        queueMessageRepository.cleanQueue(queueName.name());
        long toTimestamp = System.currentTimeMillis() - consumptionDelayInMillis;
        QueueMessage message = queueMessageConsumerService.popFirstMessage(queueName, toTimestamp);
        if (message != null) {
            var jobContext = new JobContext(message.getDocumentId(), message.getFileId(), queueName);
            // We have to add the process Id here because the process will be completed inside another thread to the MDC will not be shared
            LoggerUtil.addProcessId(jobContext.getProcessId());
            try {
                this.consumeMessageWithTimeout(queueName, consumer, consumptionTimeout, message, jobContext);
                queueMessageRepository.delete(message);
            } catch (InterruptedException e) {
                message.setStatus(QueueMessageStatus.FAILED);
                queueMessageRepository.save(message);
                jobContext.setJobStatus(JobStatus.INTERRUPTED);
                log.error("Thread interrupted", e);
                Thread.currentThread().interrupt();
            } catch (RetryableOperationException e) {
                log.error("Message can be re-queued", e);
                jobContext.setJobStatus(JobStatus.RETRYABLE);
                message.setStatus(QueueMessageStatus.PENDING);
                message.setTimestamp(System.currentTimeMillis());
                queueMessageRepository.save(message);
            } catch (TimeoutException e) {
                jobContext.setJobStatus(JobStatus.TIMED_OUT);
                message.setStatus(QueueMessageStatus.FAILED);
                queueMessageRepository.save(message);
            }catch (Throwable t) {
                message.setStatus(QueueMessageStatus.FAILED);
                queueMessageRepository.save(message);
            }
            finally {
                if (onFinish != null) {
                    onFinish.accept(jobContext);
                }
            }
        }
    }

    private void consumeMessageWithTimeout(QueueName queueName, Consumer<QueueMessage> consumer, long consumptionTimeout, QueueMessage message, JobContext jobContext) throws Throwable {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // We have to init the MDC of the process thread to aggregate logs generated inside
            LoggerUtil.addProcessId(jobContext.getProcessId());
            log.info("Received message on {} to process: {}", queueName, message);
            consumer.accept(message);
        });
        try {
            future.get(consumptionTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            log.error("Timeout while consume message {}", message.getDocumentId());
            throw e;
        } catch (ExecutionException e) {
            log.error("Error while consume message {}", message.getDocumentId(), e);
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        } catch (InterruptedException e) {
            log.error("Error while consume message {}", message.getDocumentId(), e);
            throw e;
        }
    }
}
