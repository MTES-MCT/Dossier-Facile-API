package fr.dossierfacile.common.service.interfaces;

import fr.dossierfacile.common.entity.messaging.QueueMessage;
import fr.dossierfacile.common.entity.messaging.QueueName;
import fr.dossierfacile.common.model.JobContext;

import java.util.function.Consumer;

public interface QueueMessageService {
    /**
     * Allows to consume a message in the specified queue.
     * <br/>
     * If RetryableOperationException is intercepted during process then the message id delayed by setting the current date on timestamp.
     *
     * @param queueName                name of the queue
     * @param consumptionDelayInMillis indicate the delay before consuming the message (based on timstamp)
     * @param consumptionTimeout       indicated the timeout before interruption
     * @param messageConsumer          processed function
     */
    void consume(QueueName queueName, long consumptionDelayInMillis, long consumptionTimeout, Consumer<QueueMessage> messageConsumer, Consumer<JobContext> onFinish);
}
